package com.wifiprint.app.ui.screens.printers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifiprint.app.data.models.PrinterInfo
import com.wifiprint.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterListScreen(
    onBack: () -> Unit,
    viewModel: PrinterViewModel = hiltViewModel()
) {
    val printers by viewModel.printers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Health dashboard bottom sheet
    var selectedPrinterForHealth by remember { mutableStateOf<PrinterInfo?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (selectedPrinterForHealth != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedPrinterForHealth = null },
            sheetState = sheetState
        ) {
            PrinterHealthDashboard(
                printer = selectedPrinterForHealth!!,
                onDismiss = { selectedPrinterForHealth = null }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Printers") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPrinters() }) {
                        Icon(Icons.Filled.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (printers.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.PrintDisabled, null, Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("No printers found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Make sure you're connected to a server",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(printers) { printer ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        onClick = { selectedPrinterForHealth = printer }
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Print, null, tint = Purple600, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(printer.name, fontWeight = FontWeight.SemiBold)
                                        if (printer.isDefault) {
                                            Spacer(Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Cyan400.copy(alpha = 0.15f)
                                            ) {
                                                Text("Default",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    color = Cyan400, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                    Text(printer.driverName, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (printer.supportsColor) {
                                            AssistChip(onClick = {}, label = { Text("Color") },
                                                leadingIcon = { Icon(Icons.Filled.Palette, null, Modifier.size(14.dp)) })
                                        }
                                        if (printer.supportsDuplex) {
                                            AssistChip(onClick = {}, label = { Text("Duplex") },
                                                leadingIcon = { Icon(Icons.Filled.FlipToBack, null, Modifier.size(14.dp)) })
                                        }
                                    }
                                }
                                // Online status dot
                                Icon(
                                    Icons.Filled.Circle,
                                    contentDescription = if (printer.isOnline) "Online" else "Offline",
                                    tint = if (printer.isOnline) Green400 else Red400,
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            // Quick health indicators
                            if (printer.tonerLevelPercent != null || printer.inkLevelBlack != null) {
                                Spacer(Modifier.height(12.dp))
                                Divider()
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    printer.tonerLevelPercent?.let { level ->
                                        QuickHealthChip("Toner", level)
                                    }
                                    printer.inkLevelBlack?.let { level ->
                                        QuickHealthChip("Black", level)
                                    }
                                    QuickHealthChip("Paper", when(printer.paperTrayStatus) {
                                        "OK" -> 100; "Low" -> 25; "Empty" -> 0; else -> -1
                                    })
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Tap for full health dashboard",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickHealthChip(label: String, level: Int) {
    val color = when {
        level < 0 -> MaterialTheme.colorScheme.onSurfaceVariant
        level > 50 -> Green400
        level > 20 -> Orange400
        else -> Red400
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            when {
                level < 0 -> Icons.Filled.HelpOutline
                level > 50 -> Icons.Filled.CheckCircle
                level > 20 -> Icons.Filled.Warning
                else -> Icons.Filled.Error
            },
            null, tint = color, modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            if (level >= 0) "$label: $level%" else "$label: ?",
            style = MaterialTheme.typography.labelSmall,
            color = color, fontWeight = FontWeight.Medium
        )
    }
}
