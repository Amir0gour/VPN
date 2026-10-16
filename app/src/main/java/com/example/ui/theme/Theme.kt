package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF031623),
    primaryContainer = Color(0xFF003847),
    onPrimaryContainer = Color(0xFFBCEFFF),
    secondary = CyberEmerald,
    onSecondary = Color(0xFF003822),
    secondaryContainer = Color(0xFF005333),
    onSecondaryContainer = Color(0xFFA6F2CE),
    tertiary = CyberBlue,
    background = CyberDarkBg,
    onBackground = CyberTextPrimaryDark,
    surface = CyberSurfaceDark,
    onSurface = CyberTextPrimaryDark,
    surfaceVariant = CyberSurfaceVariantDark,
    onSurfaceVariant = CyberTextSecondaryDark,
    outline = CyberBorderDark,
    error = CyberRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00687A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB0ECFA),
    onPrimaryContainer = Color(0xFF001F26),
    secondary = Color(0xFF006C47),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF8CF5C2),
    onSecondaryContainer = Color(0xFF002113),
    tertiary = CyberBlue,
    background = CyberLightBg,
    onBackground = CyberTextPrimaryLight,
    surface = CyberSurfaceLight,
    onSurface = CyberTextPrimaryLight,
    surfaceVariant = CyberSurfaceVariantLight,
    onSurfaceVariant = CyberTextSecondaryLight,
    outline = CyberBorderLight,
    error = CyberRed,
    onError = Color.White
)

@Composable
fun NovaVpnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
