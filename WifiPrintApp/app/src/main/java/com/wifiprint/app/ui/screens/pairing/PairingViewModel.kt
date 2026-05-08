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

data class PairingState(
    val pin: String = "",
    val isPairing: Boolean = false,
    val isPaired: Boolean = false,
    val error: String? = null,
    val serverName: String = ""
)

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val repository: PrintRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PairingState())
    val state: StateFlow<PairingState> = _state

    fun updatePin(pin: String) {
        if (pin.length <= 6 && pin.all { it.isDigit() }) {
            _state.value = _state.value.copy(pin = pin, error = null)
        }
    }

    fun pair(serverIp: String, serverPort: Int) {
        val pin = _state.value.pin
        if (pin.length != 6) {
            _state.value = _state.value.copy(error = "Enter the 6-digit PIN shown on the server")
            return
        }

        _state.value = _state.value.copy(isPairing = true, error = null)

        viewModelScope.launch {
            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
            val result = repository.pairWithServer(serverIp, serverPort, pin, deviceName)

            result.fold(
                onSuccess = { auth ->
                    _state.value = _state.value.copy(
                        isPairing = false, isPaired = true,
                        serverName = auth.serverName
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isPairing = false,
                        error = e.message ?: "Pairing failed"
                    )
                }
            )
        }
    }
}
