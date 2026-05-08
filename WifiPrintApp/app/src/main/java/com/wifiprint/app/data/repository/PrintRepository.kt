package com.wifiprint.app.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.wifiprint.app.data.api.PrintApiService
import com.wifiprint.app.data.db.PrintJobDao
import com.wifiprint.app.data.db.ServerDao
import com.wifiprint.app.data.models.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.*

/**
 * Central repository coordinating between API, local database, and UI layer.
 * Handles dynamic base URL switching when connecting to different servers.
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
    private var apiService: PrintApiService? = null

    /** Connect to a specific server and configure the API client. */
    fun connectToServer(server: ServerInfo) {
        currentBaseUrl = "https://${server.ipAddress}:${server.port}/"
        currentToken = server.token

        // Build authenticated OkHttpClient
        val authClient = okHttpClient.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                currentToken?.let { request.addHeader("Authorization", "Bearer $it") }
                chain.proceed(request.build())
            }
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PrintApiService::class.java)
    }

    /**
     * Ensures we are connected to a server before making API calls.
     * Auto-reconnects to the last paired server if not currently connected.
     */
    private suspend fun ensureConnected(): PrintApiService {
        apiService?.let { return it }

        // Try to auto-reconnect to the last paired server
        val server = serverDao.getLastPairedServer()
            ?: throw IllegalStateException("No paired server found. Please connect to a server first.")

        connectToServer(server)
        return apiService
            ?: throw IllegalStateException("Failed to connect to server ${server.name}")
    }

    // ── Authentication ──────────────────────────────────────────────

    /**
     * Request connection approval from the server.
     * This call blocks (up to 60s) waiting for the PC user to click Allow/Deny.
     * Uses a longer timeout OkHttp client to handle the long-polling.
     */
    suspend fun requestConnectionApproval(
        serverIp: String, port: Int, deviceName: String
    ): Result<AuthResponse> {
        return try {
            // Create a long-timeout client for the approval request (60s server wait + buffer)
            val longPollClient = okHttpClient.newBuilder()
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build()

            val tempApi = Retrofit.Builder()
                .baseUrl("https://$serverIp:$port/")
                .client(longPollClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PrintApiService::class.java)

            val request = ConnectionRequest(
                deviceName = deviceName,
                deviceModel = android.os.Build.MODEL
            )

            val response = tempApi.requestConnection(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()!!.data!!

                // Save the server with token to local database
                val server = ServerInfo(
                    id = "$serverIp:$port",
                    name = authData.serverName,
                    ipAddress = serverIp,
                    port = port,
                    token = authData.token,
                    isPaired = true
                )
                serverDao.insertServer(server)
                connectToServer(server)

                Result.success(authData)
            } else {
                val errorMsg = when (response.code()) {
                    403 -> "Connection denied by the PC user"
                    408 -> "Request timed out — no response from PC"
                    else -> response.body()?.error ?: "Connection failed"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Request timed out — no response from PC. Please try again."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Legacy: Pair with server using a PIN code. */
    suspend fun pairWithServer(
        serverIp: String, port: Int, pin: String, deviceName: String
    ): Result<AuthResponse> {
        return try {
            val tempClient = okHttpClient.newBuilder()
                .readTimeout(90, TimeUnit.SECONDS)
                .build()
            val tempApi = Retrofit.Builder()
                .baseUrl("https://$serverIp:$port/")
                .client(tempClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PrintApiService::class.java)

            val response = tempApi.pairDevice(PairRequest(deviceName, pin))
            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()!!.data!!
                val server = ServerInfo(
                    id = "$serverIp:$port",
                    name = authData.serverName,
                    ipAddress = serverIp,
                    port = port,
                    token = authData.token,
                    isPaired = true
                )
                serverDao.insertServer(server)
                connectToServer(server)
                Result.success(authData)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Pairing failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Printers ────────────────────────────────────────────────────

    /** Fetch all printers from the connected server. */
    suspend fun getPrinters(): Result<List<PrinterInfo>> {
        return try {
            val api = ensureConnected()
            val response = api.getPrinters()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to get printers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Print Jobs ──────────────────────────────────────────────────

    /** Upload a file and create a print job. */
    suspend fun submitPrintJob(
        fileUri: Uri,
        fileName: String,
        settings: PrintSettings,
        printerName: String
    ): Result<PrintJobResponse> {
        return try {
            val api = ensureConnected()

            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: return Result.failure(Exception("Cannot read file"))

            val bytes = inputStream.readBytes()
            inputStream.close()

            val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"
            val filePart = MultipartBody.Part.createFormData(
                "file", fileName,
                bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            )

            // Server expects settingsJson as a plain text form field, not JSON content type
            val settingsJson = gson.toJson(settings)
                .toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadPrintJob(filePart, settingsJson)
            if (response.isSuccessful && response.body()?.data != null) {
                val jobResponse = response.body()!!.data!!

                // Save to local database for history — use the actual printer display name
                printJobDao.insertJob(PrintJob(
                    id = jobResponse.jobId,
                    fileName = fileName,
                    fileSize = bytes.size.toLong(),
                    fileType = getFileType(fileName),
                    printerName = printerName,
                    serverName = currentBaseUrl,
                    status = jobResponse.status
                ))

                Result.success(jobResponse)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Get page count of a file (used by page range selector). */
    suspend fun getPageCount(fileUri: Uri, fileName: String): Result<PageCountResponse> {
        return try {
            val api = ensureConnected()

            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: return Result.failure(Exception("Cannot read file"))

            val bytes = inputStream.readBytes()
            inputStream.close()

            val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"
            val filePart = MultipartBody.Part.createFormData(
                "file", fileName,
                bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            )

            val response = api.getPageCount(filePart)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to get page count"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Get all jobs from the server (uses ServerPrintJob DTO for proper deserialization). */
    suspend fun getServerJobs(): Result<List<ServerPrintJob>> {
        return try {
            val api = ensureConnected()
            val response = api.getJobs()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to get jobs"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Cancel a job on the server. */
    suspend fun cancelJob(jobId: String): Result<Unit> {
        return try {
            val api = ensureConnected()
            val response = api.cancelJob(jobId)
            if (response.isSuccessful) {
                printJobDao.getJobById(jobId)?.let {
                    printJobDao.updateJob(it.copy(status = "Cancelled"))
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Cancel failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Retry a failed job. */
    suspend fun retryJob(jobId: String): Result<Unit> {
        return try {
            val api = ensureConnected()
            val response = api.retryJob(jobId)
            if (response.isSuccessful) {
                printJobDao.getJobById(jobId)?.let {
                    printJobDao.updateJob(it.copy(status = "Pending", progress = 0))
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Retry failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Set job priority. */
    suspend fun setJobPriority(jobId: String, priority: String): Result<Unit> {
        return try {
            val api = ensureConnected()
            val response = api.setJobPriority(jobId, mapOf("priority" to priority))
            if (response.isSuccessful) {
                printJobDao.getJobById(jobId)?.let {
                    printJobDao.updateJob(it.copy(priority = priority))
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to set priority"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Pause a pending job. */
    suspend fun pauseJob(jobId: String): Result<Unit> {
        return try {
            val api = ensureConnected()
            val response = api.pauseJob(jobId)
            if (response.isSuccessful) {
                printJobDao.getJobById(jobId)?.let {
                    printJobDao.updateJob(it.copy(status = "Paused"))
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Pause failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Resume a paused job. */
    suspend fun resumeJob(jobId: String): Result<Unit> {
        return try {
            val api = ensureConnected()
            val response = api.resumeJob(jobId)
            if (response.isSuccessful) {
                printJobDao.getJobById(jobId)?.let {
                    printJobDao.updateJob(it.copy(status = "Pending"))
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Resume failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Local Database ──────────────────────────────────────────────

    fun getLocalJobs(): Flow<List<PrintJob>> = printJobDao.getAllJobs()
    fun getSavedServers(): Flow<List<ServerInfo>> = serverDao.getAllServers()
    suspend fun getLastPairedServer(): ServerInfo? = serverDao.getLastPairedServer()
    suspend fun getLocalJobById(jobId: String): PrintJob? = printJobDao.getJobById(jobId)
    suspend fun updateLocalJob(job: PrintJob) = printJobDao.updateJob(job)

    /** Check if currently connected to a server. */
    fun isConnected(): Boolean = apiService != null

    /**
     * Verify the connection is actually working by calling the server's status endpoint.
     * Returns true if the server responds successfully.
     */
    suspend fun verifyConnection(): Boolean {
        return try {
            val api = apiService ?: return false
            val response = api.getServerStatus()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────

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
