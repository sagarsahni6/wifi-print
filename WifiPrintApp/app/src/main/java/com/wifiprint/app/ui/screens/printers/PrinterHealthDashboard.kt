package com.wifiprint.app.ui.screens.printers

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wifiprint.app.data.models.PrinterInfo
import com.wifiprint.app.ui.theme.*

/**
 * Printer Health Dashboard — shows toner/ink levels, paper status, and diagnostics.
 * Displayed as a bottom sheet or dialog from the Printer List screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterHealthDashboard(
    printer: PrinterInfo,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Printer Health", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Text(printer.name, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusChip(printer.status)
        }

        Divider()

        // ── Toner / Ink Levels ──────────────────────────────────────
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Opacity, null, tint = Purple600)
                    Spacer(Modifier.width(8.dp))
                    Text("Supply Levels", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(16.dp))

                if (printer.tonerLevelPercent != null) {
                    // Laser printer — single toner
                    SupplyLevel("Toner", printer.tonerLevelPercent, Color(0xFF2D2D2D))
                }

                if (printer.inkLevelBlack != null) {
                    SupplyLevel("Black", printer.inkLevelBlack, Color(0xFF2D2D2D))
                }
                if (printer.inkLevelCyan != null) {
                    SupplyLevel("Cyan", printer.inkLevelCyan, Color(0xFF00BCD4))
                }
                if (printer.inkLevelMagenta != null) {
                    SupplyLevel("Magenta", printer.inkLevelMagenta, Color(0xFFE91E63))
                }
                if (printer.inkLevelYellow != null) {
                    SupplyLevel("Yellow", printer.inkLevelYellow, Color(0xFFFFC107))
                }

                if (printer.tonerLevelPercent == null && printer.inkLevelBlack == null) {
                    Text("Supply levels not available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // ── Paper Tray ──────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Description, null, tint = Orange400)
                    Spacer(Modifier.width(8.dp))
                    Text("Paper Tray", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))

                val trayColor = when (printer.paperTrayStatus) {
                    "OK" -> Green400
                    "Low" -> Orange400
                    "Empty" -> Red400
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val trayIcon = when (printer.paperTrayStatus) {
                    "OK" -> Icons.Filled.CheckCircle
                    "Low" -> Icons.Filled.Warning
                    "Empty" -> Icons.Filled.Error
                    else -> Icons.Filled.HelpOutline
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(trayIcon, null, tint = trayColor, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(printer.paperTrayStatus, fontWeight = FontWeight.SemiBold, color = trayColor)
                        Text(
                            when (printer.paperTrayStatus) {
                                "OK" -> "Paper tray is loaded and ready"
                                "Low" -> "Paper is running low — add more soon"
                                "Empty" -> "Paper tray is empty — add paper immediately"
                                else -> "Status unknown"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ── Statistics ──────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.BarChart, null, tint = Cyan400)
                    Spacer(Modifier.width(8.dp))
                    Text("Statistics", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Total Pages", "${printer.totalPagesPrinted}")
                    StatItem("Paper Sizes", "${printer.supportedPaperSizes.size}")
                    StatItem("Duplex", if (printer.supportsDuplex) "Yes" else "No")
                    StatItem("Color", if (printer.supportsColor) "Yes" else "No")
                }
            }
        }

        // ── Last Error ──────────────────────────────────────────────
        if (printer.lastError != null) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Red400.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ErrorOutline, null, tint = Red400)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Last Error", fontWeight = FontWeight.SemiBold, color = Red400)
                        Text(printer.lastError, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Close button
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Close") }
    }
}

@Composable
private fun SupplyLevel(label: String, level: Int, color: Color) {
    val animatedLevel by animateFloatAsState(
        targetValue = level / 100f,
        animationSpec = tween(1000), label = "supply_$label"
    )
    val levelColor = when {
        level > 50 -> Green400
        level > 20 -> Orange400
        else -> Red400
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color circle
        Canvas(modifier = Modifier.size(16.dp)) {
            drawCircle(color = color, radius = size.minDimension / 2)
        }
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.width(64.dp), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        // Progress bar
        Box(modifier = Modifier.weight(1f).height(10.dp)) {
            LinearProgressIndicator(
                progress = animatedLevel,
                modifier = Modifier.fillMaxSize(),
                color = levelColor,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text("$level%", style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold, color = levelColor)
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status) {
        "Ready" -> Green400
        "Busy" -> Orange400
        "Offline" -> MaterialTheme.colorScheme.onSurfaceVariant
        "Error", "PaperJam", "OutOfPaper" -> Red400
        "TonerLow" -> Orange400
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = color, style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold)
    }
}
