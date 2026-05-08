package com.wifiprint.app.ui.screens.preview

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PreviewUiState(
    val fileUri: Uri? = null,
    val fileName: String = "",
    val fileType: String = "Unknown",
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val zoomLevel: Float = 1f,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DocumentPreviewViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(PreviewUiState())
    val state: StateFlow<PreviewUiState> = _state

    fun setFileUri(uriString: String) {
        val uri = Uri.parse(uriString)

        // Get display name from ContentResolver (works for content:// URIs)
        val displayName = getDisplayName(uri) ?: uri.lastPathSegment ?: "file"

        // Get MIME type from ContentResolver first, fall back to extension
        val mimeType = context.contentResolver.getType(uri)
        val fileType = getFileTypeFromMime(mimeType) ?: getFileTypeFromExtension(displayName)

        _state.update {
            it.copy(fileUri = uri, fileName = displayName, fileType = fileType, isLoading = false)
        }
    }

    /**
     * Query ContentResolver for the actual display name (e.g. "document.pdf").
     * This works correctly for content:// URIs from document pickers.
     */
    private fun getDisplayName(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) cursor.getString(nameIndex) else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Map MIME type to our file type categories.
     */
    private fun getFileTypeFromMime(mimeType: String?): String? {
        if (mimeType == null) return null
        return when {
            mimeType == "application/pdf" -> "PDF"
            mimeType.startsWith("image/") -> "Image"
            mimeType.startsWith("text/") -> "Text"
            mimeType == "application/msword" ||
            mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "Document"
            mimeType == "application/vnd.ms-excel" ||
            mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "Spreadsheet"
            mimeType == "application/vnd.ms-powerpoint" ||
            mimeType == "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "Presentation"
            else -> null
        }
    }

    /**
     * Fall back to extension-based file type detection.
     */
    private fun getFileTypeFromExtension(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "pdf" -> "PDF"
            "jpg", "jpeg", "png", "bmp", "gif", "webp" -> "Image"
            "txt", "text", "csv", "log", "md" -> "Text"
            "docx", "doc" -> "Document"
            "xlsx", "xls" -> "Spreadsheet"
            "pptx", "ppt" -> "Presentation"
            else -> "Unknown"
        }
    }

    fun setCurrentPage(page: Int) {
        _state.update { it.copy(currentPage = page.coerceIn(1, it.totalPages)) }
    }

    fun setTotalPages(total: Int) {
        _state.update { it.copy(totalPages = total) }
    }

    fun setZoomLevel(zoom: Float) {
        _state.update { it.copy(zoomLevel = zoom.coerceIn(0.5f, 3f)) }
    }

    fun setLoading(loading: Boolean) {
        _state.update { it.copy(isLoading = loading) }
    }

    fun setError(error: String?) {
        _state.update { it.copy(error = error, isLoading = false) }
    }
}
