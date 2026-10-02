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

    // ═══════════════════════════════════════════════════════════════
    //  Session Tracking (anti-spam / time allotment)
    // ═══════════════════════════════════════════════════════════════

    /// <summary>When the current session started (set on pairing/reconnect).</summary>
    public DateTime SessionStartedAt { get; set; } = DateTime.UtcNow;

    /// <summary>True if this device connected via the Cloudflare tunnel (remote). Rate limits only apply to these devices.</summary>
    public bool ConnectedViaTunnel { get; set; } = false;

    /// <summary>Number of print jobs submitted in the current session.</summary>
    public int SessionPrintCount { get; set; } = 0;

    /// <summary>Timestamp of the last print job from this device.</summary>
    public DateTime? LastPrintAt { get; set; }

    /// <summary>Checks if the device session has expired based on the configured duration.</summary>
    public bool IsSessionExpired(int sessionDurationMinutes)
    {
        if (sessionDurationMinutes <= 0) return false; // 0 = unlimited
        return (DateTime.UtcNow - SessionStartedAt).TotalMinutes > sessionDurationMinutes;
    }

    /// <summary>Checks if the device has exceeded the max prints per session.</summary>
    public bool HasExceededPrintLimit(int maxPrintsPerSession)
    {
        if (maxPrintsPerSession <= 0) return false; // 0 = unlimited
        return SessionPrintCount >= maxPrintsPerSession;
    }

    /// <summary>Checks if the device is in cooldown between prints.</summary>
    public bool IsInCooldown(int cooldownSeconds)
    {
        if (cooldownSeconds <= 0 || LastPrintAt == null) return false;
        return (DateTime.UtcNow - LastPrintAt.Value).TotalSeconds < cooldownSeconds;
    }

    /// <summary>Records a print job for rate limiting.</summary>
    public void RecordPrint()
    {
        SessionPrintCount++;
        LastPrintAt = DateTime.UtcNow;
    }

    /// <summary>Resets the session (e.g. when re-pairing after expiry).</summary>
    public void ResetSession()
    {
        SessionStartedAt = DateTime.UtcNow;
        SessionPrintCount = 0;
        LastPrintAt = null;
    }
}
