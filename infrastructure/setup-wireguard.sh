#!/usr/bin/env bash
# ==============================================================================
# NovaVPN Production WireGuard Server Setup Script
# Target OS: Ubuntu 22.04 LTS / 24.04 LTS or Debian 12
# ==============================================================================

set -euo pipefail

echo "========================================================"
echo "    NovaVPN • Production WireGuard Server Installer     "
echo "========================================================"

if [[ $EUID -ne 0 ]]; then
   echo "Error: This script must be run as root (sudo bash setup-wireguard.sh)"
   exit 1
fi

SERVER_IP=$(curl -s4 ifconfig.me || curl -s4 api.ipify.org || hostname -I | awk '{print $1}')
WG_PORT=51820
WG_SUBNET="10.66.66.1/24"
NET_INTERFACE=$(ip route get 8.8.8.8 | awk '{print $5; exit}')

echo "Detected Public Server IP: ${SERVER_IP}"
echo "Detected Primary Network Interface: ${NET_INTERFACE}"

# 1. Update system & install WireGuard
echo "[1/5] Installing wireguard, iptables, and qrencode..."
apt-get update -q
apt-get install -y wireguard iptables qrencode curl

# 2. Enable kernel packet forwarding
echo "[2/5] Configuring IPv4 and IPv6 kernel packet forwarding..."
cat << 'EOF' > /etc/sysctl.d/99-novavpn.conf
net.ipv4.ip_forward = 1
net.ipv6.conf.all.forwarding = 1
net.ipv4.conf.all.rp_filter = 1
EOF
sysctl -p /etc/sysctl.d/99-novavpn.conf

# 3. Generate WireGuard Server Cryptographic Keys
echo "[3/5] Generating server Curve25519 cryptographic keypair..."
mkdir -p /etc/wireguard
chmod 700 /etc/wireguard
cd /etc/wireguard

umask 077
if [[ ! -f server_private.key ]]; then
    wg genkey | tee server_private.key | wg pubkey > server_public.key
fi

SERVER_PRIVKEY=$(cat server_private.key)
SERVER_PUBKEY=$(cat server_public.key)

# 4. Create wg0.conf
echo "[4/5] Writing /etc/wireguard/wg0.conf..."
cat << EOF > /etc/wireguard/wg0.conf
[Interface]
Address = ${WG_SUBNET}
SaveConfig = false
PrivateKey = ${SERVER_PRIVKEY}
ListenPort = ${WG_PORT}

# NAT Routing Rules via iptables
PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o ${NET_INTERFACE} -j MASQUERADE
PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o ${NET_INTERFACE} -j MASQUERADE

# Client peers will be appended below or dynamically via:
# wg set wg0 peer <PubKey> allowed-ips <IP>/32
EOF

chmod 600 /etc/wireguard/wg0.conf

# 5. Enable and start wg-quick@wg0
echo "[5/5] Enabling and starting WireGuard systemd service..."
systemctl enable wg-quick@wg0
systemctl restart wg-quick@wg0

echo "========================================================"
echo "    NovaVPN WireGuard Server Setup Complete!            "
echo "========================================================"
echo "Server Public IP:  ${SERVER_IP}"
echo "WireGuard Port:    ${WG_PORT}/UDP"
echo "Server Public Key: ${SERVER_PUBKEY}"
echo ""
echo "Add this server to your NovaVPN Android App using the"
echo "'Add Custom Server' dialog or in the server registry:"
echo "Endpoint:    ${SERVER_IP}:${WG_PORT}"
echo "Public Key:  ${SERVER_PUBKEY}"
echo "========================================================"
