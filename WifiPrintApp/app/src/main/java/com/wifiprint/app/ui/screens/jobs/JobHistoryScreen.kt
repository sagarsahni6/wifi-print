package com.wifiprint.app.ui.screens.jobs

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifiprint.app.data.models.PrintJob
import com.wifiprint.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobHistoryScreen(
    viewModel: JobHistoryViewModel = hiltViewModel()
) {
    val queueJobs by viewModel.queueJobs.collectAsState()
    val historyJobs by viewModel.historyJobs.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Print Jobs",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        ),
                        color = Color.White
                    )
                    IconButton(onClick = { viewModel.refreshFromServer() }) {
                        if (isRefreshing) CircularProgressIndicator(
                            Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White
                        )
                        else Icon(Icons.Filled.Refresh, "Refresh", tint = Color.White)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Track and manage your print queue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(12.dp))

            // ── Pill-shaped Tab Row ──────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                                    color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                    contentPadding = PaddingValues(vertical = 8.dp)
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
                        // Priority badge
                        if (job.priority != "Normal" && showQueueActions) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (job.priority) {
                                    "High" -> Red400.copy(alpha = 0.15f)
                                    "Low" -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(
                                    job.priority,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (job.priority) {
                                        "High" -> Red400
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
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

            // Action buttons
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Queue actions — pause, resume, priority
                if (showQueueActions) {
                    when (job.status) {
                        "Pending" -> {
                            FilledTonalButton(
                                onClick = onPause,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Pause, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Pause")
                            }
                            PriorityDropdown(
                                currentPriority = job.priority,
                                onSetPriority = onSetPriority
                            )
                        }
                        "Paused" -> {
                            FilledTonalButton(
                                onClick = onResume,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.PlayArrow, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Resume")
                            }
                        }
                    }
                }

                if (job.status == "Failed") {
                    FilledTonalButton(
                        onClick = onRetry,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Replay, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Retry")
                    }
                }
                if (job.status in listOf("Pending", "Printing", "Paused")) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Cancel, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityDropdown(
    currentPriority: String,
    onSetPriority: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilledTonalButton(onClick = { expanded = true }, shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Filled.LowPriority, null, Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(currentPriority)
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
