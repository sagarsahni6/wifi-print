package com.wifiprint.app.ui.screens.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifiprint.app.data.models.ServerInfo
import com.wifiprint.app.ui.screens.discovery.ConnectViewModel
import com.wifiprint.app.ui.screens.discovery.DiscoveryViewModel
import com.wifiprint.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPrint: () -> Unit,
    onNavigateToJobs: () -> Unit,
    onNavigateToPrinters: () -> Unit,
    onNavigateToScanner: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToTemplates: () -> Unit = {},
    onNavigateToDiscovery: () -> Unit = {},
    onNavigateToQrScanner: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    discoveryViewModel: DiscoveryViewModel = hiltViewModel(),
    connectViewModel: ConnectViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val discoveredServers by discoveryViewModel.discoveredServers.collectAsState()
    val isSearching by discoveryViewModel.isSearching.collectAsState()
    val connectState by connectViewModel.state.collectAsState()
    val context = LocalContext.current

    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Auto-discover servers when not connected
    var hasNearbyWifiPermission by remember {
        mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
    }
    val nearbyPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNearbyWifiPermission = granted
        if (granted) discoveryViewModel.startDiscovery()
    }

    LaunchedEffect(state.isConnected) {
        if (!state.isConnected) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                discoveryViewModel.startDiscovery()
            } else {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.NEARBY_WIFI_DEVICES
                ) == PackageManager.PERMISSION_GRANTED
                hasNearbyWifiPermission = granted
                if (granted) discoveryViewModel.startDiscovery()
            }
        }
    }

    // Navigate home when connected via QR/discovery from home
    LaunchedEffect(connectState.isConnected) {
        if (connectState.isConnected) {
            viewModel.refreshConnectionStatus()
        }
    }

    DisposableEffect(Unit) {
        onDispose { discoveryViewModel.stopDiscovery() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Gradient Hero Header ────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GradientStart,
                            GradientEnd
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 32.dp)
        ) {
            Column {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "WiFi Print",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-1).sp
                            ),
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Print wirelessly from your device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                    // Connection status indicator
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (state.isConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                                contentDescription = "Connection status",
                                tint = if (state.isConnected) Green400 else Orange400,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Connection status card inside hero — tap to connect
                Surface(
                    modifier = Modifier.clickable {
                        if (!state.isConnected) onNavigateToDiscovery()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val pulse = rememberInfiniteTransition(label = "pulse")
                        val alpha by pulse.animateFloat(
                            initialValue = 0.6f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                tween(1000), RepeatMode.Reverse
                            ),
                            label = "pulseAlpha"
                        )

                        Surface(
                            shape = CircleShape,
                            color = (if (state.isConnected) Green400 else Orange400).copy(
                                alpha = if (state.isConnected) alpha else 0.8f
                            ),
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                when {
                                    state.isConnected -> "Connected"
                                    state.isConnecting -> "Connecting..."
                                    !state.isWifiConnected -> "WiFi Disconnected"
                                    else -> "Not Connected"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                            Text(
                                when {
                                    state.isConnected -> state.serverName
                                    state.isConnecting -> "Verifying server..."
                                    !state.isWifiConnected -> "Turn on WiFi to print"
                                    state.connectionMessage != null -> state.connectionMessage!!
                                    state.wifiSsid.isNotEmpty() -> "On ${state.wifiSsid} • Tap to connect"
                                    else -> "Tap to connect to a server"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                        when {
                            state.isConnected -> Icon(Icons.Filled.CheckCircle, null,
                                tint = Green400, modifier = Modifier.size(20.dp))
                            state.isConnecting -> CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White.copy(alpha = 0.7f))
                            !state.isWifiConnected -> Icon(Icons.Filled.WifiOff, null,
                                tint = Orange400, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // ── Stats Row ───────────────────────────────────────────────
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400, delayMillis = 100)) + slideInVertically(
                tween(400, delayMillis = 100), initialOffsetY = { it / 4 }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-16).dp)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(
                    Modifier.weight(1f),
                    value = "${state.recentJobs.count { it.status == "Completed" }}",
                    label = "Printed",
                    icon = Icons.Filled.CheckCircle,
                    color = Green400
                )
                StatChip(
                    Modifier.weight(1f),
                    value = "${state.recentJobs.count { it.status == "Pending" || it.status == "Printing" }}",
                    label = "In Queue",
                    icon = Icons.Filled.Schedule,
                    color = Cyan400
                )
                StatChip(
                    Modifier.weight(1f),
                    value = "${state.recentJobs.size}",
                    label = "Total",
                    icon = Icons.Filled.Summarize,
                    color = Secondary
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // ── WiFi Warning Banner ─────────────────────────────────
            if (!state.isWifiConnected) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Orange400.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.WifiOff, null, tint = Orange400,
                            modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("WiFi Not Connected",
                                fontWeight = FontWeight.SemiBold,
                                color = Orange400)
                            Text("Connect to the same WiFi network as your PC to print",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ── Connect to Server Section (when not connected) ──────
            if (!state.isConnected && state.isWifiConnected) {
                // Compact QR Scanner Button
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Primary.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToQrScanner() }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.QrCodeScanner, null,
                                    tint = Primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Scan QR Code",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold)
                            Text("Scan the code on your PC server dashboard",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Filled.ChevronRight, null,
                            tint = Primary, modifier = Modifier.size(20.dp))
                    }
                }

                // Connection error from QR/discovery
                if (connectState.error != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Red400.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Icon(Icons.Filled.Error, null, tint = Red400,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(connectState.error!!, color = Red400,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Scanning indicator
                if (isSearching) {
                    val infiniteTransition = rememberInfiniteTransition(label = "scan")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f, targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing)
                        ), label = "scanRotation"
                    )
                    Row(
                        modifier = Modifier.padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Wifi, null,
                            tint = Tertiary, modifier = Modifier.size(16.dp).rotate(rotation))
                        Spacer(Modifier.width(8.dp))
                        Text("Scanning network...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Discovered Servers
                if (discoveredServers.isNotEmpty()) {
                    Text("Discovered Servers",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp))

                    discoveredServers.forEach { server ->
                        val isConnecting = connectState.isConnecting && connectState.connectingTo == server.id
                        Card(
                            onClick = { connectViewModel.connectToServer(server) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            enabled = !connectState.isConnecting,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Primary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Computer, null,
                                            tint = Primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(server.name, fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium)
                                    Text("${server.ipAddress}:${server.port}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isConnecting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Filled.ChevronRight, null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
            }

            // ── Quick Actions ────────────────────────────────────────
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            // Row 1 — Primary actions with staggered entrance
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 200)) + slideInVertically(
                    tween(400, delayMillis = 200), initialOffsetY = { it / 4 }
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Print File — gradient accent card
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Print,
                        label = "Print File",
                        subtitle = "PDF, Images, Docs",
                        color = Primary,
                        isGradient = true,
                        onClick = onNavigateToPrint
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.DocumentScanner,
                        label = "Scan Doc",
                        subtitle = "Camera to PDF",
                        color = Tertiary,
                        onClick = onNavigateToScanner
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Row 2
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 300)) + slideInVertically(
                    tween(400, delayMillis = 300), initialOffsetY = { it / 4 }
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Badge,
                        label = "Templates",
                        subtitle = "ID / Passport",
                        color = Secondary,
                        onClick = onNavigateToTemplates
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Print,
                        label = "Printers",
                        subtitle = "Manage & Health",
                        color = Cyan400,
                        onClick = onNavigateToPrinters
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Row 3
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400, delayMillis = 400)) + slideInVertically(
                    tween(400, delayMillis = 400), initialOffsetY = { it / 4 }
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.History,
                        label = "Job Queue",
                        subtitle = "View & Manage",
                        color = Orange400,
                        onClick = onNavigateToJobs
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Settings,
                        label = "Settings",
                        subtitle = "App Config",
                        color = Color(0xFF78909C),
                        onClick = onNavigateToSettings
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Recent Activity ──────────────────────────────────────
            if (state.recentJobs.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    TextButton(onClick = onNavigateToJobs) {
                        Text("See All")
                        Icon(Icons.Filled.ChevronRight, null, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))

                state.recentJobs.take(5).forEachIndexed { index, job ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.animateContentSize()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // File type icon with colored background
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when (job.fileType) {
                                    "PDF" -> Red400.copy(alpha = 0.1f)
                                    "Image" -> Cyan400.copy(alpha = 0.1f)
                                    else -> Secondary.copy(alpha = 0.1f)
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        when (job.fileType) {
                                            "PDF" -> Icons.Filled.PictureAsPdf
                                            "Image" -> Icons.Filled.Image
                                            else -> Icons.Filled.Description
                                        },
                                        null,
                                        tint = when (job.fileType) {
                                            "PDF" -> Red400
                                            "Image" -> Cyan400
                                            else -> Secondary
                                        },
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    job.fileName,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                                Text(
                                    job.printerName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            val statusColor = when (job.status) {
                                "Completed" -> Green400
                                "Failed" -> Red400
                                "Printing" -> Cyan400
                                "Paused" -> Secondary
                                else -> Orange400
                            }
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = statusColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    job.status,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = statusColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    if (index < state.recentJobs.take(5).size - 1) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            } else {
                // Empty state
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.PrintDisabled,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No print jobs yet",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Start by printing a file or scanning a document",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // ── Cross-Promotion: Download PC Server ─────────────────
            if (!state.isConnected) {
                val context = LocalContext.current
                Spacer(Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Primary.copy(alpha = 0.08f),
                                        Secondary.copy(alpha = 0.10f)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.Computer,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Need the PC Server?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Download & install the free WiFi Print Server on your Windows PC to start printing wirelessly.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://wifiprint.app/#download")
                                    )
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Download Server",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Stat Chip Component ─────────────────────────────────────────────────

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Quick Action Card Component ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    subtitle: String = "",
    color: Color,
    isGradient: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = if (isGradient) {
        Primary.copy(alpha = 0.12f)
    } else {
        color.copy(alpha = 0.08f)
    }

    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isGradient) 2.dp else 0.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle gradient overlay for primary card
            if (isGradient) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    GradientStart.copy(alpha = 0.05f),
                                    GradientEnd.copy(alpha = 0.08f)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = color.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (subtitle.isNotEmpty()) {
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
