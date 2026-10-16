# NovaVPN • Security Architecture & Leak Testing Instructions

## 1. Threat Model & Privacy Guarantees

NovaVPN is engineered to protect user traffic against:
- **Untrusted Local Networks**: Open Wi-Fi sniffing, rogue access points, ARP poisoning, and captive portal snooping.
- **ISP Surveillance & Throttling**: Deep Packet Inspection (DPI) of target hostnames and protocols through 256-bit ChaCha20-Poly1305 encryption.
- **DNS Hijacking & Poisoning**: DNS requests are encapsulated within the VPN tunnel and forwarded strictly to designated zero-log resolvers.

### Transparent Disclosures & Limitations
- **No Absolute Anonymity**: VPNs provide privacy and encrypted transit, not complete anonymity. Account identifiers, payments, and browser fingerprinting remain relevant vectors.
- **Traffic Correlation**: Sophisticated nation-state adversaries monitoring both entry and exit nodes could perform statistical timing correlation.
- **No-Logs Verification**: While our backend code and RAM-only server configuration enforce a zero-log policy, users should rely on verifiable source code and independent cryptographic audits rather than unbacked claims.

---

## 2. In-Depth IP & DNS Leak Verification Guide

To independently verify that your traffic is protected and no data leaks outside the tunnel:

### Test 1: Public IPv4 & Geo Leak Test
1. Disconnect NovaVPN and check your public IP:
   ```bash
   curl https://api.ipify.org
   ```
   Note your ISP and real IP address.
2. Connect to a NovaVPN server (e.g. Zurich, Switzerland).
3. Query the external IP again:
   ```bash
   curl https://api.ipify.org
   ```
   *Expected Result*: The returned IP address must match the VPN server's IP address.

### Test 2: DNS Leak Inspection
1. While connected to NovaVPN, visit an independent testing service such as [dnsleaktest.com](https://www.dnsleaktest.com) or run:
   ```bash
   dig whoami.akamai.net
   ```
2. *Expected Result*: All resolving DNS servers should belong to the VPN datacenter or the configured privacy provider (e.g., Cloudflare, Quad9), and never your local ISP's recursive resolvers.

### Test 3: IPv6 Leak Containment
1. Check if IPv6 traffic bypasses the VPN tunnel:
   ```bash
   curl -6 https://icanhazip.com
   ```
2. *Expected Result*: Either the connection routes through the WireGuard IPv6 tunnel address (`::/0`) or fails gracefully (blocking IPv6 traffic to prevent unencrypted leaks).

### Test 4: Kill Switch Failure Simulation
1. Connect to NovaVPN with Kill Switch enabled.
2. In Android Settings -> Network -> VPN, enable **Block connections without VPN**.
3. Force stop the VPN tunnel or sever the remote endpoint connection.
4. Attempt to access a website or ping an external IP.
5. *Expected Result*: Traffic should be completely blocked until the VPN connection is restored.
