using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using WifiPrintServer.Models;
using WifiPrintServer.Services;

namespace WifiPrintServer.Controllers;

/// <summary>
/// Handles file upload and print job creation.
/// POST /api/print — Upload a file with print settings to create a new print job.
/// </summary>
[Authorize]
[ApiController]
[Route("api/[controller]")]
public class PrintController : ControllerBase
{
    private readonly PrintQueueManager _queueManager;
    private readonly FileProcessingService _fileService;
    private readonly PrinterService _printerService;
    private readonly AuthService _authService;
    private readonly ILogger<PrintController> _logger;

    public PrintController(
        PrintQueueManager queueManager,
        FileProcessingService fileService,
        PrinterService printerService,
        AuthService authService,
        ILogger<PrintController> logger)
    {
        _queueManager = queueManager;
        _fileService = fileService;
        _printerService = printerService;
        _authService = authService;
        _logger = logger;
    }

    /// <summary>
    /// Upload a file and create a print job.
    /// Accepts multipart form data with a file and JSON settings.
    /// </summary>
    [HttpPost]
    [RequestSizeLimit(104_857_600)] // 100MB
    public async Task<IActionResult> UploadAndPrint(
        IFormFile file,
        [FromForm] string? settingsJson)
    {
        // Check if the device is blocked
        var deviceId = User.FindFirst("deviceId")?.Value;
        if (string.IsNullOrWhiteSpace(deviceId))
            return Unauthorized(ApiResponse<object>.Fail("Missing device identity"));

        if (_authService.IsDeviceBlocked(deviceId))
            return StatusCode(403, ApiResponse<object>.Fail("Device is blocked by the server administrator"));

        // Session & rate limiting checks
        var device = _authService.GetDeviceById(deviceId);
        if (device == null)
            return Unauthorized(ApiResponse<object>.Fail("Device not found. Please re-pair."));

        var appSettings = Program.Settings;

        // Rate limiting ONLY applies to remote/tunnel devices.
        // Same-network users print freely without restrictions.
        if (device.ConnectedViaTunnel)
        {
            // Check session expiry
            if (device.IsSessionExpired(appSettings.SessionDurationMinutes))
            {
                var mins = appSettings.SessionDurationMinutes;
                _logger.LogWarning("Print rejected for device {Id}: session expired after {Mins} minutes", deviceId, mins);
                return StatusCode(403, ApiResponse<object>.Fail(
                    $"Your session has expired (limit: {mins} minutes). Please scan the QR code again to reconnect."));
            }

            // Check max prints per session
            if (device.HasExceededPrintLimit(appSettings.MaxPrintsPerSession))
            {
                var max = appSettings.MaxPrintsPerSession;
                _logger.LogWarning("Print rejected for device {Id}: exceeded {Max} prints per session", deviceId, max);
                return StatusCode(429, ApiResponse<object>.Fail(
                    $"Print limit reached ({max} prints per session). Please wait for a new session or ask the server admin."));
            }

            // Check cooldown between prints
            if (device.IsInCooldown(appSettings.PrintCooldownSeconds))
            {
                var remaining = appSettings.PrintCooldownSeconds - (int)(DateTime.UtcNow - device.LastPrintAt!.Value).TotalSeconds;
                _logger.LogWarning("Print rejected for device {Id}: cooldown ({Remaining}s left)", deviceId, remaining);
                return StatusCode(429, ApiResponse<object>.Fail(
                    $"Please wait {remaining} seconds before sending another print job."));
            }
        }

        if (file == null || file.Length == 0)
            return BadRequest(ApiResponse<object>.Fail("No file uploaded"));

        // Parse print settings
        var printSettings = new PrintSettings();
        if (!string.IsNullOrEmpty(settingsJson))
        {
            try
            {
                printSettings = System.Text.Json.JsonSerializer.Deserialize<PrintSettings>(settingsJson,
                    new System.Text.Json.JsonSerializerOptions { PropertyNameCaseInsensitive = true })
                    ?? new PrintSettings();
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "Failed to parse print settings, using defaults");
            }
        }

        // Determine printer
        string printerName;
        if (!string.IsNullOrEmpty(printSettings.SelectedPrinterId))
        {
            var printer = _printerService.GetPrinter(printSettings.SelectedPrinterId);
            printerName = printer?.Name ?? _printerService.GetDefaultPrinter()?.Name ?? "";
        }
        else
        {
            printerName = _printerService.GetDefaultPrinter()?.Name ?? "";
        }

        if (string.IsNullOrEmpty(printerName))
            return BadRequest(ApiResponse<object>.Fail("No printer available"));

