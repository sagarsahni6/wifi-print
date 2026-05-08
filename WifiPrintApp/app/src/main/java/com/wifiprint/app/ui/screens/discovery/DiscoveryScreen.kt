package com.wifiprint.app.ui.screens.discovery

import android.os.Build
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.wifiprint.app.data.models.ServerInfo
import com.wifiprint.app.data.repository.PrintRepository
import com.wifiprint.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    onConnected: () -> Unit,
    onBack: () -> Unit,
    viewModel: DiscoveryViewModel = hiltViewModel(),
    connectViewModel: ConnectViewModel = hiltViewModel()
) {
    val discoveredServers by viewModel.discoveredServers.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val connectState by connectViewModel.state.collectAsState()

    var manualIp by remember { mutableStateOf("") }
    var manualPort by remember { mutableStateOf("5000") }
    var showManualEntry by remember { mutableStateOf(false) }

    // Start discovery on screen launch
    LaunchedEffect(Unit) { viewModel.startDiscovery() }
    DisposableEffect(Unit) { onDispose { viewModel.stopDiscovery() } }

    // Navigate on successful connection
    LaunchedEffect(connectState.isConnected) {
        if (connectState.isConnected) onConnected()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connect to Server") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.stopDiscovery()
                        viewModel.startDiscovery()
                    }) {
                        Icon(Icons.Filled.Refresh, "Rescan")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Scanning Status ─────────────────────────────────────
            if (isSearching) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Cyan400.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Cyan400
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Scanning network...", fontWeight = FontWeight.Medium)
                            Text(
                                "Looking for WiFi Print servers on your network",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Discovered Servers ──────────────────────────────────
            if (discoveredServers.isNotEmpty()) {
                Text("Discovered Servers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)

                discoveredServers.forEach { server ->
                    ServerCard(
                        server = server,
                        isConnecting = connectState.isConnecting && connectState.connectingTo == server.id,
                        onClick = { connectViewModel.connectToServer(server) }
                    )
                }
            } else if (!isSearching) {
                // Empty state
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.WifiFind,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No servers found",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Make sure the WiFi Print Server is running on your PC\nand both devices are on the same network",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = {
                            viewModel.stopDiscovery()
                            viewModel.startDiscovery()
                        }) {
                            Icon(Icons.Filled.Refresh, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Scan Again")
                        }
                    }
                }
            }

            // ── Error Message ───────────────────────────────────────
            if (connectState.error != null) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Red400.copy(alpha = 0.1f)
                    )
                ) {
                    Row(modifier = Modifier.padding(14.dp)) {
                        Icon(Icons.Filled.Error, null, tint = Red400)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            connectState.error!!,
                            color = Red400,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── Manual Entry ────────────────────────────────────────
            Divider()

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.animateContentSize()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Edit, null, tint = Primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Manual Connection",
                                fontWeight = FontWeight.SemiBold)
                        }
                        TextButton(onClick = { showManualEntry = !showManualEntry }) {
                            Text(if (showManualEntry) "Hide" else "Show")
                        }
                    }

                    if (showManualEntry) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Enter the IP address shown in the server window",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = manualIp,
                                onValueChange = { manualIp = it },
                                label = { Text("IP Address") },
                                placeholder = { Text("192.168.1.100") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = manualPort,
                                onValueChange = { manualPort = it },
                                label = { Text("Port") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val port = manualPort.toIntOrNull() ?: 5000
                                val server = viewModel.addManualServer(manualIp.trim(), port)
                                connectViewModel.connectToServer(server)
                            },
                            enabled = manualIp.isNotBlank() && !connectState.isConnecting,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (connectState.isConnecting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Connecting...")
                            } else {
                                Icon(Icons.Filled.Link, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Connect", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Help card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Primary.copy(alpha = 0.06f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("How to Connect", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    HelpStep("1", "Run WiFi Print Server on your Windows PC")
                    HelpStep("2", "Ensure both phone and PC are on the same WiFi")
                    HelpStep("3", "Tap a discovered server or enter IP manually")
                    HelpStep("4", "Accept the connection request on your PC")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerCard(
    server: ServerInfo,
    isConnecting: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = 0.06f)
        ),
        enabled = !isConnecting
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Primary.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Computer, null, tint = Primary,
                        modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(server.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "${server.ipAddress}:${server.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Filled.ChevronRight, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HelpStep(number: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Primary.copy(alpha = 0.12f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(number, style = MaterialTheme.typography.labelSmall,
                    color = Primary, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}
