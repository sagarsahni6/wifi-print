using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Serilog;
using WifiPrintServer.Data;
using WifiPrintServer.Hubs;
using WifiPrintServer.Models;
using WifiPrintServer.Services;

namespace WifiPrintServer;

/// <summary>
/// Entry point — hosts both the WPF desktop app and the Kestrel HTTPS web server.
/// </summary>
public partial class Program
{
    public static WebApplication? WebApp { get; private set; }
    public static AppSettings Settings { get; private set; } = AppSettings.LoadOrCreate();
    public static PrintQueueManager? QueueManager { get; private set; }
    public static AuthService? AuthServiceInstance { get; private set; }
    public static DiscoveryService? Discovery { get; private set; }
    public static StatusBroadcaster? Broadcaster { get; private set; }
    public static PrinterService? PrinterServiceInstance { get; private set; }
    public static X509Certificate2? ServerCertificate { get; private set; }

    /// <summary>
    /// Starts the Kestrel web server on a background thread.
    /// Called from App.xaml.cs after WPF initializes.
    /// </summary>
    public static async Task StartWebServerAsync()
    {
        var builder = WebApplication.CreateBuilder();
        builder.Host.UseSerilog((_, _, loggerConfiguration) =>
        {
            Directory.CreateDirectory(Settings.LogDirectory);
            loggerConfiguration
                .Enrich.FromLogContext()
                .WriteTo.File(
                    Path.Combine(Settings.LogDirectory, "server-.log"),
                    rollingInterval: RollingInterval.Day,
                    retainedFileCountLimit: 14,
                    shared: true);
        });

        // Configure HTTPS with self-signed certificate
        ServerCertificate = GetOrCreateSelfSignedCert();
        builder.Services.Configure<KestrelServerOptions>(k =>
        {
            k.ListenAnyIP(Settings.ServerPort, lo => lo.UseHttps(ServerCertificate));
        });

        // Register services
        builder.Services.AddSingleton(Settings);
        builder.Services.AddDbContextFactory<ServerStateContext>(options =>
        {
            options.UseSqlite($"Data Source={Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "WifiPrintServer", "wifiprint.db")}");
        });
        builder.Services.AddSingleton<ServerStateStore>();
        builder.Services.AddSingleton<PrinterService>();
        builder.Services.AddSingleton<PrintQueueManager>();
        builder.Services.AddSingleton<FileProcessingService>();
        builder.Services.AddSingleton<AuthService>();
        builder.Services.AddSingleton<StatusBroadcaster>();
        builder.Services.AddHostedService(sp => sp.GetRequiredService<PrintQueueManager>());

        // Add controllers + SignalR
        builder.Services.AddControllers()
            .AddJsonOptions(options =>
            {
                // Serialize enums as strings (e.g., "Pending" instead of 0)
                // Critical: Android client expects string status values
                options.JsonSerializerOptions.Converters.Add(
                    new System.Text.Json.Serialization.JsonStringEnumConverter());
                options.JsonSerializerOptions.PropertyNamingPolicy =
                    System.Text.Json.JsonNamingPolicy.CamelCase;
            });
        builder.Services.AddSignalR();

        // JWT authentication
        builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
            .AddJwtBearer(options =>
            {
                options.TokenValidationParameters = new TokenValidationParameters
                {
                    ValidateIssuer = true,
                    ValidIssuer = "WifiPrintServer",
                    ValidateAudience = true,
                    ValidAudience = "WifiPrintClient",
                    ValidateLifetime = true,
                    IssuerSigningKey = new SymmetricSecurityKey(
                        Encoding.UTF8.GetBytes(Settings.JwtSecret))
                };

                // Allow JWT in query string for SignalR
                options.Events = new JwtBearerEvents
                {
                    OnMessageReceived = context =>
                    {
                        var accessToken = context.Request.Query["access_token"];
                        var path = context.HttpContext.Request.Path;
                        if (!string.IsNullOrEmpty(accessToken) && path.StartsWithSegments("/ws"))
                            context.Token = accessToken;
                        return Task.CompletedTask;
                    },
                    OnTokenValidated = context =>
                    {
                        var authService = context.HttpContext.RequestServices.GetRequiredService<AuthService>();
                        var deviceId = context.Principal?.FindFirst("deviceId")?.Value;
                        if (string.IsNullOrWhiteSpace(deviceId))
                        {
                            context.Fail("Missing deviceId claim");
                            return Task.CompletedTask;
                        }

                        var device = authService.GetDeviceById(deviceId);
                        if (device == null || device.IsBlocked || !device.IsActive)
                        {
                            context.Fail("Device is not authorized");
                            return Task.CompletedTask;
                        }

                        authService.MarkDeviceSeen(deviceId);
                        return Task.CompletedTask;
                    }
                };
            });

        builder.Services.AddAuthorization();

        // Build app
        WebApp = builder.Build();

        var stateStore = WebApp.Services.GetRequiredService<ServerStateStore>();
        stateStore.EnsureCreated();
        stateStore.ExpirePendingApprovals();

        // Middleware pipeline
        WebApp.UseAuthentication();
        WebApp.UseAuthorization();
        WebApp.MapControllers();
        WebApp.MapHub<PrintStatusHub>("/ws/status");

        // Resolve singletons for WPF access
        QueueManager = WebApp.Services.GetRequiredService<PrintQueueManager>();
        AuthServiceInstance = WebApp.Services.GetRequiredService<AuthService>();
        Broadcaster = WebApp.Services.GetRequiredService<StatusBroadcaster>();
        PrinterServiceInstance = WebApp.Services.GetRequiredService<PrinterService>();

        // Wire up queue events to SignalR broadcasts
        QueueManager.OnJobStatusChanged += async update =>
        {
            try { await Broadcaster.BroadcastJobUpdate(update); }
            catch { /* ignore broadcast failures */ }
        };

        // Start mDNS discovery
        Discovery = new DiscoveryService(
            WebApp.Services.GetRequiredService<ILogger<DiscoveryService>>(),
            Settings.ServerPort,
            Settings.ServerName);
        Discovery.Start();

        QueueManager.RestoreJobs();

        // Run the web server (non-blocking)
        await WebApp.RunAsync();
    }

