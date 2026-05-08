using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace WifiPrintServer.Models;

/// <summary>
/// Represents a print job in the system with full lifecycle tracking.
/// </summary>
public class PrintJob : INotifyPropertyChanged
{
    private PrintJobStatus _status;
    private int _progress;
    private string? _errorMessage;

    public string Id { get; set; } = Guid.NewGuid().ToString("N")[..12].ToUpper();
    public string FileName { get; set; } = string.Empty;
    public string OriginalFileName { get; set; } = string.Empty;
    public long FileSize { get; set; }
    public string FileType { get; set; } = string.Empty;
    public string FilePath { get; set; } = string.Empty;
    public string? ConvertedFilePath { get; set; }

    public PrintSettings Settings { get; set; } = new();
    public string PrinterName { get; set; } = string.Empty;
    public string DeviceId { get; set; } = string.Empty;
    public string DeviceName { get; set; } = string.Empty;

    public PrintJobStatus Status
    {
        get => _status;
        set { _status = value; OnPropertyChanged(); }
    }

    public int Progress
    {
        get => _progress;
        set { _progress = value; OnPropertyChanged(); }
    }

    public string? ErrorMessage
    {
        get => _errorMessage;
        set { _errorMessage = value; OnPropertyChanged(); }
    }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? StartedAt { get; set; }
    public DateTime? CompletedAt { get; set; }
    public int RetryCount { get; set; }
    public string Priority { get; set; } = "Normal"; // High, Normal, Low

    public event PropertyChangedEventHandler? PropertyChanged;

    protected void OnPropertyChanged([CallerMemberName] string? name = null)
    {
        PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(name));
    }
}

/// <summary>
/// Lifecycle status of a print job.
/// </summary>
public enum PrintJobStatus
{
    Pending,
    Validating,
    Converting,
    Queued,
    Printing,
    Completed,
    Failed,
    Cancelled,
    Paused
}
