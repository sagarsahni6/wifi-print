package com.wifiprint.app.ui.screens.discovery

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.json.JSONObject
import java.util.concurrent.Executors

/**
 * QR code data parsed from the server's connection QR.
 */
data class QrConnectionData(
    val ip: String,
    val port: Int,
    val name: String,
    val certFingerprint: String,
    val qrToken: String? = null
)

/**
 * QR Scanner screen — opens the camera, detects QR codes via ML Kit,
 * and prompts for the 6-digit server PIN to complete connection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    connectViewModel: ConnectViewModel = hiltViewModel(),
    onConnected: () -> Unit = {},
    onQrScanned: ((QrConnectionData) -> Unit)? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val connectState by connectViewModel.state.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var scannedQrData by remember { mutableStateOf<QrConnectionData?>(null) }
    var pinInput by remember { mutableStateOf("") }

    LaunchedEffect(connectState.isConnected) {
        if (connectState.isConnected) {
            onConnected()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR Code") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
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
                    Icon(
                        Icons.Filled.QrCodeScanner, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Camera permission required to scan QR codes", textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Permission")
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Camera preview with ML Kit barcode scanning
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

                            val barcodeScanner = BarcodeScanning.getClient()
                            val analysisExecutor = Executors.newSingleThreadExecutor()

                            @androidx.annotation.OptIn(ExperimentalGetImage::class)
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { analysis ->
                                    analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null && scannedQrData == null) {
                                            val image = InputImage.fromMediaImage(
                                                mediaImage, imageProxy.imageInfo.rotationDegrees
                                            )
                                            barcodeScanner.process(image)
                                                .addOnSuccessListener { barcodes ->
                                                    for (barcode in barcodes) {
                                                        if (barcode.valueType == Barcode.TYPE_TEXT ||
                                                            barcode.valueType == Barcode.TYPE_UNKNOWN) {
                                                            val rawValue = barcode.rawValue ?: continue
                                                            val parsed = parseQrPayload(rawValue)
                                                            if (parsed != null && scannedQrData == null) {
                                                                if (onQrScanned != null) {
                                                                    onQrScanned(parsed)
                                                                } else {
                                                                    scannedQrData = parsed
                                                                    pinInput = ""
                                                                }
                                                                return@addOnSuccessListener
                                                            }
                                                        }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("QrScanner", "Barcode scan failed", e)
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }
                                }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                Log.e("QrScanner", "Camera bind failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // QR viewfinder overlay
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Transparent)
                            .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    )
                }

                // Instructions at top
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "📷 Point at the QR code on your PC",
                            color = Color.White, fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Scan QR, then enter the PIN shown on your PC screen",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // PIN Entry Dialog immediately after scanning QR
                if (scannedQrData != null) {
                    val server = scannedQrData!!
                    AlertDialog(
                        onDismissRequest = {
                            if (!connectState.isConnecting) {
                                scannedQrData = null
                                pinInput = ""
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        title = {
                            Text(
                                text = "Enter Server PIN",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        },
                        text = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "🖥 " + server.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "${server.ip}:${server.port}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(14.dp))
                                Text(
                                    text = "Enter the 6-digit PIN displayed on your PC server dashboard:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = pinInput,
                                    onValueChange = { input ->
                                        if (input.length <= 6 && input.all { it.isDigit() }) {
                                            pinInput = input
                                        }
                                    },
                                    label = { Text("6-Digit PIN") },
                                    placeholder = { Text("123456") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(
                                        textAlign = TextAlign.Center,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        letterSpacing = 4.sp
                                    ),
                                    isError = connectState.error != null
                                )

                                if (connectState.error != null) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = connectState.error!!,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (pinInput.length == 6) {
                                        connectViewModel.connectFromQr(
                                            ip = server.ip,
                                            port = server.port,
                                            name = server.name,
                                            certFingerprint = server.certFingerprint,
                                            qrToken = server.qrToken,
                                            pin = pinInput
                                        )
                                    }
                                },
                                enabled = pinInput.length == 6 && !connectState.isConnecting,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (connectState.isConnecting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Verifying...")
                                } else {
                                    Text("Connect")
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    scannedQrData = null
                                    pinInput = ""
                                },
                                enabled = !connectState.isConnecting
                            ) {
                                Text("Rescan")
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Parses the QR code JSON payload: { "ip": "...", "port": ..., "name": "...", "cert": "..." }
 */
private fun parseQrPayload(raw: String): QrConnectionData? {
    return try {
        val json = JSONObject(raw)
        val token = json.optString("token").ifBlank { json.optString("qrToken").ifBlank { null } }
        QrConnectionData(
            ip = json.getString("ip"),
            port = json.getInt("port"),
            name = json.optString("name", "WiFi Print Server"),
            certFingerprint = json.optString("cert", ""),
            qrToken = token
        )
    } catch (e: Exception) {
        Log.e("QrScanner", "Failed to parse QR payload: $raw", e)
        null
    }
}
