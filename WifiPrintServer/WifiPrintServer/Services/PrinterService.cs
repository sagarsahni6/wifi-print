using System.Drawing;
using System.Drawing.Imaging;
using System.Drawing.Printing;
using System.Printing;
using System.Runtime.InteropServices;
using Docnet.Core;
using Docnet.Core.Models;
using WifiPrintServer.Models;
using PrinterInfo = WifiPrintServer.Models.PrinterInfo;
using PrinterStatus = WifiPrintServer.Models.PrinterStatus;

namespace WifiPrintServer.Services;

/// <summary>
/// Wraps Windows printing APIs to enumerate printers, query capabilities, and send documents.
/// Uses Docnet.Core (PDFium) for reliable PDF rendering and System.Drawing.Printing for job dispatch.
/// </summary>
public class PrinterService
{
    private readonly ILogger<PrinterService> _logger;
    private readonly AppSettings _settings;

    public PrinterService(ILogger<PrinterService> logger, AppSettings settings)
    {
        _logger = logger;
        _settings = settings;
    }

    public List<PrinterInfo> GetAllPrinters()
    {
        var printers = new List<PrinterInfo>();
        try
        {
            using var printServer = new LocalPrintServer();
            var queues = printServer.GetPrintQueues(
                new[] { EnumeratedPrintQueueTypes.Local, EnumeratedPrintQueueTypes.Connections });
            string osPrinterName = new PrinterSettings().PrinterName;

            foreach (var queue in queues)
            {
                try
                {
                    // Mark as default if it matches the user-configured default,
                    // or fall back to OS default if none configured
                    bool isDefault = !string.IsNullOrEmpty(_settings.DefaultPrinter)
                        ? queue.Name == _settings.DefaultPrinter
                        : queue.Name == osPrinterName;

                    var printer = new PrinterInfo
                    {
                        Id = Convert.ToBase64String(
                            System.Text.Encoding.UTF8.GetBytes(queue.FullName)).Replace("=", ""),
                        Name = queue.Name,
                        DriverName = queue.QueueDriver?.Name ?? "Unknown",
                        PortName = queue.QueuePort?.Name ?? "Unknown",
                        IsDefault = isDefault,
                        IsOnline = !queue.IsOffline,
                        SupportsColor = queue.DefaultPrintTicket?.OutputColor != OutputColor.Monochrome,
                        SupportsDuplex = queue.GetPrintCapabilities()?.DuplexingCapability?.Count > 1,
                        SupportedPaperSizes = GetPaperSizes(queue),
                        SupportedQualities = new List<string> { "Draft", "Normal", "High" },
                        Status = MapQueueStatus(queue)
                    };
                    printers.Add(printer);
                }
                catch (Exception ex)
                {
                    _logger.LogWarning(ex, "Failed to query printer: {Name}", queue.Name);
                }
                finally { queue.Dispose(); }
            }
        }
        catch (Exception ex) { _logger.LogError(ex, "Failed to enumerate printers"); }
        return printers;
    }

    public PrinterInfo? GetPrinter(string printerId) =>
        GetAllPrinters().FirstOrDefault(p => p.Id == printerId);

    /// <summary>
    /// Returns the user-configured default printer, or falls back to OS default.
    /// </summary>
    public PrinterInfo? GetDefaultPrinter() =>
        GetAllPrinters().FirstOrDefault(p => p.IsDefault);

    /// <summary>
    /// Set a printer as the default for WiFi Print jobs.
    /// </summary>
    public void SetDefaultPrinter(string printerName)
    {
        _settings.DefaultPrinter = printerName;
        _settings.Save();
        _logger.LogInformation("Default printer set to: {Printer}", printerName);
    }

