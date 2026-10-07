using System.Collections.Concurrent;
using WifiPrintServer.Models;

namespace WifiPrintServer.Services;

/// <summary>
/// Thread-safe print job queue manager. Processes jobs sequentially per printer
/// with support for multiple printers in parallel. Emits status events for real-time updates.
/// Supports job priority, pause/resume, and queue reordering.
/// </summary>
public class PrintQueueManager : BackgroundService
{
    private readonly ConcurrentDictionary<string, PrintJob> _jobs = new();
    private readonly HashSet<string> _queuedJobIds = new(StringComparer.OrdinalIgnoreCase);
    private readonly object _queueLock = new();
    private readonly ConcurrentDictionary<string, CancellationTokenSource> _activeJobCancellations = new();
    private readonly PrinterService _printerService;
    private readonly ServerStateStore _stateStore;
    private readonly ILogger<PrintQueueManager> _logger;
    private readonly AppSettings _settings;
    private readonly SemaphoreSlim _signal = new(0);
    private readonly ConcurrentDictionary<string, byte> _busyPrinters = new(StringComparer.OrdinalIgnoreCase);

    /// <summary>
    /// Fired whenever a job's status changes. Used by SignalR hub to push updates.
    /// </summary>
    public event Action<JobStatusUpdate>? OnJobStatusChanged;

    public PrintQueueManager(
        PrinterService printerService,
        ServerStateStore stateStore,
        ILogger<PrintQueueManager> logger,
        AppSettings? settings = null)
    {
        _printerService = printerService;
        _stateStore = stateStore;
        _logger = logger;
        _settings = settings ?? Program.Settings ?? new AppSettings();
    }

    /// <summary>
    /// Enqueues a new print job and returns its ID.
    /// </summary>
    public string EnqueueJob(PrintJob job)
    {
        job.Status = PrintJobStatus.Pending;
        job.QueueState = "Queued";
        job.FailureCode = null;
        job.ErrorMessage = null;
        job.UpdatedAt = DateTime.UtcNow;
        _jobs[job.Id] = job;
        QueueJob(job.Id);

        _logger.LogInformation("Job {JobId} enqueued: {File} for printer {Printer}",
            job.Id, job.OriginalFileName, job.PrinterName);

        EmitStatus(job, "Job queued");
        PersistJob(job);
        return job.Id;
    }

    /// <summary>
    /// Gets all jobs, optionally filtered by status. Sorted by priority then creation date.
    /// </summary>
    public List<PrintJob> GetAllJobs(PrintJobStatus? statusFilter = null)
    {
        var jobs = _jobs.Values
            .OrderBy(j => GetPriorityOrder(j.Priority))
            .ThenByDescending(j => j.CreatedAt);

        return statusFilter.HasValue
            ? jobs.Where(j => j.Status == statusFilter.Value).ToList()
            : jobs.ToList();
    }

    /// <summary>
    /// Gets a specific job by ID.
    /// </summary>
    public PrintJob? GetJob(string jobId)
    {
        _jobs.TryGetValue(jobId, out var job);
        return job;
    }

    /// <summary>
    /// Cancels a pending or printing job.
    /// </summary>
    public bool CancelJob(string jobId)
    {
        if (!_jobs.TryGetValue(jobId, out var job))
            return false;

        if (job.Status == PrintJobStatus.Completed || job.Status == PrintJobStatus.Cancelled)
            return false;

        job.Status = PrintJobStatus.Cancelled;
        job.QueueState = "Cancelled";
        job.FailureCode = "cancelled";
        job.CompletedAt = DateTime.UtcNow;
        job.UpdatedAt = DateTime.UtcNow;
        RemoveQueuedJob(jobId);
        if (_activeJobCancellations.TryRemove(jobId, out var cts))
            cts.Cancel();
        EmitStatus(job, "Job cancelled");
        PersistJob(job);

        if (_settings.AutoCleanupAfterPrint)
        {
            CleanupJobFiles(job);
        }

        _logger.LogInformation("Job {JobId} cancelled", jobId);
        return true;
    }

    /// <summary>
    /// Removes all completed or cancelled jobs from memory.
    /// </summary>
    public int ClearCompletedJobs()
    {
        var completedIds = _jobs.Values
            .Where(j => j.Status == PrintJobStatus.Completed || j.Status == PrintJobStatus.Cancelled)
            .Select(j => j.Id)
            .ToList();

        int count = 0;
        foreach (var id in completedIds)
        {
            if (_jobs.TryRemove(id, out var job))
            {
                count++;
                if (_settings.AutoCleanupAfterPrint && job != null)
                {
                    CleanupJobFiles(job);
                }
            }
        }

        _logger.LogInformation("Cleared {Count} completed/cancelled jobs", count);
        return count;
    }

