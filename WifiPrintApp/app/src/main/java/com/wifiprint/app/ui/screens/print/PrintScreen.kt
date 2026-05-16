package com.wifiprint.app.ui.screens.print

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.provider.OpenableColumns
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wifiprint.app.data.models.PrintSettings
import com.wifiprint.app.data.models.SelectedFile
import com.wifiprint.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintScreen(
    onJobCreated: () -> Unit,
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
                result.data?.flags?.let { flags ->
                    val persistableFlags = flags and
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    context.contentResolver.takePersistableUriPermission(uri, persistableFlags)
                }
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
                        result.data?.flags?.let { flags ->
                            val persistableFlags = flags and
                                (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                            context.contentResolver.takePersistableUriPermission(uri, persistableFlags)
                        }
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
                        result.data?.flags?.let { flags ->
                            val persistableFlags = flags and
                                (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                            context.contentResolver.takePersistableUriPermission(uri, persistableFlags)
                        }
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
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Column {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Print a File",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    ),
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Select a file and customize print settings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── File Selection ──────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Description, null, tint = Primary,
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("Select File", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(14.dp))

                    // Show selected files (batch mode)
                    if (state.isBatchMode && state.selectedFiles.isNotEmpty()) {
                        state.selectedFiles.forEach { file ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Icon(Icons.Filled.InsertDriveFile, null, tint = Primary,
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
                            Icon(Icons.Filled.InsertDriveFile, null, tint = Primary)
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(state.selectedFileName, style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1, fontWeight = FontWeight.Medium)
                                Text(state.fileType, style = MaterialTheme.typography.bodySmall,
                                    color = Tertiary)
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
                                color = Tertiary, fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // File picker buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.clearFiles()
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = "*/*"
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                                        "application/pdf", "image/jpeg", "image/png",
                                        "text/plain",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                    ))
                                }
                                filePicker.launch(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.FolderOpen, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Single File")
                        }

                        FilledTonalButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = "*/*"
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
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
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.FileCopy, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Batch (${state.selectedFiles.size})")
                        }
                    }
                }
            }

            // ── Inline Auto-Preview ─────────────────────────────────────
            if (!state.isBatchMode && state.selectedFileUri != null &&
                state.selectedFileUri != android.net.Uri.EMPTY &&
                state.fileType != "Unknown"
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Cyan400.copy(alpha = 0.1f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Visibility, null, tint = Cyan400,
                                        modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Text("Preview", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(12.dp))

                        when (state.fileType) {
                            "PDF" -> InlinePdfPreview(uri = state.selectedFileUri!!)
                            "Image" -> InlineImagePreview(uri = state.selectedFileUri!!)
                            "Text" -> InlineTextPreview(uri = state.selectedFileUri!!)
                            else -> {
                                Text("Preview not available for ${state.fileType} files",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(20.dp))
                            }
                        }
                    }
                }
            }

            // ── Printer Selection ───────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Tertiary.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Print, null, tint = Tertiary,
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("Printer", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(14.dp))

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
                                shape = RoundedCornerShape(12.dp)
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Secondary.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Settings, null, tint = Secondary,
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("Settings", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(14.dp))

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

                    Divider(modifier = Modifier.padding(vertical = 4.dp),
                        color = DividerColor)

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
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp),
                        color = DividerColor)

                    // Orientation
                    SettingRow("Orientation") {
                        SegmentedButtons(
                            options = listOf("Portrait", "Landscape"),
                            selected = state.settings.orientation,
                            onSelected = { viewModel.updateSettings(state.settings.copy(orientation = it)) }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp),
                        color = DividerColor)

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

                    Divider(modifier = Modifier.padding(vertical = 4.dp),
                        color = DividerColor)

                    // Page Size
                    SettingRow("Paper") {
                        SegmentedButtons(
                            options = listOf("A4", "Letter", "Legal"),
                            selected = state.settings.pageSize,
                            onSelected = { viewModel.updateSettings(state.settings.copy(pageSize = it)) }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp),
                        color = DividerColor)

                    // Duplex
                    SettingRow("Double-sided") {
                        Switch(
                            checked = state.settings.duplex,
                            onCheckedChange = { viewModel.updateSettings(state.settings.copy(duplex = it)) }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp),
                        color = DividerColor)

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
                    shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Error, null, tint = Red400)
                        Spacer(Modifier.width(8.dp))
                        Text(state.error!!, color = Red400, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // ── Gradient Print Button ────────────────────────────────────
            Button(
                onClick = { viewModel.submitPrintJob() },
                enabled = (state.selectedFileUri != null || state.selectedFiles.isNotEmpty()) &&
                    state.selectedPrinter != null && !state.isUploading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Primary.copy(alpha = 0.4f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
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

// ── Inline Preview Composables ──────────────────────────────────────────

@Composable
private fun InlinePdfPreview(uri: Uri) {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<Bitmap?>(null) }
    var pageCount by remember(uri) { mutableStateOf(0) }
    var error by remember(uri) { mutableStateOf<String?>(null) }

    LaunchedEffect(uri) {
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                val renderer = PdfRenderer(fd)
                try {
                    pageCount = renderer.pageCount
                    if (renderer.pageCount > 0) {
                        val page = renderer.openPage(0)
                        val bmp = Bitmap.createBitmap(
                            page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888
                        )
                        bmp.eraseColor(android.graphics.Color.WHITE)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        bitmap = bmp
                    }
                } finally {
                    renderer.close()
                }
            } ?: run { error = "Cannot open PDF" }
        } catch (e: Exception) {
            error = "PDF render failed: ${e.message}"
        }
    }

    when {
        error != null -> {
            Text(error!!, color = Red400, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp))
        }
        bitmap == null -> {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        }
        else -> {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "PDF Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentScale = ContentScale.Fit
                )
                // Page count badge
                if (pageCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Primary.copy(alpha = 0.9f),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                    ) {
                        Text(
                            "$pageCount pages",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineImagePreview(uri: Uri) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(uri)
            .crossfade(true)
            .build(),
        contentDescription = "Image preview",
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 360.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun InlineTextPreview(uri: Uri) {
    val context = LocalContext.current
    var textContent by remember(uri) { mutableStateOf("Loading...") }

    LaunchedEffect(uri) {
        textContent = try {
            context.contentResolver.openInputStream(uri)?.use {
                it.bufferedReader().readText().take(2000)
            } ?: "Cannot read file"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
    ) {
        Text(
            text = textContent,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}
