package com.wifiprint.app.ui.screens.jobs

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifiprint.app.data.models.PrintJob
import androidx.activity.compose.BackHandler
import com.wifiprint.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobHistoryScreen(
    onBack: () -> Unit = {},
    viewModel: JobHistoryViewModel = hiltViewModel()
) {
    val queueJobs by viewModel.queueJobs.collectAsState()
    val historyJobs by viewModel.historyJobs.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    // Handle system back button/gesture to ensure single-press navigation
    BackHandler { onBack() }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // ── Modern Surface Header ─────────────────────────────────────
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 0.5.dp
        ) {
            Column {
                Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onBack() }
                            ) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Print Queue & Jobs",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Live spooling queue and print history",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { viewModel.refreshFromServer() }
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    Modifier.size(18.dp), strokeWidth = 2.dp, color = Primary
                                )
                            } else {
                                Icon(Icons.Filled.Refresh, "Refresh", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(12.dp))

            // ── Pill-shaped Tab Row ──────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(0, 1).forEach { tabIndex ->
                        val isSelected = selectedTab == tabIndex
                        val tabLabel = if (tabIndex == 0) "Queue" else "History"
                        val count = if (tabIndex == 0) queueJobs.size else historyJobs.size

                        Surface(
                            onClick = { viewModel.setTab(tabIndex) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.surface
                            else Color.Transparent,
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    tabLabel,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (count > 0 && tabIndex == 0) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Primary.copy(alpha = 0.15f),
                                        modifier = Modifier.height(20.dp)
                                    ) {
                                        Text(
                                            "$count",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            val displayJobs = if (selectedTab == 0) queueJobs else historyJobs

            if (displayJobs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Primary.copy(alpha = 0.08f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (selectedTab == 0) Icons.Filled.Queue else Icons.Filled.Inbox,
                                    null, Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (selectedTab == 0) "No jobs in queue" else "No print history",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (selectedTab == 0) "Print a file to see it here" else "Completed jobs will appear here",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    items(displayJobs, key = { it.id }) { job ->
                        JobDetailCard(
                            job = job,
                            showQueueActions = selectedTab == 0,
                            onCancel = { viewModel.cancelJob(job.id) },
                            onRetry = { viewModel.retryJob(job.id) },
                            onPause = { viewModel.pauseJob(job.id) },
                            onResume = { viewModel.resumeJob(job.id) },
                            onSetPriority = { priority -> viewModel.setJobPriority(job.id, priority) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JobDetailCard(
    job: PrintJob,
    showQueueActions: Boolean = false,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    onSetPriority: (String) -> Unit = {}
) {
    val statusColor = when (job.status) {
        "Completed" -> Green400
        "Failed" -> Red400
        "Printing" -> Cyan400
        "Pending", "Queued" -> Orange400
        "Paused" -> Secondary
        "Cancelled" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Text(job.fileName, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${job.printerName} • ${dateFormat.format(Date(job.createdAt))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        // Priority interactive chip
                        if (showQueueActions) {
                            Spacer(Modifier.width(6.dp))
                            PriorityChip(
                                currentPriority = job.priority,
                                onSetPriority = onSetPriority
                            )
                        }
                    }
                }
                // Status pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(job.status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        color = statusColor, style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold)
                }
            }

            // Progress bar for active jobs
            if (job.status == "Printing" && job.progress > 0) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = job.progress / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    color = Cyan400
                )
                Text("${job.progress}%", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Error message
            if (job.status == "Failed" && job.errorMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text("Error: ${job.errorMessage}", color = Red400,
                    style = MaterialTheme.typography.bodySmall)
            }

            // Action buttons with 50/50 balanced weight — will NEVER vertically wrap!
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showQueueActions && job.status == "Pending") {
                    FilledTonalButton(
                        onClick = onPause,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Filled.Pause, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Pause", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500),
                        border = BorderStroke(1.dp, Red400.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Filled.Close, null, Modifier.size(16.dp), tint = Red500)
                        Spacer(Modifier.width(6.dp))
                        Text("Cancel", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                } else if (showQueueActions && job.status == "Paused") {
                    Button(
                        onClick = onResume,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Resume", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500),
                        border = BorderStroke(1.dp, Red400.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Filled.Close, null, Modifier.size(16.dp), tint = Red500)
                        Spacer(Modifier.width(6.dp))
                        Text("Cancel", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                } else if (job.status == "Printing") {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500),
                        border = BorderStroke(1.dp, Red400.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Filled.Close, null, Modifier.size(16.dp), tint = Red500)
                        Spacer(Modifier.width(6.dp))
                        Text("Cancel Print Job", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                } else if (job.status == "Failed") {
                    Button(
                        onClick = onRetry,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Filled.Replay, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Retry Print", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityChip(
    currentPriority: String,
    onSetPriority: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = when (currentPriority) {
                "High" -> Red400.copy(alpha = 0.15f)
                "Low" -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    currentPriority,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (currentPriority) {
                        "High" -> Red400
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    Icons.Filled.ArrowDropDown,
                    null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("High", "Normal", "Low").forEach { priority ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                when (priority) {
                                    "High" -> Icons.Filled.KeyboardDoubleArrowUp
                                    "Low" -> Icons.Filled.KeyboardDoubleArrowDown
                                    else -> Icons.Filled.DragHandle
                                },
                                null,
                                tint = when (priority) {
                                    "High" -> Red400
                                    "Low" -> MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> Orange400
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(priority)
                        }
                    },
                    onClick = {
                        onSetPriority(priority)
                        expanded = false
                    }
                )
            }
        }
    }
}
