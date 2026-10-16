package com.example

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.NovaVpnViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SecurityShieldScreen
import com.example.ui.screens.ServerListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyberBorderDark
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.NovaVpnTheme

enum class AppTab(val title: String) {
    SHIELD("Shield"),
    LOCATIONS("Locations"),
    SECURITY("Security"),
    SETTINGS("Settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: NovaVpnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

            NovaVpnTheme(darkTheme = userSettings.darkTheme) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: NovaVpnViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(AppTab.SHIELD) }

    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val servers by viewModel.filteredServers.collectAsStateWithLifecycle()
    val trafficStats by viewModel.trafficStats.collectAsStateWithLifecycle()
    val publicIpInfo by viewModel.publicIpInfo.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val clientKeyPair by viewModel.clientKeyPair.collectAsStateWithLifecycle()
    val leakTestResult by viewModel.leakTestResult.collectAsStateWithLifecycle()
    val isTestingLeaks by viewModel.isTestingLeaks.collectAsStateWithLifecycle()
    val durationSeconds by viewModel.connectionDurationSeconds.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    // Android Notification Permission Launcher (API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Notification permission recommended for VPN status bar", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Android VpnService System Preparation Launcher
    val vpnPrepareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.connect(context)
        } else {
            Toast.makeText(context, "VPN tunnel permission was declined", Toast.LENGTH_SHORT).show()
        }
    }

    val handleConnect = {
        val prepareIntent = viewModel.prepareVpn(context)
        if (prepareIntent != null) {
            vpnPrepareLauncher.launch(prepareIntent)
        } else {
            viewModel.connect(context)
        }
    }

    BackHandler(enabled = currentTab != AppTab.SHIELD) {
        currentTab = AppTab.SHIELD
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = CyberSurfaceDark,
                tonalElevation = androidx.compose.ui.unit.Dp(0f)
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.SHIELD,
                    onClick = { currentTab = AppTab.SHIELD },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield"
                        )
                    },
                    label = {
                        Text(
                            text = "Shield",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.SHIELD) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan.copy(alpha = 0.2f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.LOCATIONS,
                    onClick = { currentTab = AppTab.LOCATIONS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Locations"
                        )
                    },
                    label = {
                        Text(
                            text = "Locations",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.LOCATIONS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan.copy(alpha = 0.2f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.SECURITY,
                    onClick = { currentTab = AppTab.SECURITY },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security"
                        )
                    },
                    label = {
                        Text(
                            text = "Security",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.SECURITY) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan.copy(alpha = 0.2f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.SETTINGS,
                    onClick = { currentTab = AppTab.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = {
                        Text(
                            text = "Settings",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberCyan,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan.copy(alpha = 0.2f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.SHIELD -> {
                    HomeScreen(
                        connectionState = connectionState,
                        selectedServer = selectedServer,
                        servers = servers,
                        trafficStats = trafficStats,
                        publicIpInfo = publicIpInfo,
                        userSettings = userSettings,
                        durationSeconds = durationSeconds,
                        onConnectClicked = handleConnect,
                        onDisconnectClicked = { viewModel.disconnect(context) },
                        onSelectServerClicked = { currentTab = AppTab.LOCATIONS },
                        onServerSelectedOnMap = { s ->
                            viewModel.selectServer(s, if (connectionState is com.example.vpn.model.VpnConnectionState.Connected) context else null)
                        },
                        onRefreshIp = { viewModel.refreshPublicIp() }
                    )
                }

                AppTab.LOCATIONS -> {
                    ServerListScreen(
                        servers = servers,
                        selectedServer = selectedServer,
                        searchQuery = searchQuery,
                        selectedFilter = selectedFilter,
                        isPremiumUser = userSettings.isPremiumUser,
                        onSearchChanged = { viewModel.setSearchQuery(it) },
                        onFilterChanged = { viewModel.setServerFilter(it) },
                        onServerSelected = { s ->
                            viewModel.selectServer(s, if (connectionState is com.example.vpn.model.VpnConnectionState.Connected) context else null)
                            currentTab = AppTab.SHIELD
                        },
                        onAddCustomServer = { name, ip, port, pubKey ->
                            val newServer = viewModel.addCustomServer(name, ip, port, pubKey)
                            viewModel.selectServer(newServer)
                            currentTab = AppTab.SHIELD
                        },
                        onImportConfig = { text ->
                            viewModel.importWireGuardConfig(text)
                        }
                    )
                }

                AppTab.SECURITY -> {
                    SecurityShieldScreen(
                        userSettings = userSettings,
                        clientKeyPair = clientKeyPair,
                        leakTestResult = leakTestResult,
                        isTesting = isTestingLeaks,
                        isVpnConnected = connectionState is com.example.vpn.model.VpnConnectionState.Connected,
                        onToggleKillSwitch = { viewModel.toggleKillSwitch(it) },
                        onRunLeakTest = { viewModel.runLeakTest() },
                        onRegenerateKeys = { viewModel.regenerateKeys() },
                        onExportConfig = { viewModel.exportCurrentWireGuardConfig() }
                    )
                }

                AppTab.SETTINGS -> {
                    SettingsScreen(
                        userSettings = userSettings,
                        onProtocolSelected = { viewModel.setProtocol(it) },
                        onDnsProviderSelected = { provider, custom ->
                            viewModel.setDnsProvider(provider, custom)
                        },
                        onMtuChanged = { viewModel.setMtu(it) },
                        onToggleAutoReconnect = { /* Handled in settings */ },
                        onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                        onTogglePremium = { viewModel.togglePremiumUser() }
                    )
                }
            }
        }
    }
}
