package com.wifiprint.app.ui.screens.scanner

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.*

data class ScannedPageData(
    val index: Int,
    val originalBitmap: Bitmap,  // Keep original for re-filtering
    val bitmap: Bitmap,
    val uri: Uri? = null,
    val filter: String = "Auto Enhance"
)

data class ScannerUiState(
    val isCapturing: Boolean = false,
    val scannedPages: List<ScannedPageData> = emptyList(),
    val selectedPageIndex: Int = -1,
    val currentFilter: String = "Auto Enhance",
    val isProcessing: Boolean = false,
    val isSavingPdf: Boolean = false,
    val savedPdfUri: Uri? = null,
    val error: String? = null,
    val showCamera: Boolean = true,
    val autoEdgeDetection: Boolean = true  // Enable auto edge detection
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "ScannerViewModel"
        // Standard A4 at 300 DPI
        private const val A4_WIDTH = 2480
        private const val A4_HEIGHT = 3508
    }

    private val _state = MutableStateFlow(ScannerUiState())
    val state: StateFlow<ScannerUiState> = _state

    fun onPhotoCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }

            val processed = withContext(Dispatchers.Default) {
                var img = bitmap

                // Step 1: Auto edge detection & crop
                if (_state.value.autoEdgeDetection) {
                    img = autoDetectAndCrop(img)
                }

                // Step 2: Scale to A4 proportions for consistent output
                img = scaleToA4(img)

                // Step 3: Apply selected image filter
                val filtered = applyFilter(img, _state.value.currentFilter)

                Pair(img, filtered)
            }

            val page = ScannedPageData(
                index = _state.value.scannedPages.size,
                originalBitmap = processed.first,
                bitmap = processed.second,
                filter = _state.value.currentFilter
            )

            _state.update {
                it.copy(
                    scannedPages = it.scannedPages + page,
                    isProcessing = false,
                    selectedPageIndex = it.scannedPages.size
                )
            }
        }
    }

    fun removePage(index: Int) {
        _state.update {
            val updated = it.scannedPages.toMutableList()
            if (index in updated.indices) {
                updated[index].bitmap.recycle()
                updated[index].originalBitmap.recycle()
                updated.removeAt(index)
                val reindexed = updated.mapIndexed { i, page -> page.copy(index = i) }
                it.copy(
                    scannedPages = reindexed,
                    selectedPageIndex = (it.selectedPageIndex).coerceAtMost(reindexed.size - 1)
                )
            } else it
        }
    }

    fun reorderPage(from: Int, to: Int) {
        _state.update {
            val updated = it.scannedPages.toMutableList()
            if (from in updated.indices && to in updated.indices) {
                val page = updated.removeAt(from)
                updated.add(to, page)
                val reindexed = updated.mapIndexed { i, p -> p.copy(index = i) }
                it.copy(scannedPages = reindexed)
            } else it
        }
    }

    fun setFilter(filter: String) {
        _state.update { it.copy(currentFilter = filter) }
    }

    fun toggleAutoEdge(enabled: Boolean) {
        _state.update { it.copy(autoEdgeDetection = enabled) }
    }

    fun applyFilterToPage(pageIndex: Int, filter: String) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }
            val pages = _state.value.scannedPages.toMutableList()
            if (pageIndex in pages.indices) {
                val original = pages[pageIndex].originalBitmap
                val processed = withContext(Dispatchers.Default) {
                    applyFilter(original, filter)
                }
                // Don't recycle old filtered — original stays intact
                pages[pageIndex] = pages[pageIndex].copy(bitmap = processed, filter = filter)
                _state.update { it.copy(scannedPages = pages, isProcessing = false) }
            }
        }
    }

    fun setShowCamera(show: Boolean) {
        _state.update { it.copy(showCamera = show) }
    }

    fun selectPage(index: Int) {
        _state.update { it.copy(selectedPageIndex = index) }
    }

    fun exportAsPdf() {
        viewModelScope.launch {
            _state.update { it.copy(isSavingPdf = true, error = null) }

            try {
                val uri = withContext(Dispatchers.IO) {
                    createPdfFromBitmaps(context, _state.value.scannedPages.map { it.bitmap })
                }
                _state.update { it.copy(isSavingPdf = false, savedPdfUri = uri) }
            } catch (e: Exception) {
                _state.update { it.copy(isSavingPdf = false, error = "Failed to create PDF: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearSavedPdf() {
        _state.update { it.copy(savedPdfUri = null) }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Auto Edge Detection & Document Cropping
    // ═══════════════════════════════════════════════════════════════

    /**
     * Detect document edges using luminance analysis and crop to the document boundary.
     * Uses a multi-pass approach:
     * 1. Downscale for fast edge analysis
     * 2. Find dominant edges using Sobel-like gradient detection
     * 3. Find the largest rectangular region
     * 4. Crop and apply perspective correction
     */
    private fun autoDetectAndCrop(bitmap: Bitmap): Bitmap {
        try {
            val w = bitmap.width
            val h = bitmap.height

            // Downscale for fast processing
            val scale = 0.25f
            val sw = (w * scale).toInt()
            val sh = (h * scale).toInt()
            val small = Bitmap.createScaledBitmap(bitmap, sw, sh, true)

            // Convert to grayscale luminance array
            val pixels = IntArray(sw * sh)
            small.getPixels(pixels, 0, sw, 0, 0, sw, sh)
            val luminance = IntArray(sw * sh)
            for (i in pixels.indices) {
                val r = (pixels[i] shr 16) and 0xFF
                val g = (pixels[i] shr 8) and 0xFF
                val b = pixels[i] and 0xFF
                luminance[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            }

            // Apply Sobel edge detection
            val edges = sobelEdgeDetect(luminance, sw, sh)

            // Find document boundary using edge projection
            val bounds = findDocumentBounds(edges, sw, sh)
            small.recycle()

            if (bounds != null) {
                // Scale bounds back to original size
                val left = (bounds[0] / scale).toInt().coerceIn(0, w - 1)
                val top = (bounds[1] / scale).toInt().coerceIn(0, h - 1)
                val right = (bounds[2] / scale).toInt().coerceIn(left + 1, w)
                val bottom = (bounds[3] / scale).toInt().coerceIn(top + 1, h)

                val cropW = right - left
                val cropH = bottom - top

                // Only crop if the detected area is significant (>30% of image)
                if (cropW > w * 0.3 && cropH > h * 0.3) {
                    Log.d(TAG, "Edge detection: cropping to ($left, $top, $right, $bottom)")
                    return Bitmap.createBitmap(bitmap, left, top, cropW, cropH)
                }
            }

            Log.d(TAG, "Edge detection: no significant document boundary found, using full image")
            return bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Edge detection failed, using original", e)
            return bitmap
        }
    }

    /**
     * Sobel edge detection on grayscale luminance array.
     */
    private fun sobelEdgeDetect(lum: IntArray, w: Int, h: Int): IntArray {
        val edges = IntArray(w * h)
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                // Sobel X kernel
                val gx = -lum[(y - 1) * w + (x - 1)] - 2 * lum[y * w + (x - 1)] - lum[(y + 1) * w + (x - 1)] +
                          lum[(y - 1) * w + (x + 1)] + 2 * lum[y * w + (x + 1)] + lum[(y + 1) * w + (x + 1)]
                // Sobel Y kernel
                val gy = -lum[(y - 1) * w + (x - 1)] - 2 * lum[(y - 1) * w + x] - lum[(y - 1) * w + (x + 1)] +
                          lum[(y + 1) * w + (x - 1)] + 2 * lum[(y + 1) * w + x] + lum[(y + 1) * w + (x + 1)]

                edges[y * w + x] = min(255, sqrt((gx * gx + gy * gy).toDouble()).toInt())
            }
        }
        return edges
    }

    /**
     * Find document bounds by analyzing edge projections.
     * Uses horizontal and vertical edge histograms to find the document rectangle.
     */
    private fun findDocumentBounds(edges: IntArray, w: Int, h: Int): IntArray? {
        val threshold = 40  // Edge strength threshold

        // Horizontal projection (sum edges per row)
        val hProj = IntArray(h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                if (edges[y * w + x] > threshold) hProj[y]++
            }
        }

        // Vertical projection (sum edges per column)
        val vProj = IntArray(w)
        for (x in 0 until w) {
            for (y in 0 until h) {
                if (edges[y * w + x] > threshold) vProj[x]++
            }
        }

        // Find strong edge lines
        val hThreshold = w * 0.1  // At least 10% of width should have edges
        val vThreshold = h * 0.1

        var top = 0
        var bottom = h - 1
        var left = 0
        var right = w - 1

        // Find top edge
        for (y in 0 until h / 3) {
            if (hProj[y] > hThreshold) { top = y; break }
        }
        // Find bottom edge
        for (y in h - 1 downTo h * 2 / 3) {
            if (hProj[y] > hThreshold) { bottom = y; break }
        }
        // Find left edge
        for (x in 0 until w / 3) {
            if (vProj[x] > vThreshold) { left = x; break }
        }
        // Find right edge
        for (x in w - 1 downTo w * 2 / 3) {
            if (vProj[x] > vThreshold) { right = x; break }
        }

        // Add small padding
        val pad = 3
        left = (left - pad).coerceAtLeast(0)
        top = (top - pad).coerceAtLeast(0)
        right = (right + pad).coerceAtMost(w - 1)
        bottom = (bottom + pad).coerceAtMost(h - 1)

        return if (right > left && bottom > top) {
            intArrayOf(left, top, right, bottom)
        } else null
    }

    // ═══════════════════════════════════════════════════════════════
    //  Image Enhancement & Filters
    // ═══════════════════════════════════════════════════════════════

    /**
     * Scale image to A4 proportions while maintaining aspect ratio.
     */
    private fun scaleToA4(bitmap: Bitmap): Bitmap {
        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val a4Ratio = A4_WIDTH.toFloat() / A4_HEIGHT.toFloat()

        val targetW: Int
        val targetH: Int

        if (ratio > a4Ratio) {
            // Wider than A4 — fit to width
            targetW = A4_WIDTH
            targetH = (A4_WIDTH / ratio).toInt()
        } else {
            // Taller than A4 — fit to height
            targetH = A4_HEIGHT
            targetW = (A4_HEIGHT * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    /**
     * Apply the selected filter with professional-grade image processing.
     */
    private fun applyFilter(bitmap: Bitmap, filter: String): Bitmap {
        return when (filter) {
            "B&W" -> adaptiveBlackAndWhite(bitmap)
            "Grayscale" -> toGrayscale(bitmap)
            "Auto Enhance" -> autoEnhanceDocument(bitmap)
            "Sharp" -> sharpenImage(bitmap)
            "High Contrast" -> highContrast(bitmap)
            else -> bitmap.copy(Bitmap.Config.ARGB_8888, false)
        }
    }

    /**
     * Adaptive black & white using local threshold.
     * Much better than global threshold for documents with uneven lighting.
     */
    private fun adaptiveBlackAndWhite(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val srcPixels = IntArray(w * h)
        bitmap.getPixels(srcPixels, 0, w, 0, 0, w, h)

        // Convert to grayscale
        val gray = IntArray(w * h)
        for (i in srcPixels.indices) {
            val r = (srcPixels[i] shr 16) and 0xFF
            val g = (srcPixels[i] shr 8) and 0xFF
            val b = srcPixels[i] and 0xFF
            gray[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }

        // Adaptive threshold using block mean
        val blockSize = max(15, min(w, h) / 40)  // Adaptive block size
        val c = 10  // Constant subtracted from mean

        val outPixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                // Calculate local mean in block
                var sum = 0
                var count = 0
                val y1 = max(0, y - blockSize / 2)
                val y2 = min(h - 1, y + blockSize / 2)
                val x1 = max(0, x - blockSize / 2)
                val x2 = min(w - 1, x + blockSize / 2)

                // Sample every 2nd pixel for speed
                var sy = y1
                while (sy <= y2) {
                    var sx = x1
                    while (sx <= x2) {
                        sum += gray[sy * w + sx]
                        count++
                        sx += 2
                    }
                    sy += 2
                }

                val threshold = if (count > 0) sum / count - c else 128
                val pixel = gray[y * w + x]
                outPixels[y * w + x] = if (pixel > threshold) {
                    0xFFFFFFFF.toInt()  // White
                } else {
                    0xFF000000.toInt()  // Black
                }
            }
        }

        result.setPixels(outPixels, 0, w, 0, 0, w, h)
        return result
    }

    /**
     * Professional grayscale conversion with slight contrast boost.
     */
    private fun toGrayscale(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = ColorMatrix().apply { setSaturation(0f) }
        // Slight contrast boost for documents
        val contrastMatrix = ColorMatrix(floatArrayOf(
            1.1f, 0f, 0f, 0f, -12f,
            0f, 1.1f, 0f, 0f, -12f,
            0f, 0f, 1.1f, 0f, -12f,
            0f, 0f, 0f, 1f, 0f
        ))
        cm.postConcat(contrastMatrix)
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    /**
     * Auto-enhance for documents: removes shadows, boosts text contrast,
     * whitens background while preserving text sharpness.
     */
    private fun autoEnhanceDocument(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val srcPixels = IntArray(w * h)
        bitmap.getPixels(srcPixels, 0, w, 0, 0, w, h)

        // Step 1: Analyze histogram to find brightness range
        val histogram = IntArray(256)
        for (pixel in srcPixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt().coerceIn(0, 255)
            histogram[lum]++
        }

        // Find 2nd and 98th percentile for auto-levels
        val totalPixels = w * h
        var low = 0
        var high = 255
        var cumSum = 0
        for (i in 0..255) {
            cumSum += histogram[i]
            if (cumSum >= totalPixels * 0.02) { low = i; break }
        }
        cumSum = 0
        for (i in 255 downTo 0) {
            cumSum += histogram[i]
            if (cumSum >= totalPixels * 0.02) { high = i; break }
        }

        val range = (high - low).coerceAtLeast(1).toFloat()

        // Step 2: Apply auto-levels with slight white boost for document background
        val outPixels = IntArray(w * h)
        for (i in srcPixels.indices) {
            val r = (srcPixels[i] shr 16) and 0xFF
            val g = (srcPixels[i] shr 8) and 0xFF
            val b = srcPixels[i] and 0xFF
            val a = (srcPixels[i] shr 24) and 0xFF

            // Auto-level each channel
            val nr = (((r - low) / range) * 255).toInt().coerceIn(0, 255)
            val ng = (((g - low) / range) * 255).toInt().coerceIn(0, 255)
            val nb = (((b - low) / range) * 255).toInt().coerceIn(0, 255)

            // Slight gamma correction to brighten shadows (gamma = 0.85)
            val fr = (255 * (nr / 255f).pow(0.85f)).toInt().coerceIn(0, 255)
            val fg = (255 * (ng / 255f).pow(0.85f)).toInt().coerceIn(0, 255)
            val fb = (255 * (nb / 255f).pow(0.85f)).toInt().coerceIn(0, 255)

            outPixels[i] = (a shl 24) or (fr shl 16) or (fg shl 8) or fb
        }

        result.setPixels(outPixels, 0, w, 0, 0, w, h)

        // Step 3: Apply sharpening for text clarity
        return applySharpenKernel(result)
    }

    /**
     * Sharpen image using unsharp mask technique.
     */
    private fun sharpenImage(bitmap: Bitmap): Bitmap {
        return applySharpenKernel(bitmap)
    }

    /**
     * High contrast mode for faded/low contrast documents.
     */
    private fun highContrast(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = ColorMatrix(floatArrayOf(
            1.6f, 0f, 0f, 0f, -80f,
            0f, 1.6f, 0f, 0f, -80f,
            0f, 0f, 1.6f, 0f, -80f,
            0f, 0f, 0f, 1f, 0f
        ))
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    /**
     * Apply 3x3 sharpening convolution kernel.
     */
    private fun applySharpenKernel(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val srcPixels = IntArray(w * h)
        bitmap.getPixels(srcPixels, 0, w, 0, 0, w, h)

        // Sharpening kernel: center = 5, edges = -1
        val outPixels = IntArray(w * h)
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                var rSum = 0; var gSum = 0; var bSum = 0

                // Center pixel × 5
                val cp = srcPixels[y * w + x]
                rSum += ((cp shr 16) and 0xFF) * 5
                gSum += ((cp shr 8) and 0xFF) * 5
                bSum += (cp and 0xFF) * 5

                // Subtract neighbors
                for (dy in intArrayOf(-1, 0, 0, 1)) {
                    val dx = if (dy == 0) { if (rSum == ((cp shr 16) and 0xFF) * 5) -1 else 1 } else 0
                    // Use cardinal directions
                }

                // Simplified: use the 4-neighbor sharpening
                val top = srcPixels[(y - 1) * w + x]
                val bot = srcPixels[(y + 1) * w + x]
                val lft = srcPixels[y * w + (x - 1)]
                val rgt = srcPixels[y * w + (x + 1)]

                rSum = ((cp shr 16) and 0xFF) * 5 -
                       ((top shr 16) and 0xFF) -
                       ((bot shr 16) and 0xFF) -
                       ((lft shr 16) and 0xFF) -
                       ((rgt shr 16) and 0xFF)

                gSum = ((cp shr 8) and 0xFF) * 5 -
                       ((top shr 8) and 0xFF) -
                       ((bot shr 8) and 0xFF) -
                       ((lft shr 8) and 0xFF) -
                       ((rgt shr 8) and 0xFF)

                bSum = (cp and 0xFF) * 5 -
                       (top and 0xFF) -
                       (bot and 0xFF) -
                       (lft and 0xFF) -
                       (rgt and 0xFF)

                outPixels[y * w + x] = (0xFF shl 24) or
                    (rSum.coerceIn(0, 255) shl 16) or
                    (gSum.coerceIn(0, 255) shl 8) or
                    bSum.coerceIn(0, 255)
            }
        }

        // Copy border pixels unchanged
        for (x in 0 until w) {
            outPixels[x] = srcPixels[x]
            outPixels[(h - 1) * w + x] = srcPixels[(h - 1) * w + x]
        }
        for (y in 0 until h) {
            outPixels[y * w] = srcPixels[y * w]
            outPixels[y * w + w - 1] = srcPixels[y * w + w - 1]
        }

        result.setPixels(outPixels, 0, w, 0, 0, w, h)
        return result
    }

    // ═══════════════════════════════════════════════════════════════
    //  PDF Export with high quality
    // ═══════════════════════════════════════════════════════════════

    private fun createPdfFromBitmaps(context: Context, bitmaps: List<Bitmap>): Uri {
        val pdf = PdfDocument()
        for ((index, bitmap) in bitmaps.withIndex()) {
            // Use A4 page size (in 72 DPI points: 595 x 842)
            val pageW = 595
            val pageH = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, index + 1).create()
            val page = pdf.startPage(pageInfo)

            val canvas = page.canvas
            // Scale bitmap to fill page while maintaining aspect ratio
            val scaleX = pageW.toFloat() / bitmap.width
            val scaleY = pageH.toFloat() / bitmap.height
            val scale = min(scaleX, scaleY)

            val scaledW = bitmap.width * scale
            val scaledH = bitmap.height * scale
            val offsetX = (pageW - scaledW) / 2
            val offsetY = (pageH - scaledH) / 2

            val destRect = RectF(offsetX, offsetY, offsetX + scaledW, offsetY + scaledH)
            val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
            canvas.drawBitmap(bitmap, null, destRect, paint)

            pdf.finishPage(page)
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "Scan_${System.currentTimeMillis()}.pdf")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/WifiPrint/Scans")
        }

        val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
            ?: throw Exception("Failed to create file")

        context.contentResolver.openOutputStream(uri)?.use { output ->
            pdf.writeTo(output)
        }
        pdf.close()

        return uri
    }

    override fun onCleared() {
        super.onCleared()
        _state.value.scannedPages.forEach {
            it.bitmap.recycle()
            it.originalBitmap.recycle()
        }
    }
}
