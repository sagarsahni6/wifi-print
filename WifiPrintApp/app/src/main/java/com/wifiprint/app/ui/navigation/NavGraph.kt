package com.wifiprint.app.ui.navigation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wifiprint.app.data.models.PrintJob
import com.wifiprint.app.ui.screens.discovery.ConnectViewModel
import com.wifiprint.app.ui.screens.discovery.DiscoveryScreen
import com.wifiprint.app.ui.screens.discovery.DiscoveryViewModel
import com.wifiprint.app.ui.screens.discovery.QrScannerScreen
import com.wifiprint.app.ui.screens.home.HomeViewModel
import com.wifiprint.app.ui.screens.jobs.JobHistoryScreen
import com.wifiprint.app.ui.screens.preview.DocumentPreviewScreen
import com.wifiprint.app.ui.screens.print.PrintScreen
import com.wifiprint.app.ui.screens.printers.PrinterListScreen
import com.wifiprint.app.ui.screens.scanner.ScannerScreen
import com.wifiprint.app.ui.screens.settings.SettingsScreen
import com.wifiprint.app.ui.screens.templates.PrintTemplateScreen
import com.wifiprint.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Route constants ────────────────────────────────────────────────────────
object Routes {
    const val MAIN = "main"
    const val DISCOVERY = "discovery"
    const val QR_SCANNER = "qr_scanner"
    const val QR_SCANNER_HOME = "home_qr_scanner"
    const val PRINTERS = "printers"
    const val JOBS = "jobs"
    const val SETTINGS = "settings"
    const val TEMPLATES = "templates"
    const val PREVIEW = "preview/{fileUri}"
    fun preview(fileUri: String) = "preview/${Uri.encode(fileUri)}"
}

