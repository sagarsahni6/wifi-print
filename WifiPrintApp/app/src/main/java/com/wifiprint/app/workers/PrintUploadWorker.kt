package com.wifiprint.app.workers

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.wifiprint.app.data.repository.PrintRepository
import com.wifiprint.app.data.models.PrintSettings
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker for reliable background file uploads.
 * Handles large files, retries on failure, and reports progress.
 */
@HiltWorker
class PrintUploadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: PrintRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_FILE_URI = "file_uri"
        const val KEY_FILE_NAME = "file_name"
        const val KEY_PRINTER_NAME = "printer_name"
        const val KEY_SETTINGS_JSON = "settings_json"
        const val KEY_PROGRESS = "progress"
        const val KEY_JOB_ID = "job_id"
        const val KEY_ERROR = "error"

        fun buildRequest(fileUri: Uri, fileName: String, settings: PrintSettings, printerName: String): OneTimeWorkRequest {
            val settingsJson = Gson().toJson(settings)
            val data = workDataOf(
                KEY_FILE_URI to fileUri.toString(),
                KEY_FILE_NAME to fileName,
                KEY_PRINTER_NAME to printerName,
                KEY_SETTINGS_JSON to settingsJson
            )

            return OneTimeWorkRequestBuilder<PrintUploadWorker>()
                .setInputData(data)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    java.util.concurrent.TimeUnit.MILLISECONDS
                )
                .build()
        }
    }

    override suspend fun doWork(): Result {
        val uriString = inputData.getString(KEY_FILE_URI) ?: return Result.failure()
        val fileName = inputData.getString(KEY_FILE_NAME) ?: return Result.failure()
        val printerName = inputData.getString(KEY_PRINTER_NAME) ?: "Default"
        val settingsJson = inputData.getString(KEY_SETTINGS_JSON) ?: "{}"

        val uri = Uri.parse(uriString)
        val settings = try {
            Gson().fromJson(settingsJson, PrintSettings::class.java)
        } catch (_: Exception) {
            PrintSettings()
        }

        setProgress(workDataOf(KEY_PROGRESS to 10))

        return try {
            val result = repository.submitPrintJob(uri, fileName, settings, printerName)
            result.fold(
                onSuccess = { response ->
                    setProgress(workDataOf(KEY_PROGRESS to 100))
                    Result.success(workDataOf(KEY_JOB_ID to response.jobId))
                },
                onFailure = { e ->
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Upload failed")))
                    }
                }
            )
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry()
            else Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Upload failed")))
        }
    }
}
