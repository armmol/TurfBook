package com.sports.turfbook.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sports.turfbook.ui.theme.*

/**
 * Landing / onboarding screen.
 * Background: dark gradient with faint geometric pitch lines drawn on canvas —
 * subtle enough to stay out of the way of the copy.
 */
@Composable
fun LandingScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit
) {
    // Entry animation
    var visible by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue  = if (visible) 1f else 0f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "alpha"
    )
    val contentSlide by animateFloatAsState(
        targetValue  = if (visible) 0f else 40f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "slide"
    )

    // Slow drift on the background geometry
    val infiniteTransition = rememberInfiniteTransition(label = "drift")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to Color(0xFF0B1C12),
                    0.6f to Color(0xFF0F2318),
                    1.0f to BackgroundDark
                )
            )
            .drawBehind { pitchLines(drift) }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ── Logo row ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simple wordmark dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(GreenPrimary, shape = RoundedCornerShape(50))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "TurfBook",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── Centre copy ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .alpha(contentAlpha)
                    .offset(y = contentSlide.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Your Next Game\nStarts Here.",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        lineHeight  = 46.sp
                    ),
                    color = Color.White
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Find and book premium turf slots near you — no calls, no hassle.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceLight.copy(alpha = 0.55f),
                    lineHeight = 23.sp
                )

                Spacer(Modifier.height(28.dp))

                // Sport chips — scrollable row
                val sports = listOf(
                    "⚽" to "Football",
                    "🏏" to "Cricket",
                    "🏸" to "Badminton",
                    "🥒" to "Pickleball",
                    "🏀" to "Basketball",
                    "🏐" to "Volleyball",
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    sports.forEach { (emoji, name) ->
                        SportChip(emoji, name)
                    }
                }
            }

            // ── CTA block ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .alpha(contentAlpha)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Trust row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    TrustItem("500+", "Turfs")
                    VerticalDivider()
                    TrustItem("Instant", "Booking")
                    VerticalDivider()
                    TrustItem("Secure", "Payments")
                }

                Button(
                    onClick    = onGetStarted,
                    modifier   = Modifier.fillMaxWidth().height(54.dp),
                    shape      = RoundedCornerShape(14.dp),
                    colors     = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor   = Color.White
                    ),
                    elevation  = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        "Get Started",
                        style  = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape    = RoundedCornerShape(14.dp),
                    border   = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        "I already have an account",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

// ── Subtle pitch-line background ─────────────────────────────────────────────

/**
 * Draws faint football-pitch geometry directly on the canvas.
 * [progress] (0..1) drives a slow parallax drift so the lines breathe.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.pitchLines(progress: Float) {
    val lineColor = Color(0xFF4CAF50).copy(alpha = 0.07f)
    val stroke = Stroke(width = 1.2f)

    val w = size.width
    val h = size.height

    // Slow vertical drift: lines float up by up to 40px over the cycle
    val dy = progress * 40f

    // Outer boundary rectangle (perspective foreshortened)
    val left   = w * 0.08f
    val right  = w * 0.92f
    val top    = h * 0.12f - dy
    val bottom = h * 0.85f - dy

    drawRect(
        color    = lineColor,
        topLeft  = Offset(left, top),
        size     = androidx.compose.ui.geometry.Size(right - left, bottom - top),
        style    = stroke
    )

    // Centre line (horizontal)
    val midY = (top + bottom) / 2f
    drawLine(lineColor, Offset(left, midY), Offset(right, midY), strokeWidth = 1.2f)

    // Centre circle
    val cx = w / 2f
    drawCircle(
        color  = lineColor,
        radius = (right - left) * 0.18f,
        center = Offset(cx, midY),
        style  = stroke
    )

    // Centre spot
    drawCircle(
        color  = Color(0xFF4CAF50).copy(alpha = 0.14f),
        radius = 5f,
        center = Offset(cx, midY)
    )

    // Left penalty box
    val pBoxW = (right - left) * 0.22f
    val pBoxH = (bottom - top) * 0.38f
    val pBoxTop = midY - pBoxH / 2f
    drawRect(
        color    = lineColor,
        topLeft  = Offset(left, pBoxTop),
        size     = androidx.compose.ui.geometry.Size(pBoxW, pBoxH),
        style    = stroke
    )

    // Right penalty box
    drawRect(
        color    = lineColor,
        topLeft  = Offset(right - pBoxW, pBoxTop),
        size     = androidx.compose.ui.geometry.Size(pBoxW, pBoxH),
        style    = stroke
    )

    // Soft radial glow at centre
    drawCircle(
        brush  = Brush.radialGradient(
            colors = listOf(Color(0xFF4CAF50).copy(alpha = 0.06f), Color.Transparent),
            center = Offset(cx, midY),
            radius = w * 0.55f
        ),
        radius = w * 0.55f,
        center = Offset(cx, midY)
    )
}

// ── Small components ─────────────────────────────────────────────────────────

@Composable
private fun SportChip(emoji: String, name: String) {
    Row(
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(50)
            )
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(emoji, fontSize = 14.sp)
        Text(
            text  = name,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.80f)
        )
    }
}

@Composable
private fun TrustItem(value: String, label: String) {
    Column {
        Text(
            text  = value,
            style = MaterialTheme.typography.titleMedium,
            color = GreenLight,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceLight.copy(alpha = 0.45f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun RowScope.VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(Color.White.copy(alpha = 0.10f))
    )
}
