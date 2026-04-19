package com.sports.turfbook.ui.screens.auth

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.sports.turfbook.auth.GoogleSignInOutcome
import com.sports.turfbook.auth.rememberGoogleSignInClient
import com.sports.turfbook.ui.theme.*
import kotlinx.coroutines.launch

enum class AuthMode { SignUp, Login }

@Composable
fun AuthScreen(
    mode: AuthMode = AuthMode.SignUp,
    onGoogleSuccess: (idToken: String, email: String, displayName: String?) -> Unit,
    onPhoneInstead: () -> Unit,
    onBack: () -> Unit,
    onSwitchMode: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "alpha"
    )
    val slide by animateFloatAsState(
        targetValue = if (visible) 0f else 32f,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "slide"
    )
    LaunchedEffect(Unit) { visible = true }

    val googleClient = rememberGoogleSignInClient()
    val scope = rememberCoroutineScope()

    var isGoogleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to Color(0xFF0B1C12),
                    0.5f to Color(0xFF0F2318),
                    1.0f to BackgroundDark
                )
            )
            .drawBehind { subtlePitchLines() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp)
        ) {

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackArrowButton(onClick = onBack)
                Spacer(Modifier.weight(1f))
                // Wordmark
                Text(
                    "TurfBook",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.weight(1f))

            // ── Headline ──────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .alpha(alpha)
                    .offset(y = slide.dp)
            ) {
                Text(
                    text = if (mode == AuthMode.SignUp) "Create your\naccount" else "Welcome\nback",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        lineHeight = 42.sp
                    ),
                    color = Color.White
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (mode == AuthMode.SignUp)
                        "Book turf slots in seconds — no calls, no hassle."
                    else
                        "Sign in to manage your bookings.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceLight.copy(alpha = 0.5f),
                    lineHeight = 22.sp
                )
            }

            Spacer(Modifier.weight(1f))

            // ── Auth buttons ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .alpha(alpha)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Error message
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ErrorRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }

                // Google button
                Button(
                    onClick = {
                        if (!isGoogleLoading) {
                            scope.launch {
                                isGoogleLoading = true
                                errorMessage = null
                                when (val result = googleClient.signIn()) {
                                    is GoogleSignInOutcome.Success ->
                                        onGoogleSuccess(result.idToken, result.email, result.displayName)
                                    is GoogleSignInOutcome.Failure ->
                                        errorMessage = result.error
                                    GoogleSignInOutcome.Cancelled -> Unit
                                }
                                isGoogleLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F1F1F)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF1F1F1F),
                            strokeWidth = 2.dp
                        )
                    } else {
                        GoogleGLogo()
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (mode == AuthMode.SignUp) "Continue with Google" else "Sign in with Google",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.12f)
                    )
                    Text(
                        "or",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.35f)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color.White.copy(alpha = 0.12f)
                    )
                }

                // Phone button
                OutlinedButton(
                    onClick = onPhoneInstead,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text = if (mode == AuthMode.SignUp) "Sign up with phone number" else "Sign in with phone number",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Switch mode + terms
                if (mode == AuthMode.SignUp) {
                    Text(
                        text = "By continuing, you agree to our Terms of Service and Privacy Policy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceLight.copy(alpha = 0.35f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (mode == AuthMode.SignUp) "Already have an account? " else "New to TurfBook? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceLight.copy(alpha = 0.45f)
                    )
                    TextButton(
                        onClick = onSwitchMode,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (mode == AuthMode.SignUp) "Sign in" else "Create account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GreenLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ── Back button ───────────────────────────────────────────────────────────────

@Composable
internal fun BackArrowButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Text(
            text = "←",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.65f)
        )
    }
}

// ── Google "G" logo — four-colour arc approximation ──────────────────────────

@Composable
private fun GoogleGLogo() {
    Box(
        modifier = Modifier
            .size(22.dp)
            .drawBehind {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.minDimension / 2f
                val sw = size.width * 0.18f

                // Blue arc (top)
                drawArc(Color(0xFF4285F4), -60f, 150f, false,
                    topLeft = Offset(cx - r, cy - r),
                    size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                    style = Stroke(width = sw))
                // Red arc (top-left)
                drawArc(Color(0xFFEA4335), 90f, 90f, false,
                    topLeft = Offset(cx - r, cy - r),
                    size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                    style = Stroke(width = sw))
                // Yellow arc (bottom-left)
                drawArc(Color(0xFFFBBC05), 180f, 90f, false,
                    topLeft = Offset(cx - r, cy - r),
                    size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                    style = Stroke(width = sw))
                // Green arc (bottom)
                drawArc(Color(0xFF34A853), 270f, 60f, false,
                    topLeft = Offset(cx - r, cy - r),
                    size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                    style = Stroke(width = sw))

                // Right bar of the "G" cutout
                val barTop = cy - sw * 0.5f
                val barBottom = cy + sw * 0.5f
                drawRect(
                    color = Color(0xFF4285F4),
                    topLeft = Offset(cx, barTop),
                    size = androidx.compose.ui.geometry.Size(r - sw * 0.5f, barBottom - barTop)
                )
            }
    )
}

// ── Reusable faint pitch-line background ─────────────────────────────────────

private fun androidx.compose.ui.graphics.drawscope.DrawScope.subtlePitchLines() {
    val lineColor = Color(0xFF4CAF50).copy(alpha = 0.05f)
    val stroke = Stroke(width = 1f)
    val w = size.width
    val h = size.height
    val left = w * 0.08f; val right = w * 0.92f
    val top = h * 0.10f; val bottom = h * 0.82f
    drawRect(color = lineColor,
        topLeft = Offset(left, top),
        size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
        style = stroke)
    val midY = (top + bottom) / 2f
    drawLine(lineColor, Offset(left, midY), Offset(right, midY), strokeWidth = 1f)
    drawCircle(color = lineColor, radius = (right - left) * 0.18f,
        center = Offset(w / 2f, midY), style = stroke)
}
