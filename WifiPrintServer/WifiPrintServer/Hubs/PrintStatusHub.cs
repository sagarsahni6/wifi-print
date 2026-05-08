using Microsoft.AspNetCore.SignalR;
using WifiPrintServer.Models;

namespace WifiPrintServer.Hubs;

/// <summary>
/// SignalR hub for real-time print job status updates.
/// Android clients connect to receive live progress on their print jobs.
/// </summary>
public class PrintStatusHub : Hub
{
    private readonly ILogger<PrintStatusHub> _logger;

    public PrintStatusHub(ILogger<PrintStatusHub> logger)
    {
        _logger = logger;
    }

    public override Task OnConnectedAsync()
    {
        _logger.LogInformation("Client connected to StatusHub: {Id}", Context.ConnectionId);
        return base.OnConnectedAsync();
    }

    public override Task OnDisconnectedAsync(Exception? exception)
    {
        _logger.LogInformation("Client disconnected from StatusHub: {Id}", Context.ConnectionId);
        return base.OnDisconnectedAsync(exception);
    }

    /// <summary>
    /// Client can subscribe to updates for a specific job.
    /// </summary>
    public async Task SubscribeToJob(string jobId)
    {
        await Groups.AddToGroupAsync(Context.ConnectionId, $"job_{jobId}");
        _logger.LogDebug("Client {Id} subscribed to job {Job}", Context.ConnectionId, jobId);
    }

    /// <summary>
    /// Client can subscribe to all updates for their device.
    /// </summary>
    public async Task SubscribeToDevice(string deviceId)
    {
        await Groups.AddToGroupAsync(Context.ConnectionId, $"device_{deviceId}");
    }
}

/// <summary>
/// Helper to broadcast status updates from services to connected clients.
/// </summary>
public class StatusBroadcaster
{
    private readonly IHubContext<PrintStatusHub> _hubContext;

    public StatusBroadcaster(IHubContext<PrintStatusHub> hubContext)
    {
        _hubContext = hubContext;
    }

    public async Task BroadcastJobUpdate(JobStatusUpdate update)
    {
        await _hubContext.Clients.Group($"job_{update.JobId}")
            .SendAsync("JobStatusUpdate", update);
        await _hubContext.Clients.All.SendAsync("JobStatusUpdate", update);
    }
}
