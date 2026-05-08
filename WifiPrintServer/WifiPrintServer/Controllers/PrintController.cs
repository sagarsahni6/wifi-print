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
        var deviceId = User.FindFirst("deviceId")?.Value ?? "unknown";
        if (_authService.IsDeviceBlocked(deviceId))
            return StatusCode(403, ApiResponse<object>.Fail("Device is blocked by the server administrator"));

        if (file == null || file.Length == 0)
            return BadRequest(ApiResponse<object>.Fail("No file uploaded"));

        // Parse print settings
        var settings = new PrintSettings();
        if (!string.IsNullOrEmpty(settingsJson))
        {
            try
            {
                settings = System.Text.Json.JsonSerializer.Deserialize<PrintSettings>(settingsJson,
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
        if (!string.IsNullOrEmpty(settings.SelectedPrinterId))
        {
            var printer = _printerService.GetPrinter(settings.SelectedPrinterId);
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
            Settings = settings,
            PrinterName = printerName,
            DeviceId = deviceId,
            DeviceName = deviceName
        };

        var jobId = _queueManager.EnqueueJob(job);

        _logger.LogInformation("Print job created: {JobId} for {File}", jobId, file.FileName);

        return Ok(ApiResponse<PrintJobResponse>.Ok(new PrintJobResponse
        {
            JobId = jobId,
            Status = "Pending",
            QueuePosition = _queueManager.GetQueuePosition(jobId)
        }, "Print job created"));
    }

    /// <summary>
    /// POST /api/print/pagecount — Get the page count of a PDF file without printing.
    /// Used by the Android app to populate the page range selector.
    /// </summary>
    [HttpPost("pagecount")]
    [RequestSizeLimit(104_857_600)]
    public async Task<IActionResult> GetPageCount(IFormFile file)
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

            var pageCount = _printerService.GetPdfPageCount(tempPath);
            return Ok(ApiResponse<object>.Ok(new { pageCount, fileType = "PDF" }, "Page count retrieved"));
        }
        finally
        {
            if (System.IO.File.Exists(tempPath))
                System.IO.File.Delete(tempPath);
        }
    }
}
