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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    isConnected: Boolean = true,
    serverName: String = "",
    onNavigateToQr: () -> Unit = {},
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

    LaunchedEffect(isConnected) {
        if (isConnected) {
            viewModel.loadPrinters()
        }
    }

    if (state.showPasswordDialog) {
        PdfPasswordDialog(
            fileName = state.selectedFileName,
            errorMessage = state.passwordError,
            isVerifying = state.isVerifyingPassword,
            onConfirm = { password -> viewModel.unlockPdfWithPassword(password) },
            onDismiss = { viewModel.setShowPasswordDialog(false) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Offline Warning Banner ──────────────────────────────────
        if (!isConnected) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Red400.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Red400.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Red400.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.WifiOff, null, tint = Red400, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Printer Server Offline",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Red400
                        )
                        Text(
                            "Connect to LAN or scan QR code",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onNavigateToQr,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.QrCodeScanner, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Scan QR", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // ── 1. Document Selection Area ──────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Selected Document",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            val readyCount = if (state.isBatchMode) state.selectedFiles.size else if (state.selectedFileName.isNotEmpty()) 1 else 0
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (readyCount > 0) Primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    if (readyCount > 0) "$readyCount File Ready" else "No File Chosen",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (readyCount > 0) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Active Populated File Preview Card
        if (state.selectedFileName.isNotEmpty() && !state.isBatchMode) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(5.dp).fillMaxHeight().background(Primary))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (iconColor, icon) = when (state.fileType) {
                            "PDF" -> Red400 to Icons.Filled.PictureAsPdf
                            "Image" -> Cyan400 to Icons.Filled.Image
                            else -> Secondary to Icons.Filled.Description
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = iconColor.copy(alpha = 0.12f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                state.selectedFileName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(3.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = iconColor.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        state.fileType,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = iconColor
                                    )
                                }
                                if (state.totalPages != null) {
                                    Text("•", color = MaterialTheme.colorScheme.outline)
                                    Text(
                                        "${state.totalPages} Pages",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text("•", color = MaterialTheme.colorScheme.outline)
                                Text(
                                    if (state.settings.colorMode == "BlackAndWhite") "B&W" else "Color",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Tertiary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.setFile(android.net.Uri.EMPTY, "") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.Close, "Remove File", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Locked PDF Banner if password protected
            if (state.isPdfLocked) {
                if (state.isPasswordVerified) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Green50,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.LockOpen, null, tint = Green400, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Password Verified", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Text("Ready to print", style = MaterialTheme.typography.bodySmall, color = Color(0xFF388E3C))
                            }
                            TextButton(onClick = { viewModel.setShowPasswordDialog(true) }) {
                                Text("Change", style = MaterialTheme.typography.labelSmall, color = Primary)
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Orange50,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Lock, null, tint = Orange400, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Password-Protected PDF", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                Text("Password required to print", style = MaterialTheme.typography.bodySmall, color = Color(0xFFEF6C00))
                            }
                            Button(
                                onClick = { viewModel.setShowPasswordDialog(true) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Orange400),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Enter", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        } else if (state.isBatchMode && state.selectedFiles.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Batch Files (${state.selectedFiles.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    state.selectedFiles.forEach { file ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.InsertDriveFile, null, tint = Primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(file.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), maxLines = 1)
                            IconButton(onClick = { viewModel.removeFile(file) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Close, "Remove", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Browse / Drop Zone Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.clearFiles()
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                            "application/pdf", "image/jpeg", "image/png", "text/plain",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        ))
                    }
                    filePicker.launch(intent)
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.CloudUpload, null, tint = Primary, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (state.selectedFileName.isNotEmpty() || state.selectedFiles.isNotEmpty()) "Tap to Select Another Document"
                    else "Select File or Tap to Browse",
                    style = MaterialTheme.typography.titleSmall,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "Supports PDF, JPG, PNG, DOCX, XLSX (up to 50MB)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = {
                            viewModel.clearFiles()
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                                    "application/pdf", "image/jpeg", "image/png", "text/plain",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                ))
                            }
                            filePicker.launch(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.FolderOpen, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Single File", style = MaterialTheme.typography.labelMedium)
                    }
                    FilledTonalButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                                    "application/pdf", "image/jpeg", "image/png", "text/plain",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                ))
                            }
                            batchFilePicker.launch(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.FileCopy, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Batch Mode", style = MaterialTheme.typography.labelMedium)
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
                elevation = CardDefaults.cardElevation(1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Cyan400.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Visibility, null, tint = Cyan400, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("Document Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))

                    when (state.fileType) {
                        "PDF" -> InlinePdfPreview(
                            uri = state.selectedFileUri!!,
                            isLocked = state.isPdfLocked,
                            isPasswordVerified = state.isPasswordVerified,
                            totalPages = state.totalPages,
                            onUnlockClick = { viewModel.setShowPasswordDialog(true) }
                        )
                        "Image" -> InlineImagePreview(uri = state.selectedFileUri!!)
                        "Text" -> InlineTextPreview(uri = state.selectedFileUri!!)
                        else -> {
                            Text(
                                "Preview not available for ${state.fileType} files",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── 2. Print Preferences Section ────────────────────────────
        Text(
            "Print Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Printer Selector Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "DESTINATION PRINTER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Green400.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Green400))
                            Spacer(Modifier.width(5.dp))
                            Text("Online", style = MaterialTheme.typography.labelSmall, color = Green400, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                if (state.isLoadingPrinters) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else if (state.printers.isEmpty()) {
                    Text("No printers found on network", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                } else {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Primary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Print, null, tint = Primary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        state.selectedPrinter?.name ?: "Select a Printer",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        state.selectedPrinter?.let { if (it.isDefault) "Default Printer" else it.status } ?: "Tap to choose",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Filled.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            state.printers.forEach { printer ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(printer.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                if (printer.isDefault) "Default • ${printer.status}" else printer.status,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
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

        // Print Settings Matrix Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Copies
                SettingRow("Copies") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (state.settings.copies > 1)
                                    viewModel.updateSettings(state.settings.copy(copies = state.settings.copies - 1))
                            },
                            modifier = Modifier.size(32.dp)
                        ) { Icon(Icons.Filled.Remove, "Decrease", modifier = Modifier.size(16.dp)) }
                        Text(
                            "${state.settings.copies}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = {
                                viewModel.updateSettings(state.settings.copy(copies = state.settings.copies + 1))
                            },
                            modifier = Modifier.size(32.dp)
                        ) { Icon(Icons.Filled.Add, "Increase", modifier = Modifier.size(16.dp)) }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)

                // Page Range
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
                                Text("Total: ${state.totalPages} pages", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)

                // Color Mode
                SettingRow("Color Mode") {
                    SegmentedButtons(
                        options = listOf("Color", "B&W"),
                        selected = if (state.settings.colorMode == "BlackAndWhite") "B&W" else "Color",
                        onSelected = {
                            val mode = if (it == "B&W") "BlackAndWhite" else "Color"
                            viewModel.updateSettings(state.settings.copy(colorMode = mode))
                        }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)

                // Paper Size
                SettingRow("Paper Size") {
                    SegmentedButtons(
                        options = listOf("A4", "Letter", "Legal"),
                        selected = state.settings.pageSize,
                        onSelected = { viewModel.updateSettings(state.settings.copy(pageSize = it)) }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)

                // Orientation
                SettingRow("Orientation") {
                    SegmentedButtons(
                        options = listOf("Portrait", "Landscape"),
                        selected = state.settings.orientation,
                        onSelected = { viewModel.updateSettings(state.settings.copy(orientation = it)) }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)

                // Two-sided Duplex
                SettingRow("Two-Sided (Duplex)") {
                    Switch(
                        checked = state.settings.duplex,
                        onCheckedChange = { viewModel.updateSettings(state.settings.copy(duplex = it)) }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)

                // Quality
                SettingRow("Print Quality") {
                    SegmentedButtons(
                        options = listOf("Draft", "Normal", "High"),
                        selected = state.settings.quality,
                        onSelected = { viewModel.updateSettings(state.settings.copy(quality = it)) }
                    )
                }
            }
        }

        // Error Banner
        if (state.error != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Red400.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Error, null, tint = Red400, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        state.error!!,
                        color = Red400,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.loadPrinters() }) {
                        Text("Retry", fontWeight = FontWeight.Bold, color = Red400)
                    }
                }
            }
        }

        // ── Primary Action Button ────────────────────────────────────
        Button(
            onClick = {
                if (state.isPdfLocked && !state.isPasswordVerified) {
                    viewModel.setShowPasswordDialog(true)
                } else {
                    viewModel.submitPrintJob()
                }
            },
            enabled = (state.selectedFileUri != null || state.selectedFiles.isNotEmpty()) &&
                state.selectedPrinter != null && !state.isUploading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                disabledContainerColor = Primary.copy(alpha = 0.4f)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 6.dp)
        ) {
            if (state.isUploading) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(12.dp))
                if (state.isBatchMode) {
                    Text("Uploading ${state.batchProgress}/${state.batchTotal}...", fontWeight = FontWeight.Bold)
                } else {
                    Text("Uploading to Printer...", fontWeight = FontWeight.Bold)
                }
            } else {
                Icon(
                    if (state.isPdfLocked && !state.isPasswordVerified) Icons.Filled.LockOpen else Icons.Filled.Print,
                    null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                val label = when {
                    state.isBatchMode -> "Start Batch Print (${state.selectedFiles.size} Files)"
                    state.isPdfLocked && !state.isPasswordVerified -> "Unlock & Print Document"
                    else -> "Start Printing"
                }
                Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(24.dp))
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
private fun InlinePdfPreview(
    uri: Uri,
    isLocked: Boolean = false,
    isPasswordVerified: Boolean = false,
    totalPages: Int? = null,
    onUnlockClick: () -> Unit = {}
) {
    if (isLocked) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isPasswordVerified) Green50 else Orange50,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isPasswordVerified) Icons.Filled.LockOpen else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (isPasswordVerified) Green400 else Orange400,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    if (isPasswordVerified) "Encrypted PDF Unlocked" else "Password-Protected PDF",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (isPasswordVerified)
                        "Password verified. ${totalPages ?: 1} page(s) ready to print."
                    else
                        "Encrypted document content is protected. Enter password to unlock and print.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isPasswordVerified) {
                    Spacer(Modifier.height(14.dp))
                    FilledTonalButton(
                        onClick = onUnlockClick,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Key, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Enter Password")
                    }
                }
            }
        }
        return
    }

    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<Bitmap?>(null) }
    var pageCount by remember(uri) { mutableStateOf(0) }
    var currentPage by remember(uri) { mutableStateOf(0) }
    var error by remember(uri) { mutableStateOf<String?>(null) }
    var isLoading by remember(uri) { mutableStateOf(true) }

    LaunchedEffect(uri, currentPage) {
        isLoading = true
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                val renderer = PdfRenderer(fd)
                try {
                    pageCount = renderer.pageCount
                    if (renderer.pageCount > 0) {
                        val validPage = currentPage.coerceIn(0, renderer.pageCount - 1)
                        val page = renderer.openPage(validPage)
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
        } catch (_: SecurityException) {
            error = "Password protected document"
        } catch (e: Exception) {
            error = "PDF render failed: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    when {
        error != null -> {
            Text(error!!, color = Red400, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp))
        }
        bitmap == null && isLoading -> {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        }
        else -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "PDF Preview Page ${currentPage + 1}",
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
                                "Page ${currentPage + 1} of $pageCount",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Page Navigation Bar if multiple pages
                if (pageCount > 1) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { if (currentPage > 0) currentPage-- },
                            enabled = currentPage > 0,
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.ChevronLeft, "Previous page", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Prev", style = MaterialTheme.typography.labelMedium)
                        }

                        Text(
                            "Page ${currentPage + 1} of $pageCount",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        FilledTonalButton(
                            onClick = { if (currentPage < pageCount - 1) currentPage++ },
                            enabled = currentPage < pageCount - 1,
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Next", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.ChevronRight, "Next page", modifier = Modifier.size(18.dp))
                        }
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

@Composable
fun PdfPasswordDialog(
    fileName: String,
    errorMessage: String?,
    isVerifying: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isVerifying) onDismiss() },
        icon = {
            Surface(
                shape = CircleShape,
                color = Primary.copy(alpha = 0.12f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        title = {
            Text(
                "Unlock PDF Document",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "\"$fileName\" is password-protected. Enter the password below to decrypt and prepare for printing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Document Password") },
                    placeholder = { Text("Enter password") },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage, color = Red400, style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (password.isNotEmpty()) onConfirm(password) },
                enabled = password.isNotEmpty() && !isVerifying,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Verifying...")
                } else {
                    Icon(Icons.Filled.LockOpen, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Unlock")
                }
            }
        },
        dismissButton = {
            if (!isVerifying) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
