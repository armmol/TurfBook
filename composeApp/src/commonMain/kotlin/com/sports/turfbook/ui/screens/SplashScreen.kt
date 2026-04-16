package com.sports.turfbook.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.sports.turfbook.ui.theme.GreenDark
import com.sports.turfbook.ui.theme.GreenPrimary
import com.sports.turfbook.ui.theme.BackgroundDark
import com.sports.turfbook.ui.theme.YellowAccent
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.Image
import turfbook.composeapp.generated.resources.Res
import turfbook.composeapp.generated.resources.ic_logo

/**
 * Splash screen — shown for ~2.5 s on cold start.
 * Animates the logo in with a scale + fade, then calls [onFinished].
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {

    // ── Animation state ────────────────────────────────────────────────────
    var logoVisible by remember { mutableStateOf(false) }
    var textVisible by remember { mutableStateOf(false) }
    var taglineVisible by remember { mutableStateOf(false) }

    val logoAlpha by animateFloatAsState(
        targetValue  = if (logoVisible) 1f else 0f,
        animationSpec = tween(700, easing = EaseOutCubic),
        label = "logoAlpha"
    )
    val logoScale by animateFloatAsState(
        targetValue  = if (logoVisible) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "logoScale"
    )
    val textAlpha by animateFloatAsState(
        targetValue  = if (textVisible) 1f else 0f,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "textAlpha"
    )
    val taglineAlpha by animateFloatAsState(
        targetValue  = if (taglineVisible) 1f else 0f,
        animationSpec = tween(500, easing = EaseOutCubic),
        label = "taglineAlpha"
    )

    // Subtle pulse on the logo once it's visible
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue  = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // ── Sequenced launch ───────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        delay(150)
        logoVisible = true
        delay(500)
        textVisible = true
        delay(350)
        taglineVisible = true
        delay(1400)
        onFinished()
    }

    // ── UI ─────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        GreenDark,
                        BackgroundDark,
                        Color(0xFF060E09)
                    ),
                    radius = 900f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Glow ring behind logo
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .alpha(glowAlpha)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(GreenPrimary, Color.Transparent)
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )

                // Logo
                Image(
                    painter = painterResource(Res.drawable.ic_logo),
                    contentDescription = "TurfBook logo",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha)
                )
            }

            Spacer(Modifier.height(28.dp))

            // App name
            Text(
                text = "TurfBook",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                ),
                color = Color.White,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Book · Play · Win",
                style = MaterialTheme.typography.titleMedium,
                color = YellowAccent,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha)
            )
        }

        // Bottom version text
        Text(
            text = "v1.0",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.25f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(taglineAlpha)
        )
    }
}
