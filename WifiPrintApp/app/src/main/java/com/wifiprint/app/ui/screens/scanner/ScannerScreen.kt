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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
    onBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // Handle saved PDF navigation
    LaunchedEffect(state.savedPdfUri) {
        state.savedPdfUri?.let { uri ->
            onScanComplete(uri.toString())
            viewModel.clearSavedPdf()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Document Scanner", style = MaterialTheme.typography.titleMedium)
                        if (state.scannedPages.isNotEmpty()) {
                            Text("${state.scannedPages.size} page(s) captured",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (state.scannedPages.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setShowCamera(!state.showCamera) }) {
                            Icon(
                                if (state.showCamera) Icons.Filled.ViewCarousel else Icons.Filled.CameraAlt,
                                "Toggle view"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (!hasCameraPermission) {
            // Permission request UI
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CameraAlt, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("Camera permission is required to scan documents",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                        Text("Grant Permission")
                    }
                }
            }
        } else if (state.showCamera) {
            // Main scanner UI with mode tabs
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Mode selector tabs
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.scanMode == ScanMode.Document,
                        onClick = { viewModel.setScanMode(ScanMode.Document) },
                        label = { Text("📄 Document") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = state.scanMode == ScanMode.IDCard,
                        onClick = { viewModel.setScanMode(ScanMode.IDCard) },
                        label = { Text("🪪 ID Card") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = state.scanMode == ScanMode.Batch,
                        onClick = { viewModel.setScanMode(ScanMode.Batch) },
                        label = { Text("📚 Batch") },
                        modifier = Modifier.weight(1f)
                    )
                }

                when (state.scanMode) {
                    ScanMode.Document -> {
                        // Document mode — launch ML Kit scanner
                        DocumentModeView(
                            scannedPages = state.scannedPages,
                            isProcessing = state.isProcessing,
                            isSavingPdf = state.isSavingPdf,
                            onLaunchScanner = {
                                activity?.let { act ->
                                    scanner.getStartScanIntent(act)
                                        .addOnSuccessListener { intentSender ->
                                            scannerLauncher.launch(
                                                IntentSenderRequest.Builder(intentSender).build()
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            viewModel.clearError()
                                        }
                                }
                            },
                            onExportPdf = {
                                if (state.scannedPages.isNotEmpty()) viewModel.exportAsPdf()
                            }
                        )
                    }
                    ScanMode.IDCard -> {
                        // ID Card mode — ML Kit scan for each side
                        IdCardModeView(
                            step = state.idCardStep,
                            frontBitmap = state.idCardFrontBitmap,
                            backBitmap = state.idCardBackBitmap,
                            isProcessing = state.isProcessing,
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
                            onSkipBack = { viewModel.skipIdCardBackSide() },
                            isBackSkipped = state.isBackSkipped,
                            onCombine = { viewModel.combineIdCardSides() },
                            onReset = { viewModel.resetIdCard() }
                        )
                    }
                    ScanMode.Batch -> {
                        BatchModeView(
                            scannedPages = state.scannedPages,
                            batchCount = state.batchScanCount,
                            isProcessing = state.isProcessing,
                            onLaunchScanner = {
                                activity?.let { act ->
                                    scanner.getStartScanIntent(act)
                                        .addOnSuccessListener { intentSender ->
                                            scannerLauncher.launch(
                                                IntentSenderRequest.Builder(intentSender).build()
                                            )
                                        }
                                }
                            },
                            onFinishBatch = { viewModel.finishBatchScan() }
                        )
                    }
                }
            }
        } else {
            // Review mode
            ReviewView(
                modifier = Modifier.fillMaxSize().padding(padding),
                state = state,
                onSelectPage = { viewModel.selectPage(it) },
                onDeletePage = { viewModel.removePage(it) },
                onApplyFilter = { index, filter -> viewModel.applyFilterToPage(index, filter) },
                onBackToCamera = { viewModel.setShowCamera(true) },
                onExportPdf = { viewModel.exportAsPdf() },
                onSharePdf = { viewModel.sharePdf() },
                onRotatePage = { index, degrees -> viewModel.rotatePage(index, degrees) },
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
        }

        // Error snackbar
        if (state.error != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
                }
            ) { Text(state.error!!) }
        }
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

@Composable
private fun IdCardModeView(
    step: IdCardStep,
    frontBitmap: Bitmap?,
    backBitmap: Bitmap?,
    isProcessing: Boolean,
    idCardFrontOnly: Boolean = false,
    onToggleFrontOnly: (Boolean) -> Unit = {},
    onScanFront: () -> Unit,
    onScanBack: () -> Unit,
    onSkipBack: () -> Unit = {},
    isBackSkipped: Boolean = false,
    onCombine: () -> Unit,
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
                    // Front Only option
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
                    // Both Sides option
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
                        onClick = onReset,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Retake")
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
            // Preview
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val isSingle = isBackSkipped || backBitmap == null
                Text(
                    if (isSingle) "ID Card Preview (Front Only)" else "ID Card Preview (Both Sides)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (isSingle) "Front side captured • Back side skipped" else "Both sides captured. Review and add to document.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(14.dp))

                // Front preview
                Text("FRONT", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                frontBitmap?.let {
                    Image(bitmap = it.asImageBitmap(), contentDescription = "Front",
                        modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit)
                }

                if (!isSingle && backBitmap != null) {
                    Spacer(Modifier.height(12.dp))
                    Text("BACK", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.height(4.dp))
                    Image(bitmap = backBitmap.asImageBitmap(), contentDescription = "Back",
                        modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit)
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReset,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Retake")
                    }
                    Button(
                        onClick = onCombine,
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Filled.Check, null)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(if (isSingle) "Add ID Card" else "Combine & Add")
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
                listOf("Auto Enhance", "B&W", "Grayscale", "Sharp", "High Contrast", "Original").forEach { filter ->
                    FilterChip(
                        selected = pages[safeIndex].filter == filter,
                        onClick = { onApplyFilter(safeIndex, filter) },
                        label = {
                            Text(when (filter) { "Auto Enhance" -> "Auto"; "High Contrast" -> "HiCon"; else -> filter },
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
