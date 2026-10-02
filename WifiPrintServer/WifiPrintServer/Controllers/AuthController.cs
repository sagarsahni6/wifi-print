using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using WifiPrintServer.Models;
using WifiPrintServer.Security;
using WifiPrintServer.Services;

namespace WifiPrintServer.Controllers;

/// <summary>
/// Authentication endpoints — device approval flow (replaces PIN pairing).
/// </summary>
[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly AuthService _authService;
    private readonly AppSettings _settings;
    private readonly ILogger<AuthController> _logger;

    public AuthController(AuthService authService, AppSettings settings, ILogger<AuthController> logger)
    {
        _authService = authService;
        _settings = settings;
        _logger = logger;
    }

    /// <summary>
    /// POST /api/auth/request — Phone requests connection approval.
    /// If on the SAME local network, device is auto-approved instantly.
    /// If on ANOTHER network, QR code scan is strictly required.
    /// </summary>
    [AllowAnonymous]
    [HttpPost("request")]
    public async Task<IActionResult> RequestConnection([FromBody] ConnectionRequest request)
    {
        _logger.LogInformation("Connection request received from: {Name} ({Model})",
            request.DeviceName, request.DeviceModel);

        if (string.IsNullOrEmpty(request.DeviceName))
            return BadRequest(ApiResponse<object>.Fail("Device name required"));

        var remoteIp = HttpContext.Connection.RemoteIpAddress;
        var ipAddress = remoteIp?.ToString() ?? "unknown";
        if (ipAddress.StartsWith("::ffff:"))
            ipAddress = ipAddress.Substring(7);

        // Detect if this request came through the Cloudflare tunnel.
        // cloudflared sets Cf-Connecting-Ip header with the real client IP.
        bool isViaTunnel = HttpContext.Request.Headers.ContainsKey("Cf-Connecting-Ip");
        if (isViaTunnel)
        {
            // Use the real client IP from the tunnel header
            ipAddress = HttpContext.Request.Headers["Cf-Connecting-Ip"].FirstOrDefault() ?? ipAddress;
        }

        bool isSameNetwork = !isViaTunnel && remoteIp != null && NetworkUtils.IsSameLocalSubnet(remoteIp);
        _logger.LogInformation("Connection request: {Name} from {IP} (SameNetwork={SameNet}, ViaTunnel={Tunnel})",
            request.DeviceName, ipAddress, isSameNetwork, isViaTunnel);

        var cleanPin = request.Pin?.Trim().Replace(" ", "").Replace("-", "");
        bool hasPin = !string.IsNullOrWhiteSpace(cleanPin);
        bool hasQr = !string.IsNullOrWhiteSpace(request.QrToken);

        // 1. Same-network Wi-Fi users (Auto-connect without prompt or QR scan)
        if (isSameNetwork && _settings.AutoApproveSameNetwork)
        {
            var autoAuth = _authService.AutoApproveLocalDevice(
                request.DeviceName,
                request.DeviceModel ?? "Unknown",
                ipAddress,
                connectedViaTunnel: false);

            _logger.LogInformation("Auto-approved client '{Name}' on same local Wi-Fi network ({IP})",
                request.DeviceName, ipAddress);
            return Ok(ApiResponse<AuthResponse>.Ok(autoAuth, "Connected automatically on local network"));
        }

        // 2. Validate QR token if provided (remote users scanning the printed QR code)
        if (hasQr)
        {
            if (!string.Equals(request.QrToken?.Trim(), _settings.CurrentQrPairingToken?.Trim(), StringComparison.Ordinal))
            {
                _logger.LogWarning("Connection rejected for device {Name} at {IP}: Invalid QR token",
                    request.DeviceName, ipAddress);
                return StatusCode(403, ApiResponse<object>.Fail(
                    "Invalid QR code. Please scan the official printed QR code."));
            }
        }

        // 3. Validate PIN if provided — entering the active on-screen PIN approves immediately
        if (hasPin)
        {
            if (!string.Equals(cleanPin, _settings.CurrentConnectionPin?.Trim(), StringComparison.OrdinalIgnoreCase))
            {
                _logger.LogWarning("Connection rejected for device {Name} at {IP}: Invalid PIN",
                    request.DeviceName, ipAddress);
                return StatusCode(403, ApiResponse<object>.Fail(
                    "Invalid PIN. Please enter the current 6-digit PIN displayed on the PC screen."));
            }

            var pinAuth = _authService.AutoApproveLocalDevice(
                request.DeviceName,
                request.DeviceModel ?? "Unknown",
                ipAddress,
                connectedViaTunnel: isViaTunnel);

            _logger.LogInformation("Device '{Name}' approved via PIN entry ({IP}, tunnel={Tunnel})",
                request.DeviceName, ipAddress, isViaTunnel);
            return Ok(ApiResponse<AuthResponse>.Ok(pinAuth, "Connected successfully via PIN verification"));
        }

        // 4. If remote user has neither QR nor PIN:
        if (!hasQr && !hasPin && !isSameNetwork && _settings.RequireQrCodeOutsideLocalNetwork)
        {
            _logger.LogWarning("Connection rejected for device {Name} at {IP}: Cross-network without QR/PIN",
                request.DeviceName, ipAddress);
            return StatusCode(403, ApiResponse<object>.Fail(
                "Please scan the printed QR code on the counter to connect to the printer."));
        }

        // 5. Remote users / Scanned QR:
        // REQUIRE APPROVAL FROM SERVER SIDE DASHBOARD AND NOTIFICATION PANEL!
        _logger.LogInformation("Requesting server dashboard approval for remote device {Name} at {IP} (Tunnel={Tunnel})",
            request.DeviceName, ipAddress, isViaTunnel);

        var result = await _authService.RequestApprovalAsync(
            request.DeviceName,
            request.DeviceModel ?? "Unknown",
            ipAddress,
            connectedViaTunnel: isViaTunnel);

        if (result != null)
        {
            _logger.LogInformation("Device approved by PC admin: {Name} (Tunnel={Tunnel})",
                request.DeviceName, isViaTunnel);
            return Ok(ApiResponse<AuthResponse>.Ok(result, "Connected successfully after admin approval"));
        }

        // Denied or timed out
        _logger.LogWarning("Device denied or timed out by PC admin: {Name}", request.DeviceName);
        return StatusCode(403, ApiResponse<object>.Fail("Connection request was denied or timed out on the PC server."));
    }

    /// <summary>
    /// POST /api/auth/pair — Legacy PIN-based pairing (kept for backward compatibility).
    /// Redirects to the approval flow, ignoring the PIN.
    /// </summary>
    [AllowAnonymous]
    [HttpPost("pair")]
    public async Task<IActionResult> PairLegacy([FromBody] PairRequest request)
    {
        if (string.IsNullOrEmpty(request.DeviceName))
            return BadRequest(ApiResponse<object>.Fail("Device name required"));

        var ipAddress = HttpContext.Connection.RemoteIpAddress?.ToString() ?? "unknown";
        if (ipAddress.StartsWith("::ffff:"))
            ipAddress = ipAddress.Substring(7);

        // If PIN matches the server's current rotating PIN, auto-approve immediately
        var cleanPin = request.Pin?.Trim().Replace(" ", "").Replace("-", "");
        if (!string.IsNullOrWhiteSpace(cleanPin) &&
            string.Equals(cleanPin, _settings.CurrentConnectionPin?.Trim(), StringComparison.OrdinalIgnoreCase))
        {
            var pinAuth = _authService.AutoApproveLocalDevice(
                request.DeviceName,
                "Unknown",
                ipAddress);
            return Ok(ApiResponse<AuthResponse>.Ok(pinAuth, "Pairing successful via PIN"));
        }

        var result = await _authService.RequestApprovalAsync(
            request.DeviceName,
            "Unknown",
            ipAddress);

        if (result != null)
            return Ok(ApiResponse<AuthResponse>.Ok(result, "Pairing successful"));

        return Unauthorized(ApiResponse<object>.Fail("Connection denied or timed out"));
    }

    /// <summary>
    /// GET /api/auth/status — Check if the current token is valid and device not blocked.
    /// </summary>
    [Authorize]
    [HttpGet("status")]
    public IActionResult Status()
    {
        var deviceId = User.FindFirst("deviceId")?.Value;
        var deviceName = User.FindFirst("deviceName")?.Value;

        // Check if device is blocked
        if (deviceId != null && _authService.IsDeviceBlocked(deviceId))
        {
            return StatusCode(403, ApiResponse<object>.Fail("Device is blocked by the server administrator"));
        }

        return Ok(ApiResponse<object>.Ok(new
        {
            Authenticated = true,
            DeviceId = deviceId,
            DeviceName = deviceName
        }));
    }

    /// <summary>
    /// GET /api/auth/devices — List all paired devices.
    /// </summary>
    [LocalOnly]
    [HttpGet("devices")]
    public IActionResult GetDevices()
    {
        return Ok(ApiResponse<List<DeviceInfo>>.Ok(_authService.GetPairedDevices()));
    }

    /// <summary>
    /// POST /api/auth/devices/{id}/block — Block a paired device.
    /// </summary>
    [LocalOnly]
    [HttpPost("devices/{id}/block")]
    public IActionResult BlockDevice(string id)
    {
        if (_authService.BlockDevice(id))
            return Ok(ApiResponse<object>.Ok(new object(), "Device blocked"));
        return NotFound(ApiResponse<object>.Fail("Device not found"));
    }

    /// <summary>
    /// POST /api/auth/devices/{id}/unblock — Unblock a paired device.
    /// </summary>
    [LocalOnly]
    [HttpPost("devices/{id}/unblock")]
    public IActionResult UnblockDevice(string id)
    {
        if (_authService.UnblockDevice(id))
            return Ok(ApiResponse<object>.Ok(new object(), "Device unblocked"));
        return NotFound(ApiResponse<object>.Fail("Device not found"));
    }

    /// <summary>
    /// DELETE /api/auth/devices/{id} — Remove a paired device.
    /// </summary>
    [LocalOnly]
    [HttpDelete("devices/{id}")]
    public IActionResult RemoveDevice(string id)
    {
        if (_authService.RemoveDevice(id))
            return Ok(ApiResponse<object>.Ok(new object(), "Device removed"));
        return NotFound(ApiResponse<object>.Fail("Device not found"));
    }
}