// ════════════════════════════════════════════════════════════════════════════
//  Main Navigation Graph
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.MAIN) {

        // ── Main Tab Screen (Print / Scan / More) ────────────────
        composable(Routes.MAIN) {
            val parentEntry = remember(it) { navController.getBackStackEntry(Routes.MAIN) }
            val connectViewModel: ConnectViewModel = hiltViewModel(parentEntry)

            MainTabScreen(
                connectViewModel = connectViewModel,
                onNavigateToJobs = { navController.navigate(Routes.JOBS) },
                onNavigateToPrinters = { navController.navigate(Routes.PRINTERS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToTemplates = { navController.navigate(Routes.TEMPLATES) },
                onNavigateToDiscovery = { navController.navigate(Routes.DISCOVERY) },
                onNavigateToQrScanner = { navController.navigate(Routes.QR_SCANNER_HOME) },
                onJobCreated = { navController.navigate(Routes.JOBS) { popUpTo(Routes.MAIN) } },
                onScanComplete = { navController.navigate(Routes.JOBS) { popUpTo(Routes.MAIN) } }
            )
        }

        // ── Sub-screens (pushed on top of tabs) ──────────────────
        composable(Routes.JOBS) { JobHistoryScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.SETTINGS) { SettingsScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.PRINTERS) { PrinterListScreen(onBack = { navController.popBackStack() }) }

        composable(Routes.TEMPLATES) {
            PrintTemplateScreen(
                onBack = { navController.popBackStack() },
                onTemplateReady = { _, _ -> navController.navigate(Routes.MAIN) { popUpTo(Routes.MAIN) { inclusive = true } } }
            )
        }

        composable(Routes.DISCOVERY) {
            val parentEntry = remember(it) { navController.getBackStackEntry(Routes.DISCOVERY) }
            val connectViewModel: ConnectViewModel = hiltViewModel(parentEntry)
            DiscoveryScreen(
                onConnected = { navController.navigate(Routes.MAIN) { popUpTo(Routes.MAIN) { inclusive = true } } },
                onBack = { navController.popBackStack() },
                onNavigateToQrScanner = { navController.navigate(Routes.QR_SCANNER) },
                connectViewModel = connectViewModel
            )
        }

        composable(Routes.QR_SCANNER) {
            val parentEntry = remember(it) {
                runCatching { navController.getBackStackEntry(Routes.DISCOVERY) }.getOrNull()
            }
            val connectViewModel: ConnectViewModel = if (parentEntry != null) hiltViewModel(parentEntry) else hiltViewModel()
            QrScannerScreen(
                connectViewModel = connectViewModel,
                onConnected = { navController.navigate(Routes.MAIN) { popUpTo(Routes.MAIN) { inclusive = true } } },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.QR_SCANNER_HOME) {
            val parentEntry = remember(it) {
                runCatching { navController.getBackStackEntry(Routes.MAIN) }.getOrNull()
            }
            val connectViewModel: ConnectViewModel = if (parentEntry != null) hiltViewModel(parentEntry) else hiltViewModel()
            QrScannerScreen(
                connectViewModel = connectViewModel,
                onConnected = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PREVIEW,
            arguments = listOf(navArgument("fileUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val fileUri = backStackEntry.arguments?.getString("fileUri") ?: ""
            DocumentPreviewScreen(
                fileUriString = Uri.decode(fileUri),
                onBack = { navController.popBackStack() },
                onPrintFromPage = {}
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  Main Tab Screen — Edge-to-Edge, Top App Bar, HorizontalPager, Bottom Navigation
// ════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MainTabScreen(
    connectViewModel: ConnectViewModel,
    onNavigateToJobs: () -> Unit,
    onNavigateToPrinters: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToDiscovery: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onJobCreated: () -> Unit,
    onScanComplete: (String) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    discoveryViewModel: DiscoveryViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState { 3 }
    val scope = rememberCoroutineScope()
    val homeState by homeViewModel.uiState.collectAsState()
    val connectState by connectViewModel.state.collectAsState()
    val discoveredServers by discoveryViewModel.discoveredServers.collectAsState()
    val isSearching by discoveryViewModel.isSearching.collectAsState()
    val context = LocalContext.current

    // Auto-discovery
    val nearbyPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { if (it) discoveryViewModel.startDiscovery() }

    LaunchedEffect(homeState.isConnected) {
        if (!homeState.isConnected) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                discoveryViewModel.startDiscovery()
            } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED) {
                discoveryViewModel.startDiscovery()
            }
        }
    }
    LaunchedEffect(connectState.isConnected) {
        if (connectState.isConnected) homeViewModel.refreshConnectionStatus()
    }
    LaunchedEffect(discoveredServers, homeState.isConnected) {
        if (!homeState.isConnected && !connectState.isConnecting && !connectState.isConnected) {
            discoveredServers.firstOrNull { it.isSameNetwork }?.let { connectViewModel.connectToServer(it) }
        }
    }
    DisposableEffect(Unit) { onDispose { discoveryViewModel.stopDiscovery() } }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 0.5.dp
            ) {
                Column {
                    Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brand Logo & Title with Gradient Avatar & Pro Badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF4338CA), Color(0xFF6366F1), Color(0xFF8B5CF6))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Print,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Printora",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = (-0.3).sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Primary.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            "v2.2",
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Primary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                                Text(
                                    if (homeState.isConnected) "● Ready • ${homeState.serverName}" else "Wireless Print & Scan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (homeState.isConnected) Green500 else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Right actions: Glowing Connection Pill + Glassmorphic QR & Settings
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val haptic = LocalHapticFeedback.current

                            // Connection beacon pill
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (homeState.isConnected) Green500.copy(alpha = 0.14f) else Orange500.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, if (homeState.isConnected) Green500.copy(alpha = 0.45f) else Orange500.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onNavigateToDiscovery()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    val pulse = rememberInfiniteTransition(label = "pulse")
                                    val alpha by pulse.animateFloat(0.35f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "a")
                                    Box(
                                        Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (homeState.isConnected) Green500.copy(alpha = alpha) else Orange500
                                            )
                                    )
                                    Text(
                                        if (homeState.isConnected) (if (homeState.serverIp.contains("cloudflare", ignoreCase = true) || homeState.serverIp.contains("tunnel", ignoreCase = true)) "Cloud Ready" else "LAN Ready") else "Connect",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (homeState.isConnected) Green500 else Orange500,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            // Glassmorphic QR scan shortcut
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onNavigateToQrScanner()
                                        }
                                ) {
                                    Icon(
                                        Icons.Filled.QrCodeScanner,
                                        contentDescription = "Scan QR Code to Connect",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Glassmorphic Settings shortcut
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onNavigateToSettings()
                                        }
                                ) {
                                    Icon(
                                        Icons.Filled.Settings,
                                        contentDescription = "Open Settings",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                val navItems = listOf(
                    Triple("Print", Icons.Filled.Print, 0),
                    Triple("Scanner", Icons.Filled.DocumentScanner, 1),
                    Triple("More Hub", Icons.Filled.GridView, 2)
                )

                navItems.forEach { (label, icon, index) ->
                    val isSelected = pagerState.currentPage == index
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "nav_icon_scale"
                    )
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        icon = {
                            Box(modifier = Modifier.graphicsLayer { scaleX = iconScale; scaleY = iconScale }) {
                                if (index == 0 && homeState.activeJobCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White) {
                                                Text("${homeState.activeJobCount}")
                                            }
                                        }
                                    ) {
                                        Icon(icon, contentDescription = label)
                                    }
                                } else {
                                    Icon(icon, contentDescription = label)
                                }
                            }
                        },
                        label = {
                            Text(
                                label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            beyondBoundsPageCount = 1
        ) { page ->
            when (page) {
                0 -> PrintScreen(
                    onJobCreated = onJobCreated,
                    isConnected = homeState.isConnected,
                    serverName = homeState.serverName,
                    onNavigateToQr = onNavigateToQrScanner
                )
                1 -> ScannerScreen(
                    onScanComplete = { onScanComplete(it) },
                    onBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                    isServerConnected = homeState.isConnected,
                    serverName = homeState.serverName
                )
                2 -> MoreTab(
                    homeState = homeState,
                    onJobs = onNavigateToJobs,
                    onPrinters = onNavigateToPrinters,
                    onTemplates = onNavigateToTemplates,
                    onSettings = onNavigateToSettings,
                    onDiscovery = onNavigateToDiscovery,
                    onQrScanner = onNavigateToQrScanner
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
//  "More" Hub Tab — Modern Stitch Design System
// ════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreTab(
    homeState: com.wifiprint.app.ui.screens.home.HomeUiState,
    onJobs: () -> Unit,
    onPrinters: () -> Unit,
    onTemplates: () -> Unit,
    onSettings: () -> Unit,
    onDiscovery: () -> Unit,
    onQrScanner: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 1. High-Density Metric Cards ────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatChip(
                modifier = Modifier.weight(1f),
                value = "${homeState.completedJobCount.coerceAtLeast(homeState.recentJobs.count { it.status == "Completed" })}",
                label = "Printed",
                icon = Icons.Filled.CheckCircle,
                color = Green400
            )
            StatChip(
                modifier = Modifier.weight(1f),
                value = "${homeState.activeJobCount}",
                label = "In Queue",
                icon = Icons.Filled.Schedule,
                color = Cyan400
            )
            StatChip(
                modifier = Modifier.weight(1f),
                value = "${homeState.recentJobs.size}",
                label = "Total Jobs",
                icon = Icons.Filled.Summarize,
                color = Secondary
            )
        }

        // ── 2. Server Connection Card ───────────────────────────────
        if (homeState.isConnected) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Green400.copy(alpha = 0.12f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Computer, null, tint = Green400, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            homeState.serverName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Green400.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "Online",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Green400,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                if (homeState.serverIp.isNotEmpty()) homeState.serverIp else "Local WiFi Network",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onDiscovery,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Switch", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Orange400.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Orange400.copy(alpha = 0.18f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.WifiOff, null, tint = Orange400, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Server Disconnected",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Connect to PC to discover printers and print files",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onQrScanner,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.QrCodeScanner, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Scan QR", fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = onDiscovery,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Wifi, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Discover WiFi", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // ── 3. Quick Tools & Services (2x2 Action Grid) ─────────────
        Text(
            "Quick Tools & Services",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.History,
                label = "Job Queue",
                subtitle = "Manage active jobs",
                color = Orange400,
                badgeCount = homeState.activeJobCount,
                onClick = onJobs
            )
            ActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Print,
                label = "Printers",
                subtitle = "Fleet & ink levels",
                color = Cyan400,
                onClick = onPrinters
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Badge,
                label = "Templates",
                subtitle = "ID card & photos",
                color = Secondary,
                onClick = onTemplates
            )
            ActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Wifi,
                label = "Discovery",
                subtitle = "Pair new PC server",
                color = Green400,
                onClick = onDiscovery
            )
        }

        // ── 4. Recent Activity ──────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent Print Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (homeState.recentJobs.isNotEmpty()) {
                TextButton(onClick = onJobs) {
                    Text("See All", fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Filled.ChevronRight, null, Modifier.size(16.dp))
                }
            }
        }

        if (homeState.recentJobs.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                homeState.recentJobs.take(5).forEach { job ->
                    RecentJobCard(job = job, onClick = onJobs)
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.PrintDisabled,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "No print jobs recorded yet",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Jobs will show up here after printing files or scans.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ── 5. PC Server Helper Card ────────────────────────────────
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Primary.copy(alpha = 0.05f), Secondary.copy(alpha = 0.08f))
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Computer, null, tint = Primary, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Printora Server for PC",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Free Windows companion app to share USB/network printers over Cloud & Wi-Fi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wifiprint.app/#download")))
                        }
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = "Download Server", tint = Primary)
                    }
                }
            }
        }

        // ── 6. Preferences & Settings Row ───────────────────────────
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSettings() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Tune, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("App Preferences", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Default printer, duplex, and network options", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Printora v2.2.2 Pro", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Next-Gen Cloud Print Service & Fast LAN Active", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ── Reusable Component Cards ────────────────────────────────────────────────

@Composable
private fun StatChip(
    modifier: Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    subtitle: String,
    color: Color,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(104.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = color.copy(alpha = 0.14f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                    }
                }
                if (badgeCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Orange400
                    ) {
                        Text(
                            "$badgeCount",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentJobCard(job: PrintJob, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, tint) = when (job.fileType.lowercase()) {
                "pdf" -> Icons.Filled.PictureAsPdf to Red400
                "jpg", "jpeg", "png", "image" -> Icons.Filled.Image to Cyan400
                else -> Icons.Filled.Description to Secondary
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = tint.copy(alpha = 0.12f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    job.fileName,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${job.printerName} • ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(job.createdAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val (badgeBg, badgeText) = when (job.status) {
                "Completed" -> Green400.copy(alpha = 0.12f) to Green400
                "Failed" -> Red400.copy(alpha = 0.12f) to Red400
                "Printing" -> Cyan400.copy(alpha = 0.12f) to Cyan400
                else -> Orange400.copy(alpha = 0.12f) to Orange400
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = badgeBg
            ) {
                Text(
                    job.status,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
