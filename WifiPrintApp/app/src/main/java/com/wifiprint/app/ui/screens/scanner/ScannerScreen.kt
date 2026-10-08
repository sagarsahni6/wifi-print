package com.wifiprint.app.ui.screens.scanner

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RectF
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.BorderStroke

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.wifiprint.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onScanComplete: (String) -> Unit,
    onBack: () -> Unit = {},
    isServerConnected: Boolean = true,
    serverName: String = "",
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val haptic = LocalHapticFeedback.current

    // Handle save PDF success (shows Toast with saved location, stays on screen)
    LaunchedEffect(state.savePdfSuccessMessage) {
        state.savePdfSuccessMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearSavePdfSuccess()
        }
    }

    // Handle direct print success (sends to printer, then navigates to jobs queue)
    LaunchedEffect(state.directPrintSuccess) {
        if (state.directPrintSuccess) {
            Toast.makeText(context, "Print job sent to printer!", Toast.LENGTH_SHORT).show()
            val uriStr = state.savedPdfUri?.toString() ?: ""
            onScanComplete(uriStr)
            viewModel.clearDirectPrintSuccess()
        }
    }

    // Handle share intent
    LaunchedEffect(state.sharePdfUri) {
        state.sharePdfUri?.let { uri ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Scanned PDF"))
            viewModel.clearSharePdf()
        }
    }

    // ML Kit Document Scanner setup (multi-page for documents)
    val scannerOptions = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(20)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }
    val scanner = remember { GmsDocumentScanning.getClient(scannerOptions) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanResult?.pages?.let { pages ->
                val uris = pages.mapNotNull { it.imageUri }
                if (uris.isNotEmpty()) {
                    viewModel.onMlKitScanResult(uris)
                }
            }
        }
    }

    // ML Kit scanner for ID card (1-page limit per side, with edge detection)
    val idCardScannerOptions = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
    }
    val idCardScanner = remember { GmsDocumentScanning.getClient(idCardScannerOptions) }

    // ID card front side launcher
    val idFrontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanResult?.pages?.firstOrNull()?.imageUri?.let { uri ->
                viewModel.onIdCardFrontScanned(uri)
            }
        }
    }

    // ID card back side launcher
    val idBackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanResult?.pages?.firstOrNull()?.imageUri?.let { uri ->
                viewModel.onIdCardBackScanned(uri)
            }
        }
    }

    // Camera permission
    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) hasCameraPermission = true
        else permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    // ── OCR Result Dialog ──
    if (state.showOcrResult && state.ocrText != null) {
        OcrResultDialog(
            text = state.ocrText!!,
            onDismiss = { viewModel.dismissOcrResult() },
            onCopy = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Scanned Text", state.ocrText))
                Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        )
    }

    val launchScanner = {
        activity?.let { act ->
            scanner.getStartScanIntent(act)
                .addOnSuccessListener { intentSender ->
                    scannerLauncher.launch(
                        IntentSenderRequest.Builder(intentSender).build()
                    )
                }
                .addOnFailureListener {
                    viewModel.clearError()
                }
        }
    }

    if (!hasCameraPermission) {
        // Permission request UI
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.CameraAlt, null, modifier = Modifier.size(32.dp), tint = Primary)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Camera Access Needed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Camera permission is required for live edge detection and document scanning.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Grant Camera Permission", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        return
    }

    // ── If Crop Mode is Active ──────────────────────────────────────
    if (state.isCropMode && state.scannedPages.isNotEmpty()) {
        val safeIndex = state.selectedPageIndex.coerceIn(0, state.scannedPages.size - 1)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { viewModel.cancelCrop() }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Crop & Straighten", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { viewModel.applyCrop(safeIndex) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Apply")
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CropOverlay(
                    bitmap = state.scannedPages[safeIndex].bitmap,
                    cropRect = state.cropRect,
                    onCropRectChanged = { viewModel.updateCropRect(it) },
                    onApply = { viewModel.applyCrop(safeIndex) },
                    onCancel = { viewModel.cancelCrop() }
                )
            }
        }
        return
    }

    // ── If Review Mode is Active (showCamera == false) and pages exist ──
    if (!state.showCamera && state.scannedPages.isNotEmpty()) {
        ReviewView(
            modifier = Modifier.fillMaxSize(),
            state = state,
            onSelectPage = { viewModel.selectPage(it) },
            onDeletePage = { viewModel.removePage(it) },
            onApplyFilter = { pageIdx, filter -> viewModel.applyFilterToPage(pageIdx, filter) },
            onBackToCamera = { viewModel.setShowCamera(true) },
            onExportPdf = { viewModel.directPrint() },
            onSharePdf = { viewModel.sharePdf() },
            onRotatePage = { pageIdx, angle -> viewModel.rotatePage(pageIdx, angle) },
            onMovePage = { from, to -> viewModel.movePage(from, to) },
            onEnterCrop = { viewModel.enterCropMode() },
            onUpdateCrop = { viewModel.updateCropRect(it) },
            onApplyCrop = { viewModel.applyCrop(it) },
            onCancelCrop = { viewModel.cancelCrop() },
            onToggleAdjustments = { viewModel.toggleAdjustments(it) },
            onBrightnessChange = { viewModel.updateBrightness(it) },
            onContrastChange = { viewModel.updateContrast(it) },
            onApplyAdjustments = { viewModel.applyBrightnessContrast(it) },
            onRunOcr = { viewModel.runOcr(it) },
            onSetPageSize = { viewModel.setPageSize(it) },
            onSetWatermarkText = { viewModel.setWatermarkText(it) },
            onToggleWatermark = { viewModel.toggleWatermark(it) },
            onSetWatermarkOpacity = { viewModel.setWatermarkOpacity(it) }
        )
        return
    }

    // ── Main Document Scanner Layout ────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scanned pages quick access banner
        if (state.scannedPages.isNotEmpty()) {
            Surface(
                onClick = { viewModel.setShowCamera(false) },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Description, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("${state.scannedPages.size} Page(s) Scanned", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Tap to Review, Filter & Direct Print", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                }
            }
        }
        // ── Section Header & Connected Beacon ───────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.DocumentScanner, null, tint = Primary, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Document Scanner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Auto edge detection & optical intake", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isServerConnected) Green400.copy(alpha = 0.12f) else Orange400.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isServerConnected) Green400 else Orange400)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isServerConnected) "Online" else "Offline",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isServerConnected) Green400 else Orange400
                    )
                }
            }
        }

        // ── Scan Mode Selector ──────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.scanMode == ScanMode.Document,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.setScanMode(ScanMode.Document)
                },
                leadingIcon = { Icon(Icons.Filled.Description, null, modifier = Modifier.size(16.dp)) },
                label = { Text("Document", fontWeight = FontWeight.Medium) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = state.scanMode == ScanMode.IDCard,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.setScanMode(ScanMode.IDCard)
                },
                leadingIcon = { Icon(Icons.Filled.Badge, null, modifier = Modifier.size(16.dp)) },
                label = { Text("ID Card", fontWeight = FontWeight.Medium) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = state.scanMode == ScanMode.Batch,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.setScanMode(ScanMode.Batch)
                },
                leadingIcon = { Icon(Icons.Filled.Layers, null, modifier = Modifier.size(16.dp)) },
                label = { Text("Batch", fontWeight = FontWeight.Medium) },
                modifier = Modifier.weight(1f)
            )
        }

        when (state.scanMode) {
            ScanMode.Document -> {
                // ── 1. Hero Primary Scan Card ───────────────────────
                Card(
                    onClick = { launchScanner() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Primary, Secondary)))
                            .padding(20.dp)
                    ) {
                        // Viewfinder Corner Brackets Overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(14.dp)
                                .drawBehind {
                                    drawLine(Color.White.copy(alpha = 0.6f), Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 2.5f)
                                    drawLine(Color.White.copy(alpha = 0.6f), Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 2.5f)
                                }
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(14.dp)
                                .drawBehind {
                                    drawLine(Color.White.copy(alpha = 0.6f), Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 2.5f)
                                    drawLine(Color.White.copy(alpha = 0.6f), Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth = 2.5f)
                                }
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .size(14.dp)
                                .drawBehind {
                                    drawLine(Color.White.copy(alpha = 0.6f), Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2.5f)
                                    drawLine(Color.White.copy(alpha = 0.6f), Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 2.5f)
                                }
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(14.dp)
                                .drawBehind {
                                    drawLine(Color.White.copy(alpha = 0.6f), Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2.5f)
                                    drawLine(Color.White.copy(alpha = 0.6f), Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth = 2.5f)
                                }
                        )

                        // Animated Scanning Laser Beam
                        val scanLaserTransition = rememberInfiniteTransition(label = "laser")
                        val laserOffsetFraction by scanLaserTransition.animateFloat(
                            initialValue = 0.05f,
                            targetValue = 0.95f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2200, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "laser_y"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .align(Alignment.TopStart)
                                .graphicsLayer {
                                    translationY = 160.dp.toPx() * laserOffsetFraction
                                }
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color(0xFF22D3EE).copy(alpha = 0.8f),
                                            Color.White,
                                            Color(0xFF22D3EE).copy(alpha = 0.8f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.AutoFixHigh, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "SMART OPTICAL ENGINE",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Scan New Document",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Capture, sharpen contrast & auto-detect page edges instantly.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.Black.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            "Auto Edge Detection",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.Black.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            "Flash: Auto",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.22f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                modifier = Modifier.size(60.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    }
                }

                // ── 2. Scanned Pages Live Gallery ───────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scanned Pages", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (state.scannedPages.isNotEmpty()) Primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            "${state.scannedPages.size} pages captured",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (state.scannedPages.isNotEmpty()) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (state.scannedPages.isNotEmpty()) {
                    val safeIndex = state.selectedPageIndex.coerceIn(0, state.scannedPages.size - 1)

                    // Horizontal Thumbnails Strip
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(state.scannedPages) { index, page ->
                            val isSelected = index == safeIndex
                            Card(
                                onClick = { viewModel.selectPage(index) },
                                shape = RoundedCornerShape(12.dp),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Tertiary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(if (isSelected) 3.dp else 1.dp),
                                modifier = Modifier.width(90.dp).height(125.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        bitmap = page.bitmap.asImageBitmap(),
                                        contentDescription = "Page ${index + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Page Number Badge
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) Tertiary else Color.Black.copy(alpha = 0.65f),
                                        modifier = Modifier.align(Alignment.TopStart).padding(5.dp).size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                "${index + 1}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    // Delete Page Button
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(5.dp)
                                            .size(20.dp)
                                            .clickable { viewModel.removePage(index) }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.Close, "Delete", tint = Red400, modifier = Modifier.size(13.dp))
                                        }
                                    }
                                    // Bottom Page Label
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                                    ) {
                                        Text(
                                            "Page ${index + 1}",
                                            modifier = Modifier.padding(vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Tertiary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Add Page Card
                        item {
                            Card(
                                onClick = { launchScanner() },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary.copy(alpha = 0.4f)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.width(90.dp).height(125.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Primary.copy(alpha = 0.12f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Filled.Add, null, tint = Primary, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("Add Page", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Primary)
                                    Text("+ Camera", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // Main Active Page Preview Box
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Page ${safeIndex + 1} of ${state.scannedPages.size}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Tertiary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        state.scannedPages[safeIndex].filter,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Tertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = state.scannedPages[safeIndex].bitmap.asImageBitmap(),
                                    contentDescription = "Active page",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    // ── 3. Document Enhancement & Tools ─────────────────
                    Text("Document Enhancement & Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Crop & Rotate
                        Card(
                            onClick = { viewModel.enterCropMode() },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Primary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Crop, null, tint = Primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Crop & Rotate", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text("Adjust borders", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Color Filter
                        val filters = listOf("Magic Color", "Clean B&W", "Auto Enhance", "Grayscale", "Sharp", "Original")
                        val currentFilter = state.scannedPages[safeIndex].filter
                        Card(
                            onClick = {
                                val nextIdx = (filters.indexOf(currentFilter) + 1) % filters.size
                                viewModel.applyFilterToPage(safeIndex, filters[nextIdx])
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Secondary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.FilterVintage, null, tint = Secondary, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Color Filter", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(currentFilter, style = MaterialTheme.typography.labelSmall, color = Secondary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // OCR Search
                        Card(
                            onClick = { viewModel.runOcr(safeIndex) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Tertiary.copy(alpha = 0.12f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (state.isOcrRunning) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Tertiary)
                                        } else {
                                            Icon(Icons.Filled.TextFields, null, tint = Tertiary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("OCR Search", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text("Extract text", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Rotate 90
                        Card(
                            onClick = { viewModel.rotatePage(safeIndex, 90) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Orange400.copy(alpha = 0.12f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.RotateRight, null, tint = Orange400, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Rotate 90°", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text("Clockwise", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // ── 4. Output Specification ─────────────────────────
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.PictureAsPdf, null, tint = Primary, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Output Specification", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                                Text("PDF Standard", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Spacer(Modifier.height(12.dp))

                            // Page size selector
                            Text("Page Format", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(PageSize.A4, PageSize.Letter, PageSize.Legal).forEach { size ->
                                    val isSelected = state.pageSize == size
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setPageSize(size) },
                                        label = { Text(size.label, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }

                    // ── 5. Primary Action Buttons ───────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Save PDF
                        OutlinedButton(
                            onClick = { viewModel.savePdf() },
                            enabled = !state.isSavingPdf && !state.isDirectPrinting,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (state.isSavingPdf) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(6.dp))
                                Text("Saving...", fontWeight = FontWeight.SemiBold)
                            } else {
                                Icon(Icons.Filled.Download, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Save PDF", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Direct Print (Primary Action)
                        Button(
                            onClick = { viewModel.directPrint() },
                            enabled = !state.isSavingPdf && !state.isDirectPrinting,
                            modifier = Modifier.weight(1.4f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            if (state.isDirectPrinting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(Modifier.width(8.dp))
                                Text("Printing...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Filled.Print, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Direct Print", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Share
                        OutlinedButton(
                            onClick = { viewModel.sharePdf() },
                            enabled = !state.isSavingPdf && !state.isDirectPrinting,
                            modifier = Modifier.size(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Filled.Share, "Share", modifier = Modifier.size(20.dp))
                        }
                    }
                } else {
                    // Empty state card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.DocumentScanner, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("No scanned pages yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("Tap 'Scan New Document' above to capture with auto-edge detection.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // ── 6. Scanning Tip Card ─────────────────────────────
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.06f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.TipsAndUpdates, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Tip: Place documents on a dark background for instant edge detection & auto-deskewing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            ScanMode.IDCard -> {
                IdCardModeView(
                    step = state.idCardStep,
                    frontBitmap = state.idCardFrontBitmap,
                    backBitmap = state.idCardBackBitmap,
                    compositePreviewBitmap = state.idCardCompositePreviewBitmap,
                    selectedFilter = state.idCardFilter,
                    selectedLayout = state.idCardLayout,
                    onSelectFilter = { viewModel.setIdCardFilter(it) },
                    onSelectLayout = { viewModel.setIdCardLayout(it) },
                    isProcessing = state.isProcessing,
                    isSavingPdf = state.isSavingPdf,
                    isDirectPrinting = state.isDirectPrinting,
                    idCardFrontOnly = state.idCardFrontOnly,
                    onToggleFrontOnly = { viewModel.toggleIdCardFrontOnly(it) },
                    onScanFront = {
                        activity?.let { act ->
                            idCardScanner.getStartScanIntent(act)
                                .addOnSuccessListener { intentSender ->
                                    idFrontLauncher.launch(
                                        IntentSenderRequest.Builder(intentSender).build()
                                    )
                                }
                        }
                    },
                    onScanBack = {
                        activity?.let { act ->
                            idCardScanner.getStartScanIntent(act)
                                .addOnSuccessListener { intentSender ->
                                    idBackLauncher.launch(
                                        IntentSenderRequest.Builder(intentSender).build()
                                    )
                                }
                        }
                    },
                    onRetakeFront = { viewModel.retakeFront() },
                    onRetakeBack = { viewModel.retakeBack() },
                    onSkipBack = { viewModel.skipIdCardBackSide() },
                    isBackSkipped = state.isBackSkipped,
                    onCombine = { viewModel.combineIdCardSides() },
                    onDirectPrint = { viewModel.directPrintIdCard() },
                    onSavePdf = { viewModel.savePdfIdCard() },
                    onReset = { viewModel.resetIdCard() }
                )
            }

            ScanMode.Batch -> {
                BatchModeView(
                    scannedPages = state.scannedPages,
                    batchCount = state.batchScanCount,
                    isProcessing = state.isProcessing,
                    onLaunchScanner = { launchScanner() },
                    onFinishBatch = { viewModel.finishBatchScan() }
                )
            }
        }

        // Error message
        if (state.error != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Red400.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Error, null, tint = Red400)
                    Spacer(Modifier.width(8.dp))
                    Text(state.error!!, color = Red400, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Document Mode View (existing, unchanged)
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun DocumentModeView(
    scannedPages: List<ScannedPageData>,
    isProcessing: Boolean,
    isSavingPdf: Boolean,
    onLaunchScanner: () -> Unit,
    onExportPdf: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isProcessing) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text("Processing scanned pages...", fontWeight = FontWeight.Medium)
        } else {
            // Scan prompt
            Icon(Icons.Filled.DocumentScanner, null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Auto Edge Detection Scanner",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Live edge detection • Auto-crop • Perspective correction",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp))
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onLaunchScanner,
                modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, null)
                Spacer(Modifier.width(12.dp))
                Text("Scan Document", fontWeight = FontWeight.SemiBold)
            }

            if (scannedPages.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                // Thumbnail strip
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    itemsIndexed(scannedPages) { index, page ->
                        Image(
                            bitmap = page.bitmap.asImageBitmap(),
                            contentDescription = "Page ${index + 1}",
                            modifier = Modifier.size(56.dp, 72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onExportPdf,
                    enabled = !isSavingPdf,
                    modifier = Modifier.fillMaxWidth(0.7f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green400)
                ) {
                    if (isSavingPdf) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.PictureAsPdf, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Export & Print", color = Color.White)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Batch Mode View (NEW)
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun BatchModeView(
    scannedPages: List<ScannedPageData>,
    batchCount: Int,
    isProcessing: Boolean,
    onLaunchScanner: () -> Unit,
    onFinishBatch: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isProcessing) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text("Processing scanned pages...", fontWeight = FontWeight.Medium)
        } else {
            val infiniteTransition = rememberInfiniteTransition(label = "batch")
            val pulse by infiniteTransition.animateFloat(
                initialValue = 0.9f, targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "batchPulse"
            )

            Surface(
                shape = CircleShape,
                color = Secondary.copy(alpha = 0.12f),
                modifier = Modifier.size((80 * pulse).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.BurstMode, null,
                        modifier = Modifier.size(40.dp), tint = Secondary)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Batch Scan Mode",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Scan multiple pages continuously without returning to this menu.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp))

            if (batchCount > 0) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Green400.copy(alpha = 0.12f)
                ) {
                    Text(
                        "✅ $batchCount page(s) captured",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = Green400, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onLaunchScanner,
                modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, null)
                Spacer(Modifier.width(12.dp))
                Text(if (batchCount == 0) "Start Batch Scan" else "Scan More Pages",
                    fontWeight = FontWeight.SemiBold)
            }

            if (scannedPages.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                // Thumbnail strip
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    itemsIndexed(scannedPages) { index, page ->
                        Image(
                            bitmap = page.bitmap.asImageBitmap(),
                            contentDescription = "Page ${index + 1}",
                            modifier = Modifier.size(48.dp, 64.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onFinishBatch,
                    modifier = Modifier.fillMaxWidth(0.7f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green400)
                ) {
                    Icon(Icons.Filled.Done, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Finish & Review", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  ID Card Mode View (existing, unchanged)
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdCardModeView(
    step: IdCardStep,
    frontBitmap: Bitmap?,
    backBitmap: Bitmap?,
    compositePreviewBitmap: Bitmap? = null,
    selectedFilter: String = "Magic Color",
    selectedLayout: IdCardLayout = IdCardLayout.SideBySide,
    onSelectFilter: (String) -> Unit = {},
    onSelectLayout: (IdCardLayout) -> Unit = {},
    isProcessing: Boolean,
    isSavingPdf: Boolean = false,
    isDirectPrinting: Boolean = false,
    idCardFrontOnly: Boolean = false,
    onToggleFrontOnly: (Boolean) -> Unit = {},
    onScanFront: () -> Unit,
    onScanBack: () -> Unit,
    onRetakeFront: () -> Unit = {},
    onRetakeBack: () -> Unit = {},
    onSkipBack: () -> Unit = {},
    isBackSkipped: Boolean = false,
    onCombine: () -> Unit,
    onDirectPrint: () -> Unit = {},
    onSavePdf: () -> Unit = {},
    onReset: () -> Unit
) {
    when (step) {
        IdCardStep.Front -> {
            // Step 1: Scan front side
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.CreditCard, null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text(if (idCardFrontOnly) "Scan ID Card (Front Only)" else "Step 1: Scan Front Side",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Auto edge detection • Auto-crop • Perspective correction",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp))
                Spacer(Modifier.height(20.dp))

                // Front Only / Both Sides toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = { onToggleFrontOnly(true) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = if (idCardFrontOnly) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                    ) {
                        Text(
                            "Front Only",
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            color = if (idCardFrontOnly) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Surface(
                        onClick = { onToggleFrontOnly(false) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = if (!idCardFrontOnly) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                    ) {
                        Text(
                            "Both Sides",
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold,
                            color = if (!idCardFrontOnly) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onScanFront,
                    modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, null)
                    Spacer(Modifier.width(12.dp))
                    Text(if (idCardFrontOnly) "Scan Front" else "Scan Front Side",
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }

        IdCardStep.Back -> {
            // Step 2: Scan back side (show front preview)
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Step 2: Scan Back Side",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Front side captured ✅",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))

                // Show front preview
                frontBitmap?.let {
                    Image(bitmap = it.asImageBitmap(), contentDescription = "Front",
                        modifier = Modifier.fillMaxWidth().weight(1f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit)
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onRetakeFront,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Retake Front")
                    }
                    Button(
                        onClick = onScanBack,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.FlipCameraAndroid, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Scan Back")
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onSkipBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.SkipNext, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Skip Back Side (Front Only)", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        IdCardStep.Preview -> {
            // Rich A4 Document Print Preview
            val isSingle = isBackSkipped || backBitmap == null
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "ID Card Print Preview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isSingle) "Single side • Ready to print" else "Front + Back • 300 DPI A4 Document",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Green400.copy(alpha = 0.12f)
                    ) {
                        Text(
                            "ISO/IEC 7810 Standard",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Green400
                        )
                    }
                }

                // Simulated A4 Page Preview Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f / 1.414f)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (compositePreviewBitmap != null) {
                            Image(
                                bitmap = compositePreviewBitmap.asImageBitmap(),
                                contentDescription = "A4 Page Preview",
                                modifier = Modifier.fillMaxSize().padding(10.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else if (frontBitmap != null) {
                            Image(
                                bitmap = frontBitmap.asImageBitmap(),
                                contentDescription = "ID Card Front",
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.55f),
                            modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp)
                        ) {
                            Text(
                                "85.6 × 54.0 mm",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // CamScanner Filter Palette
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Document Filter",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Magic Color", "Clean B&W", "Auto Clarify", "Grayscale", "Original").forEach { filter ->
                            val isSelected = selectedFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectFilter(filter) },
                                label = {
                                    Text(
                                        when (filter) {
                                            "Magic Color" -> "✨ Magic Color"
                                            "Clean B&W" -> "⬛ Clean B&W"
                                            "Auto Clarify" -> "⚡ Auto"
                                            else -> filter
                                        },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }
                }

                // Layout Selector (if dual-sided)
                if (!isSingle && backBitmap != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Page Layout",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedLayout == IdCardLayout.SideBySide,
                                onClick = { onSelectLayout(IdCardLayout.SideBySide) },
                                leadingIcon = { Icon(Icons.Filled.Description, null, modifier = Modifier.size(16.dp)) },
                                label = { Text("Side-by-Side (Top)", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedLayout == IdCardLayout.Stacked,
                                onClick = { onSelectLayout(IdCardLayout.Stacked) },
                                leadingIcon = { Icon(Icons.Filled.Layers, null, modifier = Modifier.size(16.dp)) },
                                label = { Text("Stacked", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Retake Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Front Side", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text("Captured ✅", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Green400)
                            }
                            TextButton(onClick = onRetakeFront, contentPadding = PaddingValues(horizontal = 6.dp)) {
                                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("Retake", fontSize = 11.sp)
                            }
                        }
                    }

                    if (!isSingle && backBitmap != null) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Back Side", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text("Captured ✅", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Green400)
                                }
                                TextButton(onClick = onRetakeBack, contentPadding = PaddingValues(horizontal = 6.dp)) {
                                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(2.dp))
                                    Text("Retake", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Primary Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDirectPrint,
                            enabled = !isProcessing && !isDirectPrinting && !isSavingPdf,
                            modifier = Modifier.weight(1.3f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            if (isDirectPrinting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.Print, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Direct Print", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onSavePdf,
                            enabled = !isProcessing && !isDirectPrinting && !isSavingPdf,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (isSavingPdf) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.Download, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Save PDF", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Button(
                        onClick = onCombine,
                        enabled = !isProcessing,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors()
                    ) {
                        Icon(Icons.Filled.Tune, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open Review Studio (Crop, Watermark, OCR)", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Review View (MAJOR UPGRADE)
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewView(
    modifier: Modifier = Modifier,
    state: ScannerUiState,
    onSelectPage: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    onApplyFilter: (Int, String) -> Unit,
    onBackToCamera: () -> Unit,
    onExportPdf: () -> Unit,
    onSharePdf: () -> Unit,
    onRotatePage: (Int, Int) -> Unit,
    onMovePage: (Int, Int) -> Unit,
    onEnterCrop: () -> Unit,
    onUpdateCrop: (RectF) -> Unit,
    onApplyCrop: (Int) -> Unit,
    onCancelCrop: () -> Unit,
    onToggleAdjustments: (Boolean) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onApplyAdjustments: (Int) -> Unit,
    onRunOcr: (Int) -> Unit,
    onSetPageSize: (PageSize) -> Unit,
    onSetWatermarkText: (String) -> Unit,
    onToggleWatermark: (Boolean) -> Unit,
    onSetWatermarkOpacity: (Float) -> Unit
) {
    val pages = state.scannedPages
    var showSettings by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (pages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.DocumentScanner, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("No pages scanned yet")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBackToCamera) { Text("Start Scanning") }
                }
            }
        } else {
            val safeIndex = state.selectedPageIndex.coerceIn(0, pages.size - 1)

            // ── Main preview area ──
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.isCropMode) {
                    // Crop overlay mode
                    CropOverlay(
                        bitmap = pages[safeIndex].bitmap,
                        cropRect = state.cropRect,
                        onCropRectChanged = onUpdateCrop,
                        onApply = { onApplyCrop(safeIndex) },
                        onCancel = onCancelCrop
                    )
                } else {
                    Image(bitmap = pages[safeIndex].bitmap.asImageBitmap(),
                        contentDescription = "Page ${safeIndex + 1}",
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)

                    // Delete button
                    IconButton(
                        onClick = { onDeletePage(safeIndex) },
                        modifier = Modifier.align(Alignment.TopEnd)
                            .background(Red400.copy(alpha = 0.8f), CircleShape)
                    ) { Icon(Icons.Filled.Delete, "Delete", tint = Color.White) }

                    // Page info badge
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
                        shape = RoundedCornerShape(20.dp), color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text("Page ${safeIndex + 1} of ${pages.size} • ${pages[safeIndex].filter}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }

                    // OCR loading indicator
                    if (state.isOcrRunning) {
                        Surface(
                            modifier = Modifier.align(Alignment.Center),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White, strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("Recognizing text...", color = Color.White,
                                    fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // ── Brightness/Contrast Adjustments Panel ──
            AnimatedVisibility(
                visible = state.showAdjustments,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Adjustments", fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LightMode, null, modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(8.dp))
                            Text("Brightness", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(72.dp))
                            Slider(
                                value = state.pageBrightness,
                                onValueChange = onBrightnessChange,
                                valueRange = -0.5f..0.5f,
                                modifier = Modifier.weight(1f)
                            )
                            Text("${(state.pageBrightness * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Contrast, null, modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(8.dp))
                            Text("Contrast", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(72.dp))
                            Slider(
                                value = state.pageContrast,
                                onValueChange = onContrastChange,
                                valueRange = 0.5f..2f,
                                modifier = Modifier.weight(1f)
                            )
                            Text("${(state.pageContrast * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onToggleAdjustments(false) }) { Text("Cancel") }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = { onApplyAdjustments(safeIndex) },
                                shape = RoundedCornerShape(8.dp)) { Text("Apply") }
                        }
                    }
                }
            }

            // ── Edit Toolbar ──
            Row(
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Rotate
                AssistChip(
                    onClick = { onRotatePage(safeIndex, 90) },
                    label = { Text("Rotate", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Filled.RotateRight, null, modifier = Modifier.size(16.dp)) }
                )
                // Crop
                AssistChip(
                    onClick = onEnterCrop,
                    label = { Text("Crop", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Filled.Crop, null, modifier = Modifier.size(16.dp)) }
                )
                // Brightness/Contrast
                AssistChip(
                    onClick = { onToggleAdjustments(!state.showAdjustments) },
                    label = { Text("Adjust", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Filled.Tune, null, modifier = Modifier.size(16.dp)) }
                )
                // OCR
                AssistChip(
                    onClick = { onRunOcr(safeIndex) },
                    enabled = !state.isOcrRunning,
                    label = { Text("OCR", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Filled.TextFields, null, modifier = Modifier.size(16.dp)) }
                )
                // Settings
                AssistChip(
                    onClick = { showSettings = !showSettings },
                    label = { Text("Settings", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Filled.Settings, null, modifier = Modifier.size(16.dp)) }
                )
            }

            // ── Settings panel (page size, watermark) ──
            AnimatedVisibility(
                visible = showSettings,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Page size
                        Text("Page Size", fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PageSize.entries.forEach { size ->
                                FilterChip(
                                    selected = state.pageSize == size,
                                    onClick = { onSetPageSize(size) },
                                    label = { Text(size.label, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Divider()
                        Spacer(Modifier.height(12.dp))

                        // Watermark
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Watermark", fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.labelLarge)
                            Switch(
                                checked = state.watermarkEnabled,
                                onCheckedChange = onToggleWatermark
                            )
                        }

                        AnimatedVisibility(visible = state.watermarkEnabled) {
                            Column {
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = state.watermarkText,
                                    onValueChange = onSetWatermarkText,
                                    label = { Text("Watermark Text") },
                                    placeholder = { Text("CONFIDENTIAL") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Opacity", style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.width(56.dp))
                                    Slider(
                                        value = state.watermarkOpacity,
                                        onValueChange = onSetWatermarkOpacity,
                                        valueRange = 0.05f..0.5f,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("${(state.watermarkOpacity * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.width(36.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ── Filter chips ──
            Row(
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Magic Color", "Clean B&W", "Auto Enhance", "Grayscale", "Sharp", "Original").forEach { filter ->
                    FilterChip(
                        selected = pages[safeIndex].filter == filter,
                        onClick = { onApplyFilter(safeIndex, filter) },
                        label = {
                            Text(when (filter) {
                                "Magic Color" -> "✨ Magic"
                                "Clean B&W" -> "⬛ B&W"
                                "Auto Enhance" -> "⚡ Auto"
                                else -> filter
                            },
                                style = MaterialTheme.typography.labelSmall)
                        },
                        modifier = Modifier.padding(horizontal = 1.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Page thumbnails with reorder ──
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                itemsIndexed(pages) { index, page ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Card(
                            onClick = { onSelectPage(index) },
                            shape = RoundedCornerShape(8.dp),
                            border = if (index == safeIndex) CardDefaults.outlinedCardBorder() else null,
                            colors = CardDefaults.cardColors(
                                containerColor = if (index == safeIndex)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.size(56.dp, 72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(bitmap = page.bitmap.asImageBitmap(),
                                    contentDescription = "Page ${index + 1}",
                                    modifier = Modifier.fillMaxSize().padding(2.dp),
                                    contentScale = ContentScale.Fit)
                                Box(
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)
                                        .size(18.dp).clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${index + 1}", color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        // Reorder arrows
                        if (pages.size > 1) {
                            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                                if (index > 0) {
                                    IconButton(
                                        onClick = { onMovePage(index, index - 1) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Filled.ChevronLeft, "Move left",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    Spacer(Modifier.size(20.dp))
                                }
                                if (index < pages.size - 1) {
                                    IconButton(
                                        onClick = { onMovePage(index, index + 1) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Filled.ChevronRight, "Move right",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    Spacer(Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ── Bottom action bar ──
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onBackToCamera, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Add") }

                OutlinedButton(
                    onClick = onSharePdf,
                    enabled = !state.isSavingPdf,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Share")
                }

                Button(
                    onClick = onExportPdf, enabled = !state.isSavingPdf,
                    modifier = Modifier.weight(1.2f), shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isSavingPdf) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else { Icon(Icons.Filled.PictureAsPdf, null, modifier = Modifier.size(18.dp)) }
                    Spacer(Modifier.width(4.dp)); Text("Print")
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Crop Overlay Component
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun CropOverlay(
    bitmap: Bitmap,
    cropRect: RectF,
    onCropRectChanged: (RectF) -> Unit,
    onApply: () -> Unit,
    onCancel: () -> Unit
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val rect = remember(cropRect) { mutableStateOf(cropRect) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Drag to adjust crop area",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onSizeChanged { containerSize = it }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val w = containerSize.width.toFloat()
                        val h = containerSize.height.toFloat()
                        val dx = dragAmount.x / w
                        val dy = dragAmount.y / h

                        val current = rect.value
                        val touchX = change.position.x / w
                        val touchY = change.position.y / h

                        // Determine which edge/corner to drag
                        val edgeThreshold = 0.08f
                        val newRect = when {
                            // Near left edge
                            kotlin.math.abs(touchX - current.left) < edgeThreshold ->
                                RectF((current.left + dx).coerceIn(0f, current.right - 0.1f),
                                    current.top, current.right, current.bottom)
                            // Near right edge
                            kotlin.math.abs(touchX - current.right) < edgeThreshold ->
                                RectF(current.left, current.top,
                                    (current.right + dx).coerceIn(current.left + 0.1f, 1f), current.bottom)
                            // Near top edge
                            kotlin.math.abs(touchY - current.top) < edgeThreshold ->
                                RectF(current.left, (current.top + dy).coerceIn(0f, current.bottom - 0.1f),
                                    current.right, current.bottom)
                            // Near bottom edge
                            kotlin.math.abs(touchY - current.bottom) < edgeThreshold ->
                                RectF(current.left, current.top, current.right,
                                    (current.bottom + dy).coerceIn(current.top + 0.1f, 1f))
                            // Move entire rect
                            else -> {
                                val nw = current.width()
                                val nh = current.height()
                                val nl = (current.left + dx).coerceIn(0f, 1f - nw)
                                val nt = (current.top + dy).coerceIn(0f, 1f - nh)
                                RectF(nl, nt, nl + nw, nt + nh)
                            }
                        }
                        rect.value = newRect
                        onCropRectChanged(newRect)
                    }
                }
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Crop preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Draw crop overlay
            if (containerSize.width > 0 && containerSize.height > 0) {
                val r = rect.value
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Semi-transparent overlay outside crop
                    val overlayColor = Color.Black.copy(alpha = 0.5f)
                    // Top
                    drawRect(overlayColor, topLeft = Offset.Zero,
                        size = androidx.compose.ui.geometry.Size(size.width, size.height * r.top))
                    // Bottom
                    drawRect(overlayColor,
                        topLeft = Offset(0f, size.height * r.bottom),
                        size = androidx.compose.ui.geometry.Size(size.width, size.height * (1f - r.bottom)))
                    // Left
                    drawRect(overlayColor,
                        topLeft = Offset(0f, size.height * r.top),
                        size = androidx.compose.ui.geometry.Size(size.width * r.left, size.height * (r.bottom - r.top)))
                    // Right
                    drawRect(overlayColor,
                        topLeft = Offset(size.width * r.right, size.height * r.top),
                        size = androidx.compose.ui.geometry.Size(size.width * (1f - r.right), size.height * (r.bottom - r.top)))

                    // Crop border
                    drawRect(
                        Color.White,
                        topLeft = Offset(size.width * r.left, size.height * r.top),
                        size = androidx.compose.ui.geometry.Size(
                            size.width * (r.right - r.left),
                            size.height * (r.bottom - r.top)
                        ),
                        style = Stroke(width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                    )

                    // Corner handles
                    val handleSize = 12.dp.toPx()
                    listOf(
                        Offset(size.width * r.left, size.height * r.top),
                        Offset(size.width * r.right, size.height * r.top),
                        Offset(size.width * r.left, size.height * r.bottom),
                        Offset(size.width * r.right, size.height * r.bottom)
                    ).forEach { corner ->
                        drawCircle(Color.White, handleSize / 2, corner)
                        drawCircle(Primary, handleSize / 3, corner)
                    }

                    // Rule-of-thirds grid
                    val gridColor = Color.White.copy(alpha = 0.3f)
                    val cropW = size.width * (r.right - r.left)
                    val cropH = size.height * (r.bottom - r.top)
                    for (i in 1..2) {
                        val gx = size.width * r.left + cropW * i / 3
                        drawLine(gridColor, Offset(gx, size.height * r.top), Offset(gx, size.height * r.bottom), strokeWidth = 0.5f)
                        val gy = size.height * r.top + cropH * i / 3
                        drawLine(gridColor, Offset(size.width * r.left, gy), Offset(size.width * r.right, gy), strokeWidth = 0.5f)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)) { Text("Cancel") }
            Button(onClick = onApply, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Filled.Crop, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Apply Crop")
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  OCR Result Dialog
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun OcrResultDialog(
    text: String,
    onDismiss: () -> Unit,
    onCopy: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.TextFields, null, tint = Primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Recognized Text",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Close")
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (text.isBlank()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.SearchOff, null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text("No text found on this page",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(12.dp)) {
                            SelectionContainer {
                                Text(text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (text.isNotBlank()) {
                        OutlinedButton(
                            onClick = onCopy,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Copy All")
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Done") }
                }
            }
        }
    }
}
