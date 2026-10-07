package com.wifiprint.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifiprint.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val currentThemeMode by ThemeManager.themeMode

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
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column {
                Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBack()
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White
                        )
                        Text(
                            "Personalize your appearance and printing preferences",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Appearance / Dark Mode Selector ────────────────────────
            SettingsGroup(
                title = "Appearance & Theme",
                icon = Icons.Filled.Palette,
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    "Choose how Printora looks on your device",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        modifier = Modifier.weight(1f),
                        label = "System",
                        icon = Icons.Filled.BrightnessAuto,
                        selected = currentThemeMode == ThemeMode.SYSTEM,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            ThemeManager.setThemeMode(context, ThemeMode.SYSTEM)
                        }
                    )
                    ThemeOptionButton(
                        modifier = Modifier.weight(1f),
                        label = "Light",
                        icon = Icons.Filled.LightMode,
                        selected = currentThemeMode == ThemeMode.LIGHT,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            ThemeManager.setThemeMode(context, ThemeMode.LIGHT)
                        }
                    )
                    ThemeOptionButton(
                        modifier = Modifier.weight(1f),
                        label = "Dark",
                        icon = Icons.Filled.DarkMode,
                        selected = currentThemeMode == ThemeMode.DARK,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            ThemeManager.setThemeMode(context, ThemeMode.DARK)
                        }
                    )
                }
            }

            // ── Connection ─────────────────────────────────────────────
            SettingsGroup(title = "Connection", icon = Icons.Filled.Wifi, color = Tertiary) {
                SettingsToggleItem(
                    icon = Icons.Filled.WifiFind,
                    title = "Auto-connect",
                    subtitle = "Reconnect to last server automatically on startup",
                    checked = autoConnect,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        autoConnect = it
                    },
                    iconTint = Tertiary
                )
            }

            // ── Notifications ──────────────────────────────────────────
            SettingsGroup(title = "Notifications", icon = Icons.Filled.Notifications, color = Orange400) {
                SettingsToggleItem(
                    icon = Icons.Filled.NotificationsActive,
                    title = "Print Notifications",
                    subtitle = "Get notified when jobs complete, stall, or fail",
                    checked = notifications,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        notifications = it
                    },
                    iconTint = Orange400
                )
            }

            // ── Print & Preview ────────────────────────────────────────
            SettingsGroup(title = "Print & Preview", icon = Icons.Filled.Print, color = Cyan400) {
                SettingsToggleItem(
                    icon = Icons.Filled.HighQuality,
                    title = "High-Quality Preview",
                    subtitle = "Render 2x supersampled PDF page previews",
                    checked = highQualityPreview,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        highQualityPreview = it
                    },
                    iconTint = Cyan400
                )
                Divider(
                    modifier = Modifier.padding(start = 52.dp, top = 4.dp, bottom = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                SettingsToggleItem(
                    icon = Icons.Filled.CleaningServices,
                    title = "Auto-Clean Scans",
                    subtitle = "Safely prune temporary scans after print submission",
                    checked = autoCleanScans,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        autoCleanScans = it
                    },
                    iconTint = Red400
                )
            }

            // ── About ──────────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Info, null, tint = Primary, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "About Printora",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    AboutInfoRow("App Name", "Printora: Cloud Print & Scan")
                    AboutInfoRow("Version", "2.2.0 (Pro Max Edition)")
                    AboutInfoRow("Security", "AES-256 E2EE & TLS")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Print effortlessly from your Android device to any PC printer via local network or cloud relay. " +
                                "Features document scanning, batch printing, print templates, and advanced cloud queue management.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            // ── Get PC Server ──────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Tertiary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Computer, null, tint = Tertiary, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "PC Companion Server",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Printora requires a lightweight companion server running on your Windows 10/11 PC. " +
                                "Download it free to enable cloud & local printing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Theme Option Segment Button ──────────────────────────────────────

@Composable
private fun ThemeOptionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val containerColor by animateColorAsState(
        targetValue = if (selected) primaryColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        label = "theme_btn_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "theme_btn_tint"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) primaryColor else outlineColor,
        label = "theme_btn_border"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

// ── Settings Group ───────────────────────────────────────────────────

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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.animateContentSize()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = color.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

// ── Settings Toggle Item ─────────────────────────────────────────────

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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = iconTint.copy(alpha = 0.12f),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

// ── About Info Row ───────────────────────────────────────────────────

@Composable
private fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
