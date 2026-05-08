package com.wifiprint.app.ui.screens.pairing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifiprint.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(
    serverIp: String,
    serverPort: Int,
    onPaired: () -> Unit,
    onBack: () -> Unit,
    viewModel: PairingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isPaired) {
        if (state.isPaired) onPaired()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pair Device") },
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
            Icon(
                Icons.Filled.PhonelinkSetup,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Purple600
            )
            Spacer(Modifier.height(24.dp))

            Text(
                "Enter Pairing PIN",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            Text(
                "Open the WiFi Print Server on your PC and\ngenerate a PIN from the Dashboard.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            Text(
                "Server: $serverIp:$serverPort",
                style = MaterialTheme.typography.bodySmall,
                color = Cyan400
            )
            Spacer(Modifier.height(32.dp))

            // PIN Input
            OutlinedTextField(
                value = state.pin,
                onValueChange = { viewModel.updatePin(it) },
                label = { Text("6-Digit PIN") },
                placeholder = { Text("000000") },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 8.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(260.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Error message
            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.error!!, color = Red400, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(32.dp))

            // Pair button
            Button(
                onClick = { viewModel.pair(serverIp, serverPort) },
                enabled = state.pin.length == 6 && !state.isPairing,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isPairing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Pairing...")
                } else {
                    Icon(Icons.Filled.Link, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pair Device", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
