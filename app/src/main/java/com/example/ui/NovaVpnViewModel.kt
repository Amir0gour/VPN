package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vpn.NovaVpnService
import com.example.vpn.crypto.WireGuardCrypto
import com.example.vpn.model.DnsProvider
import com.example.vpn.model.LeakTestResult
import com.example.vpn.model.PublicIpInfo
import com.example.vpn.model.UserSettings
import com.example.vpn.model.VpnConnectionState
import com.example.vpn.model.VpnProtocol
import com.example.vpn.model.VpnServer
import com.example.vpn.model.VpnTrafficStats
import com.example.vpn.network.IpDiagnosticService
import com.example.vpn.repository.VpnRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ServerFilter {
    ALL, FREE, PREMIUM, P2P, STREAMING
}

class NovaVpnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VpnRepository(application)

    val connectionState: StateFlow<VpnConnectionState> = NovaVpnService.connectionState
    val trafficStats: StateFlow<VpnTrafficStats> = NovaVpnService.trafficStats

    val selectedServer: StateFlow<VpnServer> = repository.selectedServer
    val userSettings: StateFlow<UserSettings> = repository.userSettings
    val clientKeyPair = repository.clientKeyPair
    val publicIpInfo: StateFlow<PublicIpInfo> = repository.publicIpInfo

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(ServerFilter.ALL)
    val selectedFilter: StateFlow<ServerFilter> = _selectedFilter.asStateFlow()

    val filteredServers: StateFlow<List<VpnServer>> = combine(
        repository.servers,
        _searchQuery,
        _selectedFilter
    ) { servers, query, filter ->
        servers.filter { server ->
            val matchesQuery = query.isBlank() ||
                server.name.contains(query, ignoreCase = true) ||
                server.country.contains(query, ignoreCase = true) ||
                server.city.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                ServerFilter.ALL -> true
                ServerFilter.FREE -> !server.isPremium
                ServerFilter.PREMIUM -> server.isPremium
                ServerFilter.P2P -> server.p2pSupported
                ServerFilter.STREAMING -> server.streamingSupported
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _leakTestResult = MutableStateFlow<LeakTestResult?>(null)
    val leakTestResult: StateFlow<LeakTestResult?> = _leakTestResult.asStateFlow()

    private val _isTestingLeaks = MutableStateFlow(false)
    val isTestingLeaks: StateFlow<Boolean> = _isTestingLeaks.asStateFlow()

    private val _connectionDurationSeconds = MutableStateFlow(0L)
    val connectionDurationSeconds: StateFlow<Long> = _connectionDurationSeconds.asStateFlow()

    private var durationTickerJob: Job? = null

    init {
        refreshPublicIp()
        observeConnectionState()
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            connectionState.collect { state ->
                when (state) {
                    is VpnConnectionState.Connected -> {
                        startDurationTicker(state.connectedAtMillis)
                        refreshPublicIp()
                    }
                    else -> {
                        stopDurationTicker()
                        if (state is VpnConnectionState.Disconnected) {
                            refreshPublicIp()
                        }
                    }
                }
            }
        }
    }

    private fun startDurationTicker(startTimeMillis: Long) {
        durationTickerJob?.cancel()
        durationTickerJob = viewModelScope.launch {
            while (isActive) {
                val seconds = (System.currentTimeMillis() - startTimeMillis) / 1000L
                _connectionDurationSeconds.value = if (seconds > 0) seconds else 0L
                delay(1000)
            }
        }
    }

    private fun stopDurationTicker() {
        durationTickerJob?.cancel()
        durationTickerJob = null
        _connectionDurationSeconds.value = 0L
    }

    /**
     * Checks if VpnService permission needs to be requested
     * Returns Intent if user must approve system dialog, or null if already granted
     */
    fun prepareVpn(context: Context): Intent? {
        return VpnService.prepare(context)
    }

    /**
     * Starts the NovaVPN service
     */
    fun connect(context: Context) {
        val server = selectedServer.value
        val settings = userSettings.value

        // Check premium restriction
        if (server.isPremium && !settings.isPremiumUser) {
            NovaVpnService.updateState(
                VpnConnectionState.Error(
                    message = "Premium Server Required",
                    details = "This server location is reserved for Nova Pro users. Switch to a Free location or upgrade to Pro in Settings."
                )
            )
            return
        }

        val dnsPrimary = when (settings.dnsProvider) {
            DnsProvider.CUSTOM -> settings.customDnsPrimary.ifBlank { "1.1.1.1" }
            else -> settings.dnsProvider.primaryIp
        }
        val dnsSecondary = when (settings.dnsProvider) {
            DnsProvider.CUSTOM -> "1.0.0.1"
            else -> settings.dnsProvider.secondaryIp
        }

        val intent = Intent(context, NovaVpnService::class.java).apply {
            action = NovaVpnService.ACTION_CONNECT
            putExtra(NovaVpnService.EXTRA_SERVER_ID, server.id)
            putExtra(NovaVpnService.EXTRA_SERVER_NAME, server.name)
            putExtra(NovaVpnService.EXTRA_SERVER_COUNTRY, server.country)
            putExtra(NovaVpnService.EXTRA_SERVER_CITY, server.city)
            putExtra(NovaVpnService.EXTRA_ENDPOINT_IP, server.endpointIp)
            putExtra(NovaVpnService.EXTRA_ENDPOINT_PORT, server.endpointPort)
            putExtra(NovaVpnService.EXTRA_SERVER_PUBKEY, server.serverPublicKey)
            putExtra(NovaVpnService.EXTRA_CLIENT_IP, server.clientIp)
            putExtra(NovaVpnService.EXTRA_DNS_PRIMARY, dnsPrimary)
            putExtra(NovaVpnService.EXTRA_DNS_SECONDARY, dnsSecondary)
            putExtra(NovaVpnService.EXTRA_MTU, settings.mtu)
            putExtra(NovaVpnService.EXTRA_KILL_SWITCH, settings.killSwitchEnabled)
        }

        context.startService(intent)
    }

    /**
     * Disconnects the VPN service
     */
    fun disconnect(context: Context) {
        val intent = Intent(context, NovaVpnService::class.java).apply {
            action = NovaVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }

    fun selectServer(server: VpnServer, autoConnectContext: Context? = null) {
        repository.selectServer(server)
        if (connectionState.value is VpnConnectionState.Connected && autoConnectContext != null) {
            // Reconnect to new server
            disconnect(autoConnectContext)
            viewModelScope.launch {
                delay(300)
                connect(autoConnectContext)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setServerFilter(filter: ServerFilter) {
        _selectedFilter.value = filter
    }

    fun toggleKillSwitch(enabled: Boolean) {
        val current = userSettings.value
        repository.updateSettings(current.copy(killSwitchEnabled = enabled))
    }

    fun setDnsProvider(provider: DnsProvider, customDns: String = "") {
        val current = userSettings.value
        repository.updateSettings(
            current.copy(
                dnsProvider = provider,
                customDnsPrimary = customDns.ifBlank { current.customDnsPrimary }
            )
        )
    }

    fun setMtu(mtu: Int) {
        val current = userSettings.value
        repository.updateSettings(current.copy(mtu = mtu))
    }

    fun setProtocol(protocol: VpnProtocol) {
        val current = userSettings.value
        repository.updateSettings(current.copy(selectedProtocol = protocol))
    }

    fun togglePremiumUser() {
        val current = userSettings.value
        repository.updateSettings(current.copy(isPremiumUser = !current.isPremiumUser))
    }

    fun toggleDarkTheme() {
        val current = userSettings.value
        repository.updateSettings(current.copy(darkTheme = !current.darkTheme))
    }

    fun regenerateKeys() {
        repository.regenerateKeys()
    }

    fun refreshPublicIp() {
        viewModelScope.launch {
            val isConnected = connectionState.value is VpnConnectionState.Connected
            val activeCountry = (connectionState.value as? VpnConnectionState.Connected)?.server?.country ?: ""
            val info = IpDiagnosticService.fetchPublicIpInfo(isConnected, activeCountry)
            repository.updatePublicIpInfo(info)
        }
    }

    fun runLeakTest() {
        viewModelScope.launch {
            _isTestingLeaks.value = true
            val isConnected = connectionState.value is VpnConnectionState.Connected
            val vpnIp = (connectionState.value as? VpnConnectionState.Connected)?.vpnIp ?: ""
            val result = IpDiagnosticService.runLeakTest(isConnected, vpnIp)
            _leakTestResult.value = result
            _isTestingLeaks.value = false
        }
    }

    fun addCustomServer(name: String, ip: String, port: Int, publicKey: String): VpnServer {
        return repository.addCustomServer(name, ip, port, publicKey)
    }

    fun exportCurrentWireGuardConfig(): String {
        val server = selectedServer.value
        val keys = clientKeyPair.value
        val settings = userSettings.value
        val dns = when (settings.dnsProvider) {
            DnsProvider.CUSTOM -> settings.customDnsPrimary
            else -> settings.dnsProvider.primaryIp
        }
        return WireGuardCrypto.generateConfigFile(
            clientPrivateKey = keys.privateKeyBase64,
            clientAddress = server.clientIp,
            dns = dns,
            mtu = settings.mtu,
            serverPublicKey = server.serverPublicKey,
            serverEndpoint = "${server.endpointIp}:${server.endpointPort}",
            allowedIps = server.allowedIps
        )
    }

    fun importWireGuardConfig(configText: String): Boolean {
        val parsed = WireGuardCrypto.parseConfigFile(configText) ?: return false
        val endpointParts = parsed.endpoint.split(":")
        val ip = endpointParts.getOrNull(0) ?: return false
        val port = endpointParts.getOrNull(1)?.toIntOrNull() ?: 51820

        repository.addCustomServer(
            name = "Imported WireGuard Node",
            endpointIp = ip,
            endpointPort = port,
            publicKey = parsed.publicKey,
            clientIp = parsed.address
        )
        return true
    }
}
