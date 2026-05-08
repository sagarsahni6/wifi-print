package com.wifiprint.app.ui.screens.pairing

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wifiprint.app.data.repository.PrintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State for the connection request screen.
 */
data class ConnectionRequestState(
    val isRequesting: Boolean = false,
    val isApproved: Boolean = false,
    val isDenied: Boolean = false,
    val error: String? = null,
    val serverName: String = ""
)

/**
 * ViewModel for the device approval connection flow.
 * Sends a connection request to the server and waits for PC user approval.
 */
@HiltViewModel
class ConnectionRequestViewModel @Inject constructor(
    private val repository: PrintRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ConnectionRequestState())
    val state: StateFlow<ConnectionRequestState> = _state

    /**
     * Sends a connection request to the server.
     * The server blocks (up to 60s) until the PC user clicks Allow or Deny.
     */
    fun requestConnection(serverIp: String, serverPort: Int) {
        if (_state.value.isRequesting) return

        _state.value = ConnectionRequestState(isRequesting = true)

        viewModelScope.launch {
            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
            val result = repository.requestConnectionApproval(serverIp, serverPort, deviceName)

            result.fold(
                onSuccess = { auth ->
                    _state.value = _state.value.copy(
                        isRequesting = false,
                        isApproved = true,
                        serverName = auth.serverName
                    )
                },
                onFailure = { e ->
                    val message = e.message ?: "Connection failed"
                    val isDenied = message.contains("denied", ignoreCase = true)
                    _state.value = _state.value.copy(
                        isRequesting = false,
                        isDenied = isDenied,
                        error = message
                    )
                }
            )
        }
    }

    /** Reset state to allow retry */
    fun resetState() {
        _state.value = ConnectionRequestState()
    }
}
