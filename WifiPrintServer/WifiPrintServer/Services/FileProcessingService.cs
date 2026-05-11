using WifiPrintServer.Models;

namespace WifiPrintServer.Services;

/// <summary>
/// Validates uploaded files and converts unsupported formats to PDF.
/// </summary>
public class FileProcessingService
{
    private readonly ILogger<FileProcessingService> _logger;
    private readonly AppSettings _settings;

    private static readonly HashSet<string> SupportedExtensions = new(StringComparer.OrdinalIgnoreCase)
    {
        ".pdf", ".jpg", ".jpeg", ".png", ".bmp", ".gif",
        ".txt", ".text", ".docx", ".doc",
        ".xlsx", ".xls", ".pptx", ".ppt", ".csv", ".rtf", ".odt", ".ods", ".odp"
    };

    private static readonly HashSet<string> ImageExtensions = new(StringComparer.OrdinalIgnoreCase)
    {
        ".jpg", ".jpeg", ".png", ".bmp", ".gif"
    };

    public FileProcessingService(ILogger<FileProcessingService> logger, AppSettings settings)
    {
        _logger = logger;
        _settings = settings;
    }

    public async Task<(string? FilePath, string? Error)> SaveAndValidateFileAsync(
        Stream fileStream, string fileName, long fileSize)
    {
        var ext = Path.GetExtension(fileName);
        if (!SupportedExtensions.Contains(ext))
            return (null, $"Unsupported file type: {ext}");

        long maxBytes = _settings.MaxFileSizeMB * 1024L * 1024L;
        if (fileSize > maxBytes)
            return (null, $"File too large. Max: {_settings.MaxFileSizeMB}MB");

        Directory.CreateDirectory(_settings.UploadDirectory);
        string uniqueName = $"{DateTime.UtcNow:yyyyMMdd_HHmmss}_{Guid.NewGuid().ToString("N")[..6]}_{fileName}";
        string filePath = Path.Combine(_settings.UploadDirectory, uniqueName);

        try
        {
            using var fs = new FileStream(filePath, FileMode.Create);
            await fileStream.CopyToAsync(fs);
            _logger.LogInformation("File saved: {Path} ({Size} bytes)", filePath, fileSize);
            return (filePath, null);
        }
        catch (Exception ex)
        {
            return (null, $"Failed to save file: {ex.Message}");
        }
    }

    public async Task<(string? ConvertedPath, string? Error)> ConvertIfNeededAsync(string filePath)
    {
        var ext = Path.GetExtension(filePath).ToLowerInvariant();
        if (ext == ".pdf" || ImageExtensions.Contains(ext))
            return (filePath, null);

        var libreOfficeExtensions = new HashSet<string> { ".docx", ".doc", ".xlsx", ".xls", ".pptx", ".ppt", ".csv", ".rtf", ".odt", ".ods", ".odp" };

        if (libreOfficeExtensions.Contains(ext))
            return await ConvertWithLibreOfficeAsync(filePath);

        if (ext == ".txt" || ext == ".text")
            return await ConvertTextToPdfAsync(filePath);

        return (null, $"No converter for {ext}");
    }

    private async Task<(string?, string?)> ConvertWithLibreOfficeAsync(string filePath)
    {
        try
        {
            string outputDir = Path.GetDirectoryName(filePath) ?? _settings.UploadDirectory;
            string[] paths = {
                @"C:\Program Files\LibreOffice\program\soffice.exe",
                @"C:\Program Files (x86)\LibreOffice\program\soffice.exe",
                "soffice"
            };
            string? soffice = paths.FirstOrDefault(p => p == "soffice" || File.Exists(p));
            if (soffice == null)
                return (null, "LibreOffice required for document conversion but not found.");

            var psi = new System.Diagnostics.ProcessStartInfo
            {
                FileName = soffice,
                CreateNoWindow = true, UseShellExecute = false,
                RedirectStandardError = true
            };
            psi.ArgumentList.Add("--headless");
            psi.ArgumentList.Add("--convert-to");
            psi.ArgumentList.Add("pdf");
            psi.ArgumentList.Add("--outdir");
            psi.ArgumentList.Add(outputDir);
            psi.ArgumentList.Add(filePath);
            using var proc = System.Diagnostics.Process.Start(psi);
            if (proc == null) return (null, "Failed to start LibreOffice");
            await proc.WaitForExitAsync();

            string pdfPath = Path.ChangeExtension(filePath, ".pdf");
            return File.Exists(pdfPath) ? (pdfPath, null) : (null, "Conversion produced no output");
        }
        catch (Exception ex) { return (null, ex.Message); }
    }

    private async Task<(string?, string?)> ConvertTextToPdfAsync(string filePath)
    {
        try
        {
            string pdfPath = Path.ChangeExtension(filePath, ".pdf");
            string text = await File.ReadAllTextAsync(filePath);
            using var writer = new iText.Kernel.Pdf.PdfWriter(pdfPath);
            using var pdf = new iText.Kernel.Pdf.PdfDocument(writer);
            var doc = new iText.Layout.Document(pdf);
            var font = iText.Kernel.Font.PdfFontFactory.CreateFont(
                iText.IO.Font.Constants.StandardFonts.COURIER);
            doc.Add(new iText.Layout.Element.Paragraph(text).SetFont(font).SetFontSize(10));
            doc.Close();
            return (pdfPath, null);
        }
        catch (Exception ex) { return (null, ex.Message); }
    }

    public static string GetFileType(string fileName)
    {
        var ext = Path.GetExtension(fileName).ToLowerInvariant();
        if (ext == ".pdf") return "PDF";
        if (ImageExtensions.Contains(ext)) return "Image";
        if (new[] { ".docx", ".doc", ".rtf", ".odt" }.Contains(ext)) return "Document";
        if (new[] { ".xlsx", ".xls", ".csv", ".ods" }.Contains(ext)) return "Spreadsheet";
        if (new[] { ".pptx", ".ppt", ".odp" }.Contains(ext)) return "Presentation";
        if (ext is ".txt" or ".text") return "Text";
        return "Unknown";
    }

    public void CleanupOldFiles()
    {
        try
        {
            if (!Directory.Exists(_settings.UploadDirectory)) return;
            var cutoff = DateTime.UtcNow.AddHours(-24);
            foreach (var file in Directory.GetFiles(_settings.UploadDirectory))
                if (File.GetCreationTimeUtc(file) < cutoff) File.Delete(file);
        }
        catch (Exception ex) { _logger.LogWarning(ex, "Cleanup error"); }
    }
}
