package com.wifiprint.app.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Monitors WiFi network state and provides real-time connectivity info.
 * Detects WiFi connect/disconnect events so the app can update connection status.
 */
class NetworkMonitor(private val context: Context) {

    companion object {
        private const val TAG = "NetworkMonitor"
    }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _isWifiConnected = MutableStateFlow(checkWifiConnected())
    val isWifiConnected: StateFlow<Boolean> = _isWifiConnected

    private val _wifiSsid = MutableStateFlow(getCurrentSsid())
    val wifiSsid: StateFlow<String> = _wifiSsid

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    /**
     * Start listening for WiFi state changes.
     */
    fun startMonitoring() {
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isWifiConnected.value = true
                _wifiSsid.value = getCurrentSsid()
            }

            override fun onLost(network: Network) {
                _isWifiConnected.value = false
                _wifiSsid.value = ""
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                _isWifiConnected.value = hasWifi
                if (hasWifi) _wifiSsid.value = getCurrentSsid()
            }
        }

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
    }

    /**
     * Stop listening for WiFi state changes.
     */
    fun stopMonitoring() {
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unregister network callback", e)
            }
        }
        networkCallback = null
    }

    /**
     * Check current WiFi connection state.
     */
    fun checkWifiConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Check if device has active internet access (via WiFi, Cellular, or Ethernet).
     */
    fun checkInternetConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Get the current WiFi SSID (network name).
     */
    @Suppress("DEPRECATION")
    fun getCurrentSsid(): String {
        return try {
            val info = wifiManager.connectionInfo
            val ssid = info.ssid ?: ""
            // Remove surrounding quotes
            ssid.trim('"')
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Get the device's local IPv4 address on the current Wi-Fi network.
     * Prefers active Wi-Fi IPv4 address first, then falls back to non-loopback interfaces.
     */
    fun getLocalIpAddress(): String? {
        // 1. Prefer Wi-Fi IPv4 address when Wi-Fi is connected
        @Suppress("DEPRECATION")
        try {
            val ipInt = wifiManager.connectionInfo?.ipAddress ?: 0
            if (ipInt != 0) {
                return String.format(
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
            }
        } catch (_: Exception) {}

        // 2. Fall back to active non-loopback IPv4 network interfaces
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces() ?: return null
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is java.net.Inet4Address && !addr.isLoopbackAddress) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (_: Exception) {}

        return null
    }

    /**
     * Checks if a target server IP address is on the same local network subnet as this Android device.
     */
    fun isSameLocalSubnet(serverIp: String): Boolean {
        if (serverIp == "localhost" || serverIp == "127.0.0.1") return true

        val localIp = getLocalIpAddress() ?: return false
        val localParts = localIp.split('.')
        val serverParts = serverIp.split('.')
        if (localParts.size == 4 && serverParts.size == 4) {
            // Match /24 subnet (first 3 octets, e.g. 192.168.1.x)
            if (localParts[0] == serverParts[0] &&
                localParts[1] == serverParts[1] &&
                localParts[2] == serverParts[2]) {
                return true
            }
            // For 10.x.x.x or 172.16.x.x networks, check matching prefix
            if (localParts[0] == "10" && serverParts[0] == "10") return true
            if (localParts[0] == "172" && serverParts[0] == "172" && localParts[1] == serverParts[1]) return true
        }

        return false
    }
}
