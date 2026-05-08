package com.wifiprint.app.ui.screens.discovery

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifiprint.app.data.models.ServerInfo
import com.wifiprint.app.data.repository.PrintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConnectState(
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val connectingTo: String = "",
    val error: String? = null,
    val serverName: String = ""
)

@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val repository: PrintRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ConnectState())
    val state: StateFlow<ConnectState> = _state

    /**
     * Connects to a server using the Device Approval flow.
     * Sends a connection request and waits for PC user to approve.
     */
    fun connectToServer(server: ServerInfo) {
        _state.update { it.copy(
            isConnecting = true,
            connectingTo = server.id,
            error = null
        ) }

        viewModelScope.launch {
            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"

            val result = repository.requestConnectionApproval(
                serverIp = server.ipAddress,
                port = server.port,
                deviceName = deviceName
            )

            result.fold(
                onSuccess = { auth ->
                    _state.update { it.copy(
                        isConnecting = false,
                        isConnected = true,
                        serverName = auth.serverName
                    ) }
                },
                onFailure = { e ->
                    _state.update { it.copy(
                        isConnecting = false,
                        error = e.message ?: "Connection failed"
                    ) }
                }
            )
        }
    }
}
