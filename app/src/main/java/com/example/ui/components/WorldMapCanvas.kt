package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberBorderDark
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.vpn.model.VpnServer
import kotlin.math.hypot

@Composable
fun WorldMapCanvas(
    servers: List<VpnServer>,
    selectedServer: VpnServer,
    isConnected: Boolean,
    onServerSelected: (VpnServer) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )
    val packetProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "packetProgress"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .pointerInput(servers) {
                detectTapGestures { tapOffset ->
                    val w = size.width
                    val h = size.height
                    // Find closest server within 36dp touch radius
                    servers.firstOrNull { s ->
                        if (s.latitude == 0f && s.longitude == 0f) return@firstOrNull false
                        val x = ((s.longitude + 180f) / 360f) * w
                        val y = ((90f - s.latitude) / 180f) * h
                        hypot((tapOffset.x - x).toDouble(), (tapOffset.y - y).toDouble()) < 48.0
                    }?.let { hitServer ->
                        onServerSelected(hitServer)
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // Draw futuristic cyber grid lines
        val gridColor = CyberBorderDark.copy(alpha = 0.35f)
        for (i in 1..5) {
            val y = (h / 6f) * i
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        }
        for (i in 1..8) {
            val x = (w / 9f) * i
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        }

        // Draw stylized continent cluster dots (simplified global map matrix)
        val matrixDots = listOf(
            // North America
            Offset(0.18f, 0.30f), Offset(0.22f, 0.35f), Offset(0.20f, 0.42f), Offset(0.26f, 0.38f),
            // South America
            Offset(0.29f, 0.65f), Offset(0.32f, 0.75f), Offset(0.30f, 0.85f),
            // Europe
            Offset(0.48f, 0.32f), Offset(0.51f, 0.28f), Offset(0.53f, 0.34f), Offset(0.49f, 0.38f),
            // Africa
            Offset(0.50f, 0.52f), Offset(0.53f, 0.62f), Offset(0.56f, 0.72f),
            // Asia
            Offset(0.68f, 0.30f), Offset(0.74f, 0.35f), Offset(0.80f, 0.38f), Offset(0.72f, 0.48f),
            Offset(0.78f, 0.55f),
            // Australia
            Offset(0.85f, 0.76f), Offset(0.88f, 0.80f)
        )

        for (dot in matrixDots) {
            drawCircle(
                color = Color(0xFF1E3557).copy(alpha = 0.6f),
                radius = 3.5f,
                center = Offset(dot.x * w, dot.y * h)
            )
        }

        // Home client reference position (Default Europe/US hub)
        val homeX = 0.22f * w
        val homeY = 0.38f * h

        // Draw Server Node Pins
        servers.forEach { server ->
            if (server.latitude == 0f && server.longitude == 0f) return@forEach
            // Equirectangular projection
            val sx = ((server.longitude + 180f) / 360f) * w
            val sy = ((90f - server.latitude) / 180f) * h
            val isSelected = server.id == selectedServer.id

            if (isSelected) {
                // Connecting line from client home to selected server
                val path = Path().apply {
                    moveTo(homeX, homeY)
                    quadraticTo(
                        (homeX + sx) / 2f,
                        ((homeY + sy) / 2f) - (h * 0.25f),
                        sx,
                        sy
                    )
                }

                val lineColor = if (isConnected) CyberEmerald else CyberCyan
                drawPath(
                    path = path,
                    color = lineColor.copy(alpha = if (isConnected) 0.85f else 0.45f),
                    style = Stroke(
                        width = 2.5f,
                        pathEffect = if (!isConnected) PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f) else null
                    )
                )

                // Animated cyber packet traveling along line when connected
                if (isConnected) {
                    val packetX = homeX + (sx - homeX) * packetProgress
                    val midY = ((homeY + sy) / 2f) - (h * 0.25f)
                    val t = packetProgress
                    val packetY = (1 - t) * (1 - t) * homeY + 2 * (1 - t) * t * midY + t * t * sy

                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(packetX, packetY)
                    )
                    drawCircle(
                        color = CyberEmerald.copy(alpha = 0.6f),
                        radius = 8f,
                        center = Offset(packetX, packetY)
                    )
                }

                // Radar pulse rings on active server
                drawCircle(
                    color = (if (isConnected) CyberEmerald else CyberCyan).copy(alpha = pulseAlpha),
                    radius = pulseRadius,
                    center = Offset(sx, sy),
                    style = Stroke(width = 2f)
                )

                // Active Node Core
                drawCircle(
                    color = if (isConnected) CyberEmerald else CyberCyan,
                    radius = 6.5f,
                    center = Offset(sx, sy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5f,
                    center = Offset(sx, sy)
                )
            } else {
                // Inactive Server Node
                val nodeColor = if (server.isPremium) Color(0xFFA855F7) else Color(0xFF64748B)
                drawCircle(
                    color = nodeColor.copy(alpha = 0.75f),
                    radius = 4f,
                    center = Offset(sx, sy)
                )
            }
        }

        // Home Client Node
        drawCircle(
            color = CyberCyan.copy(alpha = 0.9f),
            radius = 5f,
            center = Offset(homeX, homeY)
        )
        drawCircle(
            color = Color.White,
            radius = 2f,
            center = Offset(homeX, homeY)
        )
    }
}
