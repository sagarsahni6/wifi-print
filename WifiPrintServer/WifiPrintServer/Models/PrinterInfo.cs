namespace WifiPrintServer.Models;

/// <summary>
/// Represents a printer installed on the system with its capabilities and health status.
/// </summary>
public class PrinterInfo
{
    public string Id { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public string DriverName { get; set; } = string.Empty;
    public string PortName { get; set; } = string.Empty;
    public bool IsDefault { get; set; }
    public bool IsOnline { get; set; }
    public bool SupportsColor { get; set; }
    public bool SupportsDuplex { get; set; }
    public List<string> SupportedPaperSizes { get; set; } = new();
    public List<string> SupportedQualities { get; set; } = new();
    public PrinterStatus Status { get; set; } = PrinterStatus.Ready;

    // ── Health & Diagnostics ──────────────────────────────────────
    public int? TonerLevelPercent { get; set; }      // null = unknown
    public int? InkLevelBlack { get; set; }
    public int? InkLevelCyan { get; set; }
    public int? InkLevelMagenta { get; set; }
    public int? InkLevelYellow { get; set; }
    public string PaperTrayStatus { get; set; } = "OK";  // OK, Low, Empty, Unknown
    public long TotalPagesPrinted { get; set; }
    public string? LastError { get; set; }
}

public enum PrinterStatus
{
    Ready,
    Busy,
    Offline,
    Error,
    PaperJam,
    OutOfPaper,
    TonerLow
}
