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
    /// Asynchronously loads settings from disk, or creates a new file with defaults.
    /// </summary>
    public static async Task<AppSettings> LoadOrCreateAsync()
    {
        Directory.CreateDirectory(SettingsDir);

        if (File.Exists(SettingsFilePath))
        {
            try
            {
                using var stream = File.OpenRead(SettingsFilePath);
                var settings = await JsonSerializer.DeserializeAsync<AppSettings>(stream,
                    new JsonSerializerOptions { PropertyNameCaseInsensitive = true });
                if (settings != null)
                    return settings;
            }
            catch { /* fall through to create new */ }
        }

        var newSettings = new AppSettings();
        await newSettings.SaveAsync();
        return newSettings;
    }


    /// <summary>
    /// Asynchronously persists current settings to disk.
    /// </summary>
    public async Task SaveAsync()
    {
        Directory.CreateDirectory(SettingsDir);
        using var stream = File.Create(SettingsFilePath);
        await JsonSerializer.SerializeAsync(stream, this, new JsonSerializerOptions
        {
            WriteIndented = true
        });
    }
}