/// <summary>
/// Connection request from the Android phone.
/// </summary>
public class ConnectionRequest
{
    public string DeviceName { get; set; } = string.Empty;
    public string? DeviceModel { get; set; }
    public string? QrToken { get; set; }
    public string? Pin { get; set; }
    public bool IsSameNetwork { get; set; } = true;
}

/// <summary>
/// Health check endpoint — no auth required.
/// Used by Android for auto-connect and network reachability verification.
/// </summary>
[ApiController]
[Route("api/[controller]")]
public class StatusController : ControllerBase
{
    private readonly PrinterService _printerService;
    private readonly AppSettings _settings;

    public StatusController(PrinterService printerService, AppSettings settings)
    {
        _printerService = printerService;
        _settings = settings;
    }

    [AllowAnonymous]
    [HttpGet]
    public IActionResult Get()
    {
        var remoteIp = HttpContext.Connection.RemoteIpAddress;
        bool isSameSubnet = remoteIp != null && NetworkUtils.IsSameLocalSubnet(remoteIp);
        var printerCount = _printerService.GetAllPrinters().Count;

        var response = new ServerStatusResponse
        {
            Status = "Online",
            ServerName = _settings.ServerName,
            Version = "2.0.0",
            Timestamp = DateTime.UtcNow,
            RequiresPairing = true,
            PrinterAvailable = printerCount > 0,
            PrinterCount = printerCount,
            Readiness = printerCount > 0 ? "Ready" : "Degraded",
            IsSameNetwork = isSameSubnet,
            RequiresQrCode = !isSameSubnet && _settings.RequireQrCodeOutsideLocalNetwork,
            RequiresPin = !isSameSubnet && _settings.RequireQrCodeOutsideLocalNetwork
        };

        return Ok(response);
    }
}
