package com.sports.turfbook.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand colours ───────────────────────────────────────────────────────────

val GreenPrimary      = Color(0xFF4CAF50)   // Turf green
val GreenDark         = Color(0xFF1B5E20)   // Deep grass
val GreenLight        = Color(0xFF81C784)   // Highlight
val GreenContainer    = Color(0xFF1E3A2A)   // Dark card surface
val BackgroundDark    = Color(0xFF0D1F14)   // Near-black green
val SurfaceDark       = Color(0xFF132019)
val OnSurfaceLight    = Color(0xFFE8F5E9)
val YellowAccent      = Color(0xFFFFD600)   // Stadium light / CTA accent
val ErrorRed          = Color(0xFFEF5350)

// ── Dark scheme (default — sports apps look better dark) ────────────────────

private val DarkColors = darkColorScheme(
    primary            = GreenPrimary,
    onPrimary          = Color.White,
    primaryContainer   = GreenContainer,
    onPrimaryContainer = GreenLight,
    secondary          = YellowAccent,
    onSecondary        = Color.Black,
    background         = BackgroundDark,
    onBackground       = OnSurfaceLight,
    surface            = SurfaceDark,
    onSurface          = OnSurfaceLight,
    surfaceVariant     = GreenContainer,
    onSurfaceVariant   = GreenLight,
    error              = ErrorRed,
    onError            = Color.White,
    outline            = GreenLight.copy(alpha = 0.4f)
)

// ── Light scheme (optional, not the default) ────────────────────────────────

private val LightColors = lightColorScheme(
    primary            = Color(0xFF2E7D32),
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFB8F0BA),
    onPrimaryContainer = Color(0xFF002106),
    secondary          = Color(0xFFF9A825),
    onSecondary        = Color.Black,
    background         = Color(0xFFF5FBF4),
    onBackground       = Color(0xFF0D1F14),
    surface            = Color.White,
    onSurface          = Color(0xFF0D1F14),
    error              = ErrorRed,
    onError            = Color.White
)

@Composable
fun TurfBookTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = TurfBookTypography,
        content     = content
    )
}