    /// <summary>
    /// Retries a failed job by re-enqueuing it.
    /// </summary>
    public bool RetryJob(string jobId)
    {
        if (!_jobs.TryGetValue(jobId, out var job))
            return false;

        if (job.Status != PrintJobStatus.Failed)
            return false;

        job.RetryCount++;
        job.Status = PrintJobStatus.Pending;
        job.ErrorMessage = null;
        job.FailureCode = null;
        job.Progress = 0;
        job.CompletedAt = null;
        job.QueueState = "Queued";
        job.UpdatedAt = DateTime.UtcNow;
        QueueJob(jobId);

        EmitStatus(job, $"Job retry #{job.RetryCount}");
        PersistJob(job);
        _logger.LogInformation("Job {JobId} retrying (attempt {Retry})", jobId, job.RetryCount);
        return true;
    }

    /// <summary>
    /// Sets the priority of a pending job (High, Normal, Low).
    /// </summary>
    public bool SetJobPriority(string jobId, string priority)
    {
        if (!_jobs.TryGetValue(jobId, out var job))
            return false;

        if (job.Status != PrintJobStatus.Pending && job.Status != PrintJobStatus.Paused)
            return false;

        job.Priority = priority;
        job.UpdatedAt = DateTime.UtcNow;
        EmitStatus(job, $"Priority changed to {priority}");
        PersistJob(job);
        _logger.LogInformation("Job {JobId} priority set to {Priority}", jobId, priority);
        return true;
    }

    /// <summary>
    /// Pauses a pending job — prevents it from being picked up by the processor.
    /// </summary>
    public bool PauseJob(string jobId)
    {
        if (!_jobs.TryGetValue(jobId, out var job))
            return false;

        if (job.Status != PrintJobStatus.Pending)
            return false;

        job.Status = PrintJobStatus.Paused;
        job.QueueState = "Paused";
        job.UpdatedAt = DateTime.UtcNow;
        RemoveQueuedJob(jobId);
        EmitStatus(job, "Job paused");
        PersistJob(job);
        _logger.LogInformation("Job {JobId} paused", jobId);
        return true;
    }

    /// <summary>
    /// Resumes a paused job — re-adds it to the processing queue.
    /// </summary>
    public bool ResumeJob(string jobId)
    {
        if (!_jobs.TryGetValue(jobId, out var job))
            return false;

        if (job.Status != PrintJobStatus.Paused)
            return false;

        job.Status = PrintJobStatus.Pending;
        job.QueueState = "Queued";
        job.UpdatedAt = DateTime.UtcNow;
        QueueJob(jobId);

        EmitStatus(job, "Job resumed");
        PersistJob(job);
        _logger.LogInformation("Job {JobId} resumed", jobId);
        return true;
    }

    /// <summary>
    /// Gets the position of a job in the pending queue.
    /// </summary>
    public int GetQueuePosition(string jobId)
    {
        lock (_queueLock)
        {
            var pending = GetOrderedQueuedJobIds();
            for (int i = 0; i < pending.Count; i++)
            {
                if (pending[i] == jobId)
                    return i + 1;
            }

            return 0;
        }
    }

    public void RestoreJobs()
    {
        var storedJobs = _stateStore.LoadJobs();
        foreach (var job in storedJobs)
        {
            if (job.Status is PrintJobStatus.Completed or PrintJobStatus.Cancelled or PrintJobStatus.Failed)
            {
                _jobs[job.Id] = job;
                continue;
            }

            if (job.Status == PrintJobStatus.Paused)
            {
                job.QueueState = "Paused";
                job.UpdatedAt = DateTime.UtcNow;
                _jobs[job.Id] = job;
                PersistJob(job);
                continue;
            }

            job.Status = PrintJobStatus.Pending;
            job.QueueState = "Queued";
            job.ErrorMessage = null;
            job.FailureCode = null;
            job.Progress = 0;
            job.UpdatedAt = DateTime.UtcNow;
            _jobs[job.Id] = job;
            QueueJob(job.Id);
            PersistJob(job);
        }

        if (storedJobs.Count > 0)
            _logger.LogInformation("Recovered {Count} jobs from persistent store", storedJobs.Count);
    }

