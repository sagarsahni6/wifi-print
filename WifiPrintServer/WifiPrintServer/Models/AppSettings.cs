using System.Text.Json;
using System.Security.Cryptography;

namespace WifiPrintServer.Models;

/// <summary>
/// Application configuration — persisted to a JSON file so settings
/// (especially JwtSecret) survive server restarts.
/// </summary>
public class AppSettings
{
    public static readonly string AppDataDir = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
        "Printora");

    public static readonly string SettingsDir = AppDataDir;

    public static readonly string SettingsFilePath = Path.Combine(SettingsDir, "settings.json");

    public int ServerPort { get; set; } = 5000;
    public string ServerName { get; set; } = Environment.MachineName;

    /// <summary>
    /// Permanent unique identifier for this Host PC (e.g. DESKTOP-PLK2AIK-W-58291).
    /// Generated once at installation / first launch and never changes.
    /// Forms the dedicated URL: https://print.yourshop.com/{HostIdentifier}
    /// </summary>
    public string HostIdentifier { get; set; } = GenerateHostIdentifier();
    public string JwtSecret { get; set; } = GenerateSecureToken(48);
    public string CertificatePassword { get; set; } = GenerateSecureToken(24);
    public int JwtExpirationDays { get; set; } = 365;
    public string? DefaultPrinter { get; set; }
    public bool AutoStart { get; set; } = false;
    public bool MinimizeToTray { get; set; } = true;
    public string UploadDirectory { get; set; } = Path.Combine(AppDataDir, "Uploads");
    public string LogDirectory { get; set; } = Path.Combine(AppDataDir, "Logs");
    public int MaxFileSizeMB { get; set; } = 100;
    public bool RequireAuth { get; set; } = true;

    /// <summary>
    /// If true, uploaded documents and converted temporary files are automatically deleted
    /// from the Host PC immediately after printing finishes successfully or is cancelled.
    /// Protects privacy and prevents local disk storage buildup.
    /// </summary>
    public bool AutoCleanupAfterPrint { get; set; } = true;

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
    public bool EnableCloudRelay { get; set; } = true;

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
    /// If true, users accessing via Cloudflare Tunnel or public internet must enter the 6-digit PIN.
    /// Local Wi-Fi users print freely without PIN.
    /// </summary>
    public bool RequirePinForWebTunnel { get; set; } = true;

    /// <summary>
    /// Maximum allowed copies per web print submission to prevent paper exhaustion.
    /// </summary>
    public int MaxWebPrintCopies { get; set; } = 3;

    /// <summary>
    /// Maximum allowed pages in a PDF document per web print submission.
    /// </summary>
    public int MaxWebPrintPages { get; set; } = 30;

    /// <summary>
    /// Max print submissions allowed per minute per IP address.
    /// </summary>
    public int WebPrintRateLimitPerMinute { get; set; } = 2;

    /// <summary>
    /// Max print submissions allowed per hour per IP address.
    /// </summary>
    public int WebPrintRateLimitPerHour { get; set; } = 15;

    /// <summary>
    /// Generates a permanent unique identifier combining the machine name with a random 5-digit number.
    /// e.g. DESKTOP-PLK2AIK-W-48291
    /// </summary>
    public static string GenerateHostIdentifier()
    {
        var cleanMachineName = new string(Environment.MachineName
            .Where(c => char.IsLetterOrDigit(c) || c == '-' || c == '_')
            .ToArray());
        if (string.IsNullOrWhiteSpace(cleanMachineName))
            cleanMachineName = "HOST-PC";

        var bytes = new byte[4];
        using var rng = RandomNumberGenerator.Create();
        rng.GetBytes(bytes);
        var num = Math.Abs(BitConverter.ToInt32(bytes, 0)) % 90000 + 10000;
        return $"{cleanMachineName}-{num}";
    }

    /// <summary>
    /// Gets the unique public Web Print URL for this Host PC.
    /// Format: https://{domain}/{HostIdentifier} or https://{tunnel}/{HostIdentifier}
    /// e.g. https://print.yourshop.com/DESKTOP-PLK2AIK-W-58291
    /// </summary>
    public string GetWebPrintUrl(string? tunnelUrl = null, string? localIp = null)
    {
        string baseUrl;
        if (!string.IsNullOrWhiteSpace(CustomPublicUrl))
        {
            baseUrl = CustomPublicUrl.Trim().TrimEnd('/');
        }
        else if (!string.IsNullOrWhiteSpace(tunnelUrl))
        {
            baseUrl = tunnelUrl.Trim().TrimEnd('/');
        }
        else
        {
            var ip = !string.IsNullOrWhiteSpace(localIp) ? localIp : "127.0.0.1";
            baseUrl = $"https://{ip}:{ServerPort}";
        }

        return $"{baseUrl}/{HostIdentifier}";
    }

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

        if (!File.Exists(SettingsFilePath))
        {
            // Check legacy folders to migrate existing setup seamlessly
            var legacyPaths = new[]
            {
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "SpoolDrop", "settings.json"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "WifiPrintServer", "settings.json")
            };

            foreach (var legacyPath in legacyPaths)
            {
                if (File.Exists(legacyPath))
                {
                    try
                    {
                        File.Copy(legacyPath, SettingsFilePath, true);
                        break;
                    }
                    catch { }
                }
            }
        }

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
                    settings.Save();
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

        if (string.IsNullOrWhiteSpace(HostIdentifier))
            HostIdentifier = GenerateHostIdentifier();
    }

    public static string GenerateSecureToken(int numBytes)
    {
        var bytes = RandomNumberGenerator.GetBytes(numBytes);
        return Convert.ToBase64String(bytes);
    }
}
