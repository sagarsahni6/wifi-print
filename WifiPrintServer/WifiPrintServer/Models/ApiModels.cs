namespace WifiPrintServer.Models;

/// <summary>
/// DTO for API responses.
/// </summary>
public class ApiResponse<T>
{
    public bool Success { get; set; }
    public string Message { get; set; } = string.Empty;
    public T? Data { get; set; }
    public string? Error { get; set; }

    public static ApiResponse<T> Ok(T data, string message = "Success") => new()
    {
        Success = true,
        Message = message,
        Data = data
    };

    public static ApiResponse<T> Fail(string error) => new()
    {
        Success = false,
        Error = error
    };

    public static ApiResponse<T> Fail(string error, T data) => new()
    {
        Success = false,
        Error = error,
        Data = data
    };
}

/// <summary>
/// Result of PDF inspection and password validation.
/// </summary>
public class PdfValidationResult
{
    public bool IsValid { get; set; }
    public int PageCount { get; set; }
    public bool IsEncrypted { get; set; }
    public bool RequiresPassword { get; set; }
    public string? ErrorMessage { get; set; }
}

/// <summary>
/// Pairing request from Android client.
/// </summary>
public class PairRequest
{
    public string DeviceName { get; set; } = string.Empty;
    public string Pin { get; set; } = string.Empty;
}

/// <summary>
/// Token response after successful pairing.
/// </summary>
public class AuthResponse
{
    public string Token { get; set; } = string.Empty;
    public string DeviceId { get; set; } = string.Empty;
    public string ServerName { get; set; } = string.Empty;
    public DateTime ExpiresAt { get; set; }
}

/// <summary>
/// Print job submission response.
/// </summary>
public class PrintJobResponse
{
    public string JobId { get; set; } = string.Empty;
    public string Status { get; set; } = string.Empty;
    public int QueuePosition { get; set; }
}

/// <summary>
/// Real-time job status update sent via WebSocket.
/// </summary>
public class JobStatusUpdate
{
    public string JobId { get; set; } = string.Empty;
    public string Status { get; set; } = string.Empty;
    public int Progress { get; set; }
    public string? Message { get; set; }
    public string QueueState { get; set; } = "Idle";
    public string? FailureCode { get; set; }
    public string? FailureMessage { get; set; }
    public string SourceDeviceId { get; set; } = string.Empty;
    public DateTime Timestamp { get; set; } = DateTime.UtcNow;
}

public class ServerStatusResponse
{
    public string Status { get; set; } = "Online";
    public string ServerName { get; set; } = Environment.MachineName;
    public string Version { get; set; } = "1.0.0";
    public DateTime Timestamp { get; set; } = DateTime.UtcNow;
    public bool RequiresPairing { get; set; } = true;
    public bool PrinterAvailable { get; set; }
    public int PrinterCount { get; set; }
    public string Readiness { get; set; } = "Ready";
    public bool IsSameNetwork { get; set; } = true;
    public bool RequiresQrCode { get; set; } = false;
    public bool RequiresPin { get; set; } = false;
}
