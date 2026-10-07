using System.Diagnostics;
using System.IO;
using System.Net.Http;
using System.Text.RegularExpressions;

namespace WifiPrintServer.Services;

/// <summary>
/// Manages a Cloudflare Quick Tunnel (cloudflared) subprocess to expose
/// the local print server to the internet via a public *.trycloudflare.com URL.
/// No Cloudflare account is needed — it uses free ephemeral tunnels.
/// </summary>
public class TunnelService : IDisposable
{
    private readonly ILogger<TunnelService> _logger;
    private Process? _tunnelProcess;
    private string? _publicUrl;
    private bool _isStarting;
    private readonly object _lock = new();

    private static readonly string CloudflaredDir = Path.Combine(
        AppSettings.AppDataDir, "cloudflared");

    private static readonly string CloudflaredExePath = Path.Combine(CloudflaredDir, "cloudflared.exe");

    /// <summary>
    /// The public tunnel URL (e.g. https://xxx.trycloudflare.com) or null if not running.
    /// </summary>
    public string? PublicUrl
    {
        get { lock (_lock) return _publicUrl; }
        private set { lock (_lock) _publicUrl = value; }
    }

    /// <summary>True when the tunnel is running and has a public URL.</summary>
    public bool IsActive => !string.IsNullOrEmpty(PublicUrl) && _tunnelProcess is { HasExited: false };

    /// <summary>True when the tunnel is in the process of starting.</summary>
    public bool IsStarting
    {
        get { lock (_lock) return _isStarting; }
        private set { lock (_lock) _isStarting = value; }
    }

    /// <summary>Fires when the tunnel URL becomes available or changes.</summary>
    public event Action<string>? OnTunnelReady;

    /// <summary>Fires when the tunnel stops or encounters an error.</summary>
    public event Action<string>? OnTunnelStopped;

    /// <summary>Fires log messages for UI or console diagnostics.</summary>
    public event Action<string>? OnLog;

    public TunnelService(ILogger<TunnelService> logger)
    {
        _logger = logger;
    }

    /// <summary>
    /// Ensures cloudflared.exe is downloaded, then starts the tunnel.
    /// This method is idempotent — calling it when already running is a no-op.
    /// </summary>
    public async Task StartAsync(int localPort)
    {
        if (IsActive || IsStarting) return;
        IsStarting = true;

        try
        {
            OnLog?.Invoke("⏳ Starting Printora Cloud Relay (Cloudflare)...");

            // 1. Ensure cloudflared.exe exists
            if (!File.Exists(CloudflaredExePath))
            {
                var legacyExes = new[]
                {
                    Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "SpoolDrop", "cloudflared", "cloudflared.exe"),
                    Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "WifiPrintServer", "cloudflared", "cloudflared.exe")
                };
                foreach (var legacy in legacyExes)
                {
                    if (File.Exists(legacy))
                    {
                        try
                        {
                            Directory.CreateDirectory(CloudflaredDir);
                            File.Copy(legacy, CloudflaredExePath, true);
                            break;
                        }
                        catch { }
                    }
                }
            }

            if (!File.Exists(CloudflaredExePath))
            {
                _logger.LogInformation("Downloading cloudflared.exe...");
                OnLog?.Invoke("⬇ Downloading cloudflared binary for Printora Cloud Relay...");
                await DownloadCloudflaredAsync();
                OnLog?.Invoke("✓ Printora Cloud Relay binary downloaded successfully.");
            }

