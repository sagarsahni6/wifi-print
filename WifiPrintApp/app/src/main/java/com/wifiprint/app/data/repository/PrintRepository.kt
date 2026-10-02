package com.wifiprint.app.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.gson.Gson
import org.json.JSONObject
import com.wifiprint.app.data.api.ContentUriRequestBody
import com.wifiprint.app.data.api.PrintApiService
import com.wifiprint.app.data.db.PrintJobDao
import com.wifiprint.app.data.db.ServerDao
import com.wifiprint.app.data.models.ApiResponse
import com.wifiprint.app.data.models.AuthResponse
import com.wifiprint.app.data.models.ConnectionRequest
import com.wifiprint.app.data.models.PageCountResponse
import com.wifiprint.app.data.models.PairRequest
import com.wifiprint.app.data.models.PrintJob
import com.wifiprint.app.data.models.PrintJobResponse
import com.wifiprint.app.data.models.PrintSettings
import com.wifiprint.app.data.models.PrinterInfo
import com.wifiprint.app.data.models.ServerInfo
import com.wifiprint.app.data.models.ServerPrintJob
import com.wifiprint.app.network.CertificatePinning
import com.wifiprint.app.network.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central repository coordinating between API, local database, and UI layer.
 * Handles dynamic base URL switching, per-server certificate trust, and local state sync.
 */
