package com.wifiprint.app.ui.screens.home

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifiprint.app.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToPrint: () -> Unit,
    onNavigateToJobs: () -> Unit,
    onNavigateToPrinters: () -> Unit,
    onNavigateToScanner: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToTemplates: () -> Unit = {},
    onNavigateToDiscovery: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

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
                            Primary,
                            PrimaryDark.copy(alpha = 0.9f)
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
                    shape = RoundedCornerShape(14.dp),
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
                color = Purple400
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // ── WiFi Warning Banner ─────────────────────────────────
            if (!state.isWifiConnected) {
                Card(
                    shape = RoundedCornerShape(14.dp),
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
            } else if (!state.isConnected && state.wifiSsid.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Cyan400.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        .clickable { onNavigateToDiscovery() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Wifi, null, tint = Cyan400,
                            modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Connected to ${state.wifiSsid}",
                                fontWeight = FontWeight.SemiBold)
                            Text("Tap to connect to a print server",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Filled.ChevronRight, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ── Quick Actions ────────────────────────────────────────
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            // Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Print,
                    label = "Print File",
                    subtitle = "PDF, Images, Docs",
                    color = Primary,
                    onClick = onNavigateToPrint
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.DocumentScanner,
                    label = "Scan Doc",
                    subtitle = "Camera to PDF",
                    color = Accent,
                    onClick = onNavigateToScanner
                )
            }

            Spacer(Modifier.height(10.dp))

            // Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Badge,
                    label = "Templates",
                    subtitle = "ID / Passport",
                    color = Color(0xFF7E57C2),
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

            Spacer(Modifier.height(10.dp))

            // Row 3
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
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
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
                                    else -> Purple400.copy(alpha = 0.1f)
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
                                            else -> Purple400
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
                                "Paused" -> Purple400
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
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
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
        shape = RoundedCornerShape(14.dp),
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
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
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
