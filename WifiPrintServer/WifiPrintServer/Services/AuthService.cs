using System.Collections.Concurrent;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.IdentityModel.Tokens;
using WifiPrintServer.Models;

namespace WifiPrintServer.Services;

/// <summary>
/// Handles device authentication via a "Device Approval" flow.
/// When a phone requests connection, the PC user sees a popup and clicks Allow/Deny.
/// Paired devices and their tokens are persisted to disk.
/// </summary>
public class AuthService
{
    private readonly AppSettings _settings;
    private readonly ILogger<AuthService> _logger;
    private readonly ServerStateStore _stateStore;
    private readonly ConcurrentDictionary<string, DeviceInfo> _pairedDevices = new();
    private readonly ConcurrentDictionary<string, PendingApproval> _pendingApprovals = new();

    /// <summary>Fired when a new device requests connection — UI shows approval dialog.</summary>
    public event Action<PendingApproval>? OnApprovalRequested;

    /// <summary>Fired when a device is approved and paired.</summary>
    public event Action<DeviceInfo>? OnDevicePaired;

    public AuthService(AppSettings settings, ILogger<AuthService> logger, ServerStateStore stateStore)
    {
        _settings = settings;
        _logger = logger;
        _stateStore = stateStore;
        LoadPairedDevices();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Device Approval Flow
    // ═══════════════════════════════════════════════════════════════

    /// <summary>
    /// Called when a phone sends a connection request.
    /// Creates a pending approval and waits (up to 60s) for the PC user to Allow/Deny.
    /// Returns AuthResponse on approval, null on denial/timeout.
    /// </summary>
    public async Task<AuthResponse?> RequestApprovalAsync(string deviceName, string deviceModel, string ipAddress)
    {
        var approval = new PendingApproval
        {
            DeviceName = deviceName,
            DeviceModel = deviceModel,
            IpAddress = ipAddress
        };

        _pendingApprovals[approval.Id] = approval;
        _stateStore.UpsertApproval(approval, "Pending");
        _logger.LogInformation("Connection request from {Name} ({Model}) at {Ip}",
            deviceName, deviceModel, ipAddress);

        // Notify the WPF UI to show the approval popup
        if (OnApprovalRequested != null)
        {
            _logger.LogInformation("Firing approval event to {Count} handler(s)", OnApprovalRequested.GetInvocationList().Length);
            OnApprovalRequested.Invoke(approval);
        }
        else
        {
            _logger.LogWarning("No approval handler registered; request will time out unless approved locally");
        }

        try
        {
            // Wait up to 60 seconds for the user to respond
            using var cts = new CancellationTokenSource(TimeSpan.FromSeconds(60));
            var result = await approval.CompletionSource.Task.WaitAsync(cts.Token);

            _pendingApprovals.TryRemove(approval.Id, out _);

            if (result)
            {
                // Approved — create device and issue token
                var device = new DeviceInfo
                {
                    Name = deviceName,
                    Model = deviceModel,
                    IpAddress = ipAddress
                };

                var token = GenerateJwtToken(device.Id, device.Name);
                device.Token = token;
                _pairedDevices[device.Id] = device;
                SavePairedDevices();
                _stateStore.UpsertApproval(approval, "Approved");

                _logger.LogInformation("Device approved: {Name} ({Id})", device.Name, device.Id);
                OnDevicePaired?.Invoke(device);

                return new AuthResponse
                {
                    Token = token,
                    DeviceId = device.Id,
                    ServerName = _settings.ServerName,
                    ExpiresAt = DateTime.UtcNow.AddDays(_settings.JwtExpirationDays)
                };
            }
            else
            {
                _stateStore.UpsertApproval(approval, "Denied");
                _logger.LogInformation("Device denied: {Name}", deviceName);
                return null;
            }
        }
        catch (OperationCanceledException)
        {
            _pendingApprovals.TryRemove(approval.Id, out _);
            _stateStore.UpsertApproval(approval, "TimedOut");
            _logger.LogInformation("Connection request timed out for {Name}", deviceName);
            return null;
        }
    }

    /// <summary>PC user clicked "Allow" — resolves the waiting request.</summary>
    public void ApproveDevice(string approvalId)
    {
        if (_pendingApprovals.TryGetValue(approvalId, out var approval))
        {
            approval.CompletionSource.TrySetResult(true);
            _stateStore.UpsertApproval(approval, "Approved");
            _logger.LogInformation("Approval granted for {Name}", approval.DeviceName);
        }
    }

    /// <summary>PC user clicked "Deny" — resolves the waiting request.</summary>
    public void DenyDevice(string approvalId)
    {
        if (_pendingApprovals.TryGetValue(approvalId, out var approval))
        {
            approval.CompletionSource.TrySetResult(false);
            _stateStore.UpsertApproval(approval, "Denied");
            _logger.LogInformation("Approval denied for {Name}", approval.DeviceName);
        }
    }

    public List<PendingApproval> GetPendingRequests() => _pendingApprovals.Values.ToList();

    // ═══════════════════════════════════════════════════════════════
    //  JWT Token Management
    // ═══════════════════════════════════════════════════════════════

    private string GenerateJwtToken(string deviceId, string deviceName)
    {
        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_settings.JwtSecret));
        var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

