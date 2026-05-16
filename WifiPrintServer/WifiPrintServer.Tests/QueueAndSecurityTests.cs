using System.Security.Claims;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging.Abstractions;
using WifiPrintServer.Controllers;
using WifiPrintServer.Data;
using WifiPrintServer.Models;
using WifiPrintServer.Services;
using Xunit;

namespace WifiPrintServer.Tests;

public sealed class QueueAndSecurityTests : IDisposable
{
    private readonly string _tempDirectory = Path.Combine(Path.GetTempPath(), "WifiPrintServerTests", Guid.NewGuid().ToString("N"));

    [Fact]
    public void ParsePageRange_supports_single_pages_and_ranges()
    {
        var pages = PrinterService.ParsePageRange("1,3,5-7,12", 12);

        Assert.Equal(new[] { 0, 2, 4, 5, 6, 11 }, pages);
    }

    [Fact]
    public void Queue_position_respects_priority_without_duplicate_resume_entries()
    {
        var queueManager = CreateQueueManager();

        var low = CreateJob("LOW", "device-a", "Low", DateTime.UtcNow.AddMinutes(-3));
        var normal = CreateJob("NORMAL", "device-a", "Normal", DateTime.UtcNow.AddMinutes(-2));
        var high = CreateJob("HIGH", "device-a", "High", DateTime.UtcNow.AddMinutes(-1));

        queueManager.EnqueueJob(low);
        queueManager.EnqueueJob(normal);
        queueManager.EnqueueJob(high);

        Assert.Equal(1, queueManager.GetQueuePosition(high.Id));
        Assert.Equal(2, queueManager.GetQueuePosition(normal.Id));
        Assert.Equal(3, queueManager.GetQueuePosition(low.Id));

        Assert.True(queueManager.PauseJob(normal.Id));
        Assert.True(queueManager.ResumeJob(normal.Id));
        Assert.False(queueManager.ResumeJob(normal.Id));

        Assert.Equal(1, queueManager.GetQueuePosition(high.Id));
        Assert.Equal(2, queueManager.GetQueuePosition(normal.Id));
        Assert.Equal(3, queueManager.GetQueuePosition(low.Id));
    }

    [Fact]
    public void RestoreJobs_requeues_only_incomplete_jobs()
    {
        var store = CreateStateStore();
        store.EnsureCreated();

        store.UpsertJob(CreateJob("PENDING", "device-a", "Normal", DateTime.UtcNow.AddMinutes(-3)));

        var failed = CreateJob("FAILED", "device-a", "Normal", DateTime.UtcNow.AddMinutes(-2));
        failed.Status = PrintJobStatus.Failed;
        failed.QueueState = "Failed";
        failed.ErrorMessage = "printer failed";
        failed.FailureCode = "print_failed";
        store.UpsertJob(failed);

        var paused = CreateJob("PAUSED", "device-a", "Normal", DateTime.UtcNow.AddMinutes(-1));
        paused.Status = PrintJobStatus.Paused;
        paused.QueueState = "Paused";
        store.UpsertJob(paused);

        var queueManager = CreateQueueManager(store);
        queueManager.RestoreJobs();

        Assert.Equal(1, queueManager.GetQueuePosition("PENDING"));
        Assert.Equal(PrintJobStatus.Pending, queueManager.GetJob("PENDING")?.Status);
        Assert.Equal(PrintJobStatus.Failed, queueManager.GetJob("FAILED")?.Status);
        Assert.Equal(PrintJobStatus.Paused, queueManager.GetJob("PAUSED")?.Status);
        Assert.Equal(0, queueManager.GetQueuePosition("FAILED"));
        Assert.Equal(0, queueManager.GetQueuePosition("PAUSED"));
    }

    [Fact]
    public void JobsController_limits_visibility_to_the_authenticated_device()
    {
        var queueManager = CreateQueueManager();
        queueManager.EnqueueJob(CreateJob("DEVICE1", "device-1", "Normal", DateTime.UtcNow.AddMinutes(-2)));
        queueManager.EnqueueJob(CreateJob("DEVICE2", "device-2", "Normal", DateTime.UtcNow.AddMinutes(-1)));

        var controller = new JobsController(queueManager)
        {
            ControllerContext = new ControllerContext
            {
                HttpContext = new DefaultHttpContext
                {
                    User = new ClaimsPrincipal(new ClaimsIdentity(
                        new[]
                        {
                            new Claim("deviceId", "device-1"),
                            new Claim("deviceName", "Phone 1")
                        },
                        "TestAuth"))
                }
            }
        };

        var allJobsResult = Assert.IsType<OkObjectResult>(controller.GetAll(null));
        var payload = Assert.IsType<ApiResponse<List<PrintJob>>>(allJobsResult.Value);
        Assert.Single(payload.Data!);
        Assert.Equal("DEVICE1", payload.Data![0].Id);

        var foreignJobResult = controller.GetById("DEVICE2");
        Assert.IsType<NotFoundObjectResult>(foreignJobResult);
    }

    private PrintQueueManager CreateQueueManager(ServerStateStore? stateStore = null)
    {
        stateStore ??= CreateStateStore();
        stateStore.EnsureCreated();

        var printerService = new PrinterService(
            NullLogger<PrinterService>.Instance,
            new AppSettings());

        return new PrintQueueManager(
            printerService,
            stateStore,
            NullLogger<PrintQueueManager>.Instance);
    }

    private ServerStateStore CreateStateStore()
    {
        Directory.CreateDirectory(_tempDirectory);
        var dbPath = Path.Combine(_tempDirectory, $"{Guid.NewGuid():N}.db");
        var factory = new TestDbContextFactory(dbPath);
        return new ServerStateStore(factory, NullLogger<ServerStateStore>.Instance);
    }

    private static PrintJob CreateJob(string id, string deviceId, string priority, DateTime createdAt) => new()
    {
        Id = id,
        FileName = $"{id}.pdf",
        OriginalFileName = $"{id}.pdf",
        FileSize = 1024,
        FileType = "PDF",
        FilePath = $"C:\\Temp\\{id}.pdf",
        PrinterName = "Test Printer",
        DeviceId = deviceId,
        DeviceName = deviceId,
        CreatedAt = createdAt,
        UpdatedAt = createdAt,
        Priority = priority,
        QueueState = "Queued",
        Status = PrintJobStatus.Pending
    };

    public void Dispose()
    {
        try
        {
            if (Directory.Exists(_tempDirectory))
                Directory.Delete(_tempDirectory, true);
        }
        catch (IOException)
        {
            // SQLite may still be finalizing handles when the test process tears down.
        }
    }

    private sealed class TestDbContextFactory : IDbContextFactory<ServerStateContext>
    {
        private readonly DbContextOptions<ServerStateContext> _options;

        public TestDbContextFactory(string databasePath)
        {
            _options = new DbContextOptionsBuilder<ServerStateContext>()
                .UseSqlite($"Data Source={databasePath}")
                .Options;
        }

        public ServerStateContext CreateDbContext() => new(_options);
    }
}
