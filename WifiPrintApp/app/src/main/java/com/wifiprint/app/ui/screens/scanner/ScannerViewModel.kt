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
    val originalBitmap: Bitmap,
    val bitmap: Bitmap,
    val uri: Uri? = null,
    val filter: String = "Auto Enhance"
)

enum class ScanMode { Document, IDCard }
enum class IdCardStep { Front, Back, Preview }

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
    val autoEdgeDetection: Boolean = true,
    // Scan mode
    val scanMode: ScanMode = ScanMode.Document,
    // ID Card state
    val idCardStep: IdCardStep = IdCardStep.Front,
    val idCardFrontBitmap: Bitmap? = null,
    val idCardBackBitmap: Bitmap? = null
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "ScannerViewModel"
        private const val A4_WIDTH = 2480
        private const val A4_HEIGHT = 3508
    }

    private val _state = MutableStateFlow(ScannerUiState())
    val state: StateFlow<ScannerUiState> = _state

    // ═══════════════════════════════════════════════════════════════
    //  Scan Mode
    // ═══════════════════════════════════════════════════════════════

    fun setScanMode(mode: ScanMode) {
        _state.update {
            it.copy(
                scanMode = mode,
                idCardStep = IdCardStep.Front,
                idCardFrontBitmap = null,
                idCardBackBitmap = null
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  ML Kit Document Scanner result handling
    // ═══════════════════════════════════════════════════════════════

    /** Called when ML Kit Document Scanner returns scanned page URIs. */
    fun onMlKitScanResult(pageUris: List<Uri>) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }

            val newPages = withContext(Dispatchers.Default) {
                pageUris.mapIndexed { idx, uri ->
                    val bitmap = loadBitmapFromUri(uri) ?: return@mapIndexed null
                    val scaled = scaleToA4(bitmap)
                    val filtered = applyFilter(scaled, _state.value.currentFilter)
                    ScannedPageData(
                        index = _state.value.scannedPages.size + idx,
                        originalBitmap = scaled,
                        bitmap = filtered,
                        uri = uri,
                        filter = _state.value.currentFilter
                    )
                }.filterNotNull()
            }

            _state.update {
                it.copy(
                    scannedPages = it.scannedPages + newPages,
                    isProcessing = false,
                    selectedPageIndex = it.scannedPages.size,
                    showCamera = false
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  ID Card Scanning (Front + Back → Single Page)
    // ═══════════════════════════════════════════════════════════════

    fun onIdCardFrontCaptured(bitmap: Bitmap) {
        _state.update {
            it.copy(idCardFrontBitmap = bitmap, idCardStep = IdCardStep.Back)
        }
    }

    fun onIdCardBackCaptured(bitmap: Bitmap) {
        _state.update {
            it.copy(idCardBackBitmap = bitmap, idCardStep = IdCardStep.Preview)
        }
    }

    /** Called when ML Kit returns the scanned front side URI (with edge detection). */
    fun onIdCardFrontScanned(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }
            val bitmap = withContext(Dispatchers.IO) { loadBitmapFromUri(uri) }
            if (bitmap != null) {
                _state.update {
                    it.copy(idCardFrontBitmap = bitmap, idCardStep = IdCardStep.Back, isProcessing = false)
                }
            } else {
                _state.update { it.copy(isProcessing = false, error = "Failed to load front side image") }
            }
        }
    }

    /** Called when ML Kit returns the scanned back side URI (with edge detection). */
    fun onIdCardBackScanned(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }
            val bitmap = withContext(Dispatchers.IO) { loadBitmapFromUri(uri) }
            if (bitmap != null) {
                _state.update {
                    it.copy(idCardBackBitmap = bitmap, idCardStep = IdCardStep.Preview, isProcessing = false)
                }
            } else {
                _state.update { it.copy(isProcessing = false, error = "Failed to load back side image") }
            }
        }
    }

    /** Combine front+back into a single composite bitmap and add as a page. */
    fun combineIdCardSides() {
        val front = _state.value.idCardFrontBitmap ?: return
        val back = _state.value.idCardBackBitmap ?: return

        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }

            val composite = withContext(Dispatchers.Default) {
                combineIdCardBitmaps(front, back)
            }

            val filtered = withContext(Dispatchers.Default) {
                applyFilter(composite, _state.value.currentFilter)
            }

            val page = ScannedPageData(
                index = _state.value.scannedPages.size,
                originalBitmap = composite,
                bitmap = filtered,
                filter = _state.value.currentFilter
            )

            _state.update {
                it.copy(
                    scannedPages = it.scannedPages + page,
                    isProcessing = false,
                    showCamera = false,
                    idCardStep = IdCardStep.Front,
                    idCardFrontBitmap = null,
                    idCardBackBitmap = null,
                    selectedPageIndex = it.scannedPages.size
                )
            }
        }
    }

    fun resetIdCard() {
        _state.update {
            it.copy(
                idCardStep = IdCardStep.Front,
                idCardFrontBitmap = null,
                idCardBackBitmap = null
            )
        }
    }

    /**
     * Combines front and back ID card bitmaps into a single landscape A4 page
     * with both sides placed SIDE BY SIDE (front left, back right).
     */
    private fun combineIdCardBitmaps(front: Bitmap, back: Bitmap): Bitmap {
        // Landscape A4: width > height
        val targetW = A4_HEIGHT  // 3508 (landscape width)
        val targetH = A4_WIDTH   // 2480 (landscape height)
        val composite = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(composite)
        canvas.drawColor(Color.WHITE)

        val padding = 80
        val labelHeight = 60
        val cardAreaW = (targetW - padding * 3) / 2
        val cardAreaH = targetH - padding * 2 - labelHeight

        // Scale front to fit left half
        val frontScale = min(
            cardAreaW.toFloat() / front.width,
            cardAreaH.toFloat() / front.height
        )
        val frontW = (front.width * frontScale).toInt()
        val frontH = (front.height * frontScale).toInt()
        val frontLeft = padding + (cardAreaW - frontW) / 2f
        val frontTop = padding + labelHeight + (cardAreaH - frontH) / 2f

        // Label
        val labelPaint = Paint().apply {
            color = Color.DKGRAY; textSize = 44f; isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("FRONT", (padding + cardAreaW / 2f), (padding + 44f), labelPaint)

        canvas.drawBitmap(front, null,
            RectF(frontLeft, frontTop, frontLeft + frontW, frontTop + frontH),
            Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))

        // Scale back to fit right half
        val backScale = min(
            cardAreaW.toFloat() / back.width,
            cardAreaH.toFloat() / back.height
        )
        val backW = (back.width * backScale).toInt()
        val backH = (back.height * backScale).toInt()
        val rightAreaLeft = padding * 2 + cardAreaW
        val backLeft = rightAreaLeft + (cardAreaW - backW) / 2f
        val backTop = padding + labelHeight + (cardAreaH - backH) / 2f

        canvas.drawText("BACK", (rightAreaLeft + cardAreaW / 2f), (padding + 44f), labelPaint)

        canvas.drawBitmap(back, null,
            RectF(backLeft, backTop, backLeft + backW, backTop + backH),
            Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))

        // Separator line between front and back
        val separatorPaint = Paint().apply {
            color = Color.LTGRAY; strokeWidth = 2f; isAntiAlias = true
        }
        val separatorX = (padding * 1.5f + cardAreaW)
        canvas.drawLine(separatorX, padding.toFloat(), separatorX, (targetH - padding).toFloat(), separatorPaint)

        return composite
    }

    // ═══════════════════════════════════════════════════════════════
    //  Legacy capture (fallback for CameraX if ML Kit unavailable)
    // ═══════════════════════════════════════════════════════════════

    fun onPhotoCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }

            val processed = withContext(Dispatchers.Default) {
                val img = scaleToA4(bitmap)
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

    // ═══════════════════════════════════════════════════════════════
    //  Page Management
    // ═══════════════════════════════════════════════════════════════

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

    fun clearError() { _state.update { it.copy(error = null) } }
    fun clearSavedPdf() { _state.update { it.copy(savedPdfUri = null) } }

    // ═══════════════════════════════════════════════════════════════
    //  Image Processing & Filters
    // ═══════════════════════════════════════════════════════════════

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bitmap from URI", e)
            null
        }
    }

    private fun scaleToA4(bitmap: Bitmap): Bitmap {
        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val a4Ratio = A4_WIDTH.toFloat() / A4_HEIGHT.toFloat()
        val targetW: Int
        val targetH: Int
        if (ratio > a4Ratio) {
            targetW = A4_WIDTH; targetH = (A4_WIDTH / ratio).toInt()
        } else {
            targetH = A4_HEIGHT; targetW = (A4_HEIGHT * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    private fun applyFilter(bitmap: Bitmap, filter: String): Bitmap {
        return when (filter) {
            "B&W" -> adaptiveBlackAndWhite(bitmap)
            "Grayscale" -> toGrayscale(bitmap)
            "Auto Enhance" -> autoEnhanceDocument(bitmap)
            "Sharp" -> applySharpenKernel(bitmap)
            "High Contrast" -> highContrast(bitmap)
            else -> bitmap.copy(Bitmap.Config.ARGB_8888, false)
        }
    }

    private fun adaptiveBlackAndWhite(bitmap: Bitmap): Bitmap {
        val w = bitmap.width; val h = bitmap.height
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val srcPixels = IntArray(w * h)
        bitmap.getPixels(srcPixels, 0, w, 0, 0, w, h)
        val gray = IntArray(w * h)
        for (i in srcPixels.indices) {
            val r = (srcPixels[i] shr 16) and 0xFF
            val g = (srcPixels[i] shr 8) and 0xFF
            val b = srcPixels[i] and 0xFF
            gray[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }
        val blockSize = max(15, min(w, h) / 40)
        val c = 10
        val outPixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                var sum = 0; var count = 0
                val y1 = max(0, y - blockSize / 2); val y2 = min(h - 1, y + blockSize / 2)
                val x1 = max(0, x - blockSize / 2); val x2 = min(w - 1, x + blockSize / 2)
                var sy = y1; while (sy <= y2) { var sx = x1; while (sx <= x2) { sum += gray[sy * w + sx]; count++; sx += 2 }; sy += 2 }
                val threshold = if (count > 0) sum / count - c else 128
                outPixels[y * w + x] = if (gray[y * w + x] > threshold) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            }
        }
        result.setPixels(outPixels, 0, w, 0, 0, w, h)
        return result
    }

    private fun toGrayscale(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = ColorMatrix().apply { setSaturation(0f) }
        val contrastMatrix = ColorMatrix(floatArrayOf(
            1.1f, 0f, 0f, 0f, -12f, 0f, 1.1f, 0f, 0f, -12f,
            0f, 0f, 1.1f, 0f, -12f, 0f, 0f, 0f, 1f, 0f
        ))
        cm.postConcat(contrastMatrix)
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun autoEnhanceDocument(bitmap: Bitmap): Bitmap {
        val w = bitmap.width; val h = bitmap.height
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val srcPixels = IntArray(w * h)
        bitmap.getPixels(srcPixels, 0, w, 0, 0, w, h)
        val histogram = IntArray(256)
        for (pixel in srcPixels) {
            val r = (pixel shr 16) and 0xFF; val g = (pixel shr 8) and 0xFF; val b = pixel and 0xFF
            histogram[(0.299 * r + 0.587 * g + 0.114 * b).toInt().coerceIn(0, 255)]++
        }
        val totalPixels = w * h
        var low = 0; var high = 255; var cumSum = 0
        for (i in 0..255) { cumSum += histogram[i]; if (cumSum >= totalPixels * 0.02) { low = i; break } }
        cumSum = 0
        for (i in 255 downTo 0) { cumSum += histogram[i]; if (cumSum >= totalPixels * 0.02) { high = i; break } }
        val range = (high - low).coerceAtLeast(1).toFloat()
        val outPixels = IntArray(w * h)
        for (i in srcPixels.indices) {
            val r = (srcPixels[i] shr 16) and 0xFF; val g = (srcPixels[i] shr 8) and 0xFF
            val b = srcPixels[i] and 0xFF; val a = (srcPixels[i] shr 24) and 0xFF
            val nr = (((r - low) / range) * 255).toInt().coerceIn(0, 255)
            val ng = (((g - low) / range) * 255).toInt().coerceIn(0, 255)
            val nb = (((b - low) / range) * 255).toInt().coerceIn(0, 255)
            val fr = (255 * (nr / 255f).pow(0.85f)).toInt().coerceIn(0, 255)
            val fg = (255 * (ng / 255f).pow(0.85f)).toInt().coerceIn(0, 255)
            val fb = (255 * (nb / 255f).pow(0.85f)).toInt().coerceIn(0, 255)
            outPixels[i] = (a shl 24) or (fr shl 16) or (fg shl 8) or fb
        }
        result.setPixels(outPixels, 0, w, 0, 0, w, h)
        return applySharpenKernel(result)
    }

    private fun highContrast(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = ColorMatrix(floatArrayOf(
            1.6f, 0f, 0f, 0f, -80f, 0f, 1.6f, 0f, 0f, -80f,
            0f, 0f, 1.6f, 0f, -80f, 0f, 0f, 0f, 1f, 0f
        ))
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun applySharpenKernel(bitmap: Bitmap): Bitmap {
        val w = bitmap.width; val h = bitmap.height
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val srcPixels = IntArray(w * h)
        bitmap.getPixels(srcPixels, 0, w, 0, 0, w, h)
        val outPixels = IntArray(w * h)
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val cp = srcPixels[y * w + x]
                val top = srcPixels[(y - 1) * w + x]; val bot = srcPixels[(y + 1) * w + x]
                val lft = srcPixels[y * w + (x - 1)]; val rgt = srcPixels[y * w + (x + 1)]
                val rSum = ((cp shr 16) and 0xFF) * 5 - ((top shr 16) and 0xFF) - ((bot shr 16) and 0xFF) - ((lft shr 16) and 0xFF) - ((rgt shr 16) and 0xFF)
                val gSum = ((cp shr 8) and 0xFF) * 5 - ((top shr 8) and 0xFF) - ((bot shr 8) and 0xFF) - ((lft shr 8) and 0xFF) - ((rgt shr 8) and 0xFF)
                val bSum = (cp and 0xFF) * 5 - (top and 0xFF) - (bot and 0xFF) - (lft and 0xFF) - (rgt and 0xFF)
                outPixels[y * w + x] = (0xFF shl 24) or (rSum.coerceIn(0, 255) shl 16) or (gSum.coerceIn(0, 255) shl 8) or bSum.coerceIn(0, 255)
            }
        }
        for (x in 0 until w) { outPixels[x] = srcPixels[x]; outPixels[(h - 1) * w + x] = srcPixels[(h - 1) * w + x] }
        for (y in 0 until h) { outPixels[y * w] = srcPixels[y * w]; outPixels[y * w + w - 1] = srcPixels[y * w + w - 1] }
        result.setPixels(outPixels, 0, w, 0, 0, w, h)
        return result
    }

    // ═══════════════════════════════════════════════════════════════
    //  PDF Export
    // ═══════════════════════════════════════════════════════════════

    private fun createPdfFromBitmaps(context: Context, bitmaps: List<Bitmap>): Uri {
        val pdf = PdfDocument()
        for ((index, bitmap) in bitmaps.withIndex()) {
            val pageW = 595; val pageH = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, index + 1).create()
            val page = pdf.startPage(pageInfo)
            val canvas = page.canvas
            val scaleX = pageW.toFloat() / bitmap.width; val scaleY = pageH.toFloat() / bitmap.height
            val scale = min(scaleX, scaleY)
            val scaledW = bitmap.width * scale; val scaledH = bitmap.height * scale
            val offsetX = (pageW - scaledW) / 2; val offsetY = (pageH - scaledH) / 2
            val destRect = RectF(offsetX, offsetY, offsetX + scaledW, offsetY + scaledH)
            canvas.drawBitmap(bitmap, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
            pdf.finishPage(page)
        }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "Scan_${System.currentTimeMillis()}.pdf")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/WifiPrint/Scans")
        }
        val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
            ?: throw Exception("Failed to create file")
        context.contentResolver.openOutputStream(uri)?.use { output -> pdf.writeTo(output) }
        pdf.close()
        return uri
    }

    override fun onCleared() {
        super.onCleared()
        _state.value.scannedPages.forEach { it.bitmap.recycle(); it.originalBitmap.recycle() }
        _state.value.idCardFrontBitmap?.recycle()
        _state.value.idCardBackBitmap?.recycle()
    }
}
