package com.wifiprint.app.ui.screens.scanner

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

import com.wifiprint.app.ui.theme.*
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onScanComplete: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Handle saved PDF navigation
    LaunchedEffect(state.savedPdfUri) {
        state.savedPdfUri?.let { uri ->
            onScanComplete(uri.toString())
            viewModel.clearSavedPdf()
        }
    }

    var hasCameraPermission by remember { mutableStateOf(false) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) {
            hasCameraPermission = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
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
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Permission")
                    }
                }
            }
        } else if (state.showCamera) {
            CameraView(
                modifier = Modifier.fillMaxSize().padding(padding),
                onPhotoCaptured = { bitmap -> viewModel.onPhotoCaptured(bitmap) },
                scannedPages = state.scannedPages,
                currentFilter = state.currentFilter,
                onFilterChanged = { viewModel.setFilter(it) },
                isProcessing = state.isProcessing,
                autoEdge = state.autoEdgeDetection,
                onToggleAutoEdge = { viewModel.toggleAutoEdge(it) },
                onExportPdf = {
                    if (state.scannedPages.isNotEmpty()) viewModel.exportAsPdf()
                },
                isSavingPdf = state.isSavingPdf
            )
        } else {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraView(
    modifier: Modifier = Modifier,
    onPhotoCaptured: (Bitmap) -> Unit,
    scannedPages: List<ScannedPageData>,
    currentFilter: String,
    onFilterChanged: (String) -> Unit,
    isProcessing: Boolean,
    autoEdge: Boolean,
    onToggleAutoEdge: (Boolean) -> Unit,
    onExportPdf: () -> Unit,
    isSavingPdf: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    Column(modifier = modifier) {
        // Camera preview
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .setTargetResolution(Size(2480, 3508))
                            .build()
                        imageCapture = capture

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview, capture
                            )
                        } catch (e: Exception) {
                            // Handle camera init error
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Document guide overlay — shows edge detection area
            if (autoEdge) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                        .border(2.dp, Primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                )
                // Label
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        "Align document within guide",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            // Processing overlay
            if (isProcessing) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(Modifier.height(12.dp))
                        Text("Detecting edges & enhancing...",
                            color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Page count badge
            if (scannedPages.isNotEmpty()) {
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text("${scannedPages.size}", modifier = Modifier.padding(4.dp))
                }
            }
        }

        // Controls bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Auto edge detection toggle + filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Auto edge toggle
                    FilterChip(
                        selected = autoEdge,
                        onClick = { onToggleAutoEdge(!autoEdge) },
                        label = { Text("Auto Crop", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(Icons.Filled.Crop, null, modifier = Modifier.size(16.dp))
                        }
                    )

                    Spacer(Modifier.width(4.dp))

                    // Filter chips
                    listOf("Auto Enhance", "B&W", "Grayscale", "Sharp", "High Contrast").forEach { filter ->
                        FilterChip(
                            selected = currentFilter == filter,
                            onClick = { onFilterChanged(filter) },
                            label = {
                                Text(
                                    when (filter) {
                                        "Auto Enhance" -> "Auto"
                                        "High Contrast" -> "HiCon"
                                        else -> filter
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.padding(horizontal = 1.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Capture & action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbnail strip of captured pages
                    if (scannedPages.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            itemsIndexed(scannedPages) { _, page ->
                                Image(
                                    bitmap = page.bitmap.asImageBitmap(),
                                    contentDescription = "Page ${page.index + 1}",
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Spacer(Modifier.weight(1f))
                    }

                    // Capture button
                    FloatingActionButton(
                        onClick = {
                            if (!isProcessing) {
                                imageCapture?.takePicture(
                                    cameraExecutor,
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(image: ImageProxy) {
                                            val buffer = image.planes[0].buffer
                                            val bytes = ByteArray(buffer.remaining())
                                            buffer.get(bytes)
                                            var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                                            // Correct rotation
                                            val rotation = image.imageInfo.rotationDegrees
                                            if (rotation != 0) {
                                                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                                                bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                                    bitmap.width, bitmap.height, matrix, true)
                                            }

                                            onPhotoCaptured(bitmap)
                                            image.close()
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            // Handle capture error
                                        }
                                    }
                                )
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Filled.CameraAlt, "Capture",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }

                    Spacer(Modifier.width(8.dp))

                    // Export PDF button
                    if (scannedPages.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = onExportPdf,
                            containerColor = Green400,
                            modifier = Modifier.size(48.dp)
                        ) {
                            if (isSavingPdf) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Filled.PictureAsPdf, "Export PDF",
                                    tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
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
            // Selected page preview
            val safeIndex = selectedIndex.coerceIn(0, pages.size - 1)
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = pages[safeIndex].bitmap.asImageBitmap(),
                    contentDescription = "Page ${safeIndex + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Delete button overlay
                IconButton(
                    onClick = { onDeletePage(safeIndex) },
                    modifier = Modifier.align(Alignment.TopEnd)
                        .background(Red400.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(Icons.Filled.Delete, "Delete", tint = Color.White)
                }

                // Page info
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        "Page ${safeIndex + 1} of ${pages.size} • ${pages[safeIndex].filter}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White, style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Filter options for selected page
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf("Auto Enhance", "B&W", "Grayscale", "Sharp", "High Contrast").forEach { filter ->
                    FilterChip(
                        selected = pages[safeIndex].filter == filter,
                        onClick = { onApplyFilter(safeIndex, filter) },
                        label = {
                            Text(
                                when (filter) {
                                    "Auto Enhance" -> "Auto"
                                    "High Contrast" -> "HiCon"
                                    else -> filter
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Page thumbnail strip
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                itemsIndexed(pages) { index, page ->
                    Card(
                        onClick = { onSelectPage(index) },
                        shape = RoundedCornerShape(8.dp),
                        border = if (index == safeIndex)
                            CardDefaults.outlinedCardBorder() else null,
                        colors = CardDefaults.cardColors(
                            containerColor = if (index == safeIndex)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.size(56.dp, 72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                bitmap = page.bitmap.asImageBitmap(),
                                contentDescription = "Page ${index + 1}",
                                modifier = Modifier.fillMaxSize().padding(2.dp),
                                contentScale = ContentScale.Fit
                            )
                            Box(
                                modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)
                                    .size(18.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${index + 1}",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Bottom actions
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackToCamera,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Page")
                }

                Button(
                    onClick = onExportPdf,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Filled.PictureAsPdf, null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Export & Print")
                }
            }
        }
    }
}
