package com.example.vpn.model

enum class VpnProtocol(val displayName: String, val port: Int, val description: String) {
    WIREGUARD("WireGuard", 51820, "Next-gen cryptography (ChaCha20-Poly1305), lowest latency & highest throughput"),
    OPENVPN_UDP("OpenVPN (UDP)", 1194, "Battle-tested 256-bit AES-GCM tunnel with HMAC packet authentication"),
    IKEV2("IKEv2/IPSec", 500, "Excellent mobile roaming stability with Cisco/Android native acceleration")
}

enum class DnsProvider(val label: String, val primaryIp: String, val secondaryIp: String, val isEncrypted: Boolean) {
    NOVA_SECURE("Nova Zero-Log DNS", "10.66.0.1", "10.66.0.2", true),
    CLOUDFLARE("Cloudflare 1.1.1.1", "1.1.1.1", "1.0.0.1", true),
    QUAD9("Quad9 Threat Blocking", "9.9.9.9", "149.112.112.112", true),
    ADGUARD("AdGuard Ad-Shield", "94.140.14.14", "94.140.15.15", true),
    CUSTOM("Custom DNS", "", "", false)
}

data class VpnServer(
    val id: String,
    val name: String,
    val country: String,
    val countryCode: String,
    val flagEmoji: String,
    val city: String,
    val endpointIp: String,
    val endpointPort: Int = 51820,
    val serverPublicKey: String,
    val allowedIps: String = "0.0.0.0/0, ::/0",
    val clientIp: String = "10.66.66.2/32",
    val pingMs: Int,
    val loadPercentage: Int,
    val isPremium: Boolean,
    val p2pSupported: Boolean = true,
    val streamingSupported: Boolean = true,
    val latitude: Float,
    val longitude: Float,
    val isCustom: Boolean = false
)

sealed class VpnConnectionState {
    object Disconnected : VpnConnectionState()
    data class Connecting(val server: VpnServer, val step: String) : VpnConnectionState()
    data class Connected(
        val server: VpnServer,
        val connectedAtMillis: Long,
        val vpnIp: String,
        val protocol: VpnProtocol = VpnProtocol.WIREGUARD
    ) : VpnConnectionState()
    object Disconnecting : VpnConnectionState()
    data class Error(val message: String, val details: String? = null, val timestamp: Long = System.currentTimeMillis()) : VpnConnectionState()
}

data class VpnTrafficStats(
    val bytesUploaded: Long = 0L,
    val bytesDownloaded: Long = 0L,
    val uploadRateBps: Long = 0L,
    val downloadRateBps: Long = 0L,
    val packetsSent: Long = 0L,
    val packetsReceived: Long = 0L
)

data class PublicIpInfo(
    val ip: String = "Scanning...",
    val isp: String = "Detecting ISP...",
    val city: String = "Detecting City...",
    val country: String = "Detecting Country...",
    val isProtected: Boolean = false,
    val queryTimestamp: Long = 0L
)

data class LeakTestResult(
    val detectedIp: String,
    val detectedIsp: String,
    val detectedCountry: String,
    val dnsServersResponding: List<String>,
    val hasDnsLeak: Boolean,
    val hasIpv6Leak: Boolean,
    val overallStatus: String,
    val testedAt: Long = System.currentTimeMillis()
)

data class UserSettings(
    val killSwitchEnabled: Boolean = true,
    val autoReconnect: Boolean = true,
    val dnsProvider: DnsProvider = DnsProvider.NOVA_SECURE,
    val customDnsPrimary: String = "1.1.1.1",
    val mtu: Int = 1420,
    val selectedProtocol: VpnProtocol = VpnProtocol.WIREGUARD,
    val isPremiumUser: Boolean = false,
    val darkTheme: Boolean = true,
    val bypassLocalTraffic: Boolean = true
)
