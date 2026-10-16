# NovaVPN — Modern, Production-Grade VPN Architecture

NovaVPN is a cross-platform, cybersecurity-oriented VPN ecosystem designed with the standards of Proton VPN and Mullvad. It prioritizes zero-knowledge privacy, verifiable cryptographic tunnels, and real connection integrity.

---

## Key Architecture & Features

### 1. Android Native Application (Kotlin & Jetpack Compose)
- **Genuine Android `VpnService` Tunnel**: Opens standard Linux TUN network interface (`/dev/tun`), configures routing table (`0.0.0.0/0`), MTU (1420), and sets encrypted DNS.
- **WireGuard Handshake Verification**: Employs socket protection (`protect(socket)`) and transmits Type 1 WireGuard Initiation packets to the endpoint. Detects timeouts and connection dropouts rather than faking connection states.
- **Interactive Cyber World Map**: Vector canvas displaying global nodes (Switzerland, Iceland, Sweden, Germany, Netherlands, USA, Japan, Singapore) with animated bezier packet routes and real-time radar pulse indicators.
- **In-App Leak Testing**: Live multi-point inspection of public IP, ISP exposure, DNS leak containment, and IPv6 traffic.
- **Kill Switch & Security Center**: Seamlessly blocks non-tunnel traffic if the connection drops, with guidance for Android's system-level "Always-on VPN".
- **Cryptographic Key Management**: Real-time generation of 32-byte Curve25519 (X25519) keypairs with clamped bits, with one-tap WireGuard `.conf` configuration export and import.
- **Custom VPS Node Support**: Easily add and connect to self-hosted WireGuard servers.

### 2. Backend API (Node.js & TypeScript)
- Server discovery and health endpoints (`/api/servers`, `/api/health`).
- Zero-Log audit logging policy: URL queries, destination endpoints, and browsing history are excluded from log streams.
- WireGuard peer management (`/api/vpn/provision`): Dynamic allocation of internal client IPs (`10.66.66.x/32`) and registration into Linux kernel WireGuard interfaces.
- Authentication with bcrypt hashing and JWT session tokens.

### 3. Server Infrastructure
- Production Linux setup script (`infrastructure/setup-wireguard.sh`) automating `wg-quick`, IPv4/IPv6 forwarding, and `iptables` NAT masquerading.
- Docker & Docker Compose configuration for containerized deployment.
- Prometheus server monitoring configuration.

---

## Directory Structure

```
├── app/                               # Native Android VPN Client
│   ├── src/main/java/com/example/
│   │   ├── MainActivity.kt            # App navigation, edge-to-edge & VpnService launcher
│   │   ├── vpn/
│   │   │   ├── NovaVpnService.kt      # android.net.VpnService implementation & TUN manager
│   │   │   ├── model/VpnModels.kt     # Domain models, connection states & telemetry
│   │   │   ├── crypto/WireGuardCrypto.kt # Curve25519 key generator & packet builder
│   │   │   ├── network/IpDiagnosticService.kt # IP & DNS leak tester
│   │   │   └── repository/VpnRepository.kt    # Server registry & local settings
│   │   └── ui/
│   │       ├── NovaVpnViewModel.kt    # StateFlow coordinator
│   │       ├── screens/HomeScreen.kt  # Futuristic animated shield button & live stats
│   │       ├── screens/ServerListScreen.kt # Filterable server list & custom VPS dialog
│   │       ├── screens/SecurityShieldScreen.kt # Leak tests & key management
│   │       ├── screens/SettingsScreen.kt       # Protocol, DNS & MTU adjustments
│   │       ├── components/WorldMapCanvas.kt   # Interactive world map canvas
│   │       └── theme/Theme.kt         # Cybersecurity neon cyan & dark palette
├── server/                            # Node.js + TypeScript REST API
│   ├── src/index.ts                   # Express server & routes
│   ├── src/wireguard.ts               # WireGuard kernel peer provisioning
│   ├── src/schema.sql                 # PostgreSQL zero-knowledge schema
│   ├── package.json                   # Dependencies
│   └── Dockerfile                     # Multi-stage production container
├── infrastructure/                    # DevOps & Linux Server automation
│   ├── setup-wireguard.sh             # VPS WireGuard & iptables setup script
│   └── prometheus-wg-exporter.yml     # Prometheus telemetry config
├── docker-compose.yml                 # Multi-container orchestration
├── SETUP_GUIDE.md                     # Step-by-step local & VPS setup guide
└── SECURITY_AUDIT.md                  # Leak test instructions & privacy audit
```
