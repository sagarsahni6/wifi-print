namespace WifiPrintServer.Models;

/// <summary>
/// Represents a paired Android device.
/// </summary>
public class DeviceInfo
{
    public string Id { get; set; } = Guid.NewGuid().ToString("N")[..8].ToUpper();
    public string Name { get; set; } = string.Empty;
    public string? Model { get; set; }
    public string IpAddress { get; set; } = string.Empty;
    public string Token { get; set; } = string.Empty;
    public DateTime PairedAt { get; set; } = DateTime.UtcNow;
    public DateTime LastSeenAt { get; set; } = DateTime.UtcNow;
    public bool IsActive { get; set; } = true;

    /// <summary>
    /// When blocked, the device's JWT token is rejected by the server.
    /// The device cannot send print jobs until unblocked.
    /// </summary>
    public bool IsBlocked { get; set; } = false;
}
