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
        Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
        "WifiPrintServer", "cloudflared");

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
            // 1. Ensure cloudflared.exe exists
            if (!File.Exists(CloudflaredExePath))
            {
                _logger.LogInformation("Downloading cloudflared.exe...");
                await DownloadCloudflaredAsync();
            }

            // 2. Start the tunnel process
            _logger.LogInformation("Starting Cloudflare tunnel for port {Port}...", localPort);

            _tunnelProcess = new Process
            {
                StartInfo = new ProcessStartInfo
                {
                    FileName = CloudflaredExePath,
                    Arguments = $"tunnel --url https://localhost:{localPort} --no-tls-verify",
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
                OnTunnelStopped?.Invoke("Tunnel process exited");
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

            using var fileStream = new FileStream(tempPath, FileMode.Create, FileAccess.Write, FileShare.None);
            await response.Content.CopyToAsync(fileStream);

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
