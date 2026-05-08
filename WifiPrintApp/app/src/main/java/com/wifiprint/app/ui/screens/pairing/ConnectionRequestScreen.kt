package com.wifiprint.app.ui.screens.pairing

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifiprint.app.ui.theme.*

/**
 * Screen shown while waiting for PC user to approve the connection request.
 * Replaces the old PIN-based PairingScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionRequestScreen(
    serverIp: String,
    serverPort: Int,
    onApproved: () -> Unit,
    onBack: () -> Unit,
    viewModel: ConnectionRequestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Auto-navigate to Home when approved
    LaunchedEffect(state.isApproved) {
        if (state.isApproved) onApproved()
    }

    // Auto-send connection request when screen opens
    LaunchedEffect(Unit) {
        viewModel.requestConnection(serverIp, serverPort)
    }

    // Spinner rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connect to Server") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                // Waiting for approval
                state.isRequesting -> {
                    Icon(
                        Icons.Filled.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).rotate(rotation),
                        tint = Purple600
                    )
                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Waiting for Approval",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Please approve this device on your PC.\n" +
                        "A notification should appear on the server.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Server: $serverIp:$serverPort",
                        style = MaterialTheme.typography.bodySmall,
                        color = Cyan400
                    )
                    Spacer(Modifier.height(32.dp))

                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(0.6f),
                        color = Purple600,
                        trackColor = Purple600.copy(alpha = 0.15f)
                    )
                }

                // Denied or error
                state.isDenied || state.error != null -> {
                    Icon(
                        if (state.isDenied) Icons.Filled.Block else Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Red400
                    )
                    Spacer(Modifier.height(24.dp))

                    Text(
                        if (state.isDenied) "Connection Denied" else "Connection Failed",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    Text(
                        state.error ?: "Unknown error",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(32.dp))

                    // Retry button
                    Button(
                        onClick = {
                            viewModel.resetState()
                            viewModel.requestConnection(serverIp, serverPort)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Try Again", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(12.dp))

                    // Go back button
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Go Back")
                    }
                }

                // Approved (briefly shown before navigation)
                state.isApproved -> {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color(0xFF66BB6A)
                    )
                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Connected!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF66BB6A)
                    )
                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Connected to ${state.serverName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
