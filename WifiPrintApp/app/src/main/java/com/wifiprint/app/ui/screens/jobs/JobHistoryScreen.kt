package com.wifiprint.app.ui.screens.jobs

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

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Print Jobs", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold)
            IconButton(onClick = { viewModel.refreshFromServer() }) {
                if (isRefreshing) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Icon(Icons.Filled.Refresh, "Refresh")
            }
        }

        // Queue / History tabs
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { viewModel.setTab(0) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Queue")
                        if (queueJobs.isNotEmpty()) {
                            Spacer(Modifier.width(6.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                Text("${queueJobs.size}")
                            }
                        }
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { viewModel.setTab(1) },
                text = { Text("History") }
            )
        }

        Spacer(Modifier.height(8.dp))

        val displayJobs = if (selectedTab == 0) queueJobs else historyJobs

        if (displayJobs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (selectedTab == 0) Icons.Filled.Queue else Icons.Filled.Inbox,
                        null, Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (selectedTab == 0) "No jobs in queue" else "No print history",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
        "Paused" -> Purple400
        "Cancelled" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (job.fileType) {
                        "PDF" -> Icons.Filled.PictureAsPdf
                        "Image" -> Icons.Filled.Image
                        else -> Icons.Filled.Description
                    },
                    null, tint = Purple600, modifier = Modifier.size(32.dp)
                )
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
                                shape = RoundedCornerShape(4.dp),
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
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(job.status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = statusColor, style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold)
                }
            }

            // Progress bar for active jobs
            if (job.status == "Printing" && job.progress > 0) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = job.progress / 100f,
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                            // Pause
                            FilledTonalButton(onClick = onPause) {
                                Icon(Icons.Filled.Pause, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Pause")
                            }
                            // Priority dropdown
                            PriorityDropdown(
                                currentPriority = job.priority,
                                onSetPriority = onSetPriority
                            )
                        }
                        "Paused" -> {
                            FilledTonalButton(onClick = onResume) {
                                Icon(Icons.Filled.PlayArrow, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Resume")
                            }
                        }
                    }
                }

                if (job.status == "Failed") {
                    FilledTonalButton(onClick = onRetry) {
                        Icon(Icons.Filled.Replay, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Retry")
                    }
                }
                if (job.status in listOf("Pending", "Printing", "Paused")) {
                    OutlinedButton(onClick = onCancel) {
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
        FilledTonalButton(onClick = { expanded = true }) {
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
