package com.wifiprint.app.ui.screens.scanner

import android.app.Activity
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
                            onCombine = { viewModel.combineIdCardSides() },
                            onReset = { viewModel.resetIdCard() }
                        )
                    }
                }
            }
        } else {
            // Review mode
            ReviewView(
                modifier = Modifier.fillMaxSize().padding(padding),
                pages = state.scannedPages,
                selectedIndex = state.selectedPageIndex,
                onSelectPage = { viewModel.selectPage(it) },
                onDeletePage = { viewModel.removePage(it) },
                onApplyFilter = { index, filter -> viewModel.applyFilterToPage(index, filter) },
                onBackToCamera = { viewModel.setShowCamera(true) },
                onExportPdf = { viewModel.exportAsPdf() },
                isSaving = state.isSavingPdf
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

@Composable
private fun IdCardModeView(
    step: IdCardStep,
    frontBitmap: Bitmap?,
    backBitmap: Bitmap?,
    isProcessing: Boolean,
    onScanFront: () -> Unit,
    onScanBack: () -> Unit,
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
                Text("Step 1: Scan Front Side",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Auto edge detection • Auto-crop • Perspective correction",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp))
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onScanFront,
                    modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Scan Front", fontWeight = FontWeight.SemiBold)
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
                Spacer(Modifier.height(8.dp))
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
                        Text("Retake Front")
                    }
                    Button(
                        onClick = onScanBack,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.FlipCameraAndroid, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Scan Back")
                    }
                }
            }
        }

        IdCardStep.Preview -> {
            // Preview both sides
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ID Card Preview", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Both sides captured. Review and add to document.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))

                // Front preview
                Text("FRONT", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                frontBitmap?.let {
                    Image(bitmap = it.asImageBitmap(), contentDescription = "Front",
                        modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit)
                }

                Spacer(Modifier.height(12.dp))

                // Back preview
                Text("BACK", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.height(4.dp))
                backBitmap?.let {
                    Image(bitmap = it.asImageBitmap(), contentDescription = "Back",
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
                        modifier = Modifier.weight(1f),
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
                        Text("Add to Document")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewView(
    modifier: Modifier = Modifier,
    pages: List<ScannedPageData>,
    selectedIndex: Int,
    onSelectPage: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    onApplyFilter: (Int, String) -> Unit,
    onBackToCamera: () -> Unit,
    onExportPdf: () -> Unit,
    isSaving: Boolean
) {
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
            val safeIndex = selectedIndex.coerceIn(0, pages.size - 1)
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(bitmap = pages[safeIndex].bitmap.asImageBitmap(),
                    contentDescription = "Page ${safeIndex + 1}",
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                IconButton(
                    onClick = { onDeletePage(safeIndex) },
                    modifier = Modifier.align(Alignment.TopEnd)
                        .background(Red400.copy(alpha = 0.8f), CircleShape)
                ) { Icon(Icons.Filled.Delete, "Delete", tint = Color.White) }
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
                    shape = RoundedCornerShape(20.dp), color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text("Page ${safeIndex + 1} of ${pages.size} • ${pages[safeIndex].filter}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf("Auto Enhance", "B&W", "Grayscale", "Sharp", "High Contrast").forEach { filter ->
                    FilterChip(
                        selected = pages[safeIndex].filter == filter,
                        onClick = { onApplyFilter(safeIndex, filter) },
                        label = {
                            Text(when (filter) { "Auto Enhance" -> "Auto"; "High Contrast" -> "HiCon"; else -> filter },
                                style = MaterialTheme.typography.labelSmall)
                        },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                itemsIndexed(pages) { index, page ->
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
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackToCamera, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Icon(Icons.Filled.Add, null); Spacer(Modifier.width(8.dp)); Text("Add Page") }
                Button(
                    onClick = onExportPdf, enabled = !isSaving,
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else { Icon(Icons.Filled.PictureAsPdf, null) }
                    Spacer(Modifier.width(8.dp)); Text("Export & Print")
                }
            }
        }
    }
}
