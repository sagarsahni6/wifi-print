using System.Net;
using System.Security.Claims;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging.Abstractions;
using WifiPrintServer.Controllers;
using WifiPrintServer.Data;
using WifiPrintServer.Models;
using WifiPrintServer.Security;
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

    [Fact]
    public void JobsController_supports_pagination_with_headers()
    {
        var queueManager = CreateQueueManager();
        for (int i = 1; i <= 5; i++)
        {
            queueManager.EnqueueJob(CreateJob($"JOB_{i}", "device-test", "Normal", DateTime.UtcNow.AddMinutes(-i)));
        }

        var httpContext = new DefaultHttpContext
        {
            User = new ClaimsPrincipal(new ClaimsIdentity(
                new[] { new Claim("deviceId", "device-test") }, "TestAuth"))
        };

        var controller = new JobsController(queueManager)
        {
            ControllerContext = new ControllerContext { HttpContext = httpContext }
        };

        var result = Assert.IsType<OkObjectResult>(controller.GetAll(null, page: 2, pageSize: 2));
        var payload = Assert.IsType<ApiResponse<List<PrintJob>>>(result.Value);

        Assert.Equal(2, payload.Data!.Count);
        Assert.Equal("5", httpContext.Response.Headers["X-Total-Count"]);
        Assert.Equal("2", httpContext.Response.Headers["X-Page"]);
        Assert.Equal("2", httpContext.Response.Headers["X-Page-Size"]);
        Assert.Equal("3", httpContext.Response.Headers["X-Total-Pages"]);
    }

    [Fact]
    public void PrintSettings_defaults_and_custom_options()
    {
        var settings = new PrintSettings
        {
            Copies = 3,
            Collate = true,
            WatermarkText = "CONFIDENTIAL"
        };

        Assert.Equal(3, settings.Copies);
        Assert.True(settings.Collate);
        Assert.Equal("CONFIDENTIAL", settings.WatermarkText);
    }

    [Fact]
    public void AdminController_GetOverview_returns_valid_system_summary()
    {
        var store = CreateStateStore();
        store.EnsureCreated();

        var settings = new AppSettings { ServerName = "Test Server", ServerPort = 5055 };
        var printerService = new PrinterService(NullLogger<PrinterService>.Instance, settings);
        var queueManager = new PrintQueueManager(printerService, store, NullLogger<PrintQueueManager>.Instance);
        var authService = new AuthService(settings, NullLogger<AuthService>.Instance, store);
        var fileService = new FileProcessingService(NullLogger<FileProcessingService>.Instance, settings);
        var tunnelService = new TunnelService(NullLogger<TunnelService>.Instance);
        var protectionService = new WebPrintProtectionService(settings, tunnelService, NullLogger<WebPrintProtectionService>.Instance);

        var httpContext = new DefaultHttpContext();
        httpContext.Connection.RemoteIpAddress = System.Net.IPAddress.Loopback;
        var adminController = new AdminController(settings, printerService, queueManager, authService, fileService, tunnelService, protectionService, NullLogger<AdminController>.Instance)
        {
            ControllerContext = new ControllerContext { HttpContext = httpContext }
        };
        var result = Assert.IsType<OkObjectResult>(adminController.GetOverview());

        Assert.NotNull(result.Value);
    }

    [Theory]
    [InlineData("../../etc/passwd", "passwd")]
    [InlineData("..\\..\\Windows\\System32\\calc.exe", "calc.exe")]
    [InlineData("test<>:\"file.pdf", "test____file.pdf")]
    [InlineData("folder/sub\\test*?.pdf", "test__.pdf")]
    [InlineData("normal_report.pdf", "normal_report.pdf")]
    [InlineData("", "document")]
    [InlineData(null, "document")]
    public void FileProcessingService_Sanitizes_PathTraversal_And_IllegalChars(string? input, string expected)
    {
        var sanitized = FileProcessingService.SanitizeFileName(input!);
        Assert.Equal(expected, sanitized);
    }

    [Fact]
    public void NetworkUtils_Detects_Local_Subnet_Correctly()
    {
        Assert.True(NetworkUtils.IsSameLocalSubnet(IPAddress.Loopback));
        Assert.True(NetworkUtils.IsSameLocalSubnet(IPAddress.IPv6Loopback));
    }

    [Fact]
    public async Task AuthController_AutoApproves_When_OnSameNetwork()
    {
        var store = CreateStateStore();
        store.EnsureCreated();

        var settings = new AppSettings
        {
            AutoApproveSameNetwork = true,
            RequireQrCodeOutsideLocalNetwork = true,
            CurrentQrPairingToken = "TEST_QR_TOKEN_123"
        };
        var authService = new AuthService(settings, NullLogger<AuthService>.Instance, store);
        var controller = new AuthController(authService, settings, NullLogger<AuthController>.Instance);

        var httpContext = new DefaultHttpContext();
        httpContext.Connection.RemoteIpAddress = IPAddress.Loopback;
        controller.ControllerContext = new ControllerContext { HttpContext = httpContext };

        var request = new ConnectionRequest
        {
            DeviceName = "Test Phone",
            DeviceModel = "Galaxy S24"
        };

        var result = await controller.RequestConnection(request);
        var okResult = Assert.IsType<OkObjectResult>(result);
        var apiResponse = Assert.IsType<ApiResponse<AuthResponse>>(okResult.Value);
        Assert.True(apiResponse.Success);
        Assert.NotNull(apiResponse.Data?.Token);
    }

    [Fact]
    public async Task AuthController_RequiresQrCode_When_OnAnotherNetwork()
    {
        var store = CreateStateStore();
        store.EnsureCreated();

        var settings = new AppSettings
        {
            AutoApproveSameNetwork = true,
            RequireQrCodeOutsideLocalNetwork = true,
            CurrentQrPairingToken = "SECRET_QR_TOKEN"
        };
        var authService = new AuthService(settings, NullLogger<AuthService>.Instance, store);
        var controller = new AuthController(authService, settings, NullLogger<AuthController>.Instance);

        var httpContext = new DefaultHttpContext();
        httpContext.Connection.RemoteIpAddress = IPAddress.Parse("198.51.100.42");
        controller.ControllerContext = new ControllerContext { HttpContext = httpContext };

        var noQrRequest = new ConnectionRequest
        {
            DeviceName = "Remote Phone",
            DeviceModel = "iPhone 15",
            QrToken = null
        };
        var rejectedResult = await controller.RequestConnection(noQrRequest);
        var statusResult = Assert.IsType<ObjectResult>(rejectedResult);
        Assert.Equal(403, statusResult.StatusCode);

        var invalidQrRequest = new ConnectionRequest
        {
            DeviceName = "Remote Phone",
            DeviceModel = "iPhone 15",
            QrToken = "WRONG_TOKEN"
        };
        var wrongQrResult = await controller.RequestConnection(invalidQrRequest);
        var wrongStatusResult = Assert.IsType<ObjectResult>(wrongQrResult);
        Assert.Equal(403, wrongStatusResult.StatusCode);
    }

    [Fact]
    public async Task AuthController_Accepts_When_OnAnotherNetwork_With_Valid_Pin()
    {
        var store = CreateStateStore();
        store.EnsureCreated();

        var settings = new AppSettings
        {
            AutoApproveSameNetwork = true,
            RequireQrCodeOutsideLocalNetwork = true,
            CurrentQrPairingToken = "SECRET_QR_TOKEN",
            CurrentConnectionPin = "842915"
        };
        var authService = new AuthService(settings, NullLogger<AuthService>.Instance, store);
        var controller = new AuthController(authService, settings, NullLogger<AuthController>.Instance);

        var httpContext = new DefaultHttpContext();
        httpContext.Connection.RemoteIpAddress = IPAddress.Parse("198.51.100.42");
        controller.ControllerContext = new ControllerContext { HttpContext = httpContext };

        var pinRequest = new ConnectionRequest
        {
            DeviceName = "Remote Phone",
            DeviceModel = "iPhone 15",
            Pin = "842 915" // spaces should be tolerated
        };
        var result = await controller.RequestConnection(pinRequest);
        var okResult = Assert.IsType<OkObjectResult>(result);
        var apiResponse = Assert.IsType<ApiResponse<AuthResponse>>(okResult.Value);
        Assert.True(apiResponse.Success);
        Assert.NotNull(apiResponse.Data?.Token);
    }

    [Fact]
    public async Task AuthController_Requests_Approval_When_OnAnotherNetwork_With_Valid_QrCode()
    {
        var store = CreateStateStore();
        store.EnsureCreated();

        var settings = new AppSettings
        {
            AutoApproveSameNetwork = true,
            RequireQrCodeOutsideLocalNetwork = true,
            CurrentQrPairingToken = "PERMANENT_QR_TOKEN"
        };
        var authService = new AuthService(settings, NullLogger<AuthService>.Instance, store);
        var controller = new AuthController(authService, settings, NullLogger<AuthController>.Instance);

        var httpContext = new DefaultHttpContext();
        httpContext.Connection.RemoteIpAddress = IPAddress.Parse("198.51.100.42");
        controller.ControllerContext = new ControllerContext { HttpContext = httpContext };

        // Wire up approval handler (representing the server dashboard / notification panel)
        PendingApproval? receivedApproval = null;
        authService.OnApprovalRequested += approval =>
        {
            receivedApproval = approval;
            authService.ApproveDevice(approval.Id);
        };

        var qrRequest = new ConnectionRequest
        {
            DeviceName = "Remote Phone",
            DeviceModel = "Galaxy S23",
            QrToken = "PERMANENT_QR_TOKEN"
        };

        var result = await controller.RequestConnection(qrRequest);
        Assert.NotNull(receivedApproval);
        Assert.Equal("Remote Phone", receivedApproval.DeviceName);

        var okResult = Assert.IsType<OkObjectResult>(result);
        var apiResponse = Assert.IsType<ApiResponse<AuthResponse>>(okResult.Value);
        Assert.True(apiResponse.Success);
        Assert.NotNull(apiResponse.Data?.Token);
    }

    [Fact]
    public void AppSettings_GeneratePin_Produces_6Digit_Numeric_String()
    {
        for (int i = 0; i < 50; i++)
        {
            var pin = AppSettings.GeneratePin();
            Assert.Equal(6, pin.Length);
            Assert.True(pin.All(char.IsDigit));
        }
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

    [Fact]
    public void PrinterService_Can_Validate_And_Unlock_Encrypted_Pdf()
    {
        var tempPdf = Path.Combine(_tempDirectory, "encrypted_test.pdf");
        Directory.CreateDirectory(_tempDirectory);
        string password = "SecretPassword123";

        // Write pre-generated password-protected PDF fixture (encrypted with "SecretPassword123")
        File.WriteAllBytes(tempPdf, Convert.FromBase64String(EncryptedPdfFixtureBase64));

        var printerService = new PrinterService(NullLogger<PrinterService>.Instance, new AppSettings());

        // 1. Without password -> requires password
        var lockedCheck = printerService.ValidatePdf(tempPdf, null);
        Assert.False(lockedCheck.IsValid);
        Assert.True(lockedCheck.RequiresPassword);

        // 2. With incorrect password -> rejected
        var wrongPassCheck = printerService.ValidatePdf(tempPdf, "WrongPass999");
        Assert.False(wrongPassCheck.IsValid);
        Assert.True(wrongPassCheck.RequiresPassword);

        // 3. With correct password -> unlocked and page count verified
        var successCheck = printerService.ValidatePdf(tempPdf, password);
        Assert.True(successCheck.IsValid);
        Assert.False(successCheck.RequiresPassword);
        Assert.Equal(1, successCheck.PageCount);
    }

    [Fact]
    public void PrintQueueManager_ClearCompletedJobs_Removes_Only_Completed_And_Cancelled()
    {
        var store = CreateStateStore();
        store.EnsureCreated();
        var printerService = new PrinterService(NullLogger<PrinterService>.Instance, new AppSettings());
        var queueManager = new PrintQueueManager(printerService, store, NullLogger<PrintQueueManager>.Instance);

        var pendingJob = new PrintJob { FilePath = "test1.pdf", OriginalFileName = "test1.pdf", PrinterName = "Printer1" };
        var completedJob = new PrintJob { FilePath = "test2.pdf", OriginalFileName = "test2.pdf", PrinterName = "Printer1" };
        var cancelledJob = new PrintJob { FilePath = "test3.pdf", OriginalFileName = "test3.pdf", PrinterName = "Printer1" };

        queueManager.EnqueueJob(pendingJob);
        queueManager.EnqueueJob(completedJob);
        queueManager.EnqueueJob(cancelledJob);

        completedJob.Status = PrintJobStatus.Completed;
        cancelledJob.Status = PrintJobStatus.Cancelled;

        int cleared = queueManager.ClearCompletedJobs();
        Assert.Equal(2, cleared);

        var remaining = queueManager.GetAllJobs();
        Assert.Single(remaining);
        Assert.Equal(pendingJob.Id, remaining[0].Id);
    }

    [Fact]
    public void PrintQueueManager_CleanupJobFiles_Deletes_Uploaded_Files_And_Preserves_External_Files()
    {
        var tempDir = Path.Combine(Path.GetTempPath(), "PrintoraTest_" + Guid.NewGuid().ToString("N"));
        var uploadDir = Path.Combine(tempDir, "Uploads");
        Directory.CreateDirectory(uploadDir);

        try
        {
            var settings = new AppSettings
            {
                UploadDirectory = uploadDir,
                AutoCleanupAfterPrint = true
            };

            var store = CreateStateStore();
            store.EnsureCreated();
            var printerService = new PrinterService(NullLogger<PrinterService>.Instance, settings);
            var queueManager = new PrintQueueManager(printerService, store, NullLogger<PrintQueueManager>.Instance, settings);

            // 1. Managed uploaded file in uploadDir
            string uploadedFile = Path.Combine(uploadDir, "uploaded_doc.pdf");
            File.WriteAllText(uploadedFile, "dummy uploaded content");

            // 2. Converted temp file in uploadDir
            string convertedFile = Path.Combine(uploadDir, "converted_doc.pdf");
            File.WriteAllText(convertedFile, "dummy converted content");

            // 3. User's personal file outside uploadDir (e.g. desktop/documents)
            string externalFile = Path.Combine(tempDir, "personal_resume.pdf");
            File.WriteAllText(externalFile, "important user personal file");

            var job = new PrintJob
            {
                FilePath = uploadedFile,
                ConvertedFilePath = convertedFile,
                OriginalFileName = "document.docx",
                PrinterName = "TestPrinter"
            };

            var externalJob = new PrintJob
            {
                FilePath = externalFile,
                OriginalFileName = "personal_resume.pdf",
                PrinterName = "TestPrinter"
            };

            // Cleanup managed job
            queueManager.CleanupJobFiles(job);
            Assert.False(File.Exists(uploadedFile), "Uploaded file should be deleted");
            Assert.False(File.Exists(convertedFile), "Converted file should be deleted");

            // Cleanup external job
            queueManager.CleanupJobFiles(externalJob);
            Assert.True(File.Exists(externalFile), "External user file outside UploadDirectory MUST be preserved");
        }
        finally
        {
            if (Directory.Exists(tempDir))
            {
                try { Directory.Delete(tempDir, true); } catch { }
            }
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

    private const string EncryptedPdfFixtureBase64 = "JVBERi0xLjcKJeLjz9MKNSAwIG9iago8PC9GaWx0ZXIvRmxhdGVEZWNvZGUvTGVuZ3RoIDk2Pj5zdHJlYW0KKXqvoofuoY1TejxaC6m8HPK6amo13/tqAI7NNGIN05AtsGOgGJpCutZkO2teHCILRr3MDZDqiKkAM/e6SNXRgw6jZzkXysgdiuKCmk19numH5Yj/rn20hrm2TUNfS+0rCmVuZHN0cmVhbQplbmRvYmoKNCAwIG9iago8PC9Db250ZW50cyA1IDAgUi9NZWRpYUJveFswIDAgNTk1IDg0Ml0vUGFyZW50IDIgMCBSL1Jlc291cmNlczw8L0ZvbnQ8PC9GMSA2IDAgUj4+Pj4vVHJpbUJveFswIDAgNTk1IDg0Ml0vVHlwZS9QYWdlPj4KZW5kb2JqCjMgMCBvYmoKPDwvQ3JlYXRpb25EYXRlKFRcMDI3Ofp5PFxczK9fQDXN01wwMTegVYKHrkD3Z9RcMDE2XDAwMUa9vz7bjUGf8vNjaTrqYN26lff+QjspL01vZERhdGUoVFwwMjc5+nk8XFzMr19ANc3TXDAxN6BVgoeuQPdn1FwwMTZcMDAxRr2/PtuNQZ/y82NpOupg3bqV9/5COykvUHJvZHVjZXIoVFwwMjc5+nk8XFzMr19ANc3TXDAxN6CYU7jwqq3CbNZcMDAzUrhkTOw/n93sPTCVsVwwMzdSV2if8f0vLZG80FwwMTfvwVwo9sxFullcMDMz6EJNvX+vXYuPYlRcXK40odSFXDAyMkMpPj4KZW5kb2JqCjEgMCBvYmoKPDwvUGFnZXMgMiAwIFIvVHlwZS9DYXRhbG9nPj4KZW5kb2JqCjYgMCBvYmoKPDwvQmFzZUZvbnQvSGVsdmV0aWNhL0VuY29kaW5nL1dpbkFuc2lFbmNvZGluZy9TdWJ0eXBlL1R5cGUxL1R5cGUvRm9udD4+CmVuZG9iagoyIDAgb2JqCjw8L0NvdW50IDEvS2lkc1s0IDAgUl0vVHlwZS9QYWdlcz4+CmVuZG9iago3IDAgb2JqCjw8L0NGPDwvU3RkQ0Y8PC9BdXRoRXZlbnQvRG9jT3Blbi9DRk0vQUVTVjIvTGVuZ3RoIDE2Pj4+Pi9GaWx0ZXIvU3RhbmRhcmQvTGVuZ3RoIDEyOC9PICiNJINztl+BoVwwMzdP5eR7Oml2NFxmp4EnTmVcMDA1g9Dkv9t5QuwpL1AgLTE4NTIvUiA0L1N0bUYvU3RkQ0YvU3RyRi9TdGRDRi9VICifyphcMDMwQKRcMDI0QEqCXDAyNk0jPlwwMjF/XDAwMFwwMDBcMDAwXDAwMFwwMDBcMDAwXDAwMFwwMDBcMDAwXDAwMFwwMDBcMDAwXDAwMFwwMDBcMDAwXDAwMCkvViA0Pj4KZW5kb2JqCnhyZWYKMCA4CjAwMDAwMDAwMDAgNjU1MzUgZiAKMDAwMDAwMDU5MiAwMDAwMCBuIAowMDAwMDAwNzI1IDAwMDAwIG4gCjAwMDAwMDAzMTAgMDAwMDAgbiAKMDAwMDAwMDE3NyAwMDAwMCBuIAowMDAwMDAwMDE1IDAwMDAwIG4gCjAwMDAwMDA2MzcgMDAwMDAgbiAKMDAwMDAwMDc3NiAwMDAwMCBuIAp0cmFpbGVyCjw8L0VuY3J5cHQgNyAwIFIvSUQgWzw1OGZkZjAzOTY4NjUzZWUwZDhjZjQ2OTMyY2RjNTExMDdhNTA4NWNkZDY0YjE2OTI5NWM2MzRiZjYwMWMzMGIxNTU2Y2EzZTgyOGYyZDM5Y2Y4NTE5MTI3ODFlMTJjNGViZTU3YTZhODc5MWUzMDNlMTU3OTQwOTRlZDliNDNkND48NThmZGYwMzk2ODY1M2VlMGQ4Y2Y0NjkzMmNkYzUxMTA3YTUwODVjZGQ2NGIxNjkyOTVjNjM0YmY2MDFjMzBiMTU1NmNhM2U4MjhmMmQzOWNmODUxOTEyNzgxZTEyYzRlYmU1N2E2YTg3OTFlMzAzZTE1Nzk0MDk0ZWQ5YjQzZDQ+XS9JbmZvIDMgMCBSL1Jvb3QgMSAwIFIvU2l6ZSA4Pj4KJWlUZXh0LUNvcmUtOC4wLjIKc3RhcnR4cmVmCjEwNTcKJSVFT0YK";
}
