package com.example.vpn.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.vpn.crypto.WireGuardCrypto
import com.example.vpn.crypto.WireGuardKeyPair
import com.example.vpn.model.DnsProvider
import com.example.vpn.model.PublicIpInfo
import com.example.vpn.model.UserSettings
import com.example.vpn.model.VpnProtocol
import com.example.vpn.model.VpnServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VpnRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("novavpn_prefs", Context.MODE_PRIVATE)

    private val _servers = MutableStateFlow<List<VpnServer>>(emptyList())
    val servers: StateFlow<List<VpnServer>> = _servers.asStateFlow()

    private val _selectedServer = MutableStateFlow<VpnServer>(getDefaultServer())
    val selectedServer: StateFlow<VpnServer> = _selectedServer.asStateFlow()

    private val _userSettings = MutableStateFlow(loadSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    private val _clientKeyPair = MutableStateFlow(loadOrCreateKeyPair())
    val clientKeyPair: StateFlow<WireGuardKeyPair> = _clientKeyPair.asStateFlow()

    private val _publicIpInfo = MutableStateFlow(PublicIpInfo())
    val publicIpInfo: StateFlow<PublicIpInfo> = _publicIpInfo.asStateFlow()

    init {
        loadInitialServers()
    }

    private fun getDefaultServer(): VpnServer {
        return VpnServer(
            id = "ch-zurich-01",
            name = "Zurich • Secure Core #1",
            country = "Switzerland",
            countryCode = "CH",
            flagEmoji = "🇨🇭",
            city = "Zurich",
            endpointIp = "194.126.177.10",
            endpointPort = 51820,
            serverPublicKey = "rM8F4eF768jZ+X2d73pL0eWd9P3cQ2/H4bA8fT6mU1Y=",
            pingMs = 18,
            loadPercentage = 34,
            isPremium = false,
            p2pSupported = true,
            streamingSupported = true,
            latitude = 47.3769f,
            longitude = 8.5417f
        )
    }

    private fun loadInitialServers() {
        val serverList = listOf(
            VpnServer(
                id = "ch-zurich-01",
                name = "Zurich • Secure Core #1",
                country = "Switzerland",
                countryCode = "CH",
                flagEmoji = "🇨🇭",
                city = "Zurich",
                endpointIp = "194.126.177.10",
                endpointPort = 51820,
                serverPublicKey = "rM8F4eF768jZ+X2d73pL0eWd9P3cQ2/H4bA8fT6mU1Y=",
                pingMs = 18,
                loadPercentage = 34,
                isPremium = false,
                p2pSupported = true,
                streamingSupported = true,
                latitude = 47.3769f,
                longitude = 8.5417f
            ),
            VpnServer(
                id = "is-reykjavik-01",
                name = "Reykjavik • Zero-Log Haven",
                country = "Iceland",
                countryCode = "IS",
                flagEmoji = "🇮🇸",
                city = "Reykjavik",
                endpointIp = "185.112.82.14",
                endpointPort = 51820,
                serverPublicKey = "tY9G3kK559vL+A1m84qN1fXe8Q4dR3/J5cB9gU7nV2Z=",
                pingMs = 42,
                loadPercentage = 21,
                isPremium = true,
                p2pSupported = true,
                streamingSupported = true,
                latitude = 64.1466f,
                longitude = -21.9426f
            ),
            VpnServer(
                id = "se-stockholm-01",
                name = "Stockholm • 10Gbps P2P",
                country = "Sweden",
                countryCode = "SE",
                flagEmoji = "🇸🇪",
                city = "Stockholm",
                endpointIp = "193.180.119.5",
                endpointPort = 51820,
                serverPublicKey = "aB3D7kL992xM+B4v63rK2fZw9S5eT4/K6dC0hV8pW3A=",
                pingMs = 28,
                loadPercentage = 45,
                isPremium = true,
                p2pSupported = true,
                streamingSupported = false,
                latitude = 59.3293f,
                longitude = 18.0686f
            ),
            VpnServer(
                id = "nl-amsterdam-01",
                name = "Amsterdam • Fast Gateway",
                country = "Netherlands",
                countryCode = "NL",
                flagEmoji = "🇳🇱",
                city = "Amsterdam",
                endpointIp = "185.107.56.23",
                endpointPort = 51820,
                serverPublicKey = "kL4E8jR221wP+C5m94tL3aYx0T6fU5/L7eD1iW9qX4B=",
                pingMs = 14,
                loadPercentage = 62,
                isPremium = false,
                p2pSupported = true,
                streamingSupported = true,
                latitude = 52.3676f,
                longitude = 4.9041f
            ),
            VpnServer(
                id = "de-frankfurt-01",
                name = "Frankfurt • DE-CIX Backbone",
                country = "Germany",
                countryCode = "DE",
                flagEmoji = "🇩🇪",
                city = "Frankfurt",
                endpointIp = "194.187.249.12",
                endpointPort = 51820,
                serverPublicKey = "mN5F9kS332xQ+D6n05uM4bZy1U7gV6/M8fE2jX0rY5C=",
                pingMs = 19,
                loadPercentage = 51,
                isPremium = false,
                p2pSupported = true,
                streamingSupported = true,
                latitude = 50.1109f,
                longitude = 8.6821f
            ),
            VpnServer(
                id = "us-newyork-01",
                name = "New York • Ultra Stream #1",
                country = "United States",
                countryCode = "US",
                flagEmoji = "🇺🇸",
                city = "New York",
                endpointIp = "198.51.100.24",
                endpointPort = 51820,
                serverPublicKey = "pQ6G0lT443yR+E7o16vN5cAz2V8hW7/N9gF3kY1sZ6D=",
                pingMs = 78,
                loadPercentage = 68,
                isPremium = false,
                p2pSupported = false,
                streamingSupported = true,
                latitude = 40.7128f,
                longitude = -74.0060f
            ),
            VpnServer(
                id = "us-losangeles-01",
                name = "Los Angeles • Pacific Hub",
                country = "United States",
                countryCode = "US",
                flagEmoji = "🇺🇸",
                city = "Los Angeles",
                endpointIp = "198.51.100.89",
                endpointPort = 51820,
                serverPublicKey = "sT7H1mU554zS+F8p27wO6dB03W9iX8/O0hG4lZ2tA7E=",
                pingMs = 95,
                loadPercentage = 54,
                isPremium = true,
                p2pSupported = true,
                streamingSupported = true,
                latitude = 34.0522f,
                longitude = -118.2437f
            ),
            VpnServer(
                id = "jp-tokyo-01",
                name = "Tokyo • Low Ping Asia",
                country = "Japan",
                countryCode = "JP",
                flagEmoji = "🇯🇵",
                city = "Tokyo",
                endpointIp = "203.0.113.45",
                endpointPort = 51820,
                serverPublicKey = "uV8I2nV665aT+G9q38xP7eC14X0jY9/P1iH5ma3uB8F=",
                pingMs = 120,
                loadPercentage = 38,
                isPremium = false,
                p2pSupported = true,
                streamingSupported = true,
                latitude = 35.6762f,
                longitude = 139.6503f
            ),
            VpnServer(
                id = "sg-singapore-01",
                name = "Singapore • Southeast Gateway",
                country = "Singapore",
                countryCode = "SG",
                flagEmoji = "🇸🇬",
                city = "Singapore",
                endpointIp = "203.0.113.92",
                endpointPort = 51820,
                serverPublicKey = "wX9J3oW776bU+H0r49yQ8fD25Y1kZ0/Q2jI6nb4vC9G=",
                pingMs = 135,
                loadPercentage = 42,
                isPremium = true,
                p2pSupported = true,
                streamingSupported = true,
                latitude = 1.3521f,
                longitude = 103.8198f
            ),
            VpnServer(
                id = "local-loopback-01",
                name = "Nova Secure Enclave (Local Test)",
                country = "Internal Mesh",
                countryCode = "SEC",
                flagEmoji = "⚡",
                city = "Loopback Gateway",
                endpointIp = "127.0.0.1",
                endpointPort = 51820,
                serverPublicKey = "zY0K4pX887cV+I1s50zR9gE36Z2lA1/R3kJ7oc5wD0H=",
                pingMs = 1,
                loadPercentage = 8,
                isPremium = false,
                p2pSupported = true,
                streamingSupported = true,
                latitude = 0f,
                longitude = 0f
            )
        )
        _servers.value = serverList
    }

    fun selectServer(server: VpnServer) {
        _selectedServer.value = server
        prefs.edit().putString("selected_server_id", server.id).apply()
    }

    fun addCustomServer(
        name: String,
        endpointIp: String,
        endpointPort: Int,
        publicKey: String,
        clientIp: String = "10.66.66.2/32"
    ): VpnServer {
        val custom = VpnServer(
            id = "custom-${System.currentTimeMillis()}",
            name = name.ifBlank { "Custom VPS Node" },
            country = "Custom Server",
            countryCode = "VPS",
            flagEmoji = "🖥️",
            city = endpointIp,
            endpointIp = endpointIp,
            endpointPort = endpointPort,
            serverPublicKey = publicKey,
            clientIp = clientIp,
            pingMs = 25,
            loadPercentage = 10,
            isPremium = false,
            latitude = 0f,
            longitude = 0f,
            isCustom = true
        )
        _servers.value = listOf(custom) + _servers.value
        selectServer(custom)
        return custom
    }

    fun updateSettings(settings: UserSettings) {
        _userSettings.value = settings
        prefs.edit()
            .putBoolean("kill_switch", settings.killSwitchEnabled)
            .putBoolean("auto_reconnect", settings.autoReconnect)
            .putString("dns_provider", settings.dnsProvider.name)
            .putString("custom_dns", settings.customDnsPrimary)
            .putInt("mtu", settings.mtu)
            .putString("protocol", settings.selectedProtocol.name)
            .putBoolean("is_premium", settings.isPremiumUser)
            .putBoolean("dark_theme", settings.darkTheme)
            .apply()
    }

    fun regenerateKeys(): WireGuardKeyPair {
        val newPair = WireGuardCrypto.generateKeyPair()
        _clientKeyPair.value = newPair
        prefs.edit()
            .putString("client_priv_key", newPair.privateKeyBase64)
            .putString("client_pub_key", newPair.publicKeyBase64)
            .apply()
        return newPair
    }

    fun updatePublicIpInfo(info: PublicIpInfo) {
        _publicIpInfo.value = info
    }

    private fun loadOrCreateKeyPair(): WireGuardKeyPair {
        val savedPriv = prefs.getString("client_priv_key", null)
        val savedPub = prefs.getString("client_pub_key", null)

        return if (!savedPriv.isNullOrEmpty() && !savedPub.isNullOrEmpty()) {
            WireGuardKeyPair(savedPriv, savedPub)
        } else {
            val pair = WireGuardCrypto.generateKeyPair()
            prefs.edit()
                .putString("client_priv_key", pair.privateKeyBase64)
                .putString("client_pub_key", pair.publicKeyBase64)
                .apply()
            pair
        }
    }

    private fun loadSettings(): UserSettings {
        val killSwitch = prefs.getBoolean("kill_switch", true)
        val autoReconnect = prefs.getBoolean("auto_reconnect", true)
        val dnsProviderName = prefs.getString("dns_provider", DnsProvider.NOVA_SECURE.name) ?: DnsProvider.NOVA_SECURE.name
        val dnsProvider = try { DnsProvider.valueOf(dnsProviderName) } catch (e: Exception) { DnsProvider.NOVA_SECURE }
        val customDns = prefs.getString("custom_dns", "1.1.1.1") ?: "1.1.1.1"
        val mtu = prefs.getInt("mtu", 1420)
        val protoName = prefs.getString("protocol", VpnProtocol.WIREGUARD.name) ?: VpnProtocol.WIREGUARD.name
        val protocol = try { VpnProtocol.valueOf(protoName) } catch (e: Exception) { VpnProtocol.WIREGUARD }
        val isPremium = prefs.getBoolean("is_premium", false)
        val darkTheme = prefs.getBoolean("dark_theme", true)

        return UserSettings(
            killSwitchEnabled = killSwitch,
            autoReconnect = autoReconnect,
            dnsProvider = dnsProvider,
            customDnsPrimary = customDns,
            mtu = mtu,
            selectedProtocol = protocol,
            isPremiumUser = isPremium,
            darkTheme = darkTheme
        )
    }
}
