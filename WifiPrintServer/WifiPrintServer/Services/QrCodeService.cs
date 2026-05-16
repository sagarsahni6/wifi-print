using System.IO;
using System.Security.Cryptography.X509Certificates;
using System.Text.Json;
using System.Windows.Media.Imaging;
using QRCoder;

namespace WifiPrintServer.Services;

/// <summary>
/// Generates QR codes containing server connection info
/// so Android clients can scan to connect instantly.
/// </summary>
public class QrCodeService
{
    /// <summary>
    /// Generates a QR code BitmapImage containing the server connection payload.
    /// The payload includes IP, port, server name, and certificate fingerprint
    /// for secure trust-on-first-use.
    /// </summary>
    public static BitmapImage GenerateConnectionQrCode(
        string ipAddress,
        int port,
        string serverName,
        X509Certificate2? certificate)
    {
        var fingerprint = certificate != null
            ? BitConverter.ToString(certificate.GetCertHash()).Replace("-", ":")
            : "";

        var payload = JsonSerializer.Serialize(new
        {
            ip = ipAddress,
            port = port,
            name = serverName,
            cert = fingerprint
        });

        using var qrGenerator = new QRCodeGenerator();
        var qrCodeData = qrGenerator.CreateQrCode(payload, QRCodeGenerator.ECCLevel.M);
        using var qrCode = new PngByteQRCode(qrCodeData);
        var pngBytes = qrCode.GetGraphic(8);

        var bitmap = new BitmapImage();
        bitmap.BeginInit();
        bitmap.StreamSource = new MemoryStream(pngBytes);
        bitmap.CacheOption = BitmapCacheOption.OnLoad;
        bitmap.EndInit();
        bitmap.Freeze(); // Make it thread-safe for WPF

        return bitmap;
    }

    /// <summary>
    /// Returns the connection payload as a JSON string (for the REST endpoint).
    /// </summary>
    public static string GetConnectionPayloadJson(
        string ipAddress,
        int port,
        string serverName,
        X509Certificate2? certificate)
    {
        var fingerprint = certificate != null
            ? BitConverter.ToString(certificate.GetCertHash()).Replace("-", ":")
            : "";

        return JsonSerializer.Serialize(new
        {
            ip = ipAddress,
            port = port,
            name = serverName,
            cert = fingerprint
        });
    }
}
