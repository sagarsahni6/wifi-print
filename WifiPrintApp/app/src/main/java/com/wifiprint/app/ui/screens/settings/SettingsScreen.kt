package com.wifiprint.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifiprint.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var darkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    var autoConnect by remember { mutableStateOf(true) }
    var highQualityPreview by remember { mutableStateOf(true) }
    var autoCleanScans by remember { mutableStateOf(false) }

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
                .padding(horizontal = 20.dp, vertical = 32.dp)
        ) {
            Column {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    ),
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Customize your printing experience",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Appearance ──────────────────────────────────────────
            SettingsGroup(title = "Appearance", icon = Icons.Filled.Palette, color = Primary) {
                SettingsToggleItem(
                    icon = Icons.Filled.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Switch to dark theme",
                    checked = darkMode,
                    onCheckedChange = { darkMode = it },
                    iconTint = Secondary
                )
            }

            // ── Connection ──────────────────────────────────────────
            SettingsGroup(title = "Connection", icon = Icons.Filled.Wifi, color = Tertiary) {
                SettingsToggleItem(
                    icon = Icons.Filled.WifiFind,
                    title = "Auto-connect",
                    subtitle = "Reconnect to last server automatically",
                    checked = autoConnect,
                    onCheckedChange = { autoConnect = it },
                    iconTint = Tertiary
                )
            }

            // ── Notifications ───────────────────────────────────────
            SettingsGroup(title = "Notifications", icon = Icons.Filled.Notifications, color = Orange400) {
                SettingsToggleItem(
                    icon = Icons.Filled.NotificationsActive,
                    title = "Print Notifications",
                    subtitle = "Get notified when jobs complete or fail",
                    checked = notifications,
                    onCheckedChange = { notifications = it },
                    iconTint = Orange400
                )
            }

            // ── Print & Preview ─────────────────────────────────────
            SettingsGroup(title = "Print & Preview", icon = Icons.Filled.Print, color = Cyan400) {
                SettingsToggleItem(
                    icon = Icons.Filled.HighQuality,
                    title = "High-Quality Preview",
                    subtitle = "Use 2x resolution for PDF previews",
                    checked = highQualityPreview,
                    onCheckedChange = { highQualityPreview = it },
                    iconTint = Cyan400
                )
                Divider(modifier = Modifier.padding(start = 52.dp), color = DividerColor)
                SettingsToggleItem(
                    icon = Icons.Filled.CleaningServices,
                    title = "Auto-Clean Scans",
                    subtitle = "Remove scanned files after printing",
                    checked = autoCleanScans,
                    onCheckedChange = { autoCleanScans = it },
                    iconTint = Red400
                )
            }

            // ── About ───────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Info, null, tint = Primary,
                                    modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("About", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(14.dp))
                    AboutInfoRow("App Name", "WiFi Print")
                    AboutInfoRow("Version", "2.0.0")
                    AboutInfoRow("Build", "Release")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Print wirelessly from your Android device to any PC printer over Wi-Fi. " +
                                "Features document scanning, batch printing, print templates, and advanced queue management.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            // ── Get PC Server ────────────────────────────────────────
            val context = LocalContext.current
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Tertiary.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Computer, null, tint = Tertiary,
                                    modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("PC Server", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "WiFi Print requires a companion server running on your Windows 10/11 PC. " +
                                "Download it free from our website.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://wifiprint.app/#download")
                            )
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Download, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Download PC Server", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Settings Group ──────────────────────────────────────────────────────

@Composable
private fun SettingsGroup(
    title: String,
    icon: ImageVector,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = color.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ── Settings Toggle Item ────────────────────────────────────────────────

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconTint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ── About Info Row ──────────────────────────────────────────────────────

@Composable
private fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold)
    }
}
