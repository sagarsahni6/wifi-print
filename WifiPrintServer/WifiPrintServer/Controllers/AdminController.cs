using System.Net;
using System.Text;
using Microsoft.AspNetCore.Mvc;
using WifiPrintServer.Models;
using WifiPrintServer.Services;

namespace WifiPrintServer.Controllers;

public class VerifyPinRequest
{
    public string? Pin { get; set; }
}

public class WebApprovalRequest
{
    public string? DeviceName { get; set; }
}

/// <summary>
/// Web Print Studio & Server Management Dashboard.
/// Mobile-First Light Theme with 1-Click Host PC Notification Approval.
/// Zero-PIN frictionless printing on Local Wi-Fi, and 1-Click Host PC Approval over Cloudflare Tunnel.
/// </summary>
[ApiController]
public class AdminController : ControllerBase
{
    private readonly AppSettings _settings;
    private readonly PrinterService _printerService;
    private readonly PrintQueueManager _queueManager;
    private readonly AuthService _authService;
    private readonly FileProcessingService _fileService;
    private readonly TunnelService _tunnelService;
    private readonly WebPrintProtectionService _protectionService;
    private readonly ILogger<AdminController> _logger;

    public AdminController(
        AppSettings settings,
        PrinterService printerService,
        PrintQueueManager queueManager,
        AuthService authService,
        FileProcessingService fileService,
        TunnelService tunnelService,
        WebPrintProtectionService protectionService,
        ILogger<AdminController> logger)
    {
        _settings = settings;
        _printerService = printerService;
        _queueManager = queueManager;
        _authService = authService;
        _fileService = fileService;
        _tunnelService = tunnelService;
        _protectionService = protectionService;
        _logger = logger;
    }

    /// <summary>
    /// GET /api/admin/overview — Returns server stats, printers, jobs, paired devices, tunnel URL, and security context.
    /// </summary>
    [HttpGet("/api/admin/overview")]
    public IActionResult GetOverview()
    {
        var printers = _printerService.GetAllPrinters();
        var jobs = _queueManager.GetAllJobs().Take(50).ToList();
        var devices = _authService.GetPairedDevices();
        var defaultPrinter = _printerService.GetDefaultPrinter()?.Name ?? printers.FirstOrDefault()?.Name ?? "";
        var tunnelUrl = _tunnelService.PublicUrl ?? Program.TunnelServiceInstance?.PublicUrl ?? "";
        var isTunnelActive = _tunnelService.IsActive || (Program.TunnelServiceInstance?.IsActive ?? false);

        var clientIp = _protectionService.GetEffectiveClientIp(HttpContext);
        var isTunnelRequest = _protectionService.IsRequestFromTunnel(HttpContext);
        var isLocal = WebPrintProtectionService.IsLocalOrPrivateIp(clientIp) && !isTunnelRequest;
        var requiresApproval = _settings.RequirePinForWebTunnel && isTunnelRequest;

        return Ok(new
        {
            success = true,
            serverName = _settings.ServerName,
            port = _settings.ServerPort,
            defaultPrinter = defaultPrinter,
            printers = printers,
            jobs = jobs,
            devices = devices,
            tunnelUrl = tunnelUrl,
            isTunnelActive = isTunnelActive,
            clientIp = clientIp,
            isLocalSubnet = isLocal,
            requiresApproval = requiresApproval,
            requiresPin = requiresApproval,
            maxCopies = _settings.MaxWebPrintCopies,
            maxPages = _settings.MaxWebPrintPages
        });
    }

    /// <summary>
    /// POST /api/web/request-approval — Sends an instant 1-click notification to the Host PC desktop.
    /// The host PC owner can click 'Approve' right on the desktop toast without opening the server app.
    /// </summary>
    [HttpPost("/api/web/request-approval")]
    public async Task<IActionResult> RequestApproval([FromBody] WebApprovalRequest? request)
    {
        var clientIp = _protectionService.GetEffectiveClientIp(HttpContext);
        var isTunnel = _protectionService.IsRequestFromTunnel(HttpContext);

        // If on trusted local Wi-Fi, auto-approve immediately
        if (WebPrintProtectionService.IsLocalOrPrivateIp(clientIp) && !isTunnel)
        {
            var localToken = _protectionService.CreateSessionToken(clientIp, 2);
            return Ok(new
            {
                success = true,
                status = "Approved",
                token = localToken,
                message = "Connected automatically on local Wi-Fi."
            });
        }

        var userAgent = Request.Headers.UserAgent.ToString();
        var devName = request?.DeviceName?.Trim();
        if (string.IsNullOrWhiteSpace(devName))
        {
            var clientTag = "Web Browser";
            if (userAgent.Contains("Edg/")) clientTag = "Edge Web";
            else if (userAgent.Contains("Chrome/")) clientTag = "Chrome Web";
            else if (userAgent.Contains("Firefox/")) clientTag = "Firefox Web";
            else if (userAgent.Contains("Safari/") && !userAgent.Contains("Chrome")) clientTag = "Safari Web";
            if (userAgent.Contains("iPhone") || userAgent.Contains("Android") || userAgent.Contains("Mobile"))
                clientTag = "Mobile " + clientTag;
            devName = clientTag;
        }

        _logger.LogInformation("Web client requested host PC approval: {Device} from {Ip} (tunnel={Tunnel})",
            devName, clientIp, isTunnel);

        // Triggers the interactive ApprovalToastWindow on the PC desktop!
        var authResponse = await _authService.RequestApprovalAsync(
            devName, "Web Browser", clientIp, connectedViaTunnel: isTunnel);

        if (authResponse != null)
        {
            var sessionToken = _protectionService.CreateSessionToken(clientIp, 2);
            return Ok(new
            {
                success = true,
                status = "Approved",
                token = sessionToken,
                jwtToken = authResponse.Token,
                message = "Access approved by host PC! Web printing unlocked for 2 hours."
            });
        }

        return StatusCode(StatusCodes.Status403Forbidden, new
        {
            success = false,
            status = "Denied",
            message = "Approval request was denied or timed out on the host PC."
        });
    }

    /// <summary>
    /// GET /api/web/pending-approvals — Lists current pending device/web approval requests.
    /// </summary>
    [HttpGet("/api/web/pending-approvals")]
    public IActionResult GetPendingApprovals()
    {
        return Ok(new
        {
            success = true,
            pending = _authService.GetPendingRequests().Select(p => new
            {
                id = p.Id,
                deviceName = p.DeviceName,
                deviceModel = p.DeviceModel,
                ipAddress = p.IpAddress,
                connectedViaTunnel = p.ConnectedViaTunnel,
                requestedAt = p.RequestedAt
            })
        });
    }

    /// <summary>
    /// POST /api/web/approve/{id} — Directly approves a pending request by ID.
    /// </summary>
    [HttpPost("/api/web/approve/{id}")]
    public IActionResult ApprovePending(string id)
    {
        _authService.ApproveDevice(id);
        return Ok(new { success = true, message = $"Approval {id} approved." });
    }

    /// <summary>
    /// POST /api/web/deny/{id} — Directly denies a pending request by ID.
    /// </summary>
    [HttpPost("/api/web/deny/{id}")]
    public IActionResult DenyPending(string id)
    {
        _authService.DenyDevice(id);
        return Ok(new { success = true, message = $"Approval {id} denied." });
    }

    /// <summary>
    /// POST /api/web/verify-pin — Optional fallback verification with 6-digit PIN.
    /// </summary>
    [HttpPost("/api/web/verify-pin")]
    public IActionResult VerifyPin([FromBody] VerifyPinRequest? request)
    {
        var pin = request?.Pin?.Trim() ?? "";
        if (string.IsNullOrWhiteSpace(pin))
            return BadRequest(new { success = false, message = "Please enter the 6-digit PIN." });

        var clientIp = _protectionService.GetEffectiveClientIp(HttpContext);
        var (success, token, expiresAt, error) = _protectionService.VerifyPin(pin, clientIp);

        if (!success)
            return BadRequest(new { success = false, message = error ?? "Invalid PIN." });

        return Ok(new
        {
            success = true,
            token = token,
            expiresAt = expiresAt,
            message = "PIN verified successfully! Web print access granted for 2 hours."
        });
    }

    /// <summary>
    /// GET /api/web/qr — Generates a QR code PNG pointing to the Web Print Studio URL.
    /// Uses the Cloudflare tunnel URL if active, otherwise the local LAN HTTPS URL.
    /// Shop owners can print this QR for customers to scan and print documents.
    /// </summary>
    [HttpGet("/api/web/qr")]
    [HttpGet("/{hostId}/qr")]
    public IActionResult GetWebPrintQrCode(string? hostId = null)
    {
        try
        {
            var tunnelUrl = _tunnelService.PublicUrl ?? Program.TunnelServiceInstance?.PublicUrl;
            var localIp = DiscoveryService.GetLocalIpAddress();
            var webUrl = _settings.GetWebPrintUrl(tunnelUrl, localIp);

            using var qrGenerator = new QRCoder.QRCodeGenerator();
            var qrCodeData = qrGenerator.CreateQrCode(webUrl, QRCoder.QRCodeGenerator.ECCLevel.M);
            using var qrCode = new QRCoder.PngByteQRCode(qrCodeData);
            var pngBytes = qrCode.GetGraphic(12);

            return File(pngBytes, "image/png");
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to generate web print QR code");
            return StatusCode(500, "QR generation failed");
        }
    }

    /// <summary>
    /// GET /api/auth/qr — Generates a live QR code PNG for mobile app pairing.
    /// </summary>
    [HttpGet("/api/auth/qr")]
    public IActionResult GetQrCode()
    {
        try
        {
            var localIp = DiscoveryService.GetLocalIpAddress();
            var tunnelUrl = _tunnelService.PublicUrl ?? Program.TunnelServiceInstance?.PublicUrl;
            var cert = Program.ServerCertificate;
            var token = _settings.CurrentQrPairingToken;

            var payload = QrCodeService.GetConnectionPayloadJson(
                localIp, _settings.ServerPort, _settings.ServerName, cert, token, tunnelUrl);

            using var qrGenerator = new QRCoder.QRCodeGenerator();
            var qrCodeData = qrGenerator.CreateQrCode(payload, QRCoder.QRCodeGenerator.ECCLevel.M);
            using var qrCode = new QRCoder.PngByteQRCode(qrCodeData);
            var pngBytes = qrCode.GetGraphic(8);

            return File(pngBytes, "image/png");
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to generate QR code");
            return StatusCode(500, "QR generation failed");
        }
    }

