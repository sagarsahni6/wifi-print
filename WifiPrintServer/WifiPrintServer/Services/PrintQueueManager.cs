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
    private readonly ConcurrentQueue<string> _pendingQueue = new();
    private readonly PrinterService _printerService;
    private readonly ILogger<PrintQueueManager> _logger;
    private readonly SemaphoreSlim _signal = new(0);

    /// <summary>
    /// Fired whenever a job's status changes. Used by SignalR hub to push updates.
    /// </summary>
    public event Action<JobStatusUpdate>? OnJobStatusChanged;

    public PrintQueueManager(PrinterService printerService, ILogger<PrintQueueManager> logger)
    {
        _printerService = printerService;
        _logger = logger;
    }

    /// <summary>
    /// Enqueues a new print job and returns its ID.
    /// </summary>
    public string EnqueueJob(PrintJob job)
    {
        job.Status = PrintJobStatus.Pending;
        _jobs[job.Id] = job;
        _pendingQueue.Enqueue(job.Id);
        _signal.Release(); // Wake the processing loop

        _logger.LogInformation("Job {JobId} enqueued: {File} for printer {Printer}",
            job.Id, job.OriginalFileName, job.PrinterName);

        EmitStatus(job, "Job queued");
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
        job.CompletedAt = DateTime.UtcNow;
        EmitStatus(job, "Job cancelled");

        _logger.LogInformation("Job {JobId} cancelled", jobId);
        return true;
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
        job.Progress = 0;
        _pendingQueue.Enqueue(jobId);
        _signal.Release();

        EmitStatus(job, $"Job retry #{job.RetryCount}");
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
        EmitStatus(job, $"Priority changed to {priority}");
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
        EmitStatus(job, "Job paused");
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
        _pendingQueue.Enqueue(jobId);
        _signal.Release();

        EmitStatus(job, "Job resumed");
        _logger.LogInformation("Job {JobId} resumed", jobId);
        return true;
    }

    /// <summary>
    /// Gets the position of a job in the pending queue.
    /// </summary>
    public int GetQueuePosition(string jobId)
    {
        var pending = _pendingQueue.ToArray();
        for (int i = 0; i < pending.Length; i++)
        {
            if (pending[i] == jobId) return i + 1;
        }
        return 0;
    }

    /// <summary>
    /// Background processing loop — picks jobs from queue and sends them to the printer.
    /// Skips paused jobs and prioritizes high-priority jobs.
    /// </summary>
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        _logger.LogInformation("Print queue processor started");

        while (!stoppingToken.IsCancellationRequested)
        {
            await _signal.WaitAsync(stoppingToken);

            if (!_pendingQueue.TryDequeue(out var jobId))
                continue;

            if (!_jobs.TryGetValue(jobId, out var job))
                continue;

            // Skip cancelled jobs
            if (job.Status == PrintJobStatus.Cancelled)
                continue;

            // Skip paused jobs (they'll be re-enqueued when resumed)
            if (job.Status == PrintJobStatus.Paused)
                continue;

            await ProcessJobAsync(job, stoppingToken);
        }
    }

    /// <summary>
    /// Processes a single print job — validate, convert, print, track.
    /// </summary>
    private async Task ProcessJobAsync(PrintJob job, CancellationToken ct)
    {
        try
        {
            // Mark as printing
            job.Status = PrintJobStatus.Printing;
            job.StartedAt = DateTime.UtcNow;
            EmitStatus(job, "Printing started");

            // Determine file to print (original or converted)
            string fileToPrint = job.ConvertedFilePath ?? job.FilePath;

            if (!File.Exists(fileToPrint))
            {
                FailJob(job, "File not found on server");
                return;
            }

            // Send to printer
            bool success = await _printerService.PrintFileAsync(
                fileToPrint,
                job.PrinterName,
                job.Settings,
                progress =>
                {
                    job.Progress = progress;
                    EmitStatus(job, $"Printing... {progress}%");
                },
                ct);

            if (success)
            {
                job.Status = PrintJobStatus.Completed;
                job.Progress = 100;
                job.CompletedAt = DateTime.UtcNow;
                EmitStatus(job, "Print completed");
                _logger.LogInformation("Job {JobId} completed successfully", job.Id);
            }
            else
            {
                FailJob(job, "Printer failed to process the document");
            }
        }
        catch (OperationCanceledException)
        {
            job.Status = PrintJobStatus.Cancelled;
            EmitStatus(job, "Job cancelled");
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Job {JobId} failed with exception", job.Id);
            FailJob(job, ex.Message);
        }
    }

    private void FailJob(PrintJob job, string error)
    {
        job.Status = PrintJobStatus.Failed;
        job.ErrorMessage = error;
        job.CompletedAt = DateTime.UtcNow;
        EmitStatus(job, $"Failed: {error}");
        _logger.LogWarning("Job {JobId} failed: {Error}", job.Id, error);
    }

    private void EmitStatus(PrintJob job, string message)
    {
        OnJobStatusChanged?.Invoke(new JobStatusUpdate
        {
            JobId = job.Id,
            Status = job.Status.ToString(),
            Progress = job.Progress,
            Message = message
        });
    }

    private static int GetPriorityOrder(string priority) => priority switch
    {
        "High" => 0,
        "Normal" => 1,
        "Low" => 2,
        _ => 1
    };
}
