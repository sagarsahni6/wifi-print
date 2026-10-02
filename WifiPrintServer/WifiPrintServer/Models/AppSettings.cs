using System.Text.Json;
using System.Security.Cryptography;

namespace WifiPrintServer.Models;

/// <summary>
/// Application configuration — persisted to a JSON file so settings
/// (especially JwtSecret) survive server restarts.
/// </summary>
public class AppSettings
{
    private static readonly string SettingsDir = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
        "WifiPrintServer");

    private static readonly string SettingsFilePath = Path.Combine(SettingsDir, "settings.json");

    public int ServerPort { get; set; } = 5000;
    public string ServerName { get; set; } = Environment.MachineName;
    public string JwtSecret { get; set; } = GenerateSecureToken(48);
    public string CertificatePassword { get; set; } = GenerateSecureToken(24);
    public int JwtExpirationDays { get; set; } = 365;
    public string? DefaultPrinter { get; set; }
    public bool AutoStart { get; set; } = false;
    public bool MinimizeToTray { get; set; } = true;
    public string UploadDirectory { get; set; } = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
        "WifiPrintServer", "Uploads");
    public string LogDirectory { get; set; } = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
        "WifiPrintServer", "Logs");
    public int MaxFileSizeMB { get; set; } = 100;
    public bool RequireAuth { get; set; } = true;

    /// <summary>
    /// If true, devices connecting from the same local Wi-Fi / subnet are automatically approved without prompt.
    /// </summary>
    public bool AutoApproveSameNetwork { get; set; } = true;

    /// <summary>
    /// If true, devices connecting from outside the local network (routed/different subnet) MUST scan the QR code to pair.
    /// </summary>
    public bool RequireQrCodeOutsideLocalNetwork { get; set; } = true;

    /// <summary>
    /// Current cryptographic pairing token embedded in the server QR code.
    /// This is PERMANENT and does NOT change — it is part of the fixed QR code.
    /// </summary>
    public string CurrentQrPairingToken { get; set; } = GenerateSecureToken(16);

    /// <summary>
    /// 6-digit numeric PIN for cross-network connections.
    /// This REFRESHES periodically or on-demand and is shown on the dashboard.
    /// </summary>
    public string CurrentConnectionPin { get; set; } = GeneratePin();

    /// <summary>
    /// When true, starts a Cloudflare Quick Tunnel so phones can print from any network worldwide.
    /// No Cloudflare account needed — uses free ephemeral tunnels.
    /// </summary>
    public bool EnableCloudRelay { get; set; } = false;

    /// <summary>
    /// Optional permanent public URL, DDNS, or Cloudflare Tunnel domain (e.g. https://print.yourdomain.com).
    /// If set, this fixed URL is embedded in the permanent QR code so paper-printed QR codes work forever.
    /// </summary>
    public string? CustomPublicUrl { get; set; }

    /// <summary>
    /// How long (in minutes) a device session lasts after pairing.
    /// After this time, the device must re-pair. Prevents indefinite remote access.
    /// 0 = unlimited (no expiry).
    /// </summary>
    public int SessionDurationMinutes { get; set; } = 60;

    /// <summary>
    /// Maximum number of print jobs a single device can submit per session.
    /// 0 = unlimited.
    /// </summary>
    public int MaxPrintsPerSession { get; set; } = 10;

    /// <summary>
    /// Minimum seconds between print jobs from the same device.
    /// Prevents rapid-fire spam printing. 0 = no cooldown.
    /// </summary>
    public int PrintCooldownSeconds { get; set; } = 30;

    /// <summary>
    /// Generates a random 6-digit numeric PIN.
    /// </summary>
    public static string GeneratePin()
    {
        var rng = System.Security.Cryptography.RandomNumberGenerator.Create();
        var bytes = new byte[4];
        rng.GetBytes(bytes);
        var num = Math.Abs(BitConverter.ToInt32(bytes, 0)) % 1000000;
        return num.ToString("D6");
    }

    /// <summary>
    /// Loads settings from disk, or creates a new file with defaults.
    /// The JwtSecret is generated once and persisted — never changes across restarts.
    /// </summary>
    public static AppSettings LoadOrCreate()
    {
        Directory.CreateDirectory(SettingsDir);

        if (File.Exists(SettingsFilePath))
        {
            try
            {
                var json = File.ReadAllText(SettingsFilePath);
                var settings = JsonSerializer.Deserialize<AppSettings>(json,
                    new JsonSerializerOptions { PropertyNameCaseInsensitive = true });
                if (settings != null)
                {
                    settings.NormalizeSecrets();
                    return settings;
                }
            }
            catch { /* fall through to create new */ }
        }

        // First launch — create settings with a stable JWT secret
        var newSettings = new AppSettings();
        newSettings.NormalizeSecrets();
        newSettings.Save();
        return newSettings;
    }

    /// <summary>
    /// Persists current settings to disk.
    /// </summary>
    public void Save()
    {
        Directory.CreateDirectory(SettingsDir);
        NormalizeSecrets();
        var json = JsonSerializer.Serialize(this, new JsonSerializerOptions
        {
            WriteIndented = true
        });
        File.WriteAllText(SettingsFilePath, json);
    }

    private void NormalizeSecrets()
    {
        if (string.IsNullOrWhiteSpace(JwtSecret) || JwtSecret.Length < 32)
            JwtSecret = GenerateSecureToken(48);

        if (string.IsNullOrWhiteSpace(CertificatePassword) || CertificatePassword.Length < 16)
            CertificatePassword = GenerateSecureToken(24);

        if (string.IsNullOrWhiteSpace(CurrentQrPairingToken))
            CurrentQrPairingToken = GenerateSecureToken(16);

        if (string.IsNullOrWhiteSpace(CurrentConnectionPin) || CurrentConnectionPin.Length != 6)
            CurrentConnectionPin = GeneratePin();
    }

    public static string GenerateSecureToken(int numBytes)
    {
        var bytes = RandomNumberGenerator.GetBytes(numBytes);
        return Convert.ToBase64String(bytes);
    }
}
