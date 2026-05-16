package com.wifiprint.app.ui.screens.preview

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.wifiprint.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentPreviewScreen(
    fileUriString: String,
    onBack: () -> Unit,
    onPrintFromPage: ((Int) -> Unit)? = null,
    viewModel: DocumentPreviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(fileUriString) {
        viewModel.setFileUri(fileUriString)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.fileName, maxLines = 1, style = MaterialTheme.typography.titleMedium)
                        if (state.totalPages > 1) {
                            Text("Page ${state.currentPage} of ${state.totalPages}",
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
                    if (onPrintFromPage != null) {
                        IconButton(onClick = { onPrintFromPage(state.currentPage) }) {
                            Icon(Icons.Filled.Print, "Print", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Loading preview...")
                    }
                }
            } else if (state.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.ErrorOutline, null,
                            modifier = Modifier.size(64.dp), tint = Red400)
                        Spacer(Modifier.height(16.dp))
                        Text(state.error!!, textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                when (state.fileType) {
                    "PDF" -> PdfPreview(
                        uri = state.fileUri!!,
                        currentPage = state.currentPage,
                        onPageChanged = { viewModel.setCurrentPage(it) },
                        onTotalPagesKnown = { viewModel.setTotalPages(it) }
                    )
                    "Image" -> ImagePreview(uri = state.fileUri!!)
                    "Text" -> TextPreview(uri = state.fileUri!!)
                    else -> UnsupportedPreview(state.fileType)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfPreview(
    uri: Uri,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    onTotalPagesKnown: (Int) -> Unit
) {
    val context = LocalContext.current
    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Render PDF pages
    LaunchedEffect(uri) {
        try {
            val pages = mutableListOf<Bitmap>()
            context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                val renderer = PdfRenderer(fd)
                try {
                    for (i in 0 until renderer.pageCount) {
                        val page = renderer.openPage(i)
                        val bitmap = Bitmap.createBitmap(
                            page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888
                        )
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        pages.add(bitmap)
                    }
                } finally {
                    renderer.close()
                }
            } ?: run {
                errorMessage = "Could not open PDF file"
            }
            bitmaps = pages
            onTotalPagesKnown(pages.size)
        } catch (e: Exception) {
            android.util.Log.e("PdfPreview", "Failed to render PDF", e)
            errorMessage = "Could not render PDF: ${e.message}"
        }
    }

    if (errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.ErrorOutline, null,
                    modifier = Modifier.size(64.dp), tint = Red400)
                Spacer(Modifier.height(16.dp))
                Text(errorMessage!!, textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else if (bitmaps.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main preview area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 4f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val pageIndex = (currentPage - 1).coerceIn(0, bitmaps.size - 1)
                Image(
                    bitmap = bitmaps[pageIndex].asImageBitmap(),
                    contentDescription = "Page $currentPage",
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    contentScale = ContentScale.Fit
                )
            }

            // Page navigation bar
            if (bitmaps.size > 1) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // Navigation controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onPageChanged(currentPage - 1) },
                                enabled = currentPage > 1
                            ) { Icon(Icons.Filled.ChevronLeft, "Previous") }

                            Text(
                                "$currentPage / ${bitmaps.size}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            IconButton(
                                onClick = { onPageChanged(currentPage + 1) },
                                enabled = currentPage < bitmaps.size
                            ) { Icon(Icons.Filled.ChevronRight, "Next") }
                        }

                        // Thumbnail strip
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            itemsIndexed(bitmaps) { index, bitmap ->
                                val isActive = index == currentPage - 1
                                Card(
                                    onClick = { onPageChanged(index + 1) },
                                    shape = RoundedCornerShape(6.dp),
                                    border = if (isActive) CardDefaults.outlinedCardBorder()
                                    else null,
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier.size(48.dp, 64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Page ${index + 1}",
                                            modifier = Modifier.fillMaxSize().padding(2.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                        if (isActive) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(2.dp)
                                                    .size(16.dp)
                                                    .clip(CircleShape)
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
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImagePreview(uri: Uri) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 4f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = "Image preview",
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun TextPreview(uri: Uri) {
    val context = LocalContext.current
    var textContent by remember { mutableStateOf("Loading...") }

    LaunchedEffect(uri) {
        try {
            textContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: "Cannot read file"
        } catch (e: Exception) {
            textContent = "Error reading file: ${e.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = textContent,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun UnsupportedPreview(fileType: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Description, null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Text("Preview not available for $fileType files",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("You can still print this file",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
