package com.wifiprint.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a discovered/saved print server on the network.
 */
@Entity(tableName = "servers")
data class ServerInfo(
    @PrimaryKey val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int,
    val token: String? = null,
    val certificateFingerprint: String? = null,
    val isPaired: Boolean = false,
    val lastConnected: Long = System.currentTimeMillis(),
    val lastAuthCheckAt: Long? = null,
    val connectionHealth: String = "Unknown"
)

/**
 * Represents a printer connected to the server with full capabilities and health info.
 */
data class PrinterInfo(
    val id: String,
    val name: String,
    val driverName: String = "",
    val isDefault: Boolean = false,
    val isOnline: Boolean = true,
    val supportsColor: Boolean = true,
    val supportsDuplex: Boolean = false,
    val supportedPaperSizes: List<String> = listOf("A4", "Letter"),
    val status: String = "Ready",
    // Health & Diagnostics
    val tonerLevelPercent: Int? = null,
    val inkLevelBlack: Int? = null,
    val inkLevelCyan: Int? = null,
    val inkLevelMagenta: Int? = null,
    val inkLevelYellow: Int? = null,
    val paperTrayStatus: String = "OK",
    val totalPagesPrinted: Long = 0,
    val lastError: String? = null
)

/**
 * Print job with status tracking, stored locally for history.
 */
@Entity(tableName = "print_jobs")
data class PrintJob(
    @PrimaryKey val id: String,
    val fileName: String,
    val fileSize: Long,
    val fileType: String,
    val printerName: String,
    val serverName: String,
    val status: String = "Pending",
    val progress: Int = 0,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val priority: String = "Normal" // High, Normal, Low
)

/**
 * User-configurable print settings.
 */
data class PrintSettings(
    val copies: Int = 1,
    val pageSize: String = "A4",
    val orientation: String = "Portrait",
    val colorMode: String = "BlackAndWhite",
    val duplex: Boolean = false,
    val quality: String = "Normal",
    val pageRange: String? = null,  // e.g., "1-5", "1,3,5-8"
    val selectedPrinterId: String? = null
)

/**
 * API wrapper response.
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String = "",
    val data: T? = null,
    val error: String? = null
)

/**
 * Connection request for device approval flow.
 */
data class ConnectionRequest(
    val deviceName: String,
    val deviceModel: String? = android.os.Build.MODEL
)

/**
 * Legacy pairing request (kept for backward compatibility).
 */
data class PairRequest(
    val deviceName: String,
    val pin: String
)

/**
 * Auth response after pairing.
 */
data class AuthResponse(
    val token: String,
    val deviceId: String,
    val serverName: String,
    val expiresAt: String
)

/**
 * Print job creation response.
 */
data class PrintJobResponse(
    val jobId: String,
    val status: String,
    val queuePosition: Int
)

/**
 * Real-time job status update from WebSocket.
 */
data class JobStatusUpdate(
    val jobId: String,
    val status: String,
    val progress: Int,
    val message: String?,
    val queueState: String = "Idle",
    val failureCode: String? = null,
    val failureMessage: String? = null,
    val sourceDeviceId: String = ""
)

/**
 * Server-side print job DTO — matches the C# server's JSON response.
 * The server returns DateTime as ISO strings and has extra fields
 * that don't exist in the local Room entity.
 */
data class ServerPrintJob(
    val id: String,
    val fileName: String = "",
    val fileSize: Long = 0,
    val fileType: String = "",
    val printerName: String = "",
    val status: String = "Pending",
    val progress: Int = 0,
    val errorMessage: String? = null,
    val deviceId: String = "",
    val deviceName: String = "",
    val priority: String = "Normal",
    val updatedAt: String? = null,
    val failureCode: String? = null,
    val failureMessage: String? = null,
    val queueState: String = "Idle",
    val sourceDeviceId: String = "",
    val createdAt: String? = null,   // ISO DateTime string from server
    val completedAt: String? = null  // ISO DateTime string from server
)

/**
 * Page count response from the server.
 */
data class PageCountResponse(
    val pageCount: Int,
    val fileType: String
)

data class ServerStatusResponse(
    val status: String,
    val serverName: String,
    val version: String,
    val timestamp: String,
    val requiresPairing: Boolean,
    val printerAvailable: Boolean,
    val printerCount: Int,
    val readiness: String
)

/**
 * Represents a file selected for batch printing.
 */
data class SelectedFile(
    val uri: android.net.Uri,
    val name: String,
    val size: Long = 0,
    val mimeType: String = ""
)

/**
 * Scanned document page with its bitmap and metadata.
 */
data class ScannedPage(
    val index: Int,
    val imageUri: android.net.Uri,
    val filterApplied: String = "Original" // Original, B&W, Grayscale, Magic
)
