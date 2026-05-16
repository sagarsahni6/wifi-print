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
    }

    private static string GenerateSecureToken(int numBytes)
    {
        var bytes = RandomNumberGenerator.GetBytes(numBytes);
        return Convert.ToBase64String(bytes);
    }
}
