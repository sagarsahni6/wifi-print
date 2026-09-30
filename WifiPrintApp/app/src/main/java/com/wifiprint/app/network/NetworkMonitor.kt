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
     */
    @Suppress("DEPRECATION")
    fun getLocalIpAddress(): String? {
        return try {
            val ipInt = wifiManager.connectionInfo?.ipAddress ?: return null
            if (ipInt == 0) return null
            String.format(
                "%d.%d.%d.%d",
                ipInt and 0xff,
                ipInt shr 8 and 0xff,
                ipInt shr 16 and 0xff,
                ipInt shr 24 and 0xff
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Checks if a target server IP address is on the same local network subnet as this Android device.
     */
    fun isSameLocalSubnet(serverIp: String): Boolean {
        val localIp = getLocalIpAddress() ?: return false // Default false if unable to determine — safer than assuming same network
        if (serverIp == "localhost" || serverIp == "127.0.0.1") return true

        val localParts = localIp.split('.')
        val serverParts = serverIp.split('.')
        if (localParts.size == 4 && serverParts.size == 4) {
            // Check matching /24 prefix (first 3 octets, e.g. 192.168.1.X)
            return localParts[0] == serverParts[0] &&
                    localParts[1] == serverParts[1] &&
                    localParts[2] == serverParts[2]
        }
        return false
    }
}