        var claims = new[]
        {
            new Claim("deviceId", deviceId),
            new Claim("deviceName", deviceName),
            new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString())
        };

        var token = new JwtSecurityToken(
            issuer: "WifiPrintServer",
            audience: "WifiPrintClient",
            claims: claims,
            expires: DateTime.UtcNow.AddDays(_settings.JwtExpirationDays),
            signingCredentials: creds);

        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    public string? ValidateToken(string token)
    {
        try
        {
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_settings.JwtSecret));
            var handler = new JwtSecurityTokenHandler();
            var principal = handler.ValidateToken(token, new TokenValidationParameters
            {
                ValidateIssuer = true,
                ValidIssuer = "WifiPrintServer",
                ValidateAudience = true,
                ValidAudience = "WifiPrintClient",
                ValidateLifetime = true,
                IssuerSigningKey = key
            }, out _);

            var deviceId = principal.FindFirst("deviceId")?.Value;

            // Ensure the device is still paired and not blocked/removed
            if (deviceId != null)
            {
                if (!_pairedDevices.ContainsKey(deviceId))
                {
                    _logger.LogWarning("Token valid but device {Id} was removed — rejecting", deviceId);
                    return null;
                }
                if (IsDeviceBlocked(deviceId))
                {
                    _logger.LogWarning("Token valid but device {Id} is blocked — rejecting", deviceId);
                    return null;
                }
            }

            return deviceId;
        }
        catch { return null; }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Paired Devices Persistence
    // ═══════════════════════════════════════════════════════════════

    public List<DeviceInfo> GetPairedDevices() => _pairedDevices.Values.ToList();

    public DeviceInfo? GetDeviceById(string deviceId)
    {
        _pairedDevices.TryGetValue(deviceId, out var device);
        return device;
    }

    public bool RemoveDevice(string deviceId)
    {
        var result = _pairedDevices.TryRemove(deviceId, out _);
        if (result)
        {
            _stateStore.DeleteDevice(deviceId);
            SavePairedDevices();
        }
        return result;
    }

    /// <summary>Block a device — its JWT token will be rejected.</summary>
    public bool BlockDevice(string deviceId)
    {
        if (_pairedDevices.TryGetValue(deviceId, out var device))
        {
            device.IsBlocked = true;
            SavePairedDevices();
            _logger.LogInformation("Device blocked: {Name} ({Id})", device.Name, deviceId);
            return true;
        }
        return false;
    }

    /// <summary>Unblock a device — restore access.</summary>
    public bool UnblockDevice(string deviceId)
    {
        if (_pairedDevices.TryGetValue(deviceId, out var device))
        {
            device.IsBlocked = false;
            SavePairedDevices();
            _logger.LogInformation("Device unblocked: {Name} ({Id})", device.Name, deviceId);
            return true;
        }
        return false;
    }

    /// <summary>Check if a device is blocked.</summary>
    public bool IsDeviceBlocked(string deviceId)
    {
        return _pairedDevices.TryGetValue(deviceId, out var device) && device.IsBlocked;
    }

    public void MarkDeviceSeen(string deviceId)
    {
        if (!_pairedDevices.TryGetValue(deviceId, out var device))
            return;

        device.LastSeenAt = DateTime.UtcNow;
        SavePairedDevices();
    }

    private void LoadPairedDevices()
    {
        try
        {
            var devices = _stateStore.LoadDevices();
            foreach (var device in devices)
                _pairedDevices[device.Id] = device;

            _logger.LogInformation("Loaded {Count} paired devices from persistent store", devices.Count);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Failed to load paired devices");
        }
    }

    private void SavePairedDevices()
    {
        try
        {
            foreach (var device in _pairedDevices.Values)
                _stateStore.UpsertDevice(device);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Failed to save paired devices");
        }
    }
}

/// <summary>
/// Represents a pending connection request waiting for PC user approval.
/// </summary>
public class PendingApproval
{
    public string Id { get; set; } = Guid.NewGuid().ToString("N")[..8];
    public string DeviceName { get; set; } = string.Empty;
    public string DeviceModel { get; set; } = string.Empty;
    public string IpAddress { get; set; } = string.Empty;
    public DateTime RequestedAt { get; set; } = DateTime.UtcNow;
    public TaskCompletionSource<bool> CompletionSource { get; } = new();
}
