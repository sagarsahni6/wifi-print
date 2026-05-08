package com.wifiprint.app.data.api

import com.wifiprint.app.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API interface for communicating with the WiFi Print Server.
 */
interface PrintApiService {

    // ── Authentication ──────────────────────────────────────────────

    /** Request connection approval — blocks until PC user approves/denies. */
    @POST("api/auth/request")
    suspend fun requestConnection(@Body request: ConnectionRequest): Response<ApiResponse<AuthResponse>>

    /** Legacy: Exchange PIN for JWT token (redirects to approval flow on server). */
    @POST("api/auth/pair")
    suspend fun pairDevice(@Body request: PairRequest): Response<ApiResponse<AuthResponse>>

    /** Check if current auth token is valid. */
    @GET("api/auth/status")
    suspend fun checkAuthStatus(): Response<ApiResponse<Map<String, Any>>>

    // ── Printers ────────────────────────────────────────────────────

    /** Get all printers connected to the server PC (includes health info). */
    @GET("api/printers")
    suspend fun getPrinters(): Response<ApiResponse<List<PrinterInfo>>>

    /** Get a specific printer's details and capabilities. */
    @GET("api/printers/{id}")
    suspend fun getPrinter(@Path("id") id: String): Response<ApiResponse<PrinterInfo>>

    // ── Print Jobs ──────────────────────────────────────────────────

    /** Upload a file and create a print job. Multipart for file + JSON settings. */
    @Multipart
    @POST("api/print")
    suspend fun uploadPrintJob(
        @Part file: MultipartBody.Part,
        @Part("settingsJson") settings: RequestBody
    ): Response<ApiResponse<PrintJobResponse>>

    /** Upload a file and get its page count (for page range selector). */
    @Multipart
    @POST("api/print/pagecount")
    suspend fun getPageCount(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<PageCountResponse>>

    /** Get all jobs, optionally filtered by status. Uses ServerPrintJob DTO to handle DateTime strings. */
    @GET("api/jobs")
    suspend fun getJobs(@Query("status") status: String? = null): Response<ApiResponse<List<ServerPrintJob>>>

    /** Get a specific job by ID. */
    @GET("api/jobs/{id}")
    suspend fun getJob(@Path("id") id: String): Response<ApiResponse<PrintJob>>

    /** Cancel a pending/printing job. */
    @POST("api/jobs/{id}/cancel")
    suspend fun cancelJob(@Path("id") id: String): Response<ApiResponse<Any>>

    /** Retry a failed job. */
    @POST("api/jobs/{id}/retry")
    suspend fun retryJob(@Path("id") id: String): Response<ApiResponse<Any>>

    /** Set job priority (High, Normal, Low). */
    @POST("api/jobs/{id}/priority")
    suspend fun setJobPriority(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Any>>

    /** Pause a pending job. */
    @POST("api/jobs/{id}/pause")
    suspend fun pauseJob(@Path("id") id: String): Response<ApiResponse<Any>>

    /** Resume a paused job. */
    @POST("api/jobs/{id}/resume")
    suspend fun resumeJob(@Path("id") id: String): Response<ApiResponse<Any>>

    // ── Health Check ────────────────────────────────────────────────

    /** Check if the server is online. */
    @GET("api/status")
    suspend fun getServerStatus(): Response<Map<String, Any>>
}
