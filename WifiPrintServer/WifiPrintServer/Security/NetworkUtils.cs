using System.Net;
using System.Net.NetworkInformation;
using System.Net.Sockets;

namespace WifiPrintServer.Security;

/// <summary>
/// Network utilities to detect if connecting devices are on the same local subnet / LAN
/// or coming from a remote / routed network requiring QR code verification.
/// </summary>
public static class NetworkUtils
{
    /// <summary>
    /// Checks if a remote IP address is on the same local subnet as one of the server's network interfaces.
    /// Returns true for loopback addresses and devices sharing the same IPv4 subnet mask or IPv6 link-local prefix.
    /// </summary>
    public static bool IsSameLocalSubnet(IPAddress remoteIp)
    {
        if (IPAddress.IsLoopback(remoteIp))
            return true;

        // Map IPv4-mapped IPv6 addresses (e.g. ::ffff:192.168.1.5) to standard IPv4
        if (remoteIp.IsIPv4MappedToIPv6)
        {
            remoteIp = remoteIp.MapToIPv4();
        }

        try
        {
            foreach (var ni in NetworkInterface.GetAllNetworkInterfaces())
            {
                if (ni.OperationalStatus != OperationalStatus.Up ||
                    ni.NetworkInterfaceType == NetworkInterfaceType.Loopback)
                    continue;

                var ipProps = ni.GetIPProperties();
                foreach (var unicast in ipProps.UnicastAddresses)
                {
                    var serverIp = unicast.Address;
                    if (serverIp.IsIPv4MappedToIPv6)
                        serverIp = serverIp.MapToIPv4();

                    if (serverIp.AddressFamily != remoteIp.AddressFamily)
                        continue;

                    if (remoteIp.AddressFamily == AddressFamily.InterNetwork)
                    {
                        var mask = unicast.IPv4Mask;
                        // Fallback to standard class C (/24) mask if adapter mask is missing or 0.0.0.0
                        if (mask == null || mask.Equals(IPAddress.Any))
                        {
                            mask = IPAddress.Parse("255.255.255.0");
                        }

                        byte[] clientBytes = remoteIp.GetAddressBytes();
                        byte[] serverBytes = serverIp.GetAddressBytes();
                        byte[] maskBytes = mask.GetAddressBytes();

                        bool matches = true;
                        for (int i = 0; i < 4; i++)
                        {
                            if ((clientBytes[i] & maskBytes[i]) != (serverBytes[i] & maskBytes[i]))
                            {
                                matches = false;
                                break;
                            }
                        }

                        if (matches)
                            return true;
                    }
                    else if (remoteIp.AddressFamily == AddressFamily.InterNetworkV6)
                    {
                        // Match IPv6 link-local
                        if (remoteIp.IsIPv6LinkLocal && serverIp.IsIPv6LinkLocal)
                            return true;

                        byte[] clientBytes = remoteIp.GetAddressBytes();
                        byte[] serverBytes = serverIp.GetAddressBytes();

                        // Compare first 64 bits (/64 subnet prefix)
                        bool matches = true;
                        for (int i = 0; i < 8; i++)
                        {
                            if (clientBytes[i] != serverBytes[i])
                            {
                                matches = false;
                                break;
                            }
                        }

                        if (matches)
                            return true;
                    }
                }
            }
        }
        catch
        {
            // On error, default to false (not same network) for safety.
            // Previously this treated all private IPs as same-network, which
            // could auto-approve devices on different private networks (VPN, different WiFi).
        }

        return false;
    }
}
