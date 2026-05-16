package com.wifiprint.app.ui.screens.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wifiprint.app.data.models.PrintJob
import com.wifiprint.app.data.repository.PrintRepository
import com.wifiprint.app.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isWifiConnected: Boolean = true,
    val wifiSsid: String = "",
    val serverName: String = "Not connected",
    val serverIp: String = "",
    val connectionMessage: String? = null,
    val recentJobs: List<PrintJob> = emptyList(),
    val activeJobCount: Int = 0,
    val completedJobCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val repository: PrintRepository
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val HEALTH_CHECK_INTERVAL_MS = 8000L  // Check every 8 seconds
    }

    private val networkMonitor = NetworkMonitor(application)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Start WiFi monitoring
        networkMonitor.startMonitoring()

        // Observe local job history
        viewModelScope.launch {
            repository.getLocalJobs().collect { jobs ->
                _uiState.update { state ->
                    state.copy(
                        recentJobs = jobs.take(10),
                        activeJobCount = jobs.count { it.status in listOf("Pending", "Printing", "Queued") },
                        completedJobCount = jobs.count { it.status == "Completed" }
                    )
                }
            }
        }

        // Observe WiFi state changes — instantly disconnect when WiFi drops
        viewModelScope.launch {
            networkMonitor.isWifiConnected.collect { wifiConnected ->
                _uiState.update { it.copy(isWifiConnected = wifiConnected) }
                if (!wifiConnected) {
                    Log.d(TAG, "WiFi disconnected — marking as not connected")
                    _uiState.update {
                        it.copy(
                            isConnected = false,
                            serverName = "Not connected",
                            connectionMessage = "WiFi disconnected. Reconnect to the same LAN to print."
                        )
                    }
                } else {
                    // WiFi came back — try reconnecting
                    Log.d(TAG, "WiFi reconnected — attempting auto-connect")
                    delay(1500) // Small delay for network to stabilize
                    tryAutoConnect()
                }
            }
        }

        // Observe WiFi SSID
        viewModelScope.launch {
            networkMonitor.wifiSsid.collect { ssid ->
                _uiState.update { it.copy(wifiSsid = ssid) }
            }
        }

        // Auto-reconnect on startup
        tryAutoConnect()

        // Periodic health check — verifies server is still reachable
        viewModelScope.launch {
            while (true) {
                delay(HEALTH_CHECK_INTERVAL_MS)
                if (_uiState.value.isConnected) {
                    val stillConnected = try {
                        repository.verifyConnection()
                    } catch (e: Exception) {
                        false
                    }
                    if (!stillConnected) {
                        Log.w(TAG, "Health check failed — server unreachable")
                        _uiState.update {
                            it.copy(
                                isConnected = false,
                                serverName = "Not connected"
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Attempt to connect to the last paired server.
     */
    fun tryAutoConnect() {
        viewModelScope.launch {
            // Don't try if WiFi is not connected
            if (!networkMonitor.checkWifiConnected()) {
                Log.d(TAG, "WiFi not connected, skipping auto-connect")
                _uiState.update { it.copy(isConnecting = false, isConnected = false) }
                return@launch
            }

            _uiState.update { it.copy(isConnecting = true) }

            val server = repository.getLastPairedServer()
            if (server != null) {
                Log.d(TAG, "Found last paired server: ${server.name} at ${server.ipAddress}:${server.port}")
                try {
                    repository.connectToServer(server)

                    // Verify connection by calling the server status endpoint
                    val verified = repository.verifyConnection()
                    if (verified) {
                        Log.d(TAG, "Auto-connect verified: ${server.name}")
                        _uiState.update {
                            it.copy(
                                isConnected = true,
                                isConnecting = false,
                                serverName = server.name,
                                serverIp = "${server.ipAddress}:${server.port}",
                                connectionMessage = null
                            )
                        }
                    } else {
                        Log.w(TAG, "Auto-connect failed verification for ${server.name}")
                        _uiState.update {
                            it.copy(
                                isConnected = false,
                                isConnecting = false,
                                connectionMessage = "Saved server trust or approval is no longer valid. Open Connect to re-approve."
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Auto-connect failed: ${e.message}")
                    _uiState.update {
                        it.copy(
                            isConnected = false,
                            isConnecting = false,
                            connectionMessage = e.message ?: "Failed to reconnect to the saved server"
                        )
                    }
                }
            } else {
                Log.d(TAG, "No paired server found")
                _uiState.update { it.copy(isConnecting = false, connectionMessage = null) }
            }
        }
    }

    /**
     * Called when the user returns from discovery/pairing screen to refresh connection state.
     */
    fun refreshConnectionStatus() {
        tryAutoConnect()
    }

    override fun onCleared() {
        networkMonitor.stopMonitoring()
        super.onCleared()
    }
}
