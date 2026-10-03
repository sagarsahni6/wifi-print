package com.wifiprint.app.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.wifiprint.app.data.models.ServerInfo
import com.wifiprint.app.network.NetworkMonitor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException
import java.util.ArrayDeque

/**
 * Robust dual-channel discovery manager:
 * 1. mDNS / Zeroconf via NsdManager (_wifiprint._tcp) with safe sequential resolve queue
 * 2. UDP broadcast beacon receiver on port 5058 (bypasses router multicast filtering)
 * 3. Dynamic local subnet detection (Same Wi-Fi network vs. Another network)
 */
class NsdDiscoveryManager(context: Context) {

    companion object {
        private const val TAG = "NsdDiscovery"
        private const val SERVICE_TYPE = "_wifiprint._tcp."
        private const val UDP_BEACON_PORT = 5058
    }

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val networkMonitor = NetworkMonitor(context)

    private val _discoveredServers = MutableStateFlow<List<ServerInfo>>(emptyList())
    val discoveredServers: StateFlow<List<ServerInfo>> = _discoveredServers

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val serverMap = mutableMapOf<String, ServerInfo>()
    private var isDiscovering = false

    private val resolveQueue = ArrayDeque<NsdServiceInfo>()
    private var isResolving = false

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var udpJob: Job? = null

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(serviceType: String) {
            _isSearching.value = true
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, "mDNS Service found: ${serviceInfo.serviceName}")
            queueResolve(serviceInfo)
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, "Service lost: ${serviceInfo.serviceName}")
            synchronized(serverMap) {
                serverMap.remove(serviceInfo.serviceName)
                _discoveredServers.value = serverMap.values.toList()
            }
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

    private fun queueResolve(serviceInfo: NsdServiceInfo) {
        synchronized(resolveQueue) {
            resolveQueue.add(serviceInfo)
            if (!isResolving) {
                processNextResolve()
            }
        }
    }

    private fun processNextResolve() {
        synchronized(resolveQueue) {
            val next = resolveQueue.poll()
            if (next == null) {
                isResolving = false
                return
            }
            isResolving = true
            try {
                nsdManager.resolveService(next, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.e(TAG, "Resolve failed for ${serviceInfo.serviceName}: $errorCode")
                        synchronized(resolveQueue) { processNextResolve() }
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        val host = serviceInfo.host?.hostAddress
                        if (host == null) {
                            synchronized(resolveQueue) { processNextResolve() }
                            return
                        }
                        val port = serviceInfo.port
                        val name = serviceInfo.serviceName
                        val isSameNet = networkMonitor.isSameLocalSubnet(host)

                        val server = ServerInfo(
                            id = "$host:$port",
                            name = name,
                            ipAddress = host,
                            port = port
                        ).apply {
                            isSameNetwork = isSameNet
                        }

                        synchronized(serverMap) {
                            serverMap[name] = server
                            _discoveredServers.value = serverMap.values.toList()
                        }

                        synchronized(resolveQueue) { processNextResolve() }
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "resolveService error", e)
                processNextResolve()
            }
        }
    }

    private fun startUdpBeaconListener() {
        udpJob?.cancel()
        udpJob = scope.launch {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket(UDP_BEACON_PORT).apply {
                    broadcast = true
                    reuseAddress = true
                    soTimeout = 3000
                }
                val buffer = ByteArray(2048)
                val packet = DatagramPacket(buffer, buffer.size)

                while (isActive) {
                    try {
                        socket.receive(packet)
                        val text = String(packet.data, 0, packet.length, Charsets.UTF_8)
                        val json = JSONObject(text)
                        if (json.optString("service") == "wifiprint") {
                            val ip = json.optString("ip", packet.address?.hostAddress ?: "")
                            val port = json.optInt("port", 5000)
                            val name = json.optString("name", "SpoolDrop Server")

                            if (ip.isNotBlank()) {
                                val isSameNet = networkMonitor.isSameLocalSubnet(ip)
                                val server = ServerInfo(
                                    id = "$ip:$port",
                                    name = name,
                                    ipAddress = ip,
                                    port = port
                                ).apply {
                                    isSameNetwork = isSameNet
                                }

                                synchronized(serverMap) {
                                    serverMap[name] = server
                                    _discoveredServers.value = serverMap.values.toList()
                                }
                            }
                        }
                    } catch (_: SocketTimeoutException) {
                        // Normal timeout
                    } catch (e: Exception) {
                        Log.d(TAG, "UDP receive error: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "UDP socket listener failed to bind: ${e.message}")
            } finally {
                socket?.close()
            }
        }
    }

    /** Start scanning for WiFi Print servers on the local network via mDNS and UDP beacon. */
    fun startDiscovery() {
        if (isDiscovering) return
        try {
            synchronized(serverMap) {
                serverMap.clear()
                _discoveredServers.value = emptyList()
            }
            startUdpBeaconListener()
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
            udpJob?.cancel()
            udpJob = null
            nsdManager.stopServiceDiscovery(discoveryListener)
            isDiscovering = false
            _isSearching.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop discovery", e)
        }
    }

    /** Manually add a server by IP address. */
    fun addManualServer(ip: String, port: Int = 5000): ServerInfo {
        val isSameNet = networkMonitor.isSameLocalSubnet(ip)
        val server = ServerInfo(
            id = "$ip:$port",
            name = "Manual Server ($ip)",
            ipAddress = ip,
            port = port
        ).apply {
            isSameNetwork = isSameNet
        }

        synchronized(serverMap) {
            serverMap[server.id] = server
            _discoveredServers.value = serverMap.values.toList()
        }
        return server
    }
}