    /// <summary>
    /// Prints a file natively — no external apps (Adobe) are launched.
    /// PDFs are rendered via PDFium, images/text are handled directly.
    /// </summary>
    public async Task<PrintExecutionResult> PrintFileAsync(string filePath, string printerName, PrintSettings settings,
        Action<int>? progressCallback = null, CancellationToken ct = default)
    {
        var printer = GetAllPrinters().FirstOrDefault(item => item.Name == printerName);
        if (printer == null)
            return PrintExecutionResult.Fail("printer_not_found", $"Printer '{printerName}' is not installed");

        if (!printer.IsOnline)
            return PrintExecutionResult.Fail("printer_offline", $"Printer '{printerName}' is offline");

        var settingsValidationError = ValidatePrinterSettings(printer, settings);
        if (settingsValidationError != null)
            return settingsValidationError;

        return await Task.Run(() =>
        {
            _logger.LogInformation("Printing {File} to {Printer}", filePath, printerName);
            progressCallback?.Invoke(10);
            ct.ThrowIfCancellationRequested();

            var ext = Path.GetExtension(filePath).ToLowerInvariant();
            var result = ext switch
            {
                ".jpg" or ".jpeg" or ".png" or ".bmp" or ".gif"
                    => PrintImage(filePath, printerName, settings, progressCallback, ct),
                ".pdf"
                    => PrintPdf(filePath, printerName, settings, progressCallback, ct),
                ".txt" or ".text"
                    => PrintTextFile(filePath, printerName, settings, progressCallback, ct),
                _ => throw new NotSupportedException($"Unsupported file type: {ext}")
            };

            if (result.Success)
                _logger.LogInformation("Print job sent successfully to {Printer}", printerName);

            return result;
        }, ct);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Image Printing
    // ═══════════════════════════════════════════════════════════════

    private PrintExecutionResult PrintImage(string filePath, string printerName, PrintSettings settings,
        Action<int>? progressCallback, CancellationToken ct)
    {
        ct.ThrowIfCancellationRequested();
        progressCallback?.Invoke(20);
        using var image = Image.FromFile(filePath);

        var printDoc = CreatePrintDocument(printerName, filePath, settings);
        progressCallback?.Invoke(40);

        printDoc.PrintPage += (_, e) =>
        {
            if (e.Graphics == null) return;
            DrawImageFit(e.Graphics, image, e.PageBounds);
            e.HasMorePages = false;
        };

        ct.ThrowIfCancellationRequested();
        printDoc.Print();
        progressCallback?.Invoke(100);
        return PrintExecutionResult.Ok();
    }

    // ═══════════════════════════════════════════════════════════════
    //  PDF Printing — uses Docnet.Core (PDFium) for rendering
    // ═══════════════════════════════════════════════════════════════

    private PrintExecutionResult PrintPdf(string filePath, string printerName, PrintSettings settings,
        Action<int>? progressCallback, CancellationToken ct)
    {
        ct.ThrowIfCancellationRequested();
        progressCallback?.Invoke(20);

        // Render PDF pages to bitmaps using PDFium
        var pageImages = RenderPdfPages(filePath, settings, ct);
        if (pageImages.Count == 0)
        {
            _logger.LogWarning("No pages rendered from PDF");
            return PrintExecutionResult.Fail("page_range_empty", "No pages were selected for printing");
        }

        _logger.LogInformation("Rendered {Count} PDF pages to images", pageImages.Count);
        progressCallback?.Invoke(40);

        try
        {
            var printDoc = CreatePrintDocument(printerName, filePath, settings);
            int currentPage = 0;
            int totalPages = pageImages.Count;

            printDoc.PrintPage += (_, e) =>
            {
                if (e.Graphics == null) return;
                DrawImageFit(e.Graphics, pageImages[currentPage], e.PageBounds);
                currentPage++;
                e.HasMorePages = currentPage < totalPages;
                progressCallback?.Invoke(40 + (int)(55.0 * currentPage / totalPages));
            };

            ct.ThrowIfCancellationRequested();
            printDoc.Print();
            progressCallback?.Invoke(100);
            return PrintExecutionResult.Ok();
        }
        finally
        {
            foreach (var img in pageImages) img.Dispose();
        }
    }

    /// <summary>
    /// Gets the page count of a PDF file without rendering.
    /// </summary>
    public int GetPdfPageCount(string filePath)
    {
        try
        {
            using var docReader = DocLib.Instance.GetDocReader(
                filePath,
                new PageDimensions(100, 100)); // minimal dimensions for counting
            return docReader.GetPageCount();
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Failed to get page count for {File}", filePath);
            return 0;
        }
    }

    /// <summary>
    /// Parses a complex page range string like "1,3,5-8,12" into a sorted list of 0-indexed page numbers.
    /// Supports: single pages ("3"), ranges ("1-5"), comma-separated ("1,3,5"), and mixed ("1,3-5,8").
    /// </summary>
    public static List<int> ParsePageRange(string pageRange, int totalPages)
    {
        var pages = new HashSet<int>();
        var segments = pageRange.Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries);

        foreach (var segment in segments)
        {
            if (segment.Contains('-'))
            {
                var parts = segment.Split('-', 2);
                if (int.TryParse(parts[0].Trim(), out int rs) &&
                    int.TryParse(parts[1].Trim(), out int re))
                {
                    int start = Math.Max(0, rs - 1);
                    int end = Math.Min(totalPages - 1, re - 1);
                    for (int i = start; i <= end; i++)
                        pages.Add(i);
                }
            }
            else if (int.TryParse(segment.Trim(), out int single))
            {
                int idx = single - 1;
                if (idx >= 0 && idx < totalPages)
                    pages.Add(idx);
            }
        }

        return pages.OrderBy(p => p).ToList();
    }

