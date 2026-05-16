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
    private readonly ILogger<AuthController> _logger;

    public AuthController(AuthService authService, ILogger<AuthController> logger)
    {
        _authService = authService;
        _logger = logger;
    }

    /// <summary>
    /// POST /api/auth/request — Phone requests connection approval.
    /// This call blocks (up to 60s) until the PC user clicks Allow or Deny.
    /// No authentication required for this endpoint.
    /// </summary>
    [AllowAnonymous]
    [HttpPost("request")]
    public async Task<IActionResult> RequestConnection([FromBody] ConnectionRequest request)
    {
        _logger.LogInformation("Connection request received from: {Name} ({Model})",
            request.DeviceName, request.DeviceModel);

        if (string.IsNullOrEmpty(request.DeviceName))
            return BadRequest(ApiResponse<object>.Fail("Device name required"));

        var ipAddress = HttpContext.Connection.RemoteIpAddress?.ToString() ?? "unknown";
        // Clean IPv6-mapped IPv4 addresses (e.g., "::ffff:192.168.1.5" → "192.168.1.5")
        if (ipAddress.StartsWith("::ffff:"))
            ipAddress = ipAddress.Substring(7);

        _logger.LogInformation("Requesting approval for {Name} from {IP}", request.DeviceName, ipAddress);

        var result = await _authService.RequestApprovalAsync(
            request.DeviceName,
            request.DeviceModel ?? "Unknown",
            ipAddress);

        if (result != null)
        {
            _logger.LogInformation("Device approved and paired: {Name}", request.DeviceName);
            return Ok(ApiResponse<AuthResponse>.Ok(result, "Connection approved"));
        }

        // Denied or timed out
        _logger.LogWarning("Device denied or timed out: {Name}", request.DeviceName);
        return StatusCode(403, ApiResponse<object>.Fail("Connection denied or timed out. Please try again."));
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
}

/// <summary>
/// Health check endpoint — no auth required.
/// Used by Android for auto-connect verification.
/// </summary>
[ApiController]
[Route("api/[controller]")]
public class StatusController : ControllerBase
{
    private readonly PrinterService _printerService;

    public StatusController(PrinterService printerService)
    {
        _printerService = printerService;
    }

    [AllowAnonymous]
    [HttpGet]
    public IActionResult Get()
    {
        var printerCount = _printerService.GetAllPrinters().Count;
        var response = new ServerStatusResponse
        {
            Status = "Online",
            ServerName = Environment.MachineName,
            Version = "1.0.0",
            Timestamp = DateTime.UtcNow,
            RequiresPairing = true,
            PrinterAvailable = printerCount > 0,
            PrinterCount = printerCount,
            Readiness = printerCount > 0 ? "Ready" : "Degraded"
        };

        return Ok(response);
    }
}