        // Save and validate file
        var (filePath, saveError) = await _fileService.SaveAndValidateFileAsync(
            file.OpenReadStream(), file.FileName, file.Length);

        if (filePath == null)
            return BadRequest(ApiResponse<object>.Fail(saveError ?? "File validation failed"));

        // Convert if needed (DOCX→PDF, TXT→PDF)
        var (convertedPath, convertError) = await _fileService.ConvertIfNeededAsync(filePath);
        if (convertedPath == null)
            return BadRequest(ApiResponse<object>.Fail(convertError ?? "File conversion failed"));

        // Check if PDF is encrypted and requires password
        var finalPdfPath = convertedPath ?? filePath;
        if (Path.GetExtension(finalPdfPath).Equals(".pdf", StringComparison.OrdinalIgnoreCase))
        {
            var pdfValidation = _printerService.ValidatePdf(finalPdfPath, printSettings.PdfPassword);
            if (pdfValidation.RequiresPassword)
            {
                _logger.LogWarning("Print rejected: file {File} requires password", file.FileName);
                return BadRequest(ApiResponse<object>.Fail(
                    pdfValidation.ErrorMessage ?? "This PDF is password-protected. Please provide the password to print.",
                    new { requiresPassword = true, isLocked = true }));
            }
        }

        // Get device info from JWT claims (deviceId already extracted above for block check)
        var deviceName = User.FindFirst("deviceName")?.Value ?? "Unknown Device";

        // Create and enqueue job
        var job = new PrintJob
        {
            FileName = file.FileName,
            OriginalFileName = file.FileName,
            FileSize = file.Length,
            FileType = FileProcessingService.GetFileType(file.FileName),
            FilePath = filePath,
            ConvertedFilePath = convertedPath != filePath ? convertedPath : null,
            Settings = printSettings,
            PrinterName = printerName,
            DeviceId = deviceId,
            DeviceName = deviceName,
            QueueState = "Queued",
            UpdatedAt = DateTime.UtcNow
        };

        var jobId = _queueManager.EnqueueJob(job);

        // Record print for rate limiting
        device.RecordPrint();

        _logger.LogInformation("Print job created: {JobId} for {File} (Encrypted={Encrypted}, SessionPrints={Count})",
            jobId, file.FileName, !string.IsNullOrEmpty(printSettings.PdfPassword), device.SessionPrintCount);

        return Ok(ApiResponse<PrintJobResponse>.Ok(new PrintJobResponse
        {
            JobId = jobId,
            Status = "Pending",
            QueuePosition = _queueManager.GetQueuePosition(jobId)
        }, "Print job created"));
    }

    /// <summary>
    /// POST /api/print/pagecount — Get the page count of a PDF file without printing.
    /// Supports password-protected / locked PDF documents.
    /// </summary>
    [AllowAnonymous]
    [HttpPost("pagecount")]
    [RequestSizeLimit(104_857_600)]
    public async Task<IActionResult> GetPageCount(IFormFile file, [FromForm] string? password = null)
    {
        if (file == null || file.Length == 0)
            return BadRequest(ApiResponse<object>.Fail("No file uploaded"));

        var ext = Path.GetExtension(file.FileName).ToLowerInvariant();
        if (ext != ".pdf")
            return Ok(ApiResponse<object>.Ok(new { pageCount = 1, fileType = FileProcessingService.GetFileType(file.FileName) },
                "Non-PDF file, assuming 1 page"));

        // Save temporarily to get page count
        var tempPath = Path.Combine(Path.GetTempPath(), $"pagecount_{Guid.NewGuid():N}.pdf");
        try
        {
            using (var fs = new FileStream(tempPath, FileMode.Create))
                await file.CopyToAsync(fs);

            var validation = _printerService.ValidatePdf(tempPath, password);
            if (validation.RequiresPassword)
            {
                return Ok(ApiResponse<object>.Fail(
                    validation.ErrorMessage ?? "Password required",
                    new
                    {
                        isLocked = true,
                        requiresPassword = true,
                        pageCount = 0,
                        fileType = "PDF"
                    }));
            }

            return Ok(ApiResponse<object>.Ok(new
            {
                pageCount = validation.PageCount,
                fileType = "PDF",
                isLocked = validation.IsEncrypted,
                isPasswordVerified = validation.IsValid && validation.IsEncrypted
            }, "Page count retrieved"));
        }
        finally
        {
            if (System.IO.File.Exists(tempPath))
                System.IO.File.Delete(tempPath);
        }
    }
}
