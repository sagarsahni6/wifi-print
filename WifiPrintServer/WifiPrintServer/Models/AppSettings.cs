using System.Security.Cryptography;
using System.Text.Json;

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
    public string JwtSecret { get; set; } = Convert.ToBase64String(RandomNumberGenerator.GetBytes(32));
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
                    return settings;
            }
            catch { /* fall through to create new */ }
        }

        // First launch — create settings with a stable JWT secret
        var newSettings = new AppSettings();
        newSettings.Save();
        return newSettings;
    }

    /// <summary>
    /// Persists current settings to disk.
    /// </summary>
    public void Save()
    {
        Directory.CreateDirectory(SettingsDir);
        var json = JsonSerializer.Serialize(this, new JsonSerializerOptions
        {
            WriteIndented = true
        });
        File.WriteAllText(SettingsFilePath, json);
    }
}
