using System.Collections.Concurrent;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using System.Text.Json;
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
    private readonly ConcurrentDictionary<string, DeviceInfo> _pairedDevices = new();
    private readonly ConcurrentDictionary<string, PendingApproval> _pendingApprovals = new();

    private static readonly string DevicesFilePath = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
        "WifiPrintServer", "paired_devices.json");

    /// <summary>Fired when a new device requests connection — UI shows approval dialog.</summary>
    public event Action<PendingApproval>? OnApprovalRequested;

    /// <summary>Fired when a device is approved and paired.</summary>
    public event Action<DeviceInfo>? OnDevicePaired;

    public AuthService(AppSettings settings, ILogger<AuthService> logger)
    {
        _settings = settings;
        _logger = logger;
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
            _logger.LogWarning("⚠️ No approval handler registered! UI may not be showing the approval popup. Auto-approving...");
            // If no UI is connected, auto-approve to prevent hanging
            approval.CompletionSource.TrySetResult(true);
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
                    IpAddress = ipAddress
                };

                var token = GenerateJwtToken(device.Id, device.Name);
                device.Token = token;
                _pairedDevices[device.Id] = device;
                SavePairedDevices();

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
                _logger.LogInformation("Device denied: {Name}", deviceName);
                return null;
            }
        }
        catch (OperationCanceledException)
        {
            _pendingApprovals.TryRemove(approval.Id, out _);
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
            _logger.LogInformation("Approval granted for {Name}", approval.DeviceName);
        }
    }

    /// <summary>PC user clicked "Deny" — resolves the waiting request.</summary>
    public void DenyDevice(string approvalId)
    {
        if (_pendingApprovals.TryGetValue(approvalId, out var approval))
        {
            approval.CompletionSource.TrySetResult(false);
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

            return principal.FindFirst("deviceId")?.Value;
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
        if (result) SavePairedDevices();
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

    private void LoadPairedDevices()
    {
        try
        {
            if (File.Exists(DevicesFilePath))
            {
                var json = File.ReadAllText(DevicesFilePath);
                var devices = JsonSerializer.Deserialize<List<DeviceInfo>>(json,
                    new JsonSerializerOptions { PropertyNameCaseInsensitive = true });
                if (devices != null)
                {
                    foreach (var d in devices)
                        _pairedDevices[d.Id] = d;
                    _logger.LogInformation("Loaded {Count} paired devices from disk", devices.Count);
                }
            }
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
            Directory.CreateDirectory(Path.GetDirectoryName(DevicesFilePath)!);
            var json = JsonSerializer.Serialize(_pairedDevices.Values.ToList(),
                new JsonSerializerOptions { WriteIndented = true });
            File.WriteAllText(DevicesFilePath, json);
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
