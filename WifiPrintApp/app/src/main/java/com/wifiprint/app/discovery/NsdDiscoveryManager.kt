package com.wifiprint.app.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.wifiprint.app.data.models.ServerInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Uses Android's NsdManager to discover WiFi Print servers
 * broadcasting via mDNS (_wifiprint._tcp).
 */
class NsdDiscoveryManager(context: Context) {

    companion object {
        private const val TAG = "NsdDiscovery"
        private const val SERVICE_TYPE = "_wifiprint._tcp."
    }

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    private val _discoveredServers = MutableStateFlow<List<ServerInfo>>(emptyList())
    val discoveredServers: StateFlow<List<ServerInfo>> = _discoveredServers

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val serverMap = mutableMapOf<String, ServerInfo>()
    private var isDiscovering = false

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(serviceType: String) {
            _isSearching.value = true
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, "Service found: ${serviceInfo.serviceName}")
            // Resolve to get IP and port
            nsdManager.resolveService(serviceInfo, createResolveListener())
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, "Service lost: ${serviceInfo.serviceName}")
            serverMap.remove(serviceInfo.serviceName)
            _discoveredServers.value = serverMap.values.toList()
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.d(TAG, "Discovery stopped")
            _isSearching.value = false
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery start failed: $errorCode")
            _isSearching.value = false
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery stop failed: $errorCode")
        }
    }

    private fun createResolveListener() = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG, "Resolve failed for ${serviceInfo.serviceName}: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            val host = serviceInfo.host?.hostAddress ?: return
            val port = serviceInfo.port
            val name = serviceInfo.serviceName

            Log.d(TAG, "Resolved: $name at $host:$port")

            val server = ServerInfo(
                id = "$host:$port",
                name = name,
                ipAddress = host,
                port = port
            )
            serverMap[name] = server
            _discoveredServers.value = serverMap.values.toList()
        }
    }

    /** Start scanning for WiFi Print servers on the local network. */
    fun startDiscovery() {
        if (isDiscovering) return
        try {
            serverMap.clear()
            _discoveredServers.value = emptyList()
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            isDiscovering = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start discovery", e)
        }
    }

    /** Stop scanning. */
    fun stopDiscovery() {
        if (!isDiscovering) return
        try {
            nsdManager.stopServiceDiscovery(discoveryListener)
            isDiscovering = false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop discovery", e)
        }
    }

    /** Manually add a server by IP address. */
    fun addManualServer(ip: String, port: Int = 5000): ServerInfo {
        val server = ServerInfo(
            id = "$ip:$port",
            name = "Manual Server ($ip)",
            ipAddress = ip,
            port = port
        )
        serverMap[server.id] = server
        _discoveredServers.value = serverMap.values.toList()
        return server
    }
}
