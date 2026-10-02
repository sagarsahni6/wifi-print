using System.IO;
using System.Security.Cryptography.X509Certificates;
using System.Text.Json;
using System.Windows.Media.Imaging;
using QRCoder;

namespace WifiPrintServer.Services;

/// <summary>
/// Generates QR codes containing server connection info
/// so Android clients can scan to connect instantly.
/// When cloud relay is active, embeds the tunnel URL for "print from anywhere".
/// </summary>
public class QrCodeService
{
    /// <summary>
    /// Generates a QR code BitmapImage containing the server connection payload.
    /// The payload includes IP, port, server name, certificate fingerprint,
    /// and optionally a tunnel URL for remote printing.
    /// </summary>
    public static BitmapImage GenerateConnectionQrCode(
        string ipAddress,
        int port,
        string serverName,
        X509Certificate2? certificate,
        string? qrToken = null,
        string? tunnelUrl = null)
    {
        var payload = BuildPayloadJson(ipAddress, port, serverName, certificate, qrToken, tunnelUrl);

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
        X509Certificate2? certificate,
        string? qrToken = null,
        string? tunnelUrl = null)
    {
        return BuildPayloadJson(ipAddress, port, serverName, certificate, qrToken, tunnelUrl);
    }

    /// <summary>
    /// Builds the JSON payload string with server info and optional tunnel URL.
    /// When a tunnel URL is present, the phone connects via the public URL instead of local IP.
    /// </summary>
    private static string BuildPayloadJson(
        string ipAddress,
        int port,
        string serverName,
        X509Certificate2? certificate,
        string? qrToken,
        string? tunnelUrl)
    {
        var fingerprint = certificate != null
            ? BitConverter.ToString(certificate.GetCertHash()).Replace("-", ":")
            : "";

        bool hasTunnel = !string.IsNullOrWhiteSpace(tunnelUrl);

        if (hasTunnel)
        {
            return JsonSerializer.Serialize(new
            {
                ip = ipAddress,
                port = port,
                name = serverName,
                cert = fingerprint,
                token = qrToken ?? "",
                tunnel = tunnelUrl!.Trim()
            });
        }

        return JsonSerializer.Serialize(new
        {
            ip = ipAddress,
            port = port,
            name = serverName,
            cert = fingerprint,
            token = qrToken ?? ""
        });
    }
}
