namespace WifiPrintServer.Models;

/// <summary>
/// Represents print configuration options selected by the user.
/// </summary>
public class PrintSettings
{
    public int Copies { get; set; } = 1;
    public string PageSize { get; set; } = "A4";        // A4, Letter, Legal, A3
    public string Orientation { get; set; } = "Portrait"; // Portrait, Landscape
    public string ColorMode { get; set; } = "Color";     // Color, BlackAndWhite
    public bool Duplex { get; set; } = false;
    public string Quality { get; set; } = "Normal";       // Draft, Normal, High
    public string? PageRange { get; set; }                 // e.g., "1-5", "1,3,5"
    public string? SelectedPrinterId { get; set; }
}
