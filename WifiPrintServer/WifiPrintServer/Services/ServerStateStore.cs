using Microsoft.EntityFrameworkCore;
using WifiPrintServer.Data;
using WifiPrintServer.Models;

namespace WifiPrintServer.Services;

public class ServerStateStore
{
    private readonly IDbContextFactory<ServerStateContext> _contextFactory;
    private readonly ILogger<ServerStateStore> _logger;

    public ServerStateStore(
        IDbContextFactory<ServerStateContext> contextFactory,
        ILogger<ServerStateStore> logger)
    {
        _contextFactory = contextFactory;
        _logger = logger;
    }

    public void EnsureCreated()
    {
        using var db = _contextFactory.CreateDbContext();
        db.Database.EnsureCreated();

        // Migrate any missing columns in the Devices table for databases created with earlier schema versions
        try
        {
            var conn = db.Database.GetDbConnection();
            if (conn.State != System.Data.ConnectionState.Open)
            {
                conn.Open();
            }

            using var cmd = conn.CreateCommand();
            cmd.CommandText = "PRAGMA table_info(Devices);";
            var existingColumns = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
            using (var reader = cmd.ExecuteReader())
            {
                while (reader.Read())
                {
                    existingColumns.Add(reader.GetString(1));
                }
            }

            void AddColumnIfMissing(string columnName, string columnDef)
            {
                if (!existingColumns.Contains(columnName))
                {
                    using var alterCmd = conn.CreateCommand();
                    alterCmd.CommandText = $"ALTER TABLE Devices ADD COLUMN {columnName} {columnDef};";
                    alterCmd.ExecuteNonQuery();
                    _logger.LogInformation("Added missing column {Column} to Devices table", columnName);
                }
            }

            AddColumnIfMissing("ConnectedViaTunnel", "INTEGER NOT NULL DEFAULT 0");
            AddColumnIfMissing("SessionStartedAt", "TEXT NOT NULL DEFAULT '0001-01-01 00:00:00'");
            AddColumnIfMissing("SessionPrintCount", "INTEGER NOT NULL DEFAULT 0");
            AddColumnIfMissing("LastPrintAt", "TEXT NULL");
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Failed to migrate Devices table columns in SQLite");
        }
    }

    public List<PrintJob> LoadJobs()
    {
        using var db = _contextFactory.CreateDbContext();
        return db.PrintJobs.AsNoTracking().ToList();
    }

    public void UpsertJob(PrintJob job)
    {
        using var db = _contextFactory.CreateDbContext();
        var existing = db.PrintJobs.AsNoTracking().Any(item => item.Id == job.Id);
        db.Entry(CloneJob(job)).State = existing ? EntityState.Modified : EntityState.Added;
        db.SaveChanges();
    }

    public List<DeviceInfo> LoadDevices()
    {
        using var db = _contextFactory.CreateDbContext();
        return db.Devices.AsNoTracking().ToList();
    }

    public void UpsertDevice(DeviceInfo device)
    {
        using var db = _contextFactory.CreateDbContext();
        var existing = db.Devices.AsNoTracking().Any(item => item.Id == device.Id);
        db.Entry(CloneDevice(device)).State = existing ? EntityState.Modified : EntityState.Added;
        db.SaveChanges();
    }

    public void DeleteDevice(string deviceId)
    {
        using var db = _contextFactory.CreateDbContext();
        var device = db.Devices.FirstOrDefault(item => item.Id == deviceId);
        if (device == null)
            return;

        db.Devices.Remove(device);
        db.SaveChanges();
    }

    public void UpsertApproval(PendingApproval approval, string state)
    {
        using var db = _contextFactory.CreateDbContext();
        var existing = db.PendingApprovals.FirstOrDefault(item => item.Id == approval.Id);
        if (existing == null)
        {
            existing = new PendingApprovalRecord { Id = approval.Id };
            db.PendingApprovals.Add(existing);
        }

        existing.DeviceName = approval.DeviceName;
        existing.DeviceModel = approval.DeviceModel;
        existing.IpAddress = approval.IpAddress;
        existing.RequestedAt = approval.RequestedAt;
        existing.State = state;
        db.SaveChanges();
    }

    public void ExpirePendingApprovals()
    {
        using var db = _contextFactory.CreateDbContext();
        var cutoff = DateTime.UtcNow.AddMinutes(-10);
        var expired = db.PendingApprovals
            .Where(item => item.State == "Pending" && item.RequestedAt < cutoff)
            .ToList();

        if (expired.Count == 0)
            return;

        foreach (var record in expired)
            record.State = "Expired";

        db.SaveChanges();
    }

    private static PrintJob CloneJob(PrintJob job) => new()
    {
        Id = job.Id,
        FileName = job.FileName,
        OriginalFileName = job.OriginalFileName,
        FileSize = job.FileSize,
        FileType = job.FileType,
        FilePath = job.FilePath,
        ConvertedFilePath = job.ConvertedFilePath,
        Settings = new PrintSettings
        {
            Copies = job.Settings.Copies,
            PageSize = job.Settings.PageSize,
            Orientation = job.Settings.Orientation,
            ColorMode = job.Settings.ColorMode,
            Duplex = job.Settings.Duplex,
            Quality = job.Settings.Quality,
            PageRange = job.Settings.PageRange,
            SelectedPrinterId = job.Settings.SelectedPrinterId
        },
        PrinterName = job.PrinterName,
        DeviceId = job.DeviceId,
        DeviceName = job.DeviceName,
        Status = job.Status,
        Progress = job.Progress,
        ErrorMessage = job.ErrorMessage,
        CreatedAt = job.CreatedAt,
        UpdatedAt = job.UpdatedAt,
        StartedAt = job.StartedAt,
        CompletedAt = job.CompletedAt,
        RetryCount = job.RetryCount,
        Priority = job.Priority,
        QueueState = job.QueueState,
        FailureCode = job.FailureCode
    };

    private static DeviceInfo CloneDevice(DeviceInfo device) => new()
    {
        Id = device.Id,
        Name = device.Name,
        Model = device.Model,
        IpAddress = device.IpAddress,
        Token = device.Token,
        PairedAt = device.PairedAt,
        LastSeenAt = device.LastSeenAt,
        IsActive = device.IsActive,
        IsBlocked = device.IsBlocked
    };
}
