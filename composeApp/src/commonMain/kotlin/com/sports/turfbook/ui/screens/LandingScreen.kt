package com.sports.turfbook.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sports.turfbook.ui.theme.*
import org.jetbrains.compose.resources.painterResource
import turfbook.composeapp.generated.resources.Res
import turfbook.composeapp.generated.resources.ic_logo
import turfbook.composeapp.generated.resources.ic_turf_hero

/**
 * Landing / onboarding screen shown after the splash.
 * Two CTAs: "Get Started" (new user) and "Log In" (returning user).
 */
@Composable
fun LandingScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    // Entry animations
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue  = if (visible) 1f else 0f,
        animationSpec = tween(700, easing = EaseOutCubic),
        label = "landingAlpha"
    )
    val slideY by animateFloatAsState(
        targetValue  = if (visible) 0f else 60f,
        animationSpec = tween(700, easing = EaseOutCubic),
        label = "landingSlide"
    )

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // ── Hero image (top ~55%) ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.58f)
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_turf_hero),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient overlay — fades hero into background colour
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.55f to BackgroundDark.copy(alpha = 0.4f),
                            1.0f to BackgroundDark
                        )
                    )
            )

            // Small logo badge top-left
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 20.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "TurfBook",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ── Bottom card panel ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .alpha(alpha)
                .offset(y = slideY.dp)
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Feature pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 28.dp)
            ) {
                FeaturePill("⚽  Football")
                FeaturePill("🏏  Cricket")
                FeaturePill("🏸  Badminton")
            }

            // Headline
            Text(
                text = "Your Next Game\nStarts Here",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    lineHeight = 46.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            // Subheadline
            Text(
                text = "Find and book premium turf slots near you\nin seconds. No calls, no hassle.",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceLight.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(36.dp))

            // Primary CTA
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor   = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            // Secondary CTA
            OutlinedButton(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, GreenPrimary.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GreenLight
                )
            ) {
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Trust indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TrustDot("🏟️", "500+ Turfs")
                Spacer(Modifier.width(20.dp))
                TrustDot("⚡", "Instant Booking")
                Spacer(Modifier.width(20.dp))
                TrustDot("🔒", "Secure Pay")
            }
        }
    }
}

@Composable
private fun FeaturePill(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(GreenContainer)
            .border(1.dp, GreenPrimary.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = GreenLight
        )
    }
}

@Composable
private fun TrustDot(emoji: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceLight.copy(alpha = 0.55f),
            fontSize = 11.sp
        )
    }
}
