using System.Net;
using Microsoft.AspNetCore.Mvc.Filters;

namespace WifiPrintServer.Security;

[AttributeUsage(AttributeTargets.Class | AttributeTargets.Method)]
public sealed class LocalOnlyAttribute : Attribute, IAuthorizationFilter
{
    public void OnAuthorization(AuthorizationFilterContext context)
    {
        var remoteIp = context.HttpContext.Connection.RemoteIpAddress;
        if (remoteIp != null && IPAddress.IsLoopback(remoteIp))
            return;

        context.Result = new StatusCodeResult(StatusCodes.Status403Forbidden);
    }
}
