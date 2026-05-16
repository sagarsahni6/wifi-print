using System.Text.Json;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;
using WifiPrintServer.Models;

namespace WifiPrintServer.Data;

public class ServerStateContext : DbContext
{
    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        PropertyNameCaseInsensitive = true
    };

    public ServerStateContext(DbContextOptions<ServerStateContext> options)
        : base(options)
    {
    }

    public DbSet<PrintJob> PrintJobs => Set<PrintJob>();
    public DbSet<DeviceInfo> Devices => Set<DeviceInfo>();
    public DbSet<PendingApprovalRecord> PendingApprovals => Set<PendingApprovalRecord>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        var settingsConverter = new ValueConverter<PrintSettings, string>(
            value => JsonSerializer.Serialize(value, JsonOptions),
            value => string.IsNullOrWhiteSpace(value)
                ? new PrintSettings()
                : JsonSerializer.Deserialize<PrintSettings>(value, JsonOptions) ?? new PrintSettings());

        modelBuilder.Entity<PrintJob>(entity =>
        {
            entity.HasKey(job => job.Id);
            entity.Property(job => job.Status).HasConversion<string>();
            entity.Property(job => job.Settings).HasConversion(settingsConverter);
            entity.Ignore(job => job.SourceDeviceId);
            entity.Ignore(job => job.FailureMessage);
        });

        modelBuilder.Entity<DeviceInfo>(entity =>
        {
            entity.HasKey(device => device.Id);
        });

        modelBuilder.Entity<PendingApprovalRecord>(entity =>
        {
            entity.HasKey(record => record.Id);
        });
    }
}

public class PendingApprovalRecord
{
    public string Id { get; set; } = string.Empty;
    public string DeviceName { get; set; } = string.Empty;
    public string DeviceModel { get; set; } = string.Empty;
    public string IpAddress { get; set; } = string.Empty;
    public DateTime RequestedAt { get; set; }
    public string State { get; set; } = "Pending";
}