    /// <summary>
    /// Creates or loads a self-signed HTTPS certificate for local network use.
    /// </summary>
    private static X509Certificate2 GetOrCreateSelfSignedCert()
    {
        string certDir = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "WifiPrintServer");
        Directory.CreateDirectory(certDir);
        string certPath = Path.Combine(certDir, "server.pfx");
        string password = Settings.CertificatePassword;

        if (File.Exists(certPath))
        {
            try
            {
                return new X509Certificate2(certPath, password);
            }
            catch { /* regenerate if corrupt */ }
        }

        // Generate new self-signed cert
        using var rsa = RSA.Create(2048);
        var request = new CertificateRequest(
            "CN=WifiPrintServer, O=WifiPrint, OU=Local",
            rsa,
            HashAlgorithmName.SHA256,
            RSASignaturePadding.Pkcs1);

        request.CertificateExtensions.Add(
            new X509BasicConstraintsExtension(false, false, 0, false));

        // Add SAN for local IPs
        var sanBuilder = new SubjectAlternativeNameBuilder();
        sanBuilder.AddIpAddress(System.Net.IPAddress.Loopback);
        if (System.Net.IPAddress.TryParse(DiscoveryService.GetLocalIpAddress(), out var localIp))
            sanBuilder.AddIpAddress(localIp);
        sanBuilder.AddDnsName("localhost");
        sanBuilder.AddDnsName(Environment.MachineName);
        request.CertificateExtensions.Add(sanBuilder.Build());

        var cert = request.CreateSelfSigned(
            DateTimeOffset.UtcNow.AddDays(-1),
            DateTimeOffset.UtcNow.AddYears(5));

        var pfxBytes = cert.Export(X509ContentType.Pfx, password);
        File.WriteAllBytes(certPath, pfxBytes);

        return new X509Certificate2(pfxBytes, password);
    }
}