    /// <summary>
    /// Renders each PDF page to a high-resolution Bitmap using PDFium via Docnet.Core.
    /// </summary>
    private List<Bitmap> RenderPdfPages(string filePath, PrintSettings settings, CancellationToken ct)
    {
        var bitmaps = new List<Bitmap>();

        // Render at 300 DPI for sharp print output
        // A4 at 300 DPI = 2480 x 3508 pixels
        int renderWidth = 2480;
        int renderHeight = 3508;

        using var docReader = DocLib.Instance.GetDocReader(
            filePath,
            new PageDimensions(renderWidth, renderHeight));

        int pageCount = docReader.GetPageCount();

        // Parse page range — supports "1,3,5-8,12" format
        List<int> pagesToRender;
        if (!string.IsNullOrEmpty(settings.PageRange))
        {
            pagesToRender = ParsePageRange(settings.PageRange, pageCount);
            _logger.LogInformation("PDF has {Total} pages, rendering selected pages: {Pages}",
                pageCount, string.Join(",", pagesToRender.Select(p => p + 1)));
        }
        else
        {
            pagesToRender = Enumerable.Range(0, pageCount).ToList();
            _logger.LogInformation("PDF has {Total} pages, rendering all", pageCount);
        }

        foreach (int i in pagesToRender)
        {
            try
            {
                ct.ThrowIfCancellationRequested();
                using var pageReader = docReader.GetPageReader(i);
                var rawBytes = pageReader.GetImage();
                int w = pageReader.GetPageWidth();
                int h = pageReader.GetPageHeight();

                if (rawBytes == null || rawBytes.Length == 0 || w == 0 || h == 0)
                {
                    _logger.LogWarning("Page {Page} returned empty image", i + 1);
                    continue;
                }

                // PDFium returns BGRA pixel data — convert to Bitmap
                var bitmap = new Bitmap(w, h, PixelFormat.Format32bppArgb);
                var bmpData = bitmap.LockBits(
                    new Rectangle(0, 0, w, h),
                    ImageLockMode.WriteOnly,
                    PixelFormat.Format32bppArgb);

                Marshal.Copy(rawBytes, 0, bmpData.Scan0, rawBytes.Length);
                bitmap.UnlockBits(bmpData);

                // Set DPI for proper print scaling
                bitmap.SetResolution(300f, 300f);

                bitmaps.Add(bitmap);
                _logger.LogDebug("Rendered page {Page}: {W}x{H}", i + 1, w, h);
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "Failed to render page {Page}", i + 1);
            }
        }

        return bitmaps;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Text File Printing
    // ═══════════════════════════════════════════════════════════════

