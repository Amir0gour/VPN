package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WorldMapCanvas
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberBorderDark
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceVariantDark
import com.example.vpn.model.PublicIpInfo
import com.example.vpn.model.UserSettings
import com.example.vpn.model.VpnConnectionState
import com.example.vpn.model.VpnServer
import com.example.vpn.model.VpnTrafficStats
import java.util.Locale

@Composable
fun HomeScreen(
    connectionState: VpnConnectionState,
    selectedServer: VpnServer,
    servers: List<VpnServer>,
    trafficStats: VpnTrafficStats,
    publicIpInfo: PublicIpInfo,
    userSettings: UserSettings,
    durationSeconds: Long,
    onConnectClicked: () -> Unit,
    onDisconnectClicked: () -> Unit,
    onSelectServerClicked: () -> Unit,
    onServerSelectedOnMap: (VpnServer) -> Unit,
    onRefreshIp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isConnected = connectionState is VpnConnectionState.Connected
    val isConnecting = connectionState is VpnConnectionState.Connecting

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Security Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(CyberCyan.copy(alpha = 0.2f), CyberBlue.copy(alpha = 0.1f))
                            )
                        )
                        .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "NovaVPN Logo",
                        tint = CyberCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "NovaVPN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "WireGuard® Encrypted Mesh",
                        fontSize = 11.sp,
                        color = CyberCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Quick Shield Badge
            Surface(
                color = if (isConnected) CyberEmerald.copy(alpha = 0.15f) else CyberBorderDark.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isConnected) CyberEmerald.copy(alpha = 0.6f) else CyberBorderDark
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) CyberEmerald else if (isConnecting) CyberAmber else CyberRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isConnected) "SHIELDED" else if (isConnecting) "CONNECTING" else "UNSHIELDED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) CyberEmerald else if (isConnecting) CyberAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Interactive World Map Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GLOBAL ROUTING MAP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Tap node to switch",
                        fontSize = 10.sp,
                        color = CyberCyan
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                WorldMapCanvas(
                    servers = servers,
                    selectedServer = selectedServer,
                    isConnected = isConnected,
                    onServerSelected = onServerSelectedOnMap
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Futuristic Animated VPN Connection Button
        VpnConnectionButton(
            connectionState = connectionState,
            onConnect = onConnectClicked,
            onDisconnect = onDisconnectClicked
        )

        // Connecting step or Error Banner
        AnimatedVisibility(visible = isConnecting) {
            val step = (connectionState as? VpnConnectionState.Connecting)?.step ?: "Connecting..."
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberAmber.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberAmber.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = CyberAmber
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = step,
                        fontSize = 12.sp,
                        color = CyberAmber,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        AnimatedVisibility(visible = connectionState is VpnConnectionState.Error) {
            val error = connectionState as? VpnConnectionState.Error
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberRed.copy(alpha = 0.15f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GppBad,
                            contentDescription = "Connection Error",
                            tint = CyberRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error?.message ?: "Tunnel Unreachable",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberRed
                        )
                    }
                    if (!error?.details.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = error?.details ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Server Location Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectServerClicked() }
                .testTag("location_picker_card"),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = selectedServer.flagEmoji,
                        fontSize = 28.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedServer.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (selectedServer.isPremium) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFA855F7).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "PRO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFA855F7),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${selectedServer.city}, ${selectedServer.country}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = " • ",
                                color = CyberBorderDark,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${selectedServer.pingMs} ms",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedServer.pingMs < 40) CyberEmerald else CyberAmber
                            )
                            Text(
                                text = " • ",
                                color = CyberBorderDark,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${selectedServer.loadPercentage}% load",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Change",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyberCyan
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Change Server",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Connection Telemetry Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Duration & Protocol row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SESSION DURATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatDuration(durationSeconds),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isConnected) CyberCyan else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TUNNEL PROTOCOL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            color = CyberCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = userSettings.selectedProtocol.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                androidx.compose.material3.HorizontalDivider(color = CyberBorderDark.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Live Traffic Speeds & Totals
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Download
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CyberEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download",
                                tint = CyberEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "DOWNLOAD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatRate(trafficStats.downloadRateBps),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = formatBytes(trafficStats.bytesDownloaded),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Upload
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CyberBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Upload",
                                tint = CyberBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "UPLOAD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatRate(trafficStats.uploadRateBps),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = formatBytes(trafficStats.bytesUploaded),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Public IP & Privacy Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorderDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isConnected) CyberEmerald.copy(alpha = 0.15f) else CyberRed.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.GppGood else Icons.Default.GppBad,
                            contentDescription = "Security Status",
                            tint = if (isConnected) CyberEmerald else CyberRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isConnected) "VIRTUAL VPN IP" else "EXPOSED PUBLIC IP",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) CyberEmerald else CyberRed,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isConnected) selectedServer.clientIp.substringBefore("/") else publicIpInfo.ip,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${publicIpInfo.isp} • ${publicIpInfo.country}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshIp,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh IP",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun VpnConnectionButton(
    connectionState: VpnConnectionState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionState is VpnConnectionState.Connected
    val isConnecting = connectionState is VpnConnectionState.Connecting

    val infiniteTransition = rememberInfiniteTransition(label = "btnPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarRotation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(200.dp)
            .testTag("vpn_power_button")
    ) {
        // Outer pulsing ring when connecting or connected
        if (isConnected || isConnecting) {
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .scale(if (isConnecting) pulseScale else 1f)
                    .clip(CircleShape)
                    .border(
                        width = 1.5.dp,
                        color = (if (isConnected) CyberEmerald else CyberAmber).copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )
        }

        // Radar rotating border when connecting
        if (isConnecting) {
            Box(
                modifier = Modifier
                    .size(175.dp)
                    .rotate(radarRotation)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, CyberAmber, Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Main Circular Shield Button
        val buttonGradient = when {
            isConnected -> Brush.radialGradient(
                listOf(Color(0xFF064E3B), Color(0xFF022C22), CyberSurfaceDark)
            )
            isConnecting -> Brush.radialGradient(
                listOf(Color(0xFF78350F), Color(0xFF451A03), CyberSurfaceDark)
            )
            else -> Brush.radialGradient(
                listOf(CyberSurfaceVariantDark, CyberSurfaceDark, Color(0xFF050B14))
            )
        }

        val borderColor = when {
            isConnected -> CyberEmerald
            isConnecting -> CyberAmber
            else -> CyberCyan.copy(alpha = 0.5f)
        }

        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(buttonGradient)
                .border(2.5.dp, borderColor, CircleShape)
                .clickable {
                    if (isConnected) onDisconnect() else if (!isConnecting) onConnect()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = when {
                        isConnected -> Icons.Default.Lock
                        isConnecting -> Icons.Default.Bolt
                        else -> Icons.Default.LockOpen
                    },
                    contentDescription = if (isConnected) "Disconnect" else "Connect",
                    tint = when {
                        isConnected -> CyberEmerald
                        isConnecting -> CyberAmber
                        else -> CyberCyan
                    },
                    modifier = Modifier.size(46.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        isConnected -> "DISCONNECT"
                        isConnecting -> "CONNECTING"
                        else -> "CONNECT"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = when {
                        isConnected -> CyberEmerald
                        isConnecting -> CyberAmber
                        else -> CyberCyan
                    }
                )
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024 * 1024))
        bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024))
        bytes >= 1024 -> String.format(Locale.US, "%.0f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}

private fun formatRate(bps: Long): String {
    return when {
        bps >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB/s", (bps / (1024.0 * 1024)))
        bps >= 1024 -> String.format(Locale.US, "%.0f KB/s", (bps / 1024.0))
        else -> "$bps B/s"
    }
}
