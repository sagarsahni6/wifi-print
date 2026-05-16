package com.wifiprint.app.ui.screens.discovery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifiprint.app.data.models.ServerInfo
import com.wifiprint.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    onConnected: () -> Unit,
    onBack: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    viewModel: DiscoveryViewModel = hiltViewModel(),
    connectViewModel: ConnectViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val discoveredServers by viewModel.discoveredServers.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val connectState by connectViewModel.state.collectAsState()


    var hasNearbyWifiPermission by remember { mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) }

    val nearbyPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNearbyWifiPermission = granted
        if (granted) viewModel.startDiscovery()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            viewModel.startDiscovery()
        } else {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED
            hasNearbyWifiPermission = granted
            if (granted) viewModel.startDiscovery()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopDiscovery() }
    }

    LaunchedEffect(connectState.isConnected) {
        if (connectState.isConnected) onConnected()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Gradient Header ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Column {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        "Connect to Server",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White,
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    )
                    IconButton(onClick = {
                        viewModel.stopDiscovery()
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || hasNearbyWifiPermission) {
                            viewModel.startDiscovery()
                        } else {
                            nearbyPermissionLauncher.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
                        }
                    }) {
                        Icon(Icons.Filled.Refresh, "Rescan", tint = Color.White)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Find and connect to your print server",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(start = 48.dp)
                )
            }
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ═══════════════════════════════════════════
            //  PRIMARY: QR Code Connection
            // ═══════════════════════════════════════════
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.QrCodeScanner, null,
                                modifier = Modifier.size(36.dp),
                                tint = Primary
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Scan QR Code to Connect",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Open WiFi Print Server on your PC, find the QR code on the Dashboard, and scan it here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = onNavigateToQrScanner,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        enabled = !connectState.isConnecting,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 6.dp
                        )
                    ) {
                        if (connectState.isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Connecting — Accept on PC...")
                        } else {
                            Icon(Icons.Filled.QrCodeScanner, null)
                            Spacer(Modifier.width(10.dp))
                            Text("Scan QR Code", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Connection error
            if (connectState.error != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Red400.copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Filled.Error, null, tint = Red400)
                        Spacer(Modifier.width(12.dp))
                        Text(connectState.error!!, color = Red400,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // ═══════════════════════════════════════════
            //  Nearby permission warning
            // ═══════════════════════════════════════════
            if (!hasNearbyWifiPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Orange400.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nearby device permission required", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text("Android needs nearby Wi-Fi access to discover print servers on your LAN.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                nearbyPermissionLauncher.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Grant Permission") }
                    }
                }
            }

            // ═══════════════════════════════════════════
            //  Auto-discovered servers with scanning animation
            // ═══════════════════════════════════════════
            if (isSearching) {
                val infiniteTransition = rememberInfiniteTransition(label = "scan")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing)
                    ),
                    label = "scanRotation"
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Tertiary.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Wifi, null,
                            tint = Tertiary,
                            modifier = Modifier.size(24.dp).rotate(rotation)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Scanning network...", fontWeight = FontWeight.Medium)
                            Text("Looking for WiFi Print servers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (discoveredServers.isNotEmpty()) {
                Text("Discovered Servers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                discoveredServers.forEach { server ->
                    ServerCard(
                        server = server,
                        isConnecting = connectState.isConnecting && connectState.connectingTo == server.id,
                        onClick = { connectViewModel.connectToServer(server) }
                    )
                }
            }

            // ═══════════════════════════════════════════
            //  Cross-Promo: Don't have the server?
            // ═══════════════════════════════════════════
            if (discoveredServers.isEmpty() && !isSearching) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Orange400.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Orange400.copy(alpha = 0.12f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Computer, null,
                                    tint = Orange400,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Don't have the server yet?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "You need to download and install the free WiFi Print Server on your Windows PC before you can print.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(14.dp))
                        val context = LocalContext.current
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://wifiprint.app/#download")
                                )
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Filled.Download, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Download PC Server", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════
            //  How to connect guide
            // ═══════════════════════════════════════════
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.06f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("How to Connect", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    HelpStep("1", "Run WiFi Print Server on your Windows PC")
                    HelpStep("2", "Ensure both phone and PC are on the same WiFi")
                    HelpStep("3", "Scan the QR code shown on the server dashboard")
                    HelpStep("4", "Accept the connection request on your PC")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerCard(
    server: ServerInfo,
    isConnecting: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        enabled = !isConnecting
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Primary.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Computer, null, tint = Primary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(server.name, fontWeight = FontWeight.SemiBold)
                Text("${server.ipAddress}:${server.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isConnecting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.ChevronRight, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HelpStep(number: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Primary.copy(alpha = 0.12f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(number, style = MaterialTheme.typography.labelSmall,
                    color = Primary, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}