    /// <summary>
    /// POST /api/web/print — Direct in-browser document upload and printing without the Android app.
    /// Protected by Host PC Approval (or PIN), anti-bot honeypot, rate limiting, and quotas.
    /// </summary>
    [HttpPost("/api/web/print")]
    [HttpPost("/api/admin/print")]
    [RequestSizeLimit(104_857_600)] // 100MB limit
    public async Task<IActionResult> WebPrint(
        IFormFile? file,
        [FromForm] string? printerName,
        [FromForm] int? copies,
        [FromForm] string? colorMode,
        [FromForm] bool? duplex,
        [FromForm] string? orientation,
        [FromForm] string? pageSize,
        [FromForm] string? pageRange,
        [FromForm] string? pdfPassword,
        [FromForm] string? settingsJson,
        [FromForm] string? sessionToken,
        [FromForm] string? hp_website)
    {
        var clientIp = _protectionService.GetEffectiveClientIp(HttpContext);

        // 1. Honeypot check (anti-bot)
        if (!string.IsNullOrWhiteSpace(hp_website))
        {
            _logger.LogWarning("Spam honeypot triggered by {Ip}", clientIp);
            return BadRequest(new { success = false, message = "Automated submission rejected." });
        }

        // 2. Cloudflare Tunnel Approval / Token check
        var isTunnelRequest = _protectionService.IsRequestFromTunnel(HttpContext);
        if (_settings.RequirePinForWebTunnel && isTunnelRequest)
        {
            var token = sessionToken;
            if (string.IsNullOrWhiteSpace(token))
            {
                if (Request.Headers.TryGetValue("X-Web-Session-Token", out var headerToken))
                    token = headerToken.ToString();
            }

            if (!_protectionService.ValidateSession(token, clientIp))
            {
                return StatusCode(StatusCodes.Status403Forbidden, new
                {
                    success = false,
                    requiresApproval = true,
                    requiresPin = true,
                    message = "Host PC approval required. Please tap 'Request Approval' to notify the PC owner."
                });
            }
        }

        // 3. Sliding window rate limiting
        var rateLimit = _protectionService.CheckRateLimit(clientIp);
        if (!rateLimit.Allowed)
        {
            return StatusCode(StatusCodes.Status429TooManyRequests, new
            {
                success = false,
                message = rateLimit.ErrorMessage ?? "Rate limit exceeded. Please wait a minute before submitting another print job."
            });
        }

        // 4. Concurrency check (Max 1 active job per client)
        if (_protectionService.HasActiveJob(clientIp, _queueManager))
        {
            return BadRequest(new
            {
                success = false,
                message = "You already have an active print job queued or printing. Please wait for it to complete."
            });
        }

        if (file == null || file.Length == 0)
            return BadRequest(new { success = false, message = "Please select or drop a document to print." });

        var printSettings = new PrintSettings();
        if (!string.IsNullOrEmpty(settingsJson))
        {
            try
            {
                printSettings = System.Text.Json.JsonSerializer.Deserialize<PrintSettings>(settingsJson,
                    new System.Text.Json.JsonSerializerOptions { PropertyNameCaseInsensitive = true }) ?? new PrintSettings();
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "Failed to parse settingsJson, applying defaults");
            }
        }

        // Clamp copies to prevent paper waste
        var requestedCopies = copies.HasValue && copies.Value > 0 ? copies.Value : (printSettings.Copies > 0 ? printSettings.Copies : 1);
        printSettings.Copies = Math.Clamp(requestedCopies, 1, _settings.MaxWebPrintCopies);

        if (!string.IsNullOrEmpty(colorMode)) printSettings.ColorMode = colorMode;
        if (duplex.HasValue) printSettings.Duplex = duplex.Value;
        if (!string.IsNullOrEmpty(orientation)) printSettings.Orientation = orientation;
        if (!string.IsNullOrEmpty(pageSize)) printSettings.PageSize = pageSize;
        if (!string.IsNullOrEmpty(pageRange)) printSettings.PageRange = pageRange;
        if (!string.IsNullOrEmpty(pdfPassword)) printSettings.PdfPassword = pdfPassword;

        // Determine destination printer
        var targetPrinter = printerName?.Trim();
        if (string.IsNullOrWhiteSpace(targetPrinter))
        {
            targetPrinter = _printerService.GetDefaultPrinter()?.Name 
                ?? _printerService.GetAllPrinters().FirstOrDefault()?.Name 
                ?? "";
        }

        if (string.IsNullOrWhiteSpace(targetPrinter))
            return BadRequest(new { success = false, message = "No printer found on host machine. Please connect or install a printer." });

        // Save & validate file
        var (filePath, saveError) = await _fileService.SaveAndValidateFileAsync(
            file.OpenReadStream(), file.FileName, file.Length);

        if (filePath == null)
            return BadRequest(new { success = false, message = saveError ?? "File upload or validation failed" });

        // Convert if needed (DOCX->PDF, TXT->PDF)
        var (convertedPath, convertError) = await _fileService.ConvertIfNeededAsync(filePath);
        if (convertedPath == null)
            return BadRequest(new { success = false, message = convertError ?? "Document conversion failed" });

        // Check password and page count for PDFs
        var finalPdfPath = convertedPath ?? filePath;
        if (Path.GetExtension(finalPdfPath).Equals(".pdf", StringComparison.OrdinalIgnoreCase))
        {
            var pdfValidation = _printerService.ValidatePdf(finalPdfPath, printSettings.PdfPassword);
            if (pdfValidation.RequiresPassword)
            {
                return BadRequest(new
                {
                    success = false,
                    requiresPassword = true,
                    isLocked = true,
                    message = pdfValidation.ErrorMessage ?? "This PDF is password-protected. Please enter the password to print."
                });
            }

            if (pdfValidation.PageCount > _settings.MaxWebPrintPages)
            {
                try { if (System.IO.File.Exists(filePath)) System.IO.File.Delete(filePath); } catch { }
                try { if (convertedPath != null && System.IO.File.Exists(convertedPath)) System.IO.File.Delete(convertedPath); } catch { }

                return BadRequest(new
                {
                    success = false,
                    message = $"This document has {pdfValidation.PageCount} pages. The server allows a maximum of {_settings.MaxWebPrintPages} pages per web print to prevent spam and paper exhaustion."
                });
            }
        }

        // Record successful submission for rate limiting
        _protectionService.RecordSubmission(clientIp);

        var userAgent = Request.Headers.UserAgent.ToString();
        var clientTag = "Web Browser";
        if (userAgent.Contains("Edg/")) clientTag = "Edge Web";
        else if (userAgent.Contains("Chrome/")) clientTag = "Chrome Web";
        else if (userAgent.Contains("Firefox/")) clientTag = "Firefox Web";
        else if (userAgent.Contains("Safari/") && !userAgent.Contains("Chrome")) clientTag = "Safari Web";
        if (userAgent.Contains("iPhone") || userAgent.Contains("Android") || userAgent.Contains("Mobile"))
            clientTag = "Mobile " + clientTag;

        var job = new PrintJob
        {
            FileName = file.FileName,
            OriginalFileName = file.FileName,
            FileSize = file.Length,
            FileType = FileProcessingService.GetFileType(file.FileName),
            FilePath = filePath,
            ConvertedFilePath = convertedPath != filePath ? convertedPath : null,
            Settings = printSettings,
            PrinterName = targetPrinter,
            DeviceId = $"web-{clientIp}",
            DeviceName = $"{clientTag} ({clientIp})",
            QueueState = "Queued",
            UpdatedAt = DateTime.UtcNow
        };

        var jobId = _queueManager.EnqueueJob(job);
        _logger.LogInformation("Web print job queued: {JobId} for {File} on printer '{Printer}' by IP {Ip}", jobId, file.FileName, targetPrinter, clientIp);

