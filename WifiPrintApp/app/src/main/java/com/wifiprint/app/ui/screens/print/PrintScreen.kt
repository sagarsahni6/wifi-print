package com.wifiprint.app.ui.screens.print

import android.app.Activity
import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifiprint.app.data.models.PrintSettings
import com.wifiprint.app.data.models.SelectedFile
import com.wifiprint.app.ui.theme.*
import com.wifiprint.app.ui.screens.print.ImageEditorHelper
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.yalantis.ucrop.UCrop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintScreen(
    onJobCreated: () -> Unit,
    onPreview: ((String) -> Unit)? = null,
    viewModel: PrintViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // SAF file picker — single file
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                val name = cursor?.use {
                    if (it.moveToFirst()) {
                        val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) it.getString(idx) else "file"
                    } else "file"
                } ?: "file"
                viewModel.setFile(uri, name)
            }
        }
    }

    // SAF file picker — multiple files (batch mode)
    val batchFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val files = mutableListOf<SelectedFile>()
            result.data?.let { data ->
                // Multiple selection
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount.coerceAtMost(10)) {
                        val uri = clipData.getItemAt(i).uri
                        val cursor = context.contentResolver.query(uri, null, null, null, null)
                        val name = cursor?.use {
                            if (it.moveToFirst()) {
                                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (idx >= 0) it.getString(idx) else "file"
                            } else "file"
                        } ?: "file"
                        files.add(SelectedFile(uri, name))
                    }
                } else {
                    // Single selection fallback
                    data.data?.let { uri ->
                        val cursor = context.contentResolver.query(uri, null, null, null, null)
                        val name = cursor?.use {
                            if (it.moveToFirst()) {
                                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (idx >= 0) it.getString(idx) else "file"
                            } else "file"
                        } ?: "file"
                        files.add(SelectedFile(uri, name))
                    }
                }
            }
            if (files.isNotEmpty()) viewModel.addFiles(files)
        }
    }

    LaunchedEffect(state.success) {
        if (state.success) onJobCreated()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Print a File", style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold)

        // ── File Selection ──────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("📄 Select File", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                // Show selected files (batch mode)
                if (state.isBatchMode && state.selectedFiles.isNotEmpty()) {
                    state.selectedFiles.forEach { file ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Icon(Icons.Filled.InsertDriveFile, null, tint = Purple600,
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(file.name, style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f), maxLines = 1)
                            IconButton(
                                onClick = { viewModel.removeFile(file) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Filled.Close, "Remove", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                // Show single selected file
                else if (state.selectedFileName.isNotEmpty() && !state.isBatchMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.InsertDriveFile, null, tint = Purple600)
                        Spacer(Modifier.width(8.dp))
                        Text(state.selectedFileName, style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f))
                        // Preview button
                        if (onPreview != null) {
                            IconButton(onClick = {
                                state.selectedFileUri?.let {
                                    onPreview(it.toString())
                                }
                            }) {
                                Icon(Icons.Filled.Visibility, "Preview", tint = Cyan400)
                            }
                        }
                        IconButton(onClick = { viewModel.setFile(android.net.Uri.EMPTY, "") }) {
                            Icon(Icons.Filled.Close, "Remove")
                        }
                    }
                    // Show page count for PDFs
                    if (state.isLoadingPageCount) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                            Text("Getting page count...", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else if (state.totalPages != null) {
                        Text("📄 ${state.totalPages} pages",
                            style = MaterialTheme.typography.bodySmall,
                            color = Cyan400, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // File picker buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearFiles()
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                                    "application/pdf", "image/jpeg", "image/png",
                                    "text/plain",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                ))
                            }
                            filePicker.launch(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.FolderOpen, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Single File")
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                                    "application/pdf", "image/jpeg", "image/png",
                                    "text/plain",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                ))
                            }
                            batchFilePicker.launch(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.FileCopy, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Batch (${state.selectedFiles.size})")
                    }
                }
            }
        }

        // ── Printer Selection ───────────────────────────────────────
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("🖨️ Printer", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                if (state.isLoadingPrinters) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else if (state.printers.isEmpty()) {
                    Text("No printers found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = state.selectedPrinter?.name ?: "Select printer",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            state.printers.forEach { printer ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(printer.name, fontWeight = FontWeight.Medium)
                                            Text(if (printer.isDefault) "Default" else printer.status,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = { viewModel.selectPrinter(printer); expanded = false }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Print Settings ──────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("⚙️ Settings", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                // Copies
                SettingRow("Copies") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (state.settings.copies > 1)
                                viewModel.updateSettings(state.settings.copy(copies = state.settings.copies - 1))
                        }) { Icon(Icons.Filled.Remove, "Decrease") }
                        Text("${state.settings.copies}", fontWeight = FontWeight.Bold)
                        IconButton(onClick = {
                            viewModel.updateSettings(state.settings.copy(copies = state.settings.copies + 1))
                        }) { Icon(Icons.Filled.Add, "Increase") }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Page Range (Feature 1)
                SettingRow("Pages") {
                    SegmentedButtons(
                        options = listOf("All", "Custom"),
                        selected = state.pageRangeMode,
                        onSelected = { viewModel.setPageRangeMode(it) }
                    )
                }

                if (state.pageRangeMode == "Custom") {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.settings.pageRange ?: "",
                        onValueChange = { viewModel.setPageRange(it) },
                        label = { Text("e.g. 1-5, 8, 11-15") },
                        placeholder = { Text("1-5, 8, 11-15") },
                        isError = state.pageRangeError != null,
                        supportingText = {
                            if (state.pageRangeError != null) {
                                Text(state.pageRangeError!!, color = Red400)
                            } else if (state.totalPages != null) {
                                Text("Total: ${state.totalPages} pages",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Orientation
                SettingRow("Orientation") {
                    SegmentedButtons(
                        options = listOf("Portrait", "Landscape"),
                        selected = state.settings.orientation,
                        onSelected = { viewModel.updateSettings(state.settings.copy(orientation = it)) }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Color
                SettingRow("Color") {
                    SegmentedButtons(
                        options = listOf("Color", "B&W"),
                        selected = if (state.settings.colorMode == "BlackAndWhite") "B&W" else "Color",
                        onSelected = {
                            val mode = if (it == "B&W") "BlackAndWhite" else "Color"
                            viewModel.updateSettings(state.settings.copy(colorMode = mode))
                        }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Page Size
                SettingRow("Paper") {
                    SegmentedButtons(
                        options = listOf("A4", "Letter", "Legal"),
                        selected = state.settings.pageSize,
                        onSelected = { viewModel.updateSettings(state.settings.copy(pageSize = it)) }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Duplex
                SettingRow("Double-sided") {
                    Switch(
                        checked = state.settings.duplex,
                        onCheckedChange = { viewModel.updateSettings(state.settings.copy(duplex = it)) }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Quality
                SettingRow("Quality") {
                    SegmentedButtons(
                        options = listOf("Draft", "Normal", "High"),
                        selected = state.settings.quality,
                        onSelected = { viewModel.updateSettings(state.settings.copy(quality = it)) }
                    )
                }
            }
        }

        // Error
        if (state.error != null) {
            Card(colors = CardDefaults.cardColors(containerColor = Red400.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(10.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Error, null, tint = Red400)
                    Spacer(Modifier.width(8.dp))
                    Text(state.error!!, color = Red400, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Print Button
        Button(
            onClick = { viewModel.submitPrintJob() },
            enabled = (state.selectedFileUri != null || state.selectedFiles.isNotEmpty()) &&
                state.selectedPrinter != null && !state.isUploading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (state.isUploading) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(12.dp))
                if (state.isBatchMode) {
                    Text("Uploading ${state.batchProgress}/${state.batchTotal}...",
                        fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Uploading...", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Icon(Icons.Filled.Print, null)
                Spacer(Modifier.width(12.dp))
                if (state.isBatchMode) {
                    Text("Print ${state.selectedFiles.size} Files",
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                } else {
                    Text("Print", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun SettingRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedButtons(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { option ->
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(option) },
                label = { Text(option, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
