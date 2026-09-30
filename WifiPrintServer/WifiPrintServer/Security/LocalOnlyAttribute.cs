using System.Net;
using System.Net.Sockets;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;

namespace WifiPrintServer.Security;

/// <summary>
/// Restricts access to requests originating from the local machine (localhost, loopback, or local LAN IP).
/// Prevents remote devices from accessing administrative endpoints (BUG-10 fix).
/// </summary>
[AttributeUsage(AttributeTargets.Class | AttributeTargets.Method)]
public sealed class LocalOnlyAttribute : Attribute, IAuthorizationFilter
{
    public void OnAuthorization(AuthorizationFilterContext context)
    {
        var connection = context.HttpContext.Connection;
        var remoteIp = connection.RemoteIpAddress;

        if (remoteIp == null)
        {
            context.Result = new StatusCodeResult(StatusCodes.Status403Forbidden);
            return;
        }

        // 1. Loopback addresses (127.0.0.1, ::1)
        if (IPAddress.IsLoopback(remoteIp))
            return;

        // 2. Same as server connection local IP
        if (connection.LocalIpAddress != null && remoteIp.Equals(connection.LocalIpAddress))
            return;

        // 3. Any IP assigned to this machine (e.g. accessing via 192.168.x.x on the same PC)
        try
        {
            var hostAddresses = Dns.GetHostAddresses(Dns.GetHostName());
            if (hostAddresses.Any(ip => ip.Equals(remoteIp)))
                return;
        }
        catch
        {
            // Ignore DNS lookup failures and fall through to forbidden
        }

        context.Result = new StatusCodeResult(StatusCodes.Status403Forbidden);
    }
}