        return Ok(new
        {
            success = true,
            jobId = jobId,
            fileName = file.FileName,
            printerName = targetPrinter,
            status = "Queued",
            queuePosition = _queueManager.GetQueuePosition(jobId),
            message = $"Job queued successfully on '{targetPrinter}'"
        });
    }

    /// <summary>
    /// POST /api/web/jobs/{id}/cancel — Cancel a queued or active job from the web UI.
    /// </summary>
    [HttpPost("/api/web/jobs/{id}/cancel")]
    public IActionResult CancelJob(string id)
    {
        var cancelled = _queueManager.CancelJob(id);
        if (cancelled)
            return Ok(new { success = true, message = "Job cancelled successfully." });
        return BadRequest(new { success = false, message = "Could not cancel job (it may have already completed or does not exist)." });
    }

    /// <summary>
    /// POST /api/web/jobs/clear — Clear completed and cancelled jobs from memory.
    /// </summary>
    [HttpPost("/api/web/jobs/clear")]
    public IActionResult ClearJobs()
    {
        var count = _queueManager.ClearCompletedJobs();
        return Ok(new { success = true, cleared = count, message = $"Cleared {count} completed job(s)." });
    }

    /// <summary>
    /// GET /print-qr — Generates a beautiful, printable A4 poster with the Web Print Studio QR code.
    /// Shop owners can print this and display it at the counter for customers to scan.
    /// </summary>
    [HttpGet("/print-qr")]
    [HttpGet("/{hostId}/print-qr")]
    [Produces("text/html")]
    public ContentResult PrintableQrPoster(string? hostId = null)
    {
        var tunnelUrl = _tunnelService.PublicUrl ?? Program.TunnelServiceInstance?.PublicUrl ?? "";
        var localIp = DiscoveryService.GetLocalIpAddress();
        var webUrl = _settings.GetWebPrintUrl(tunnelUrl, localIp);
        var serverName = _settings.ServerName;
        var hasTunnel = !string.IsNullOrWhiteSpace(tunnelUrl);

        var html = $$"""
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Print QR - {{serverName}}</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">
    <style>
        @page {
            size: A4;
            margin: 0;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        }

        body {
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background: #F8FAFC;
            padding: 24px;
        }

        .poster {
            width: 100%;
            max-width: 520px;
            background: #FFFFFF;
            border-radius: 28px;
            padding: 48px 36px 40px;
            text-align: center;
            border: 2px solid #E2E8F0;
            box-shadow: 0 4px 24px rgba(15, 23, 42, 0.06);
        }

        @media print {
            body {
                background: #FFFFFF;
                padding: 0;
                justify-content: center;
            }
            .poster {
                border: none;
                box-shadow: none;
                border-radius: 0;
                max-width: 100%;
                padding: 60px 40px 50px;
            }
            .no-print {
                display: none !important;
            }
        }

        .logo-row {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 14px;
            margin-bottom: 8px;
        }

        .logo-icon {
            width: 48px;
            height: 48px;
            border-radius: 14px;
            background: linear-gradient(135deg, #0067C0 0%, #004F96 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            color: #FFFFFF;
            box-shadow: 0 3px 10px rgba(0, 103, 192, 0.3);
        }

        .logo-icon .material-symbols-outlined {
            font-size: 28px;
            font-variation-settings: 'FILL' 1;
        }

        .shop-name {
            font-size: 26px;
            font-weight: 800;
            color: #0F172A;
            letter-spacing: -0.5px;
        }

        .tagline {
            font-size: 15px;
            color: #475569;
            margin-top: 4px;
            margin-bottom: 32px;
            font-weight: 500;
        }

        .qr-frame {
            display: inline-block;
            padding: 16px;
            background: #FFFFFF;
            border: 3px solid #0067C0;
            border-radius: 20px;
            box-shadow: 0 4px 16px rgba(0, 103, 192, 0.12);
            margin-bottom: 24px;
        }

        .qr-frame img {
            width: 240px;
            height: 240px;
            display: block;
            border-radius: 8px;
        }

        .scan-instruction {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            font-size: 20px;
            font-weight: 700;
            color: #0067C0;
            margin-bottom: 8px;
        }

        .scan-instruction .material-symbols-outlined {
            font-size: 28px;
            font-variation-settings: 'FILL' 1;
        }

        .scan-sub {
            font-size: 14px;
            color: #64748B;
            line-height: 1.5;
            max-width: 360px;
            margin: 0 auto 20px;
        }

        .steps-row {
            display: flex;
            align-items: flex-start;
            justify-content: center;
            gap: 20px;
            margin: 24px 0 20px;
        }

        .step-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 6px;
            width: 100px;
        }

        .step-circle {
            width: 44px;
            height: 44px;
            border-radius: 50%;
            background: #EBF3FB;
            color: #0067C0;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 800;
            font-size: 16px;
            border: 2px solid #BCD7F5;
        }

        .step-circle .material-symbols-outlined {
            font-size: 22px;
            font-variation-settings: 'FILL' 1;
        }

        .step-label {
            font-size: 12px;
            font-weight: 600;
            color: #334155;
            text-align: center;
            line-height: 1.3;
        }

        .step-arrow {
            font-size: 20px;
            color: #CBD5E1;
            margin-top: 12px;
        }

        .url-display {
            background: #F1F5F9;
            border: 1px solid #E2E8F0;
            border-radius: 10px;
            padding: 10px 16px;
            font-size: 12px;
            color: #475569;
            word-break: break-all;
            font-family: 'JetBrains Mono', monospace, 'Courier New', Courier;
            margin-top: 16px;
        }

        .tunnel-note {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            font-size: 11px;
            color: #94A3B8;
            margin-top: 12px;
        }

        .tunnel-note .material-symbols-outlined {
            font-size: 16px;
        }

        .supported-formats {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            margin-top: 16px;
            flex-wrap: wrap;
        }

        .format-tag {
            background: #EBF3FB;
            color: #0067C0;
            font-size: 11px;
            font-weight: 700;
            padding: 3px 10px;
            border-radius: 6px;
            border: 1px solid #BCD7F5;
        }

        .powered-by {
            font-size: 10px;
            color: #CBD5E1;
            margin-top: 20px;
            font-weight: 500;
        }

        .print-btn-bar {
            position: fixed;
            bottom: 0;
            left: 0;
            right: 0;
            background: rgba(255,255,255,0.95);
            backdrop-filter: blur(12px);
            border-top: 1px solid #E2E8F0;
            padding: 12px 20px;
            display: flex;
            gap: 10px;
            justify-content: center;
            z-index: 100;
        }

        .btn-action {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            padding: 10px 24px;
            border-radius: 12px;
            border: none;
            font-weight: 700;
            font-size: 14px;
            cursor: pointer;
            transition: all 0.15s ease;
        }

        .btn-action:active {
            transform: scale(0.97);
        }

        .btn-print {
            background: #0067C0;
            color: #FFFFFF;
            box-shadow: 0 2px 8px rgba(0,103,192,0.25);
        }

        .btn-back {
            background: #F1F5F9;
            color: #475569;
            border: 1px solid #E2E8F0;
        }

        .material-symbols-outlined {
            font-size: 20px;
            vertical-align: middle;
            font-variation-settings: 'FILL' 0, 'wght' 400;
        }
    </style>
</head>
<body>

    <div class="poster">
        <div class="logo-row">
            <div class="logo-icon">
                <span class="material-symbols-outlined">print</span>
            </div>
            <div class="shop-name">{{serverName}}</div>
        </div>
        <div class="tagline">Self-Service Document Printing</div>

        <div class="qr-frame">
            <img src="/api/web/qr" alt="Web Print Studio QR Code">
        </div>

        <div class="scan-instruction">
            <span class="material-symbols-outlined">qr_code_scanner</span>
            Scan to Print
        </div>
        <div class="scan-sub">
            Open your phone camera, scan this QR code, and upload any document to print. No app needed!
        </div>

        <div class="steps-row">
            <div class="step-item">
                <div class="step-circle">
                    <span class="material-symbols-outlined">photo_camera</span>
                </div>
                <div class="step-label">Scan QR</div>
            </div>
            <div class="step-arrow">&rarr;</div>
            <div class="step-item">
                <div class="step-circle">
                    <span class="material-symbols-outlined">upload_file</span>
                </div>
                <div class="step-label">Upload File</div>
            </div>
            <div class="step-arrow">&rarr;</div>
            <div class="step-item">
                <div class="step-circle">
                    <span class="material-symbols-outlined">print</span>
                </div>
                <div class="step-label">Collect Print</div>
            </div>
        </div>

        <div class="supported-formats">
            <div class="format-tag">PDF</div>
            <div class="format-tag">DOCX</div>
            <div class="format-tag">JPG</div>
            <div class="format-tag">PNG</div>
            <div class="format-tag">TXT</div>
        </div>

        <div class="url-display">{{webUrl}}</div>

        <div class="tunnel-note">
            <span class="material-symbols-outlined">info</span>
            Approval from shop PC required for printing
        </div>

        <div class="powered-by">Powered by Printora Cloud Print Studio</div>
    </div>

    <div class="print-btn-bar no-print">
        <button class="btn-action btn-back" onclick="window.history.back()">
            <span class="material-symbols-outlined">arrow_back</span>
            Back
        </button>
        <button class="btn-action btn-print" onclick="window.print()">
            <span class="material-symbols-outlined">print</span>
            Print This Poster
        </button>
    </div>

</body>
</html>
""";
        return new ContentResult { Content = html, ContentType = "text/html" };
    }

    /// <summary>
    /// GET / and GET /admin — Serves the responsive, Mobile-First Light Theme Web Print Studio.
    /// </summary>
    [HttpGet("/")]
    [HttpGet("/admin")]
    [HttpGet("/{hostId}")]
    [Produces("text/html")]
    public IActionResult Index(string? hostId = null)
    {
        if (!string.IsNullOrWhiteSpace(hostId))
        {
            if (hostId.Equals("print-qr", StringComparison.OrdinalIgnoreCase))
                return PrintableQrPoster();
            if (hostId.Equals("favicon.ico", StringComparison.OrdinalIgnoreCase))
                return NotFound();
            if (hostId.Equals("api", StringComparison.OrdinalIgnoreCase))
                return NotFound();
        }

        var localIp = DiscoveryService.GetLocalIpAddress();
        var serverName = _settings.ServerName;
        var port = _settings.ServerPort;
        var initialTunnelUrl = _tunnelService.PublicUrl ?? Program.TunnelServiceInstance?.PublicUrl ?? "";
        var webUrl = _settings.GetWebPrintUrl(initialTunnelUrl, localIp);

        var html = $$"""
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover">
    <meta name="theme-color" content="#FFFFFF">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="default">
    <title>{{serverName}} - Printora Cloud Print Studio</title>
    <!-- Fonts: Inter & Material Symbols Outlined -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500;600&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-canvas: #F8FAFC;
            --bg-card: #FFFFFF;
            --bg-subtle: #F1F5F9;
            --bg-hover: #E2E8F0;

            --primary: #0067C0;
            --primary-hover: #0052A3;
            --primary-light: #EBF3FB;
            --primary-border: #BCD7F5;

            --text-main: #0F172A;
            --text-sub: #475569;
            --text-muted: #94A3B8;

            --border: #E2E8F0;
            --border-strong: #CBD5E1;

            --success: #10B981;
            --success-light: #ECFDF5;
            --success-border: #A7F3D0;
            --success-text: #047857;

            --warning: #F59E0B;
            --warning-light: #FFFBEB;
            --warning-border: #FDE68A;
            --warning-text: #B45309;

            --error: #EF4444;
            --error-light: #FEF2F2;
            --error-border: #FECACA;
            --error-text: #B91C1C;

            --radius-xl: 18px;
            --radius-lg: 14px;
            --radius-md: 10px;
            --radius-sm: 6px;
            --radius-full: 9999px;

            --shadow-subtle: 0 1px 3px rgba(15, 23, 42, 0.04), 0 1px 2px rgba(15, 23, 42, 0.02);
            --shadow-card: 0 2px 8px rgba(15, 23, 42, 0.04), 0 1px 3px rgba(15, 23, 42, 0.02);
            --shadow-sheet: 0 -4px 24px rgba(15, 23, 42, 0.15);
            --shadow-floating: 0 8px 24px rgba(0, 103, 192, 0.25);
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            -webkit-tap-highlight-color: transparent;
        }

        body {
            background-color: var(--bg-canvas);
            color: var(--text-main);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            padding-bottom: 74px;
        }

        .material-symbols-outlined {
            font-size: 20px;
            vertical-align: middle;
            font-variation-settings: 'FILL' 0, 'wght' 400, 'GRAD' 0, 'opsz' 20;
        }

        .material-symbols-outlined.filled {
            font-variation-settings: 'FILL' 1;
        }

        /* Fixed Mobile Top App Bar */
        .mobile-header {
            position: sticky;
            top: 0;
            z-index: 40;
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border-bottom: 1px solid var(--border);
            padding: 10px 16px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
        }

        .header-brand {
            display: flex;
            align-items: center;
            gap: 10px;
            min-width: 0;
        }

        .header-logo {
            width: 36px;
            height: 36px;
            border-radius: var(--radius-md);
            background: linear-gradient(135deg, #0067C0 0%, #004F96 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            color: #FFFFFF;
            flex-shrink: 0;
            box-shadow: 0 2px 6px rgba(0, 103, 192, 0.25);
        }

        .header-info {
            min-width: 0;
        }

        .header-title {
            font-size: 15px;
            font-weight: 700;
            color: var(--text-main);
            line-height: 1.2;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .header-status {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 11px;
            color: var(--text-sub);
            margin-top: 1px;
        }

        .pulse-dot {
            width: 7px;
            height: 7px;
            border-radius: 50%;
            background-color: var(--success);
            position: relative;
            flex-shrink: 0;
        }

        .pulse-dot::after {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            border-radius: 50%;
            background-color: var(--success);
            animation: pulse-ring 1.8s cubic-bezier(0.24, 0, 0.38, 1) infinite;
        }

        @keyframes pulse-ring {
            0% { transform: scale(0.9); opacity: 0.8; }
            70% { transform: scale(2.4); opacity: 0; }
            100% { transform: scale(2.4); opacity: 0; }
        }

        .header-actions {
            display: flex;
            align-items: center;
            gap: 8px;
            flex-shrink: 0;
        }

        .btn-header-icon {
            width: 36px;
            height: 36px;
            border-radius: var(--radius-md);
            background: var(--bg-subtle);
            border: 1px solid var(--border);
            color: var(--text-main);
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.15s ease;
        }

        .btn-header-icon:active {
            transform: scale(0.94);
            background: var(--bg-hover);
        }

        /* Security Context Pill Banner */
        .security-banner {
            padding: 8px 16px;
            font-size: 12px;
            font-weight: 600;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 8px;
            border-bottom: 1px solid transparent;
            transition: all 0.2s ease;
        }

        .security-banner.local {
            background: var(--success-light);
            color: var(--success-text);
            border-color: var(--success-border);
        }

        .security-banner.tunnel-verified {
            background: var(--primary-light);
            color: var(--primary);
            border-color: var(--primary-border);
        }

        .security-banner.tunnel-locked {
            background: var(--warning-light);
            color: var(--warning-text);
            border-color: var(--warning-border);
            cursor: pointer;
        }

        .security-badge-left {
            display: flex;
            align-items: center;
            gap: 6px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .security-badge-action {
            font-size: 11px;
            text-decoration: underline;
            cursor: pointer;
            flex-shrink: 0;
        }

        /* Main Mobile Container */
        .mobile-container {
            width: 100%;
            max-width: 640px;
            margin: 0 auto;
            padding: 14px 14px 20px;
            display: flex;
            flex-direction: column;
            gap: 14px;
        }

        .tab-page {
            display: none;
            flex-direction: column;
            gap: 14px;
            animation: tab-fade 0.18s ease-out;
        }

        .tab-page.active {
            display: flex;
        }

        @keyframes tab-fade {
            from { opacity: 0; transform: translateY(4px); }
            to { opacity: 1; transform: translateY(0); }
        }

        /* Mobile Cards */
        .m-card {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-xl);
            padding: 16px;
            box-shadow: var(--shadow-card);
        }

        .m-card-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 12px;
        }

        .m-card-title {
            font-size: 14px;
            font-weight: 700;
            color: var(--text-main);
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .m-card-title .material-symbols-outlined {
            color: var(--primary);
            font-size: 18px;
        }

        /* Dropzone */
        .m-dropzone {
            border: 2px dashed var(--border-strong);
            background: var(--bg-subtle);
            border-radius: var(--radius-lg);
            padding: 24px 14px;
            text-align: center;
            cursor: pointer;
            transition: all 0.2s ease;
        }

        .m-dropzone:active, .m-dropzone.dragover {
            border-color: var(--primary);
            background: var(--primary-light);
            transform: scale(0.99);
        }

        .m-drop-icon {
            width: 48px;
            height: 48px;
            background: #FFFFFF;
            border-radius: 50%;
            margin: 0 auto 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--primary);
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
            border: 1px solid var(--border);
        }

        .m-drop-icon .material-symbols-outlined {
            font-size: 26px;
        }

        .m-drop-title {
            font-size: 14px;
            font-weight: 700;
            color: var(--text-main);
        }

        .m-drop-title span {
            color: var(--primary);
            text-decoration: underline;
        }

        .m-drop-sub {
            font-size: 11px;
            color: var(--text-sub);
            margin-top: 4px;
        }

        .m-chip-row {
            display: flex;
            justify-content: center;
            gap: 4px;
            margin-top: 10px;
            flex-wrap: wrap;
        }

        .m-chip {
            padding: 2px 7px;
            border-radius: var(--radius-sm);
            background: #FFFFFF;
            border: 1px solid var(--border);
            font-size: 10px;
            font-weight: 600;
            color: var(--text-sub);
        }

        .m-quota-badge {
            margin-top: 10px;
            display: inline-flex;
            align-items: center;
            gap: 5px;
            padding: 4px 10px;
            background: #F1F5F9;
            border: 1px solid var(--border);
            border-radius: var(--radius-full);
            font-size: 11px;
            color: var(--text-sub);
            font-weight: 600;
        }

        .m-quota-badge .material-symbols-outlined {
            font-size: 14px;
            color: var(--primary);
        }

        /* Staged File Card */
        .m-staged-file {
            display: none;
            align-items: center;
            justify-content: space-between;
            padding: 12px 14px;
            background: var(--primary-light);
            border: 1px solid var(--primary-border);
            border-radius: var(--radius-lg);
            gap: 10px;
        }

        .staged-left {
            display: flex;
            align-items: center;
            gap: 10px;
            min-width: 0;
        }

        .format-badge {
            width: 38px;
            height: 38px;
            border-radius: var(--radius-md);
            background: var(--primary);
            color: #FFFFFF;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 11px;
            flex-shrink: 0;
        }

        .staged-meta {
            min-width: 0;
        }

        .staged-name {
            font-size: 13px;
            font-weight: 700;
            color: var(--text-main);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            max-width: 200px;
        }

        .staged-size {
            font-size: 11px;
            color: var(--text-sub);
            margin-top: 1px;
        }

        .btn-remove-file {
            width: 32px;
            height: 32px;
            border-radius: var(--radius-md);
            background: #FFFFFF;
            border: 1px solid var(--border);
            color: var(--error);
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            flex-shrink: 0;
        }

        /* Form Controls */
        .m-form-group {
            display: flex;
            flex-direction: column;
            gap: 5px;
            margin-top: 12px;
        }

        .m-label {
            font-size: 12px;
            font-weight: 600;
            color: var(--text-main);
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .m-select, .m-input {
            width: 100%;
            height: 44px;
            padding: 0 12px;
            border-radius: var(--radius-md);
            border: 1px solid var(--border-strong);
            background: #FFFFFF;
            color: var(--text-main);
            font-size: 14px;
            font-weight: 500;
            outline: none;
            transition: border-color 0.15s ease;
        }

        .m-select:focus, .m-input:focus {
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(0, 103, 192, 0.12);
        }

        /* Stepper */
        .m-stepper {
            display: flex;
            align-items: center;
            height: 44px;
            border: 1px solid var(--border-strong);
            border-radius: var(--radius-md);
            background: #FFFFFF;
            overflow: hidden;
        }

        .m-stepper-btn {
            width: 46px;
            height: 100%;
            background: var(--bg-subtle);
            border: none;
            font-size: 18px;
            font-weight: 600;
            color: var(--text-main);
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .m-stepper-btn:active {
            background: var(--bg-hover);
        }

        .m-stepper-val {
            flex: 1;
            height: 100%;
            border: none;
            text-align: center;
            font-size: 15px;
            font-weight: 700;
            color: var(--text-main);
            outline: none;
            background: transparent;
        }

        /* Segmented Button Groups */
        .m-segmented {
            display: flex;
            background: var(--bg-subtle);
            border: 1px solid var(--border);
            border-radius: var(--radius-md);
            padding: 3px;
            gap: 2px;
        }

        .m-segment-btn {
            flex: 1;
            height: 36px;
            border: none;
            background: transparent;
            color: var(--text-sub);
            font-size: 12px;
            font-weight: 600;
            border-radius: var(--radius-sm);
            cursor: pointer;
            transition: all 0.15s ease;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 4px;
        }

        .m-segment-btn.active {
            background: #FFFFFF;
            color: var(--primary);
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
        }

        .m-password-box {
            display: none;
            background: var(--warning-light);
            border: 1px solid var(--warning-border);
            border-radius: var(--radius-md);
            padding: 10px 12px;
            margin-top: 10px;
        }

        .m-password-box.show {
            display: block;
        }

        .btn-primary {
            width: 100%;
            height: 50px;
            border-radius: var(--radius-lg);
            background: var(--primary);
            color: #FFFFFF;
            font-size: 15px;
            font-weight: 700;
            border: none;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            cursor: pointer;
            box-shadow: var(--shadow-floating);
            transition: all 0.15s ease;
            margin-top: 18px;
        }

        .btn-primary:active {
            transform: scale(0.98);
            background: var(--primary-hover);
        }

        .btn-primary:disabled {
            background: var(--border-strong);
            color: var(--text-muted);
            box-shadow: none;
            cursor: not-allowed;
        }

        /* Queue Job Cards */
        .m-job-card {
            background: #FFFFFF;
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: 14px;
            margin-bottom: 10px;
            display: flex;
            flex-direction: column;
            gap: 10px;
            box-shadow: var(--shadow-subtle);
        }

        .job-card-top {
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            gap: 10px;
        }

        .job-card-title {
            font-size: 14px;
            font-weight: 700;
            color: var(--text-main);
            word-break: break-all;
        }

        .job-card-sub {
            font-size: 11px;
            color: var(--text-sub);
            margin-top: 2px;
        }

        .status-pill {
            padding: 3px 8px;
            border-radius: var(--radius-full);
            font-size: 11px;
            font-weight: 700;
            white-space: nowrap;
        }

        .status-pill.Queued { background: var(--warning-light); color: var(--warning-text); border: 1px solid var(--warning-border); }
        .status-pill.Processing { background: var(--primary-light); color: var(--primary); border: 1px solid var(--primary-border); }
        .status-pill.Printing { background: var(--primary-light); color: var(--primary); border: 1px solid var(--primary-border); }
        .status-pill.Completed { background: var(--success-light); color: var(--success-text); border: 1px solid var(--success-border); }
        .status-pill.Failed { background: var(--error-light); color: var(--error-text); border: 1px solid var(--error-border); }
        .status-pill.Cancelled { background: var(--bg-subtle); color: var(--text-muted); border: 1px solid var(--border); }

        .job-meta-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 11px;
            color: var(--text-sub);
            padding-top: 8px;
            border-top: 1px solid var(--bg-subtle);
        }

        .btn-cancel-job {
            padding: 4px 10px;
            background: var(--error-light);
            border: 1px solid var(--error-border);
            color: var(--error-text);
            border-radius: var(--radius-sm);
            font-size: 11px;
            font-weight: 600;
            cursor: pointer;
        }

        /* Printer Item Card */
        .m-printer-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 12px 14px;
            background: #FFFFFF;
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            margin-bottom: 8px;
            cursor: pointer;
            transition: border-color 0.15s ease;
        }

        .m-printer-item.selected {
            border-color: var(--primary);
            background: var(--primary-light);
        }

        .printer-left {
            display: flex;
            align-items: center;
            gap: 12px;
            min-width: 0;
        }

        .printer-icon {
            width: 40px;
            height: 40px;
            border-radius: var(--radius-md);
            background: #FFFFFF;
            border: 1px solid var(--border);
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--primary);
            flex-shrink: 0;
        }

        .printer-name {
            font-size: 13px;
            font-weight: 700;
            color: var(--text-main);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .printer-status {
            font-size: 11px;
            color: var(--text-sub);
            margin-top: 2px;
        }

        /* Telemetry Bento Grid */
        .telemetry-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 10px;
        }

        .telemetry-box {
            background: var(--bg-subtle);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: 12px;
            text-align: left;
        }

        .telemetry-val {
            font-size: 20px;
            font-weight: 800;
            color: var(--text-main);
            font-family: 'JetBrains Mono', monospace;
        }

        .telemetry-lbl {
            font-size: 11px;
            color: var(--text-sub);
            margin-top: 2px;
            font-weight: 500;
        }

        /* Fixed Mobile Bottom Nav */
        .mobile-bottom-nav {
            position: fixed;
            bottom: 0;
            left: 0;
            right: 0;
            height: 64px;
            background: rgba(255, 255, 255, 0.96);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border-top: 1px solid var(--border);
            display: flex;
            align-items: center;
            justify-content: space-around;
            z-index: 50;
            padding-bottom: env(safe-area-inset-bottom, 0);
        }

        .nav-item {
            flex: 1;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            gap: 3px;
            background: transparent;
            border: none;
            color: var(--text-muted);
            cursor: pointer;
            transition: color 0.15s ease;
            position: relative;
        }

        .nav-item .material-symbols-outlined {
            font-size: 22px;
        }

        .nav-label {
            font-size: 10px;
            font-weight: 600;
        }

        .nav-item.active {
            color: var(--primary);
        }

        .nav-item.active .material-symbols-outlined {
            font-variation-settings: 'FILL' 1;
        }

        .nav-badge {
            position: absolute;
            top: 8px;
            right: calc(50% - 16px);
            background: var(--error);
            color: #FFFFFF;
            font-size: 9px;
            font-weight: 700;
            border-radius: var(--radius-full);
            padding: 1px 5px;
            min-width: 14px;
            text-align: center;
        }

        /* 1-Click Host PC Approval Modal / Bottom Sheet */
        .m-pin-sheet-backdrop {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(15, 23, 42, 0.6);
            backdrop-filter: blur(8px);
            -webkit-backdrop-filter: blur(8px);
            z-index: 100;
            display: flex;
            align-items: flex-end;
            justify-content: center;
            animation: fadeIn 0.2s ease-out;
        }

        @media (min-width: 641px) {
            .m-pin-sheet-backdrop {
                align-items: center;
            }
            .m-pin-sheet {
                border-radius: var(--radius-xl) !important;
                max-width: 440px !important;
                box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2) !important;
            }
        }

        .m-pin-sheet {
            background: #FFFFFF;
            width: 100%;
            max-width: 520px;
            border-top-left-radius: 24px;
            border-top-right-radius: 24px;
            padding: 24px 20px 32px;
            box-shadow: var(--shadow-sheet);
            text-align: center;
            display: flex;
            flex-direction: column;
            align-items: center;
            animation: slideUp 0.25s cubic-bezier(0.16, 1, 0.3, 1);
        }

        @keyframes slideUp {
            from { transform: translateY(100%); }
            to { transform: translateY(0); }
        }

        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }

        .m-pin-handle {
            width: 40px;
            height: 4px;
            background: var(--border-strong);
            border-radius: var(--radius-full);
            margin-bottom: 16px;
        }

        .m-pin-icon-wrap {
            width: 56px;
            height: 56px;
            border-radius: 50%;
            background: var(--primary-light);
            border: 2px solid var(--primary-border);
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--primary);
            margin-bottom: 12px;
        }

        .m-pin-icon-wrap .material-symbols-outlined {
            font-size: 30px;
        }

        .m-pin-title {
            font-size: 18px;
            font-weight: 800;
            color: var(--text-main);
        }

        .m-pin-desc {
            font-size: 13px;
            color: var(--text-sub);
            margin-top: 6px;
            line-height: 1.4;
            max-width: 340px;
        }

        .m-pin-error {
            margin-top: 10px;
            font-size: 12px;
            font-weight: 600;
            color: var(--error-text);
            background: var(--error-light);
            border: 1px solid var(--error-border);
            border-radius: var(--radius-md);
            padding: 6px 12px;
            max-width: 320px;
        }

        .m-pin-note {
            margin-top: 16px;
            font-size: 11px;
            color: var(--text-muted);
            display: flex;
            align-items: center;
            gap: 4px;
        }

        .m-pin-note .material-symbols-outlined {
            font-size: 14px;
        }

        /* Waiting animation pulse */
        .approval-waiting-card {
            display: none;
            width: 100%;
            margin-top: 16px;
            padding: 14px 16px;
            background: var(--primary-light);
            border: 1px solid var(--primary-border);
            border-radius: var(--radius-lg);
            text-align: center;
        }

        .approval-waiting-card.active {
            display: block;
        }

        /* Toast */
        .m-toast {
            position: fixed;
            bottom: 78px;
            left: 50%;
            transform: translateX(-50%) translateY(20px);
            background: #0F172A;
            color: #FFFFFF;
            padding: 10px 18px;
            border-radius: var(--radius-full);
            font-size: 13px;
            font-weight: 600;
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
            opacity: 0;
            pointer-events: none;
            transition: all 0.2s ease;
            z-index: 80;
            white-space: nowrap;
            max-width: 90%;
            overflow: hidden;
            text-overflow: ellipsis;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .m-toast.show {
            opacity: 1;
            transform: translateX(-50%) translateY(0);
        }

        .hp-field {
            position: absolute !important;
            left: -9999px !important;
            opacity: 0 !important;
            pointer-events: none !important;
        }
    </style>
</head>
<body>

    <!-- Top App Bar -->
    <header class="mobile-header">
        <div class="header-brand">
            <div class="header-logo">
                <span class="material-symbols-outlined filled">print</span>
            </div>
            <div class="header-info">
                <div class="header-title">{{serverName}}</div>
                <div class="header-status">
                    <span class="pulse-dot"></span>
                    <span id="headerStatusText">Online</span> • <span id="headerPrinterCount">0</span> printers
                </div>
            </div>
        </div>
        <div class="header-actions">
            <button class="btn-header-icon" title="Refresh Data" onclick="fetchDashboardData(true)">
                <span class="material-symbols-outlined" id="refreshIcon">refresh</span>
            </button>
        </div>
    </header>

    <!-- Security Status Strip -->
    <div class="security-banner local" id="securityBanner">
        <div class="security-badge-left">
            <span class="material-symbols-outlined filled" id="securityIcon">verified_user</span>
            <span id="securityText">Detecting connection security...</span>
        </div>
        <span class="security-badge-action" id="securityAction" style="display:none;" onclick="showApprovalModal()">Request Approval</span>
    </div>

    <!-- Main Container -->
    <main class="mobile-container">

        <!-- TAB 1: PRINT STUDIO -->
        <section class="tab-page active" id="tab-print">
            <div class="m-card">
                <div class="m-card-header">
                    <div class="m-card-title">
                        <span class="material-symbols-outlined">description</span>
                        Document to Print
                    </div>
                    <span style="font-size: 11px; font-weight: 600; color: var(--text-sub);">Max 100MB</span>
                </div>

                <!-- Dropzone -->
                <div class="m-dropzone" id="mobileDropzone" onclick="document.getElementById('mobileFileInput').click()">
                    <div class="m-drop-icon">
                        <span class="material-symbols-outlined">cloud_upload</span>
                    </div>
                    <div class="m-drop-title">Tap to choose or <span>drop file</span></div>
                    <div class="m-drop-sub">PDF, Word DOCX, PNG, JPG, WebP, Plain Text</div>
                    <div class="m-chip-row">
                        <span class="m-chip">PDF</span>
                        <span class="m-chip">DOCX</span>
                        <span class="m-chip">IMAGE</span>
                        <span class="m-chip">TXT</span>
                    </div>
                    <div class="m-quota-badge">
                        <span class="material-symbols-outlined">shield</span>
                        <span>Max 3 copies • Max 30 pages</span>
                    </div>
                </div>

                <input type="file" id="mobileFileInput" style="display:none;" accept=".pdf,.docx,.txt,.jpg,.jpeg,.png,.webp,.bmp" onchange="handleFileSelect(this.files)">
                <input type="text" name="hp_website" id="hp_website" class="hp-field" tabindex="-1" autocomplete="off">

                <!-- Staged File Card -->
                <div class="m-staged-file" id="stagedFileCard">
                    <div class="staged-left">
                        <div class="format-badge" id="formatBadge">PDF</div>
                        <div class="staged-meta">
                            <div class="staged-name" id="stagedFileName">document.pdf</div>
                            <div class="staged-size" id="stagedFileSize">0 KB</div>
                        </div>
                    </div>
                    <button class="btn-remove-file" onclick="clearStagedFile()" title="Remove file">
                        <span class="material-symbols-outlined">close</span>
                    </button>
                </div>

                <!-- Password Box for PDF -->
                <div class="m-password-box" id="pdfPasswordBox">
                    <div class="m-label" style="color: var(--warning-text); margin-bottom: 4px;">
                        <span class="material-symbols-outlined" style="font-size:16px;">lock</span>
                        Protected PDF Password
                    </div>
                    <input type="password" id="pdfPasswordInput" class="m-input" placeholder="Enter PDF password">
                </div>

                <!-- Target Printer -->
                <div class="m-form-group">
                    <label class="m-label" for="selectPrinter">
                        Destination Printer
                        <span id="selectedPrinterStatus" style="font-size:11px; font-weight:500; color:var(--text-sub);">Ready</span>
                    </label>
                    <select id="selectPrinter" class="m-select">
                        <option value="">Searching for printers...</option>
                    </select>
                </div>

                <!-- Copies -->
                <div class="m-form-group">
                    <label class="m-label">
                        Number of Copies
                        <span style="font-size:11px; font-weight:500; color:var(--text-muted);">(Max 3)</span>
                    </label>
                    <div class="m-stepper">
                        <button type="button" class="m-stepper-btn" onclick="adjustCopies(-1)">−</button>
                        <input type="number" id="copiesVal" class="m-stepper-val" value="1" min="1" max="3" readonly>
                        <button type="button" class="m-stepper-btn" onclick="adjustCopies(1)">+</button>
                    </div>
                </div>

                <!-- Color Mode -->
                <div class="m-form-group">
                    <label class="m-label">Color Mode</label>
                    <div class="m-segmented">
                        <button type="button" class="m-segment-btn active" data-group="colorMode" data-val="Color" onclick="setSegment('colorMode', this)">
                            <span class="material-symbols-outlined">palette</span> Color
                        </button>
                        <button type="button" class="m-segment-btn" data-group="colorMode" data-val="Monochrome" onclick="setSegment('colorMode', this)">
                            <span class="material-symbols-outlined">filter_b_and_w</span> Black & White
                        </button>
                    </div>
                </div>

                <!-- Duplex -->
                <div class="m-form-group">
                    <label class="m-label">Print Sides (Duplex)</label>
                    <div class="m-segmented">
                        <button type="button" class="m-segment-btn active" data-group="duplex" data-val="false" onclick="setSegment('duplex', this)">
                            Single-Sided
                        </button>
                        <button type="button" class="m-segment-btn" data-group="duplex" data-val="true" onclick="setSegment('duplex', this)">
                            2-Sided (Long Edge)
                        </button>
                    </div>
                </div>

                <!-- Orientation -->
                <div class="m-form-group">
                    <label class="m-label">Orientation</label>
                    <div class="m-segmented">
                        <button type="button" class="m-segment-btn active" data-group="orientation" data-val="Portrait" onclick="setSegment('orientation', this)">
                            Portrait
                        </button>
                        <button type="button" class="m-segment-btn" data-group="orientation" data-val="Landscape" onclick="setSegment('orientation', this)">
                            Landscape
                        </button>
                    </div>
                </div>

                <!-- Print Action Button -->
                <button type="button" class="btn-primary" id="btnSubmitPrint" onclick="submitPrintJob()">
                    <span class="material-symbols-outlined">print</span>
                    Print Document Now
                </button>
            </div>
        </section>

        <!-- TAB 2: PRINT QUEUE -->
        <section class="tab-page" id="tab-queue">
            <div class="m-card">
                <div class="m-card-header">
                    <div class="m-card-title">
                        <span class="material-symbols-outlined">list_alt</span>
                        Print Queue
                    </div>
                    <button class="m-chip" style="cursor:pointer;" onclick="clearCompletedJobs()">Clear Completed</button>
                </div>
                <div id="queueListContainer">
                    <div style="text-align: center; padding: 30px 10px; color: var(--text-muted); font-size: 13px;">
                        No active jobs in queue.
                    </div>
                </div>
            </div>
        </section>

        <!-- TAB 3: PRINTERS LIST -->
        <section class="tab-page" id="tab-printers">
            <div class="m-card">
                <div class="m-card-header">
                    <div class="m-card-title">
                        <span class="material-symbols-outlined">devices</span>
                        Available Printers
                    </div>
                    <span class="m-chip" id="printerCountBadge">0 Online</span>
                </div>
                <div id="printersListContainer">
                    <div style="text-align: center; padding: 30px 10px; color: var(--text-muted); font-size: 13px;">
                        Scanning for system printers...
                    </div>
                </div>
            </div>
        </section>

        <!-- TAB 4: SERVER TELEMETRY & QR -->
        <section class="tab-page" id="tab-server">
            <div class="m-card">
                <div class="m-card-header">
                    <div class="m-card-title">
                        <span class="material-symbols-outlined">dns</span>
                        Host Machine Status
                    </div>
                    <span class="status-pill Completed">Active</span>
                </div>
                <div class="telemetry-grid">
                    <div class="telemetry-box">
                        <div class="telemetry-val" id="bentoJobs">0</div>
                        <div class="telemetry-lbl">Total Jobs</div>
                    </div>
                    <div class="telemetry-box">
                        <div class="telemetry-val" id="bentoPrinters">0</div>
                        <div class="telemetry-lbl">Printers Ready</div>
                    </div>
                    <div class="telemetry-box">
                        <div class="telemetry-val" id="bentoDevices">0</div>
                        <div class="telemetry-lbl">Paired Devices</div>
                    </div>
                    <div class="telemetry-box">
                        <div class="telemetry-val">{{port}}</div>
                        <div class="telemetry-lbl">HTTPS Port</div>
                    </div>
                </div>
            </div>

            <div class="m-card" id="tunnelCard">
                <div class="m-card-header">
                    <div class="m-card-title">
                        <span class="material-symbols-outlined" style="color:#F58220;">cloud_done</span>
                        Printora Cloud Relay
                    </div>
                    <span class="status-pill Completed" id="tunnelBadge">Connected</span>
                </div>
                <div style="font-size: 12px; color: var(--text-sub); line-height: 1.4; margin-bottom: 12px;">
                    Users anywhere in the world can open this link to print from mobile or desktop via Printora Cloud Service (requires 1-click PC approval).
                </div>
                <div style="display:flex; align-items:center; gap:8px;">
                    <input type="text" id="tunnelUrlBox" class="m-input" style="font-size:12px; height:40px;" readonly value="{{initialTunnelUrl}}">
                    <button class="btn-header-icon" title="Copy URL" onclick="copyTunnelUrl()">
                        <span class="material-symbols-outlined">content_copy</span>
                    </button>
                </div>
            </div>


        </section>

    </main>

    <!-- 1-Click Host PC Approval Modal / Bottom Sheet -->
    <div class="m-pin-sheet-backdrop" id="approvalBackdrop" style="display:none;">
        <div class="m-pin-sheet" id="approvalSheet">
            <div class="m-pin-handle"></div>
            <div class="m-pin-icon-wrap">
                <span class="material-symbols-outlined filled">verified_user</span>
            </div>
            <div class="m-pin-title">Host PC Approval Required</div>
            <div class="m-pin-desc">
                This print studio is accessed via secure cloud relay. Tap below to send an <strong>instant approval notification</strong> to the host computer screen.
            </div>

            <!-- Primary 1-Click Request Button -->
            <button type="button" class="btn-primary" id="btnRequestApproval" onclick="sendHostApprovalRequest()" style="width:100%;margin-top:18px;height:48px;font-size:15px;">
                <span class="material-symbols-outlined">send_to_mobile</span>
                Request Approval to Print
            </button>

            <!-- Waiting Pulse Card -->
            <div class="approval-waiting-card" id="approvalWaitingCard">
                <div style="display:flex; align-items:center; justify-content:center; gap:8px; font-weight:700; color:var(--primary); font-size:14px;">
                    <span class="pulse-dot"></span>
                    Notification Sent to PC Screen!
                </div>
                <div style="font-size:12px; color:var(--text-sub); margin-top:6px; line-height:1.4;">
                    A popup toast has appeared on the host PC. Please click <strong>"Approve"</strong> on your computer.
                </div>
            </div>

            <!-- Fallback PIN Section -->
            <div style="margin-top:16px;">
                <a href="javascript:void(0)" onclick="togglePinFallback()" style="font-size:12px; color:var(--text-sub); text-decoration:underline;">
                    Or enter 6-digit PIN manually
                </a>
            </div>

            <div id="pinFallbackBox" style="display:none; width:100%; max-width:280px; margin-top:12px;">
                <input type="text" id="pinInput" class="m-input" maxlength="6" inputmode="numeric" placeholder="6-Digit PIN" style="text-align:center; font-size:20px; font-weight:700; letter-spacing:6px; font-family:'JetBrains Mono',monospace;" onkeyup="handlePinKeyUp(event)">
                <button type="button" class="btn-primary" onclick="verifyHostPinFallback()" style="height:38px; margin-top:8px; font-size:13px;">
                    Verify PIN
                </button>
            </div>

            <div class="m-pin-error" id="approvalErrorMsg" style="display:none;"></div>

            <div class="m-pin-note">
                <span class="material-symbols-outlined">wifi</span>
                Local Wi-Fi connections print freely without an approval.
            </div>
        </div>
    </div>

    <!-- Bottom Nav -->
    <nav class="mobile-bottom-nav">
        <button class="nav-item active" onclick="switchTab('print')">
            <span class="material-symbols-outlined">print</span>
            <span class="nav-label">Print</span>
        </button>
        <button class="nav-item" onclick="switchTab('queue')">
            <span class="material-symbols-outlined">list_alt</span>
            <span class="nav-label">Queue</span>
            <span class="nav-badge" id="navQueueBadge" style="display:none;">0</span>
        </button>
        <button class="nav-item" onclick="switchTab('printers')">
            <span class="material-symbols-outlined">devices</span>
            <span class="nav-label">Printers</span>
        </button>
        <button class="nav-item" onclick="switchTab('server')">
            <span class="material-symbols-outlined">dns</span>
            <span class="nav-label">Server</span>
        </button>
    </nav>

    <!-- Toast -->
    <div class="m-toast" id="mToast">
        <span class="material-symbols-outlined" id="toastIcon" style="font-size:18px;">info</span>
        <span id="toastMsg">Action completed</span>
    </div>

    <!-- Scripts -->
    <script>
        let stagedFile = null;
        let activeTab = 'print';
        let serverOverview = null;
        let requiresApproval = false;
        let isWaitingApproval = false;

        function switchTab(tabId) {
            activeTab = tabId;
            document.querySelectorAll('.tab-page').forEach(page => page.classList.remove('active'));
            document.querySelectorAll('.nav-item').forEach(btn => btn.classList.remove('active'));

            const targetPage = document.getElementById('tab-' + tabId);
            if (targetPage) targetPage.classList.add('active');

            const navIndex = ['print', 'queue', 'printers', 'server'].indexOf(tabId);
            if (navIndex >= 0) {
                const navBtns = document.querySelectorAll('.nav-item');
                if (navBtns[navIndex]) navBtns[navIndex].classList.add('active');
            }
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }

        function adjustCopies(delta) {
            const input = document.getElementById('copiesVal');
            let val = parseInt(input.value) || 1;
            val = Math.max(1, Math.min(3, val + delta));
            input.value = val;
        }

        function setSegment(group, btn) {
            const container = btn.parentElement;
            container.querySelectorAll('.m-segment-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
        }

        function getSegmentValue(group) {
            const active = document.querySelector(`.m-segment-btn.active[data-group="${group}"]`);
            return active ? active.getAttribute('data-val') : null;
        }

        // Dropzone & File Handling
        const dropzone = document.getElementById('mobileDropzone');
        ['dragenter', 'dragover'].forEach(name => {
            dropzone.addEventListener(name, (e) => { e.preventDefault(); dropzone.classList.add('dragover'); }, false);
        });
        ['dragleave', 'drop'].forEach(name => {
            dropzone.addEventListener(name, (e) => { e.preventDefault(); dropzone.classList.remove('dragover'); }, false);
        });
        dropzone.addEventListener('drop', (e) => {
            if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
                handleFileSelect(e.dataTransfer.files);
            }
        });

        function handleFileSelect(files) {
            if (!files || files.length === 0) return;
            const file = files[0];
            stagedFile = file;

            document.getElementById('mobileDropzone').style.display = 'none';
            const stagedCard = document.getElementById('stagedFileCard');
            stagedCard.style.display = 'flex';

            document.getElementById('stagedFileName').textContent = file.name;
            document.getElementById('stagedFileSize').textContent = formatBytes(file.size);

            const ext = file.name.split('.').pop().toUpperCase();
            document.getElementById('formatBadge').textContent = ext.substring(0, 4);

            if (ext === 'PDF') {
                document.getElementById('pdfPasswordBox').classList.add('show');
            } else {
                document.getElementById('pdfPasswordBox').classList.remove('show');
            }
        }

        function clearStagedFile() {
            stagedFile = null;
            document.getElementById('mobileFileInput').value = '';
            document.getElementById('mobileDropzone').style.display = 'block';
            document.getElementById('stagedFileCard').style.display = 'none';
            document.getElementById('pdfPasswordBox').classList.remove('show');
            document.getElementById('pdfPasswordInput').value = '';
        }

        // Security Context & 1-Click Host Approval
        function updateSecurityStatus(overview) {
            const banner = document.getElementById('securityBanner');
            const icon = document.getElementById('securityIcon');
            const text = document.getElementById('securityText');
            const action = document.getElementById('securityAction');
            const storedToken = localStorage.getItem('spool_session_token');

            requiresApproval = overview.requiresApproval || overview.requiresPin;

            if (overview.isLocalSubnet) {
                banner.className = 'security-banner local';
                icon.textContent = 'verified_user';
                text.textContent = '✓ Local Wi-Fi Protected (No Approval Needed)';
                action.style.display = 'none';
                hideApprovalModal();
            } else if (requiresApproval) {
                if (storedToken) {
                    banner.className = 'security-banner tunnel-verified';
                    icon.textContent = 'lock_open';
                    text.textContent = '✓ Public Cloudflare Tunnel (Host Approved)';
                    action.style.display = 'none';
                    hideApprovalModal();
                } else {
                    banner.className = 'security-banner tunnel-locked';
                    icon.textContent = 'lock';
                    text.textContent = '🔒 Host PC Approval Required to Print';
                    action.style.display = 'inline';
                    if (!isWaitingApproval) {
                        showApprovalModal();
                    }
                }
            } else {
                banner.className = 'security-banner local';
                icon.textContent = 'verified_user';
                text.textContent = 'Protected Connection (' + overview.clientIp + ')';
                action.style.display = 'none';
            }
        }

        function showApprovalModal() {
            document.getElementById('approvalBackdrop').style.display = 'flex';
            document.getElementById('approvalErrorMsg').style.display = 'none';
        }

        function hideApprovalModal() {
            document.getElementById('approvalBackdrop').style.display = 'none';
        }

        async function sendHostApprovalRequest() {
            const btn = document.getElementById('btnRequestApproval');
            const waitingCard = document.getElementById('approvalWaitingCard');
            const errorMsg = document.getElementById('approvalErrorMsg');

            btn.disabled = true;
            btn.style.display = 'none';
            waitingCard.classList.add('active');
            errorMsg.style.display = 'none';
            isWaitingApproval = true;

            showToast('Notification sent to PC! Please approve on your computer screen.', 'send_to_mobile');

            try {
                const res = await fetch('/api/web/request-approval', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ deviceName: navigator.userAgent.includes('Mobile') ? 'Mobile Web Browser' : 'Desktop Web Browser' })
                });

                const data = await res.json();
                if (res.ok && data.success && data.token) {
                    localStorage.setItem('spool_session_token', data.token);
                    isWaitingApproval = false;
                    waitingCard.classList.remove('active');
                    btn.style.display = 'flex';
                    btn.disabled = false;
                    hideApprovalModal();
                    showToast('✓ Access Approved by PC! Printing unlocked.', 'check_circle');
                    if (serverOverview) updateSecurityStatus(serverOverview);
                } else {
                    isWaitingApproval = false;
                    waitingCard.classList.remove('active');
                    btn.style.display = 'flex';
                    btn.disabled = false;
                    btn.innerHTML = '<span class="material-symbols-outlined">refresh</span> Try Requesting Approval Again';
                    errorMsg.textContent = data.message || 'Approval was denied or timed out.';
                    errorMsg.style.display = 'block';
                }
            } catch (err) {
                isWaitingApproval = false;
                waitingCard.classList.remove('active');
                btn.style.display = 'flex';
                btn.disabled = false;
                errorMsg.textContent = 'Network error while waiting for PC approval.';
                errorMsg.style.display = 'block';
            }
        }

        function togglePinFallback() {
            const box = document.getElementById('pinFallbackBox');
            box.style.display = (box.style.display === 'none') ? 'block' : 'none';
            if (box.style.display === 'block') {
                document.getElementById('pinInput').focus();
            }
        }

        function handlePinKeyUp(event) {
            if (event.key === 'Enter') verifyHostPinFallback();
        }

        async function verifyHostPinFallback() {
            const input = document.getElementById('pinInput');
            const pin = input.value.trim();
            const errorMsg = document.getElementById('approvalErrorMsg');

            if (!pin || pin.length < 4) {
                errorMsg.textContent = 'Please enter 6-digit PIN.';
                errorMsg.style.display = 'block';
                return;
            }

            try {
                const res = await fetch('/api/web/verify-pin', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ pin: pin })
                });

                const data = await res.json();
                if (res.ok && data.success && data.token) {
                    localStorage.setItem('spool_session_token', data.token);
                    hideApprovalModal();
                    showToast('✓ PIN verified! Printing unlocked.', 'check_circle');
                    if (serverOverview) updateSecurityStatus(serverOverview);
                } else {
                    errorMsg.textContent = data.message || 'Invalid PIN.';
                    errorMsg.style.display = 'block';
                }
            } catch (err) {
                errorMsg.textContent = 'Network error during PIN verification.';
                errorMsg.style.display = 'block';
            }
        }

        // Overview Fetcher
        async function fetchDashboardData(manual = false) {
            const refreshIcon = document.getElementById('refreshIcon');
            if (manual && refreshIcon) refreshIcon.style.transform = 'rotate(180deg)';

            try {
                const res = await fetch('/api/admin/overview');
                if (!res.ok) throw new Error('Failed to load overview');
                const data = await res.json();
                serverOverview = data;

                updateSecurityStatus(data);
                updatePrintersUI(data.printers, data.defaultPrinter);
                updateQueueUI(data.jobs);

                document.getElementById('bentoJobs').textContent = data.jobs ? data.jobs.length : 0;
                document.getElementById('bentoPrinters').textContent = data.printers ? data.printers.length : 0;
                document.getElementById('bentoDevices').textContent = data.devices ? data.devices.length : 0;
                document.getElementById('headerPrinterCount').textContent = data.printers ? data.printers.length : 0;

                if (data.tunnelUrl) {
                    document.getElementById('tunnelUrlBox').value = data.tunnelUrl;
                    document.getElementById('tunnelBadge').textContent = 'Connected';
                }

                if (manual) showToast('Data refreshed', 'done');
            } catch (err) {
                console.warn('Dashboard fetch error:', err);
                if (manual) showToast('Failed to refresh data', 'error');
            } finally {
                if (refreshIcon) setTimeout(() => { refreshIcon.style.transform = 'none'; }, 300);
            }
        }

        function updatePrintersUI(printers, defaultPrinter) {
            const select = document.getElementById('selectPrinter');
            const listContainer = document.getElementById('printersListContainer');
            const countBadge = document.getElementById('printerCountBadge');

            if (!printers || printers.length === 0) {
                select.innerHTML = '<option value="">No printers detected</option>';
                listContainer.innerHTML = '<div style="text-align: center; padding: 30px 10px; color: var(--text-muted); font-size: 13px;">No printers installed on host machine.</div>';
                countBadge.textContent = '0 Online';
                return;
            }

            countBadge.textContent = printers.length + ' Online';

            const currentSelected = select.value;
            let optionsHtml = '';
            printers.forEach(p => {
                const isDef = (p.name === defaultPrinter);
                const isSel = currentSelected ? (p.name === currentSelected) : isDef;
                optionsHtml += `<option value="${escapeHtml(p.name)}" ${isSel ? 'selected' : ''}>${escapeHtml(p.name)} ${isDef ? '(Default)' : ''}</option>`;
            });
            select.innerHTML = optionsHtml;

            let cardsHtml = '';
            printers.forEach(p => {
                const isDef = (p.name === defaultPrinter);
                cardsHtml += `
                    <div class="m-printer-item ${p.name === select.value ? 'selected' : ''}" onclick="selectPrinterFromTab('${escapeJs(p.name)}')">
                        <div class="printer-left">
                            <div class="printer-icon">
                                <span class="material-symbols-outlined">print</span>
                            </div>
                            <div>
                                <div class="printer-name">${escapeHtml(p.name)}</div>
                                <div class="printer-status">${p.status || 'Ready'} • ${p.driverName || 'Windows Driver'}</div>
                            </div>
                        </div>
                        ${isDef ? '<span class="status-pill Processing">Default</span>' : ''}
                    </div>
                `;
            });
            listContainer.innerHTML = cardsHtml;
        }

        function selectPrinterFromTab(name) {
            const select = document.getElementById('selectPrinter');
            select.value = name;
            showToast('Selected printer: ' + name, 'print');
            switchTab('print');
        }

        function updateQueueUI(jobs) {
            const container = document.getElementById('queueListContainer');
            const navBadge = document.getElementById('navQueueBadge');

            if (!jobs || jobs.length === 0) {
                container.innerHTML = '<div style="text-align: center; padding: 30px 10px; color: var(--text-muted); font-size: 13px;">No active or recent jobs in queue.</div>';
                navBadge.style.display = 'none';
                return;
            }

            const activeJobs = jobs.filter(j => j.queueState === 'Queued' || j.queueState === 'Processing' || j.queueState === 'Printing');
            if (activeJobs.length > 0) {
                navBadge.textContent = activeJobs.length;
                navBadge.style.display = 'inline-block';
            } else {
                navBadge.style.display = 'none';
            }

            let html = '';
            jobs.forEach(job => {
                const canCancel = (job.queueState === 'Queued' || job.queueState === 'Processing');
                html += `
                    <div class="m-job-card">
                        <div class="job-card-top">
                            <div style="min-width:0;">
                                <div class="job-card-title">${escapeHtml(job.fileName || 'Document')}</div>
                                <div class="job-card-sub">${escapeHtml(job.printerName || 'Printer')} • ${escapeHtml(job.deviceName || 'Client')}</div>
                            </div>
                            <span class="status-pill ${job.queueState}">${job.queueState}</span>
                        </div>
                        <div class="job-meta-row">
                            <span>${formatBytes(job.fileSize || 0)} • ${job.settings ? job.settings.copies + ' copy' : '1 copy'}</span>
                            ${canCancel ? `<button class="btn-cancel-job" onclick="cancelJob('${job.id}')">Cancel Job</button>` : ''}
                        </div>
                    </div>
                `;
            });
            container.innerHTML = html;
        }

        // Job Submission
        async function submitPrintJob() {
            if (!stagedFile) {
                showToast('Please select a file to print first', 'warning');
                return;
            }

            if (requiresApproval && !localStorage.getItem('spool_session_token')) {
                showApprovalModal();
                showToast('Host PC approval required to print', 'lock');
                return;
            }

            const btn = document.getElementById('btnSubmitPrint');
            btn.disabled = true;
            btn.innerHTML = '<span class="material-symbols-outlined">sync</span> Uploading & Spooling...';

            const formData = new FormData();
            formData.append('file', stagedFile);
            formData.append('printerName', document.getElementById('selectPrinter').value);
            formData.append('copies', document.getElementById('copiesVal').value);
            formData.append('colorMode', getSegmentValue('colorMode') || 'Color');
            formData.append('duplex', getSegmentValue('duplex') || 'false');
            formData.append('orientation', getSegmentValue('orientation') || 'Portrait');
            formData.append('pdfPassword', document.getElementById('pdfPasswordInput').value);

            formData.append('hp_website', document.getElementById('hp_website').value);
            const token = localStorage.getItem('spool_session_token') || '';
            if (token) {
                formData.append('sessionToken', token);
            }

            try {
                const res = await fetch('/api/web/print', {
                    method: 'POST',
                    headers: { 'X-Web-Session-Token': token },
                    body: formData
                });

                const data = await res.json();

                if (res.status === 403 && (data.requiresApproval || data.requiresPin)) {
                    localStorage.removeItem('spool_session_token');
                    showApprovalModal();
                    showToast('Approval expired or required. Tap to request.', 'lock');
                    return;
                }

                if (res.status === 429) {
                    showToast(data.message || 'Rate limit reached. Please wait 1 minute.', 'schedule');
                    return;
                }

                if (res.ok && data.success) {
                    showToast('✓ Job queued successfully!', 'check_circle');
                    clearStagedFile();
                    switchTab('queue');
                    fetchDashboardData();
                } else {
                    showToast(data.message || 'Print submission failed', 'error');
                }
            } catch (err) {
                showToast('Network error while submitting job', 'error');
            } finally {
                btn.disabled = false;
                btn.innerHTML = '<span class="material-symbols-outlined">print</span> Print Document Now';
            }
        }

        async function cancelJob(jobId) {
            try {
                const res = await fetch(`/api/web/jobs/${jobId}/cancel`, { method: 'POST' });
                const data = await res.json();
                if (res.ok) {
                    showToast('Job cancelled', 'done');
                    fetchDashboardData();
                } else {
                    showToast(data.message || 'Could not cancel job', 'error');
                }
            } catch (err) {
                showToast('Error cancelling job', 'error');
            }
        }

        async function clearCompletedJobs() {
            try {
                const res = await fetch('/api/web/jobs/clear', { method: 'POST' });
                const data = await res.json();
                showToast(data.message || 'Queue cleared', 'done');
                fetchDashboardData();
            } catch (err) {
                showToast('Error clearing queue', 'error');
            }
        }

        function copyTunnelUrl() {
            const input = document.getElementById('tunnelUrlBox');
            input.select();
            input.setSelectionRange(0, 99999);
            navigator.clipboard.writeText(input.value).then(() => {
                showToast('Remote URL copied to clipboard!', 'content_copy');
            }).catch(() => {
                document.execCommand('copy');
                showToast('Remote URL copied!', 'content_copy');
            });
        }

        let toastTimeout;
        function showToast(msg, icon = 'info') {
            const toast = document.getElementById('mToast');
            const msgEl = document.getElementById('toastMsg');
            const iconEl = document.getElementById('toastIcon');

            msgEl.textContent = msg;
            iconEl.textContent = icon;
            toast.classList.add('show');

            clearTimeout(toastTimeout);
            toastTimeout = setTimeout(() => {
                toast.classList.remove('show');
            }, 3500);
        }

        function formatBytes(bytes) {
            if (!bytes || bytes === 0) return '0 B';
            const k = 1024;
            const sizes = ['B', 'KB', 'MB', 'GB'];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
        }

        function escapeHtml(text) {
            if (!text) return '';
            return text.toString()
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }

        function escapeJs(text) {
            if (!text) return '';
            return text.toString().replace(/'/g, "\\'");
        }

        fetchDashboardData();
        setInterval(fetchDashboardData, 3000);
    </script>
</body>
</html>
""";

        return base.Content(html, "text/html", Encoding.UTF8);
    }
}
