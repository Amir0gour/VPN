# NovaVPN • Step-by-Step Setup Guide

This guide walks through deploying the NovaVPN backend, provisioning a real WireGuard Linux VPS node, and running the Android client.

---

## 1. Quick Start: Local Backend with Docker

To run the NovaVPN PostgreSQL database and REST API locally:

```bash
# 1. Start database and API
docker compose up -d

# 2. Check service health
curl http://localhost:4000/api/health
```

The API will be available at `http://localhost:4000/api/` with endpoints:
- `GET /api/health` — Service health & WireGuard status
- `GET /api/servers` — Global VPN server list
- `POST /api/auth/register` — Create account
- `POST /api/auth/login` — Sign in and receive JWT
- `POST /api/vpn/provision` — Provision WireGuard peer

---

## 2. Deploying a Real Linux WireGuard VPS Node

To deploy a live WireGuard node on an Ubuntu 22.04 / 24.04 or Debian VPS (e.g. Hetzner, DigitalOcean, Linode, AWS):

```bash
# 1. SSH into your VPS as root
ssh root@your-vps-ip

# 2. Download and execute setup script
curl -sSL https://raw.githubusercontent.com/your-org/novavpn/main/infrastructure/setup-wireguard.sh | sudo bash
```

The script will automatically:
1. Install `wireguard` and `iptables`.
2. Enable kernel packet forwarding (`net.ipv4.ip_forward = 1`).
3. Generate server cryptographic keys at `/etc/wireguard/server_private.key` and `/etc/wireguard/server_public.key`.
4. Configure `/etc/wireguard/wg0.conf` with NAT forwarding rules.
5. Enable and start the `wg-quick@wg0` systemd service on port `51820/UDP`.

---

## 3. Connecting the Android Client

1. Open NovaVPN on your device or emulator.
2. If testing against a self-hosted node:
   - Navigate to the **Locations** tab.
   - Tap the **+** button at the bottom-right.
   - Enter your VPS public IP, UDP port (`51820`), and the server's public key generated in Step 2.
   - Tap **Save & Select**.
3. Return to the **Shield** tab and tap the large connection button.
4. Android will prompt: `"NovaVPN wants to set up a VPN connection that allows it to monitor network traffic"`. Tap **OK**.
5. NovaVPN establishes the encrypted tunnel and verifies the WireGuard handshake.
6. Verify your connection in the **Security** tab by running the **IP & DNS Leak Test**.

---

## 4. Multi-Platform Roadmap

- **Android (Native)**: Implemented with Kotlin, Jetpack Compose, and `android.net.VpnService`.
- **Desktop (Windows, macOS, Linux)**: Tauri + React + TypeScript frontend utilizing `@wireguard/wireguard-go` or `wintun.dll` on Windows.
- **iOS**: Swift & NetworkExtension (`NEPacketTunnelProvider`) with WireGuardKit.