    /// <summary>
    /// Background processing loop — picks jobs from queue and sends them to printers.
    /// Runs concurrently across different printers, processing one job per printer at a time (Feature 7).
    /// </summary>
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        _logger.LogInformation("Print queue processor started (parallel per-printer enabled)");

        while (!stoppingToken.IsCancellationRequested)
        {
            await _signal.WaitAsync(stoppingToken);

            if (!TryDequeueNextJobForAvailablePrinter(out var jobId))
                continue;

            if (!_jobs.TryGetValue(jobId, out var job))
                continue;

            // Skip cancelled jobs
            if (job.Status == PrintJobStatus.Cancelled)
            {
                ReleasePrinterForJob(job);
                continue;
            }

            // Skip paused jobs (they'll be re-enqueued when resumed)
            if (job.Status == PrintJobStatus.Paused)
            {
                ReleasePrinterForJob(job);
                continue;
            }

            // Run concurrently per printer so other printers aren't blocked
            _ = Task.Run(async () =>
            {
                try
                {
                    await ProcessJobAsync(job, stoppingToken);
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Unexpected error in print task for job {JobId}", job.Id);
                    FailJob(job, "unexpected_error", ex.Message);
                }
                finally
                {
                    ReleasePrinterForJob(job);
                }
            }, stoppingToken);
        }
    }

    /// <summary>
    /// Processes a single print job — validate, convert, print, track.
    /// </summary>
    private async Task ProcessJobAsync(PrintJob job, CancellationToken ct)
    {
        using var linkedCts = CancellationTokenSource.CreateLinkedTokenSource(ct);
        if (!_activeJobCancellations.TryAdd(job.Id, linkedCts))
            return;

        try
        {
            // Mark as printing
            job.Status = PrintJobStatus.Printing;
            job.StartedAt = DateTime.UtcNow;
            job.QueueState = "Active";
            job.UpdatedAt = DateTime.UtcNow;
            EmitStatus(job, "Printing started");
            PersistJob(job);

            // Determine file to print (original or converted)
            string fileToPrint = job.ConvertedFilePath ?? job.FilePath;

            if (!File.Exists(fileToPrint))
            {
                FailJob(job, "file_missing", "File not found on server");
                return;
            }

            // Send to printer
            var printResult = await _printerService.PrintFileAsync(
                fileToPrint,
                job.PrinterName,
                job.Settings,
                progress =>
                {
                    job.Progress = progress;
                    job.UpdatedAt = DateTime.UtcNow;
                    EmitStatus(job, $"Printing... {progress}%");
                    PersistJob(job);
                },
                linkedCts.Token);

            if (printResult.Success)
            {
                job.Status = PrintJobStatus.Completed;
                job.Progress = 100;
                job.CompletedAt = DateTime.UtcNow;
                job.QueueState = "Completed";
                job.FailureCode = null;
                job.ErrorMessage = null;
                job.UpdatedAt = DateTime.UtcNow;
                EmitStatus(job, "Print completed");
                PersistJob(job);
                _logger.LogInformation("Job {JobId} completed successfully", job.Id);

                if (_settings.AutoCleanupAfterPrint)
                {
                    CleanupJobFiles(job);
                }
            }
            else
            {
                FailJob(job, printResult.FailureCode ?? "print_failed",
                    printResult.ErrorMessage ?? "Printer failed to process the document");
            }
        }
        catch (OperationCanceledException)
        {
            job.Status = PrintJobStatus.Cancelled;
            job.QueueState = "Cancelled";
            job.FailureCode = "cancelled";
            job.CompletedAt = DateTime.UtcNow;
            job.UpdatedAt = DateTime.UtcNow;
            EmitStatus(job, "Job cancelled");
            PersistJob(job);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Job {JobId} failed with exception", job.Id);
            FailJob(job, "exception", ex.Message);
        }
        finally
        {
            if (_activeJobCancellations.TryRemove(job.Id, out var activeCts))
                activeCts.Dispose();
        }
    }

    private void FailJob(PrintJob job, string failureCode, string error)
    {
        job.Status = PrintJobStatus.Failed;
        job.ErrorMessage = error;
        job.FailureCode = failureCode;
        job.CompletedAt = DateTime.UtcNow;
        job.QueueState = "Failed";
        job.UpdatedAt = DateTime.UtcNow;
        EmitStatus(job, $"Failed: {error}");
        PersistJob(job);
        _logger.LogWarning("Job {JobId} failed: {Error}", job.Id, error);
    }

    /// <summary>
    /// Deletes uploaded documents and temporary converted files for a print job
    /// to guarantee privacy and prevent local host disk storage accumulation.
    /// Original local desktop files outside the managed upload directory are preserved.
    /// </summary>
    public void CleanupJobFiles(PrintJob job)
    {
        if (job == null) return;

        // Clean up converted file (e.g. DOCX -> PDF)
        if (!string.IsNullOrWhiteSpace(job.ConvertedFilePath))
        {
            try
            {
                if (File.Exists(job.ConvertedFilePath))
                {
                    File.Delete(job.ConvertedFilePath);
                    _logger.LogInformation("Cleaned up converted file for job {JobId}: {Path}", job.Id, job.ConvertedFilePath);
                }
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "Failed to delete converted file for job {JobId}: {Path}", job.Id, job.ConvertedFilePath);
            }
        }

        // Clean up original uploaded file if stored in managed upload directory
        if (!string.IsNullOrWhiteSpace(job.FilePath))
        {
            try
            {
                bool isManagedUpload = !string.IsNullOrWhiteSpace(_settings.UploadDirectory) &&
                                       job.FilePath.StartsWith(_settings.UploadDirectory, StringComparison.OrdinalIgnoreCase);

                if (isManagedUpload && File.Exists(job.FilePath))
                {
                    File.Delete(job.FilePath);
                    _logger.LogInformation("Cleaned up uploaded file after print for job {JobId}: {Path}", job.Id, job.FilePath);
                }
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "Failed to delete uploaded file for job {JobId}: {Path}", job.Id, job.FilePath);
            }
        }
    }

    private void EmitStatus(PrintJob job, string message)
    {
        OnJobStatusChanged?.Invoke(new JobStatusUpdate
        {
            JobId = job.Id,
            Status = job.Status.ToString(),
            Progress = job.Progress,
            Message = message,
            QueueState = job.QueueState,
            FailureCode = job.FailureCode,
            FailureMessage = job.ErrorMessage,
            SourceDeviceId = job.DeviceId
        });
    }

    private void QueueJob(string jobId)
    {
        lock (_queueLock)
        {
            if (_queuedJobIds.Add(jobId))
                _signal.Release();
        }
    }

    private void RemoveQueuedJob(string jobId)
    {
        lock (_queueLock)
        {
            _queuedJobIds.Remove(jobId);
        }
    }

    private static string GetPrinterKey(string? printerName) =>
        string.IsNullOrWhiteSpace(printerName) ? "DEFAULT_PRINTER" : printerName.Trim();

    private bool TryDequeueNextJobForAvailablePrinter(out string jobId)
    {
        lock (_queueLock)
        {
            var ordered = GetOrderedQueuedJobIds();
            foreach (var candidateId in ordered)
            {
                if (_jobs.TryGetValue(candidateId, out var job))
                {
                    var key = GetPrinterKey(job.PrinterName);
                    if (!_busyPrinters.ContainsKey(key))
                    {
                        _busyPrinters.TryAdd(key, 0);
                        _queuedJobIds.Remove(candidateId);
                        jobId = candidateId;
                        return true;
                    }
                }
            }

            jobId = string.Empty;
            return false;
        }
    }

    private void ReleasePrinterForJob(PrintJob job)
    {
        var key = GetPrinterKey(job.PrinterName);
        _busyPrinters.TryRemove(key, out _);
        lock (_queueLock)
        {
            if (_queuedJobIds.Count > 0)
            {
                try { _signal.Release(); } catch (SemaphoreFullException) { }
            }
        }
    }

    private bool TryDequeueNextJob(out string jobId)
    {
        lock (_queueLock)
        {
            var ordered = GetOrderedQueuedJobIds();
            if (ordered.Count == 0)
            {
                jobId = string.Empty;
                return false;
            }

            jobId = ordered[0];
            _queuedJobIds.Remove(jobId);
            return true;
        }
    }

    private List<string> GetOrderedQueuedJobIds() =>
        _queuedJobIds
            .Select(id => _jobs.TryGetValue(id, out var job) ? job : null)
            .Where(job => job != null && job.Status == PrintJobStatus.Pending)
            .OrderBy(job => GetPriorityOrder(job!.Priority))
            .ThenBy(job => job!.CreatedAt)
            .Select(job => job!.Id)
            .ToList();

    private void PersistJob(PrintJob job)
    {
        try
        {
            _stateStore.UpsertJob(job);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Failed to persist job {JobId}", job.Id);
        }
    }

    private static int GetPriorityOrder(string priority) => priority switch
    {
        "High" => 0,
        "Normal" => 1,
        "Low" => 2,
        _ => 1
    };
}
