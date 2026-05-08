package com.wifiprint.app.ui.screens.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifiprint.app.data.models.PrintJob
import com.wifiprint.app.data.repository.PrintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobHistoryViewModel @Inject constructor(
    private val repository: PrintRepository
) : ViewModel() {

    val jobs: StateFlow<List<PrintJob>> = repository.getLocalJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _selectedTab = MutableStateFlow(0) // 0=Queue, 1=History
    val selectedTab: StateFlow<Int> = _selectedTab

    /** Active jobs in queue (Pending, Printing, Queued, Paused) */
    val queueJobs: StateFlow<List<PrintJob>> = jobs.map { list ->
        list.filter { it.status in listOf("Pending", "Printing", "Queued", "Paused") }
            .sortedBy { getPriorityOrder(it.priority) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Completed/failed/cancelled jobs */
    val historyJobs: StateFlow<List<PrintJob>> = jobs.map { list ->
        list.filter { it.status in listOf("Completed", "Failed", "Cancelled") }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Auto-poll server every 5 seconds for status updates on active jobs
        viewModelScope.launch {
            while (true) {
                delay(5000)
                val activeJobs = jobs.value.any { it.status in listOf("Pending", "Printing", "Queued") }
                if (activeJobs) {
                    syncJobStatusFromServer()
                }
            }
        }
    }

    fun setTab(tab: Int) { _selectedTab.value = tab }

    fun refreshFromServer() {
        _isRefreshing.value = true
        viewModelScope.launch {
            syncJobStatusFromServer()
            _isRefreshing.value = false
        }
    }

    /**
     * Merges status/progress from server → local database.
     * Uses ServerPrintJob DTO which handles DateTime strings from the C# server.
     */
    private suspend fun syncJobStatusFromServer() {
        repository.getServerJobs().fold(
            onSuccess = { serverJobs ->
                serverJobs.forEach { serverJob ->
                    val localJob = repository.getLocalJobById(serverJob.id)
                    if (localJob != null && localJob.status != serverJob.status) {
                        repository.updateLocalJob(localJob.copy(
                            status = serverJob.status,
                            progress = serverJob.progress,
                            errorMessage = serverJob.errorMessage,
                            priority = serverJob.priority
                        ))
                    }
                }
            },
            onFailure = { /* ignore — server may be unreachable */ }
        )
    }

    fun cancelJob(jobId: String) {
        viewModelScope.launch { repository.cancelJob(jobId) }
    }

    fun retryJob(jobId: String) {
        viewModelScope.launch { repository.retryJob(jobId) }
    }

    fun pauseJob(jobId: String) {
        viewModelScope.launch { repository.pauseJob(jobId) }
    }

    fun resumeJob(jobId: String) {
        viewModelScope.launch { repository.resumeJob(jobId) }
    }

    fun setJobPriority(jobId: String, priority: String) {
        viewModelScope.launch { repository.setJobPriority(jobId, priority) }
    }

    private fun getPriorityOrder(priority: String): Int = when (priority) {
        "High" -> 0
        "Normal" -> 1
        "Low" -> 2
        else -> 1
    }
}