            // 2. Start the tunnel process
            var configPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), ".cloudflared", "config.yml");
            bool hasNamedTunnel = File.Exists(configPath);
            string customUrl = Program.Settings.CustomPublicUrl?.Trim() ?? "";

            string arguments = hasNamedTunnel
                ? "tunnel run"
                : $"tunnel --url https://localhost:{localPort} --no-tls-verify";

            _logger.LogInformation("Starting Cloudflare tunnel for port {Port} (named={Named}, args={Args})...", localPort, hasNamedTunnel, arguments);
            OnLog?.Invoke($"🔄 Connecting tunnel for port {localPort} to Cloudflare edge...");

            _tunnelProcess = new Process
            {
                StartInfo = new ProcessStartInfo
                {
                    FileName = CloudflaredExePath,
                    Arguments = arguments,
                    RedirectStandardOutput = true,
                    RedirectStandardError = true,
                    UseShellExecute = false,
                    CreateNoWindow = true
                },
                EnableRaisingEvents = true
            };

            _tunnelProcess.Exited += (s, e) =>
            {
                _logger.LogWarning("Cloudflare tunnel process exited with code {Code}",
                    _tunnelProcess?.ExitCode);
                PublicUrl = null;
                IsStarting = false;
                var reason = $"Tunnel process exited (code {_tunnelProcess?.ExitCode})";
                OnTunnelStopped?.Invoke(reason);
                OnLog?.Invoke($"⚠ {reason}");
            };

            _tunnelProcess.Start();

            // 3. Read stderr asynchronously to find the tunnel URL
            // cloudflared outputs the URL to stderr like:
            // "... https://xxx-yyy-zzz.trycloudflare.com ..."
            _ = Task.Run(() => ParseTunnelOutput(_tunnelProcess));
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to start Cloudflare tunnel");
            IsStarting = false;
            OnTunnelStopped?.Invoke($"Start failed: {ex.Message}");
            OnLog?.Invoke($"⚠ Cloud Relay start failed: {ex.Message}");
        }
    }

    /// <summary>
    /// Stops the tunnel gracefully.
    /// </summary>
    public void Stop()
    {
        try
        {
            if (_tunnelProcess != null && !_tunnelProcess.HasExited)
            {
                _tunnelProcess.Kill(entireProcessTree: true);
                _tunnelProcess.WaitForExit(3000);
                _logger.LogInformation("Cloudflare tunnel stopped");
            }
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Error stopping tunnel process");
        }
        finally
        {
            _tunnelProcess?.Dispose();
            _tunnelProcess = null;
            PublicUrl = null;
            IsStarting = false;
        }
    }

    /// <summary>
    /// Downloads cloudflared.exe from GitHub releases (official Cloudflare binary).
    /// </summary>
    private async Task DownloadCloudflaredAsync()
    {
        Directory.CreateDirectory(CloudflaredDir);

        const string downloadUrl = "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe";

        using var httpClient = new HttpClient();
        httpClient.Timeout = TimeSpan.FromMinutes(5);
        httpClient.DefaultRequestHeaders.UserAgent.ParseAdd("WifiPrintServer/2.0");

        var tempPath = CloudflaredExePath + ".tmp";
        try
        {
            using var response = await httpClient.GetAsync(downloadUrl, HttpCompletionOption.ResponseHeadersRead);
            response.EnsureSuccessStatusCode();

            using (var fileStream = new FileStream(tempPath, FileMode.Create, FileAccess.Write, FileShare.None))
            {
                await response.Content.CopyToAsync(fileStream);
            }

            // Atomic rename
            if (File.Exists(CloudflaredExePath))
                File.Delete(CloudflaredExePath);
            File.Move(tempPath, CloudflaredExePath);

            _logger.LogInformation("cloudflared.exe downloaded successfully to {Path}", CloudflaredExePath);
        }
        catch
        {
            // Clean up partial download
            if (File.Exists(tempPath))
                File.Delete(tempPath);
            throw;
        }
    }

    /// <summary>
    /// Reads cloudflared's stderr output to extract the tunnel URL.
    /// cloudflared logs the URL as: "... https://xxx.trycloudflare.com ..."
    /// </summary>
    private void ParseTunnelOutput(Process process)
    {
        var urlRegex = new Regex(@"https://[a-zA-Z0-9\-]+\.trycloudflare\.com", RegexOptions.Compiled);

        try
        {
            // cloudflared outputs to stderr
            while (!process.HasExited)
            {
                var line = process.StandardError.ReadLine();
                if (line == null) break;

                _logger.LogDebug("[cloudflared] {Line}", line);

                var match = urlRegex.Match(line);
                if (match.Success && string.IsNullOrEmpty(PublicUrl))
                {
                    PublicUrl = match.Value;
                    IsStarting = false;
                    _logger.LogInformation("🌐 Cloudflare tunnel active: {Url}", PublicUrl);
                    OnTunnelReady?.Invoke(PublicUrl);
                }
                else if (string.IsNullOrEmpty(PublicUrl) && line.Contains("Registered tunnel connection"))
                {
                    var configPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), ".cloudflared", "config.yml");
                    if (File.Exists(configPath))
                    {
                        PublicUrl = !string.IsNullOrWhiteSpace(Program.Settings.CustomPublicUrl)
                            ? Program.Settings.CustomPublicUrl.Trim()
                            : "https://printora.calclabz.com";
                        IsStarting = false;
                        _logger.LogInformation("🌐 Cloudflare permanent named tunnel active: {Url}", PublicUrl);
                        OnTunnelReady?.Invoke(PublicUrl);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Error reading cloudflared output");
        }
    }

    /// <summary>
    /// Checks if cloudflared.exe is already downloaded.
    /// </summary>
    public static bool IsCloudflaredInstalled() => File.Exists(CloudflaredExePath);

    public void Dispose()
    {
        Stop();
    }
}
