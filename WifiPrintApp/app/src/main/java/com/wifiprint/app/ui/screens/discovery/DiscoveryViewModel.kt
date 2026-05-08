package com.wifiprint.app.ui.screens.discovery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.wifiprint.app.data.models.ServerInfo
import com.wifiprint.app.discovery.NsdDiscoveryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val discoveryManager = NsdDiscoveryManager(application)

    val discoveredServers: StateFlow<List<ServerInfo>> = discoveryManager.discoveredServers
    val isSearching: StateFlow<Boolean> = discoveryManager.isSearching

    private val _manualIp = MutableStateFlow("")
    val manualIp: StateFlow<String> = _manualIp

    fun startDiscovery() = discoveryManager.startDiscovery()
    fun stopDiscovery() = discoveryManager.stopDiscovery()

    fun setManualIp(ip: String) { _manualIp.value = ip }

    fun addManualServer(ip: String, port: Int = 5000): ServerInfo =
        discoveryManager.addManualServer(ip, port)

    override fun onCleared() {
        discoveryManager.stopDiscovery()
        super.onCleared()
    }
}
