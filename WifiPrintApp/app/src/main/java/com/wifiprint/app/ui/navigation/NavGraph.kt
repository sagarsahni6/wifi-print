package com.wifiprint.app.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wifiprint.app.ui.screens.discovery.ConnectViewModel
import com.wifiprint.app.ui.screens.discovery.DiscoveryScreen
import com.wifiprint.app.ui.screens.discovery.QrScannerScreen
import com.wifiprint.app.ui.screens.home.HomeScreen
import com.wifiprint.app.ui.screens.jobs.JobHistoryScreen
import com.wifiprint.app.ui.screens.preview.DocumentPreviewScreen
import com.wifiprint.app.ui.screens.print.PrintScreen
import com.wifiprint.app.ui.screens.printers.PrinterListScreen
import com.wifiprint.app.ui.screens.scanner.ScannerScreen
import com.wifiprint.app.ui.screens.settings.SettingsScreen
import com.wifiprint.app.ui.screens.templates.PrintTemplateScreen
import com.wifiprint.app.ui.theme.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Print : Screen("print", "Print", Icons.Filled.Print)
    data object Jobs : Screen("jobs", "Jobs", Icons.Filled.History)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    data object Discovery : Screen("discovery", "Connect", Icons.Filled.Wifi)
    data object QrScanner : Screen("qr_scanner", "Scan QR", Icons.Filled.QrCodeScanner)
    data object Printers : Screen("printers", "Printers", Icons.Filled.Print)
    data object Scanner : Screen("scanner", "Scan", Icons.Filled.DocumentScanner)
    data object Templates : Screen("templates", "Templates", Icons.Filled.Badge)
    data object Preview : Screen("preview/{fileUri}", "Preview", Icons.Filled.Visibility) {
        fun createRoute(fileUri: String): String = "preview/${Uri.encode(fileUri)}"
    }
}

val bottomNavItems = listOf(Screen.Home, Screen.Print, Screen.Jobs, Screen.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    // Show bottom bar only on main screens
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(screen.icon, screen.label) },
                            label = {
                                Text(
                                    screen.label,
                                    fontWeight = if (isSelected) FontWeight.SemiBold
                                    else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = PrimaryLight,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(Screen.Home.route)
                }
                val connectViewModel: ConnectViewModel = hiltViewModel(parentEntry)

                HomeScreen(
                    onNavigateToPrint = { navController.navigate(Screen.Print.route) },
                    onNavigateToJobs = { navController.navigate(Screen.Jobs.route) },
                    onNavigateToPrinters = { navController.navigate(Screen.Printers.route) },
                    onNavigateToScanner = { navController.navigate(Screen.Scanner.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToTemplates = { navController.navigate(Screen.Templates.route) },
                    onNavigateToDiscovery = { navController.navigate(Screen.Discovery.route) },
                    onNavigateToQrScanner = { navController.navigate("home_qr_scanner") },
                    connectViewModel = connectViewModel
                )
            }

            // QR Scanner launched from Home screen
            composable("home_qr_scanner") {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(Screen.Home.route)
                }
                val connectViewModel: ConnectViewModel = hiltViewModel(parentEntry)

                QrScannerScreen(
                    onQrScanned = { qrData ->
                        connectViewModel.connectFromQr(
                            ip = qrData.ip,
                            port = qrData.port,
                            name = qrData.name,
                            certFingerprint = qrData.certFingerprint
                        )
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Print.route) {
                PrintScreen(
                    onJobCreated = {
                        navController.navigate(Screen.Jobs.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(Screen.Jobs.route) {
                JobHistoryScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            // Server Discovery & Connection Screen
            composable(Screen.Discovery.route) {
                // Share the ConnectViewModel between Discovery and QR Scanner
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(Screen.Discovery.route)
                }
                val connectViewModel: ConnectViewModel = hiltViewModel(parentEntry)

                DiscoveryScreen(
                    onConnected = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onNavigateToQrScanner = {
                        navController.navigate(Screen.QrScanner.route)
                    },
                    connectViewModel = connectViewModel
                )
            }

            // QR Code Scanner Screen
            composable(Screen.QrScanner.route) {
                // Share the ConnectViewModel with Discovery screen
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(Screen.Discovery.route)
                }
                val connectViewModel: ConnectViewModel = hiltViewModel(parentEntry)

                QrScannerScreen(
                    onQrScanned = { qrData ->
                        connectViewModel.connectFromQr(
                            ip = qrData.ip,
                            port = qrData.port,
                            name = qrData.name,
                            certFingerprint = qrData.certFingerprint
                        )
                        // Go back to Discovery to show connecting state
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Printers.route) {
                PrinterListScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Scanner Screen
            composable(Screen.Scanner.route) {
                ScannerScreen(
                    onScanComplete = { _ ->
                        navController.navigate(Screen.Print.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // Print Templates Screen
            composable(Screen.Templates.route) {
                PrintTemplateScreen(
                    onBack = { navController.popBackStack() },
                    onTemplateReady = { _, _ ->
                        navController.navigate(Screen.Print.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            // Document Preview Screen
            composable(
                route = Screen.Preview.route,
                arguments = listOf(navArgument("fileUri") { type = NavType.StringType })
            ) { backStackEntry ->
                val fileUri = backStackEntry.arguments?.getString("fileUri") ?: ""
                val decodedUri = Uri.decode(fileUri)
                DocumentPreviewScreen(
                    fileUriString = decodedUri,
                    onBack = { navController.popBackStack() },
                    onPrintFromPage = { /* could navigate to print with page range */ }
                )
            }
        }
    }
}
