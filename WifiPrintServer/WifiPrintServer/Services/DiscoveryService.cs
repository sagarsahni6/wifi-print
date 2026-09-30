using System.Net;
using System.Net.Sockets;
using Makaretu.Dns;

namespace WifiPrintServer.Services;

/// <summary>
/// Broadcasts this server on the local network via mDNS/Zeroconf
/// so Android clients can auto-discover it.
/// </summary>
public class DiscoveryService : IDisposable
{
    private readonly ILogger<DiscoveryService> _logger;
    private readonly int _port;
    private readonly string _serverName;
    private MulticastService? _mdns;
    private ServiceDiscovery? _serviceDiscovery;
    private ServiceProfile? _serviceProfile;

    public DiscoveryService(ILogger<DiscoveryService> logger, int port, string serverName)
    {
        _logger = logger;
        _port = port;
        _serverName = serverName;
    }

    /// <summary>
    /// Starts broadcasting the _wifiprint._tcp service via mDNS.
    /// </summary>
    public void Start()
    {
        try
        {
            _mdns = new MulticastService();
            _serviceDiscovery = new ServiceDiscovery(_mdns);

            _serviceProfile = new ServiceProfile(
                instanceName: _serverName,
                serviceName: "_wifiprint._tcp",
                port: (ushort)_port);

            // Add metadata as TXT records
            _serviceProfile.AddProperty("version", "1.0");
            _serviceProfile.AddProperty("platform", "windows");
            _serviceProfile.AddProperty("name", _serverName);

            _serviceDiscovery.Advertise(_serviceProfile);
            _mdns.Start();

            _logger.LogInformation(
                "mDNS discovery started: {Name}._wifiprint._tcp on port {Port}",
                _serverName, _port);

            // Start UDP broadcast beacon as a fallback for networks where mDNS multicast is filtered
            _udpCts = new CancellationTokenSource();
            _ = Task.Run(() => BroadcastUdpBeaconAsync(_udpCts.Token));
            _logger.LogInformation("UDP beacon discovery broadcast active on port {Port}", UdpBeaconPort);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Failed to start mDNS discovery");
        }
    }

    private const int UdpBeaconPort = 5058;
    private CancellationTokenSource? _udpCts;

    private async Task BroadcastUdpBeaconAsync(CancellationToken ct)
    {
        using var client = new UdpClient();
        client.EnableBroadcast = true;
        var endpoint = new IPEndPoint(IPAddress.Broadcast, UdpBeaconPort);

        while (!ct.IsCancellationRequested)
        {
            try
            {
                var localIp = GetLocalIpAddress();
                var payload = System.Text.Json.JsonSerializer.Serialize(new
                {
                    service = "wifiprint",
                    name = _serverName,
                    ip = localIp,
                    port = _port,
                    version = "2.0"
                });

                var bytes = System.Text.Encoding.UTF8.GetBytes(payload);
                await client.SendAsync(bytes, bytes.Length, endpoint);
            }
            catch (Exception ex)
            {
                _logger.LogTrace(ex, "UDP beacon broadcast suppressed");
            }

            try
            {
                await Task.Delay(2500, ct);
            }
            catch (OperationCanceledException)
            {
                break;
            }
        }
    }

    /// <summary>
    /// Stops the mDNS broadcast and UDP beacon.
    /// </summary>
    public void Stop()
    {
        try
        {
            _udpCts?.Cancel();
            _udpCts?.Dispose();
            _udpCts = null;

            if (_serviceProfile != null)
                _serviceDiscovery?.Unadvertise(_serviceProfile);
            _mdns?.Stop();
            _logger.LogInformation("mDNS and UDP beacon discovery stopped");
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Error stopping discovery");
        }
    }

    /// <summary>
    /// Gets the local IP address of this machine on the Wi-Fi network.
    /// </summary>
    public static string GetLocalIpAddress()
    {
        try
        {
            using var socket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, 0);
            socket.Connect("8.8.8.8", 65530);
            var endpoint = socket.LocalEndPoint as IPEndPoint;
            return endpoint?.Address.ToString() ?? "127.0.0.1";
        }
        catch
        {
            return "127.0.0.1";
        }
    }

    public void Dispose()
    {
        Stop();
        _mdns?.Dispose();
    }
}
