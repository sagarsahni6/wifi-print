package com.wifiprint.app.ui.screens.print

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifiprint.app.data.models.PrintSettings
import com.wifiprint.app.data.models.PrinterInfo
import com.wifiprint.app.data.models.SelectedFile
import com.wifiprint.app.data.repository.PrintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrintUiState(
    // Single file (backward compatible)
    val selectedFileUri: Uri? = null,
    val selectedFileName: String = "",
    // Multi-file batch
    val selectedFiles: List<SelectedFile> = emptyList(),
    val isBatchMode: Boolean = false,
    val batchProgress: Int = 0,         // current file index in batch
    val batchTotal: Int = 0,
    // Printers
    val printers: List<PrinterInfo> = emptyList(),
    val selectedPrinter: PrinterInfo? = null,
    // Settings
    val settings: PrintSettings = PrintSettings(),
    // Page range
    val pageRangeMode: String = "All",   // "All" or "Custom"
    val totalPages: Int? = null,
    val pageRangeError: String? = null,
    // Status
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val success: Boolean = false,
    val error: String? = null,
    val jobId: String? = null,
    val isLoadingPrinters: Boolean = false,
    val isLoadingPageCount: Boolean = false
)

@HiltViewModel
class PrintViewModel @Inject constructor(
    private val repository: PrintRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PrintUiState())
    val state: StateFlow<PrintUiState> = _state

    init { loadPrinters() }

    fun loadPrinters() {
        _state.update { it.copy(isLoadingPrinters = true) }
        viewModelScope.launch {
            repository.getPrinters().fold(
                onSuccess = { printers ->
                    _state.update {
                        it.copy(
                            printers = printers,
                            selectedPrinter = printers.firstOrNull { p -> p.isDefault }
                                ?: printers.firstOrNull(),
                            isLoadingPrinters = false
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(error = "Cannot load printers: ${e.message}", isLoadingPrinters = false) }
                }
            )
        }
    }

    fun setFile(uri: Uri, name: String) {
        _state.update {
            it.copy(
                selectedFileUri = uri,
                selectedFileName = name,
                error = null,
                success = false,
                totalPages = null,
                pageRangeMode = "All",
                isBatchMode = false,
                selectedFiles = emptyList()
            )
        }
        // Auto-fetch page count for PDFs
        if (name.lowercase().endsWith(".pdf") && uri != Uri.EMPTY) {
            fetchPageCount(uri, name)
        }
    }

    fun addFiles(files: List<SelectedFile>) {
        _state.update {
            it.copy(
                selectedFiles = it.selectedFiles + files,
                isBatchMode = true,
                selectedFileUri = files.firstOrNull()?.uri,
                selectedFileName = "${it.selectedFiles.size + files.size} files selected",
                error = null,
                success = false
            )
        }
    }

    fun removeFile(file: SelectedFile) {
        _state.update {
            val updated = it.selectedFiles - file
            if (updated.isEmpty()) {
                it.copy(selectedFiles = emptyList(), isBatchMode = false,
                    selectedFileUri = null, selectedFileName = "")
            } else {
                it.copy(selectedFiles = updated,
                    selectedFileName = "${updated.size} files selected")
            }
        }
    }


    fun updateFileUri(index: Int, newUri: Uri) {
        if (index == -1) {
            _state.update { it.copy(selectedFileUri = newUri) }
        } else {
            _state.update {
                val updatedFiles = it.selectedFiles.toMutableList()
                if (index in updatedFiles.indices) {
                    val oldFile = updatedFiles[index]
                    updatedFiles[index] = oldFile.copy(uri = newUri)
                }
                it.copy(selectedFiles = updatedFiles)
            }
        }
    }

    fun clearFiles() {
        _state.update {
            it.copy(
                selectedFiles = emptyList(), isBatchMode = false,
                selectedFileUri = null, selectedFileName = "",
                totalPages = null, pageRangeMode = "All"
            )
        }
    }

    private fun fetchPageCount(uri: Uri, name: String) {
        _state.update { it.copy(isLoadingPageCount = true) }
        viewModelScope.launch {
            repository.getPageCount(uri, name).fold(
                onSuccess = { response ->
                    _state.update { it.copy(totalPages = response.pageCount, isLoadingPageCount = false) }
                },
                onFailure = {
                    _state.update { it.copy(isLoadingPageCount = false) }
                }
            )
        }
    }

    fun selectPrinter(printer: PrinterInfo) {
        _state.update { it.copy(selectedPrinter = printer, settings = it.settings.copy(selectedPrinterId = printer.id)) }
    }

    fun updateSettings(settings: PrintSettings) { _state.update { it.copy(settings = settings) } }

    fun setPageRangeMode(mode: String) {
        _state.update {
            it.copy(
                pageRangeMode = mode,
                settings = if (mode == "All") it.settings.copy(pageRange = null)
                else it.settings,
                pageRangeError = null
            )
        }
    }

    fun setPageRange(range: String) {
        val error = validatePageRange(range, _state.value.totalPages)
        _state.update {
            it.copy(
                settings = it.settings.copy(pageRange = range.ifBlank { null }),
                pageRangeError = error
            )
        }
    }

    private fun validatePageRange(range: String, totalPages: Int?): String? {
        if (range.isBlank()) return "Please enter a page range"
        val segments = range.split(",").map { it.trim() }
        for (segment in segments) {
            if (segment.contains("-")) {
                val parts = segment.split("-")
                if (parts.size != 2) return "Invalid range format: $segment"
                val start = parts[0].trim().toIntOrNull() ?: return "Invalid number: ${parts[0]}"
                val end = parts[1].trim().toIntOrNull() ?: return "Invalid number: ${parts[1]}"
                if (start < 1) return "Page numbers start from 1"
                if (start > end) return "Start must be ≤ end in: $segment"
                if (totalPages != null && end > totalPages) return "Page $end exceeds total ($totalPages)"
            } else {
                val page = segment.toIntOrNull() ?: return "Invalid number: $segment"
                if (page < 1) return "Page numbers start from 1"
                if (totalPages != null && page > totalPages) return "Page $page exceeds total ($totalPages)"
            }
        }
        return null
    }

    fun submitPrintJob() {
        if (_state.value.isBatchMode) {
            submitBatchPrintJobs()
            return
        }

        val uri = _state.value.selectedFileUri ?: run {
            _state.update { it.copy(error = "Please select a file first") }; return
        }
        val printer = _state.value.selectedPrinter ?: run {
            _state.update { it.copy(error = "Please select a printer") }; return
        }
        if (_state.value.pageRangeMode == "Custom" && _state.value.pageRangeError != null) {
            _state.update { it.copy(error = "Fix page range errors first") }; return
        }

        _state.update { it.copy(isUploading = true, error = null) }

        viewModelScope.launch {
            val settings = _state.value.settings.copy(selectedPrinterId = printer.id)
            repository.submitPrintJob(
                fileUri = uri,
                fileName = _state.value.selectedFileName,
                settings = settings,
                printerName = printer.name
            ).fold(
                onSuccess = { response ->
                    _state.update { it.copy(isUploading = false, success = true, jobId = response.jobId) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isUploading = false, error = e.message ?: "Upload failed") }
                }
            )
        }
    }

    private fun submitBatchPrintJobs() {
        val files = _state.value.selectedFiles
        if (files.isEmpty()) {
            _state.update { it.copy(error = "No files selected") }; return
        }
        val printer = _state.value.selectedPrinter ?: run {
            _state.update { it.copy(error = "Please select a printer") }; return
        }

        _state.update { it.copy(isUploading = true, error = null, batchProgress = 0, batchTotal = files.size) }

        viewModelScope.launch {
            val settings = _state.value.settings.copy(selectedPrinterId = printer.id)
            var lastJobId: String? = null
            var failCount = 0

            for ((index, file) in files.withIndex()) {
                _state.update { it.copy(batchProgress = index + 1) }
                repository.submitPrintJob(
                    fileUri = file.uri,
                    fileName = file.name,
                    settings = settings,
                    printerName = printer.name
                ).fold(
                    onSuccess = { response -> lastJobId = response.jobId },
                    onFailure = { failCount++ }
                )
            }

            if (failCount == files.size) {
                _state.update { it.copy(isUploading = false, error = "All uploads failed") }
            } else {
                _state.update { it.copy(isUploading = false, success = true, jobId = lastJobId,
                    error = if (failCount > 0) "$failCount/${files.size} uploads failed" else null) }
            }
        }
    }
}
