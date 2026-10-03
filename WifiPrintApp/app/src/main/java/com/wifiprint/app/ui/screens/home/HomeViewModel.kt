package com.wifiprint.app.ui.screens.home

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wifiprint.app.data.models.PrintJob
import com.wifiprint.app.data.repository.PrintRepository
import com.wifiprint.app.discovery.NsdDiscoveryManager
import com.wifiprint.app.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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
        private const val HEALTH_CHECK_INTERVAL_MS = 25000L  // Check every 25 seconds
        private const val MAX_CONSECUTIVE_FAILURES = 3       // Require 3 consecutive failures (~75s) before disconnecting
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

        // Observe WiFi state changes — only disconnect if connected to a local LAN server without Cloud Relay
        viewModelScope.launch {
            networkMonitor.isWifiConnected.collect { wifiConnected ->
                _uiState.update { it.copy(isWifiConnected = wifiConnected) }
                if (!wifiConnected) {
                    val lastServer = repository.getLastPairedServer()
                    val hasTunnel = !lastServer?.tunnelUrl.isNullOrBlank()
                    // Cloud Relay connections work over mobile data / cellular — do NOT disconnect!
                    if (!hasTunnel) {
                        Log.d(TAG, "WiFi disconnected — local server unreachable")
                        _uiState.update {
                            it.copy(
                                isConnected = false,
                                serverName = "Not connected",
                                connectionMessage = "WiFi disconnected. Reconnect to the same LAN to print."
                            )
                        }
                    } else {
                        Log.d(TAG, "WiFi disconnected, but server has Cloud Relay — maintaining connection")
                    }
                } else {
                    // WiFi came back — try reconnecting if not connected
                    Log.d(TAG, "WiFi reconnected — attempting auto-connect")
                    delay(1500) // Small delay for network to stabilize
                    if (!_uiState.value.isConnected) {
                        tryAutoConnect()
                    }
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

        // Periodic health check — verifies server is still reachable with failure threshold
        viewModelScope.launch {
            var consecutiveFailures = 0
            while (true) {
                delay(HEALTH_CHECK_INTERVAL_MS)
                if (_uiState.value.isConnected) {
                    val stillConnected = try {
                        repository.verifyConnection()
                    } catch (e: Exception) {
                        false
                    }
                    if (stillConnected) {
                        consecutiveFailures = 0
                    } else {
                        consecutiveFailures++
                        Log.w(TAG, "Health check failed ($consecutiveFailures/$MAX_CONSECUTIVE_FAILURES)")
                        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                            Log.w(TAG, "Health check failed $MAX_CONSECUTIVE_FAILURES times — server unreachable")
                            _uiState.update {
                                it.copy(
                                    isConnected = false,
                                    serverName = "Not connected"
                                )
                            }
                        }
                    }
                } else {
                    consecutiveFailures = 0
                }
            }
        }
    }

    private val discoveryManager = NsdDiscoveryManager(application)

    /**
     * Attempt to connect to the last paired server.
     */
    fun tryAutoConnect() {
        viewModelScope.launch {
            val savedServer = repository.getLastPairedServer()
            val hasTunnel = !savedServer?.tunnelUrl.isNullOrBlank()
            val hasWifi = networkMonitor.checkWifiConnected()
            val hasInternet = networkMonitor.checkInternetConnected()

            // For local-only servers, WiFi is required. For Cloud Relay servers, any internet connection works!
            if (!hasWifi && (!hasTunnel || !hasInternet)) {
                Log.d(TAG, "Network not available for auto-connect (wifi=$hasWifi, tunnel=$hasTunnel, internet=$hasInternet)")
                _uiState.update { it.copy(isConnecting = false, isConnected = false) }
                return@launch
            }

            _uiState.update { it.copy(isConnecting = true) }

            // 1. Try reconnecting to the last paired server if still reachable
            if (savedServer != null) {
                try {
                    repository.connectToServer(savedServer)
                    val verified = repository.verifyConnection()
                    if (verified) {
                        Log.d(TAG, "Auto-connect verified with saved server: ${savedServer.name}")
                        _uiState.update {
                            it.copy(
                                isConnected = true,
                                isConnecting = false,
                                serverName = savedServer.name,
                                serverIp = "${savedServer.ipAddress}:${savedServer.port}",
                                connectionMessage = null
                            )
                        }
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Reconnecting to saved server failed: ${e.message}")
                }
            }

            // 2. Scan network for servers on the SAME local Wi-Fi subnet and auto-connect
            Log.d(TAG, "Scanning for WiFi Print servers on the same local network...")
            discoveryManager.startDiscovery()

            var autoConnected = false
            try {
                withTimeoutOrNull(4500L) {
                    discoveryManager.discoveredServers.first { list ->
                        val localServer = list.firstOrNull { it.isSameNetwork }
                        if (localServer != null) {
                            Log.d(TAG, "Found local server on same Wi-Fi: ${localServer.name} (${localServer.ipAddress})")
                            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
                            val autoResult = repository.requestConnectionApproval(
                                serverIp = localServer.ipAddress,
                                port = localServer.port,
                                deviceName = deviceName
                            )

                            autoResult.fold(
                                onSuccess = { auth ->
                                    Log.d(TAG, "Auto-connection successful: ${auth.serverName}")
                                    autoConnected = true
                                    _uiState.update {
                                        it.copy(
                                            isConnected = true,
                                            isConnecting = false,
                                            serverName = auth.serverName,
                                            serverIp = "${localServer.ipAddress}:${localServer.port}",
                                            connectionMessage = null
                                        )
                                    }
                                },
                                onFailure = { e ->
                                    Log.w(TAG, "Auto-connect attempt failed: ${e.message}")
                                }
                            )
                            true // Stop collecting — we found a same-network server
                        } else {
                            false // Keep waiting for more servers
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                discoveryManager.stopDiscovery()
                // Only reset isConnecting if we didn't successfully connect
                if (!autoConnected) {
                    _uiState.update { it.copy(isConnecting = false) }
                }
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
        discoveryManager.stopDiscovery()
        networkMonitor.stopMonitoring()
        super.onCleared()
    }
}