    private PrintExecutionResult PrintTextFile(string filePath, string printerName, PrintSettings settings,
        Action<int>? progressCallback, CancellationToken ct)
    {
        ct.ThrowIfCancellationRequested();
        progressCallback?.Invoke(20);
        var lines = File.ReadAllLines(filePath);

        var printDoc = CreatePrintDocument(printerName, filePath, settings);
        progressCallback?.Invoke(40);

        int lineIndex = 0;
        printDoc.PrintPage += (_, e) =>
        {
            if (e.Graphics == null) return;
            using var font = new Font("Consolas", 10f);
            float lineHeight = font.GetHeight(e.Graphics);
            float y = e.MarginBounds.Top;

            while (lineIndex < lines.Length && y + lineHeight < e.MarginBounds.Bottom)
            {
                ct.ThrowIfCancellationRequested();
                e.Graphics.DrawString(lines[lineIndex], font, Brushes.Black, e.MarginBounds.Left, y);
                y += lineHeight;
                lineIndex++;
            }
            e.HasMorePages = lineIndex < lines.Length;
        };

        ct.ThrowIfCancellationRequested();
        printDoc.Print();
        progressCallback?.Invoke(100);
        return PrintExecutionResult.Ok();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════

    /// <summary>
    /// Creates a configured PrintDocument with the user's print settings applied.
    /// Uses defensive coding because some HP (and other) printer drivers throw
    /// "Settings to access printer are not valid" when certain page settings
    /// are modified on their default PrinterSettings object.
    /// </summary>
    private PrintDocument CreatePrintDocument(string printerName, string filePath, PrintSettings settings)
    {
        var printDoc = new PrintDocument
        {
            DocumentName = Path.GetFileName(filePath)
        };
        printDoc.PrinterSettings.PrinterName = printerName;

        // Validate the printer is installed and accessible
        if (!printDoc.PrinterSettings.IsValid)
        {
            _logger.LogWarning("Printer '{Printer}' is not valid/installed, falling back to default", printerName);
            printDoc.PrinterSettings.PrinterName = new PrinterSettings().PrinterName;
        }

        // Set margins to zero so content fills the entire page
        // The printer hardware will still enforce its minimum non-printable border
        try
        {
            printDoc.DefaultPageSettings.Margins = new Margins(0, 0, 0, 0);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Failed to set margins for {Printer}", printerName);
        }

        // Apply copies — safe on all printers
        try
        {
            printDoc.PrinterSettings.Copies = (short)Math.Max(1, settings.Copies);
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Failed to set copies for {Printer}", printerName);
        }

        // Apply orientation — some drivers reject this on DefaultPageSettings
        try
        {
            if (settings.Orientation == "Landscape")
                printDoc.DefaultPageSettings.Landscape = true;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Failed to set landscape for {Printer}", printerName);
        }

        // Apply color mode — only if the printer supports color
        // HP LaserJet M1136 MFP is mono-only and crashes when Color is set
        try
        {
            if (settings.ColorMode == "BlackAndWhite" && printDoc.PrinterSettings.SupportsColor)
                printDoc.DefaultPageSettings.Color = false;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Failed to set color mode for {Printer}", printerName);
        }

        return printDoc;
    }

    /// <summary>
    /// Draws an image scaled to fill the entire page bounds.
    /// Simply stretches/scales the rendered image to match the full printable area.
    /// This is the simplest and most reliable approach — the PDF is already
    /// rendered at the correct aspect ratio, so stretching to fill looks correct.
    /// </summary>
    private static void DrawImageFit(Graphics g, Image image, Rectangle bounds)
    {
        g.InterpolationMode = System.Drawing.Drawing2D.InterpolationMode.HighQualityBicubic;
        // Draw the image to fill the entire bounds (full page)
        g.DrawImage(image, bounds.Left, bounds.Top, bounds.Width, bounds.Height);
    }

    private static PrinterStatus MapQueueStatus(PrintQueue queue)
    {
        if (queue.IsOffline) return PrinterStatus.Offline;
        if (queue.IsPaperJammed) return PrinterStatus.PaperJam;
        if (queue.IsOutOfPaper) return PrinterStatus.OutOfPaper;
        if (queue.HasToner == false) return PrinterStatus.TonerLow;
        if (queue.IsBusy || queue.IsPrinting) return PrinterStatus.Busy;
        return PrinterStatus.Ready;
    }

    private List<string> GetPaperSizes(PrintQueue queue)
    {
        var sizes = new List<string>();
        try
        {
            var caps = queue.GetPrintCapabilities();
            if (caps?.PageMediaSizeCapability != null)
                foreach (var size in caps.PageMediaSizeCapability)
                {
                    var name = size.PageMediaSizeName?.ToString() ?? "Unknown";
                    if (!sizes.Contains(name)) sizes.Add(name);
                }
        }
        catch { sizes.AddRange(new[] { "A4", "Letter", "Legal", "A3" }); }

        if (sizes.Count == 0)
            sizes.AddRange(new[] { "A4", "Letter", "Legal", "A3" });
        return sizes;
    }

    /// <summary>
    /// Validates and auto-corrects print settings. Returns a failure only for truly fatal mismatches.
    /// Auto-downgrades color→B&W and duplex→simplex when the printer doesn't support them.
    /// </summary>
    private PrintExecutionResult? ValidatePrinterSettings(PrinterInfo printer, PrintSettings settings)
    {
        // Auto-fallback: if printer is mono-only but client requested color, silently downgrade
        if (settings.ColorMode == "Color" && !printer.SupportsColor)
        {
            _logger.LogInformation("Printer '{Printer}' is mono-only — auto-downgrading to BlackAndWhite", printer.Name);
            settings.ColorMode = "BlackAndWhite";
        }

        // Auto-fallback: if printer doesn't support duplex, silently disable
        if (settings.Duplex && !printer.SupportsDuplex)
        {
            _logger.LogInformation("Printer '{Printer}' doesn't support duplex — disabling", printer.Name);
            settings.Duplex = false;
        }

        // Paper size: try to use the requested one, fall back to first supported
        if (!printer.SupportedPaperSizes.Contains(settings.PageSize, StringComparer.OrdinalIgnoreCase))
        {
            var fallback = printer.SupportedPaperSizes.FirstOrDefault() ?? "A4";
            _logger.LogInformation("Printer '{Printer}' doesn't support '{Size}' — using '{Fallback}'",
                printer.Name, settings.PageSize, fallback);
            settings.PageSize = fallback;
        }

        return null;
    }
}

public sealed class PrintExecutionResult
{
    public bool Success { get; init; }
    public string? FailureCode { get; init; }
    public string? ErrorMessage { get; init; }

    public static PrintExecutionResult Ok() => new() { Success = true };

    public static PrintExecutionResult Fail(string code, string message) => new()
    {
        Success = false,
        FailureCode = code,
        ErrorMessage = message
    };
}
