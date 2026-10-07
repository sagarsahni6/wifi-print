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
    val serverName: String = "",
    val requiresPinPrompt: Boolean = false,
    val pendingServer: ServerInfo? = null,
    val pendingQrToken: String? = null
)

@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val repository: PrintRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ConnectState())
    val state: StateFlow<ConnectState> = _state

    /**
     * Connects to a server using the Device Approval flow.
     * If on the same local network, auto-connects immediately.
     * If on another network, passes QR token and/or PIN to establish trust.
     */
    fun connectToServer(server: ServerInfo, qrToken: String? = null, pin: String? = null) {
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
                deviceName = deviceName,
                qrToken = qrToken,
                pin = pin,
                tunnelUrl = server.tunnelUrl
            )

            result.fold(
                onSuccess = { auth ->
                    _state.update { it.copy(
                        isConnecting = false,
                        isConnected = true,
                        serverName = auth.serverName,
                        requiresPinPrompt = false,
                        pendingServer = null,
                        pendingQrToken = null
                    ) }
                },
                onFailure = { e ->
                    val msg = e.message ?: "Connection failed"
                    val needsPin = (msg.contains("PIN", ignoreCase = true) || msg.contains("another network", ignoreCase = true)) && pin == null
                    _state.update { it.copy(
                        isConnecting = false,
                        error = msg,
                        requiresPinPrompt = needsPin,
                        pendingServer = if (needsPin) server else null,
                        pendingQrToken = if (needsPin) qrToken else null
                    ) }
                }
            )
        }
    }

    /**
     * Connects via QR code data — creates a ServerInfo from the scanned payload
     * and initiates the approval flow with the QR security token, optional tunnel URL, and optional PIN.
     */
    fun connectFromQr(
        ip: String,
        port: Int,
        name: String,
        certFingerprint: String,
        qrToken: String? = null,
        pin: String? = null,
        tunnelUrl: String? = null
    ) {
        val server = ServerInfo(
            id = if (!tunnelUrl.isNullOrBlank()) tunnelUrl else "$ip:$port",
            name = name,
            ipAddress = ip,
            port = port,
            certificateFingerprint = certFingerprint.ifBlank { null },
            tunnelUrl = tunnelUrl
        )
        connectToServer(server, qrToken = qrToken, pin = pin)
    }

    /**
     * Connects to a server manually using its IP, Port, and rotating PIN.
     */
    fun connectWithPin(ip: String, port: Int, pin: String) {
        val server = ServerInfo(
            id = "$ip:$port",
            name = "Printora Server",
            ipAddress = ip,
            port = port
        )
        connectToServer(server, pin = pin)
    }

    /**
     * Submits the 6-digit PIN for a server connection that required it.
     */
    fun submitPin(pin: String) {
        val server = _state.value.pendingServer ?: return
        val token = _state.value.pendingQrToken
        connectToServer(server, qrToken = token, pin = pin)
    }

    fun dismissPinPrompt() {
        _state.update { it.copy(requiresPinPrompt = false, pendingServer = null, pendingQrToken = null) }
    }
}