@Singleton
class PrintRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val printJobDao: PrintJobDao,
    private val serverDao: ServerDao,
    private val okHttpClient: OkHttpClient
) {
    private val gson = Gson()

    private var currentBaseUrl: String = ""
    private var currentToken: String? = null
    private var currentServer: ServerInfo? = null
    private var currentApiService: PrintApiService? = null
    private var observedServerFingerprint: String? = null

    /**
     * Determines whether the given URL is a Cloudflare Quick Tunnel or remote domain.
     * Tunnel URLs use public CA-signed SSL certificates, unlike local IP self-signed certs.
     */
    private fun isTunnelUrl(url: String): Boolean {
        return url.contains(".trycloudflare.com", ignoreCase = true) ||
               url.contains("cloudflare", ignoreCase = true) ||
               !url.matches(Regex("https?://\\d+\\.\\d+\\.\\d+\\.\\d+.*"))
    }

    /** Connect to a specific server and configure the API client. */
    fun connectToServer(server: ServerInfo) {
        currentServer = server
        val isTunnel = !server.tunnelUrl.isNullOrBlank()
        val normalizedTunnel = server.tunnelUrl?.trim()?.let {
            if (it.endsWith("/")) it else "$it/"
        }
        val isSameSubnet = !server.ipAddress.isNullOrBlank() && NetworkMonitor(context).isSameLocalSubnet(server.ipAddress)

        currentBaseUrl = if (isTunnel && !isSameSubnet) {
            normalizedTunnel!!
        } else if (!server.ipAddress.isNullOrBlank()) {
            "https://${server.ipAddress}:${server.port}/"
        } else if (isTunnel) {
            normalizedTunnel!!
        } else {
            "https://localhost:${server.port}/"
        }

        currentToken = server.token
        observedServerFingerprint = null
        val useTunnelTls = isTunnelUrl(currentBaseUrl)
        currentApiService = createApiService(
            baseUrl = currentBaseUrl,
            token = server.token,
            pinnedFingerprint = if (useTunnelTls) null else server.certificateFingerprint,
            allowTrustOnFirstUse = useTunnelTls || server.certificateFingerprint.isNullOrBlank()
        ) { fingerprint ->
            observedServerFingerprint = fingerprint
        }
    }

    /**
     * Ensures we are connected to a server before making API calls.
     * Auto-reconnects to the last paired server if not currently connected.
     */
    private suspend fun ensureConnected(): PrintApiService {
        currentApiService?.let { return it }

        val server = serverDao.getLastPairedServer()
            ?: throw IllegalStateException("No paired server found. Please connect to a server first.")

        connectToServer(server)
        return currentApiService
            ?: throw IllegalStateException("Failed to connect to server ${server.name}")
    }

    // —— Authentication ————————————————————————————————————————————————

    /**
     * Request connection approval from the server.
     * Supports both local Wi-Fi connections and remote Cloudflare Quick Tunnel URLs.
     */
    suspend fun requestConnectionApproval(
        serverIp: String,
        port: Int,
        deviceName: String,
        qrToken: String? = null,
        pin: String? = null,
        tunnelUrl: String? = null
    ): Result<AuthResponse> {
        val isSameNet = serverIp.isNotBlank() && NetworkMonitor(context).isSameLocalSubnet(serverIp)
        val hasTunnel = !tunnelUrl.isNullOrBlank()
        val normalizedTunnel = tunnelUrl?.trim()?.let { if (it.endsWith("/")) it else "$it/" }

        // Build list of target URLs to try:
        // If not on same subnet and tunnel is available, prioritize the tunnel URL.
        // Otherwise try local IP first, and fallback to tunnel if unreachable.
        val urlsToTry = mutableListOf<String>()
        if (hasTunnel && !isSameNet) {
            urlsToTry.add(normalizedTunnel!!)
            if (serverIp.isNotBlank()) urlsToTry.add("https://$serverIp:$port/")
        } else {
            if (serverIp.isNotBlank()) urlsToTry.add("https://$serverIp:$port/")
            if (hasTunnel) urlsToTry.add(normalizedTunnel!!)
        }

        var lastException: Exception? = null

        for (targetBaseUrl in urlsToTry) {
            var capturedFingerprint: String? = null
            val isTunnelTarget = isTunnelUrl(targetBaseUrl)

            try {
                val tempApi = createApiService(
                    baseUrl = targetBaseUrl,
                    token = null,
                    pinnedFingerprint = null,
                    allowTrustOnFirstUse = true,
                    readTimeoutSeconds = 90,
                    onCertificateSeen = { fingerprint -> capturedFingerprint = fingerprint }
                )

                val request = ConnectionRequest(
                    deviceName = deviceName,
                    deviceModel = android.os.Build.MODEL,
                    qrToken = qrToken,
                    pin = pin,
                    isSameNetwork = isSameNet && !isTunnelTarget
                )

                val response = tempApi.requestConnection(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val authData = response.body()!!.data!!
                    val fingerprint = capturedFingerprint ?: ""

                    val server = ServerInfo(
                        id = if (isTunnelTarget) (tunnelUrl ?: "$serverIp:$port") else "$serverIp:$port",
                        name = authData.serverName,
                        ipAddress = serverIp,
                        port = port,
                        token = authData.token,
                        certificateFingerprint = fingerprint.ifBlank { null },
                        isPaired = true,
                        lastConnected = System.currentTimeMillis(),
                        lastAuthCheckAt = System.currentTimeMillis(),
                        connectionHealth = "Healthy",
                        tunnelUrl = tunnelUrl
                    ).apply {
                        isSameNetwork = isSameNet && !isTunnelTarget
                    }
                    serverDao.insertServer(server)
                    connectToServer(server)

                    return Result.success(authData)
                } else {
                    return Result.failure(apiFailure(response, "Connection failed"))
                }
            } catch (e: SocketTimeoutException) {
                lastException = Exception("Request timed out — no response from PC. Please try again.")
            } catch (e: Exception) {
                lastException = e
            }
        }

        return Result.failure(lastException ?: Exception("Connection failed"))
    }

    /** Legacy: Pair with server using a PIN code. */
    suspend fun pairWithServer(
        serverIp: String,
        port: Int,
        pin: String,
        deviceName: String
    ): Result<AuthResponse> {
        var capturedFingerprint: String? = null

        return try {
            val tempApi = createApiService(
                baseUrl = "https://$serverIp:$port/",
                token = null,
                pinnedFingerprint = null,
                allowTrustOnFirstUse = true,
                readTimeoutSeconds = 90,
                onCertificateSeen = { fingerprint -> capturedFingerprint = fingerprint }
            )

            val response = tempApi.pairDevice(PairRequest(deviceName, pin))
            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()!!.data!!
                val fingerprint = capturedFingerprint
                    ?: return Result.failure(Exception("Server certificate was not captured during pairing"))

                val server = ServerInfo(
                    id = "$serverIp:$port",
                    name = authData.serverName,
                    ipAddress = serverIp,
                    port = port,
                    token = authData.token,
                    certificateFingerprint = fingerprint,
                    isPaired = true,
                    lastConnected = System.currentTimeMillis(),
                    lastAuthCheckAt = System.currentTimeMillis(),
                    connectionHealth = "Healthy"
                )
                serverDao.insertServer(server)
                connectToServer(server)

                Result.success(authData)
            } else {
                Result.failure(apiFailure(response, "Pairing failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // —— Printers ————————————————————————————————————————————————

    /** Fetch all printers from the connected server. */
    suspend fun getPrinters(): Result<List<PrinterInfo>> {
        return runApiCall("Failed to get printers") { api ->
            val response = api.getPrinters()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(apiFailure(response, "Failed to get printers"))
            }
        }
    }

    // —— Print Jobs ————————————————————————————————————————————————

    /** Upload a file and create a print job. Streams directly from SAF content instead of loading bytes into memory. */
    suspend fun submitPrintJob(
        fileUri: Uri,
        fileName: String,
        settings: PrintSettings,
        printerName: String
    ): Result<PrintJobResponse> {
        return runApiCall("Upload failed") { api ->
            val contentLength = getContentLength(fileUri)
            val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"
            val filePart = MultipartBody.Part.createFormData(
                "file",
                fileName,
                ContentUriRequestBody(
                    contentResolver = context.contentResolver,
                    uri = fileUri,
                    contentType = mimeType.toMediaTypeOrNull(),
                    contentLength = contentLength
                )
            )

            val settingsJson = gson.toJson(settings)
                .toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadPrintJob(filePart, settingsJson)
            if (response.isSuccessful && response.body()?.data != null) {
                val jobResponse = response.body()!!.data!!
                printJobDao.insertJob(
                    PrintJob(
                        id = jobResponse.jobId,
                        fileName = fileName,
                        fileSize = contentLength,
                        fileType = getFileType(fileName),
                        printerName = printerName,
                        serverName = currentBaseUrl,
                        status = jobResponse.status
                    )
                )
                Result.success(jobResponse)
            } else {
                Result.failure(apiFailure(response, "Upload failed"))
            }
        }
    }

    /** Get page count of a file without reading it fully into memory, supporting password-protected PDFs. */
    suspend fun getPageCount(fileUri: Uri, fileName: String, password: String? = null): Result<PageCountResponse> {
        return runApiCall("Failed to get page count") { api ->
            val contentLength = getContentLength(fileUri)
            val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"
            val filePart = MultipartBody.Part.createFormData(
                "file",
                fileName,
                ContentUriRequestBody(
                    contentResolver = context.contentResolver,
                    uri = fileUri,
                    contentType = mimeType.toMediaTypeOrNull(),
                    contentLength = contentLength
                )
            )

            val passwordPart = password?.let {
                okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), it)
            }

            val response = api.getPageCount(filePart, passwordPart)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = response.body()?.error ?: "Failed to get page count"
                Result.failure(Exception(errorMsg))
            }
        }
    }

    /** Get all jobs from the server. Jobs are device-scoped by the server. */
    suspend fun getServerJobs(): Result<List<ServerPrintJob>> {
        return runApiCall("Failed to get jobs") { api ->
            val response = api.getJobs()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(apiFailure(response, "Failed to get jobs"))
            }
        }
    }

    /** Cancel a job on the server. */
    suspend fun cancelJob(jobId: String): Result<Unit> {
        return runApiCall("Cancel failed") { api ->
            val response = api.cancelJob(jobId)
            if (response.isSuccessful) {
                printJobDao.getJobById(jobId)?.let {
                    printJobDao.updateJob(it.copy(status = "Cancelled"))
                }
                Result.success(Unit)
            } else {
                Result.failure(apiFailure(response, "Cancel failed"))
            }
        }
    }

    /** Retry a failed job. */
    suspend fun retryJob(jobId: String): Result<Unit> {
        return runApiCall("Retry failed") { api ->
            val response = api.retryJob(jobId)
            if (response.isSuccessful) {
                printJobDao.getJobById(jobId)?.let {
                    printJobDao.updateJob(it.copy(status = "Pending", progress = 0))
                }
                Result.success(Unit)
            } else {
                Result.failure(apiFailure(response, "Retry failed"))
            }
        }
    }

    /** Set job priority. */
    suspend fun setJobPriority(jobId: String, priority: String): Result<Unit> {
        return runApiCall("Failed to set priority") { api ->
            val response = api.setJobPriority(jobId, mapOf("priority" to priority))
            if (response.isSuccessful) {
                printJobDao.getJobById(jobId)?.let {
                    printJobDao.updateJob(it.copy(priority = priority))
                }
                Result.success(Unit)
            } else {
                Result.failure(apiFailure(response, "Failed to set priority"))
            }
        }
    }

    /** Pause a pending job. */
    suspend fun pauseJob(jobId: String): Result<Unit> {
        return runApiCall("Pause failed") { api ->
            val response = api.pauseJob(jobId)
            if (response.isSuccessful) {
                printJobDao.getJobById(jobId)?.let {
                    printJobDao.updateJob(it.copy(status = "Paused"))
                }
                Result.success(Unit)
            } else {
                Result.failure(apiFailure(response, "Pause failed"))
            }
        }
    }

    /** Resume a paused job. */
    suspend fun resumeJob(jobId: String): Result<Unit> {
        return runApiCall("Resume failed") { api ->
            val response = api.resumeJob(jobId)
            if (response.isSuccessful) {
                printJobDao.getJobById(jobId)?.let {
                    printJobDao.updateJob(it.copy(status = "Pending"))
                }
                Result.success(Unit)
            } else {
                Result.failure(apiFailure(response, "Resume failed"))
            }
        }
    }

    // —— Local Database ————————————————————————————————————————————————

    fun getLocalJobs(): Flow<List<PrintJob>> = printJobDao.getAllJobs()
    fun getSavedServers(): Flow<List<ServerInfo>> = serverDao.getAllServers()
    suspend fun getLastPairedServer(): ServerInfo? = serverDao.getLastPairedServer()
    suspend fun getLocalJobById(jobId: String): PrintJob? = printJobDao.getJobById(jobId)
    suspend fun updateLocalJob(job: PrintJob) = printJobDao.updateJob(job)

    /** Check if currently connected to a server. */
    fun isConnected(): Boolean = currentApiService != null

    /**
     * Verify the connection is actually working by calling the server's status endpoint.
     * A successful verification also locks in the observed certificate fingerprint for older saved servers.
     */
    suspend fun verifyConnection(): Boolean {
        return try {
            val api = ensureConnected()
            val response = api.getServerStatus()
            if (response.isSuccessful) {
                persistObservedFingerprint()
                updateCurrentServerHealth(response.body()?.readiness ?: "Healthy")
                true
            } else {
                updateCurrentServerHealth("Unhealthy")
                false
            }
        } catch (_: Exception) {
            updateCurrentServerHealth("Unreachable")
            false
        }
    }

    private suspend fun <T> runApiCall(
        failureHealth: String,
        block: suspend (PrintApiService) -> Result<T>
    ): Result<T> {
        return try {
            val api = ensureConnected()
            val result = block(api)
            if (result.isSuccess) {
                persistObservedFingerprint()
                updateCurrentServerHealth("Healthy")
            } else {
                updateCurrentServerHealth(failureHealth)
            }
            result
        } catch (e: Exception) {
            updateCurrentServerHealth("Unreachable")
            Result.failure(e)
        }
    }

    private fun createApiService(
        baseUrl: String,
        token: String?,
        pinnedFingerprint: String?,
        allowTrustOnFirstUse: Boolean,
        readTimeoutSeconds: Long = 60,
        onCertificateSeen: ((String) -> Unit)? = null
    ): PrintApiService {
        val clientBuilder = okHttpClient.newBuilder()
            .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                token?.let { builder.addHeader("Authorization", "Bearer $it") }
                chain.proceed(builder.build())
            }

        if (isTunnelUrl(baseUrl)) {
            // Tunnel connections (e.g. *.trycloudflare.com) use valid CA-signed certificates.
            // Android's default trust store validates them automatically.
        } else {
            // Local IP connections use the server's self-signed certificate with TOFU/pinning.
            val tlsBundle = CertificatePinning.createTlsBundle(
                expectedFingerprint = pinnedFingerprint,
                allowTrustOnFirstUse = allowTrustOnFirstUse,
                onCertificateSeen = onCertificateSeen
            )
            clientBuilder.sslSocketFactory(tlsBundle.sslSocketFactory, tlsBundle.trustManager)
            clientBuilder.hostnameVerifier { _, _ -> true }
        }

        val client = clientBuilder.build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PrintApiService::class.java)
    }

    private fun apiFailure(response: Response<*>, defaultMessage: String): Exception {
        var parsedError: String? = null
        try {
            val errorJson = response.errorBody()?.string()
            if (!errorJson.isNullOrBlank()) {
                val obj = JSONObject(errorJson)
                parsedError = obj.optString("error").ifBlank { obj.optString("message").ifBlank { null } }
            }
        } catch (_: Exception) {}

        val apiMessage = extractApiMessage(response.body())
        val serverMessage = parsedError ?: apiMessage

        val genericMessage = when (response.code()) {
            401 -> "Your session expired. Reconnect to the server and try again."
            403 -> "This device is not authorized or session expired."
            404 -> "The requested item was not found on the server."
            408 -> "The request timed out. Please try again."
            409 -> "The server rejected the request because the job state changed."
            429 -> "Rate limit reached. Please wait before trying again."
            503 -> "The server is online but not ready to process this request."
            else -> null
        }

        val message = serverMessage ?: genericMessage ?: defaultMessage
        return IOException(message)
    }

    private fun extractApiMessage(body: Any?): String? {
        if (body !is ApiResponse<*>) {
            return null
        }

        return body.error?.takeIf { it.isNotBlank() }
            ?: body.message.takeIf { it.isNotBlank() }
    }

    private suspend fun persistObservedFingerprint() {
        val server = currentServer ?: return
        val fingerprint = observedServerFingerprint ?: return
        if (server.certificateFingerprint.equals(fingerprint, ignoreCase = true)) {
            if (server.lastAuthCheckAt == null || server.connectionHealth != "Healthy") {
                updateCurrentServer(server.copy(
                    lastAuthCheckAt = System.currentTimeMillis(),
                    connectionHealth = "Healthy"
                ))
            }
            return
        }

        updateCurrentServer(
            server.copy(
                certificateFingerprint = fingerprint,
                lastAuthCheckAt = System.currentTimeMillis(),
                connectionHealth = "Healthy"
            )
        )
    }

    private suspend fun updateCurrentServerHealth(health: String) {
        val server = currentServer ?: return
        updateCurrentServer(server.copy(
            lastAuthCheckAt = System.currentTimeMillis(),
            connectionHealth = health,
            lastConnected = System.currentTimeMillis()
        ))
    }

    private suspend fun updateCurrentServer(updated: ServerInfo) {
        currentServer = updated
        serverDao.insertServer(updated)
    }

    private fun getContentLength(uri: Uri): Long {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    return cursor.getLong(sizeIndex)
                }
            }
        }

        return context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
            descriptor.length
        } ?: -1L
    }

    private fun getFileType(name: String): String {
        val ext = name.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "pdf" -> "PDF"
            "jpg", "jpeg", "png", "bmp", "gif" -> "Image"
            "docx", "doc" -> "Document"
            "txt" -> "Text"
            else -> "Unknown"
        }
    }
}
