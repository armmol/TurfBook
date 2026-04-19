package com.sports.turfbook.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sports.turfbook.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class PhoneStep { EnterPhone, EnterOtp }

@Composable
fun PhoneVerifyScreen(
    isLinking: Boolean,
    onVerified: (phone: String) -> Unit,
    onBack: () -> Unit
) {
    var step by remember { mutableStateOf(PhoneStep.EnterPhone) }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var resendCountdown by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()

    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500, easing = EaseOutCubic),
        label = "alpha"
    )
    LaunchedEffect(Unit) { visible = true }

    fun startResendTimer() {
        resendCountdown = 30
        scope.launch {
            while (resendCountdown > 0) {
                delay(1000)
                resendCountdown--
            }
        }
    }

    fun sendOtp() {
        val digits = phone.filter { it.isDigit() }
        if (digits.length != 10) {
            error = "Enter a valid 10-digit mobile number"
            return
        }
        scope.launch {
            isLoading = true
            error = null
            // TODO: POST /api/v1/auth/otp/request  body = { phone: "+91$digits" }
            delay(800) // simulated network delay
            isLoading = false
            step = PhoneStep.EnterOtp
            startResendTimer()
        }
    }

    fun verifyOtp() {
        if (otp.length != 6) {
            error = "Enter the 6-digit OTP"
            return
        }
        scope.launch {
            isLoading = true
            error = null
            // TODO: POST /api/v1/auth/otp/verify  body = { phone: "+91${phone.filter { it.isDigit() }}", otp }
            delay(800)
            isLoading = false
            onVerified("+91${phone.filter { it.isDigit() }}")
        }
    }

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
                BackArrowButton(onClick = {
                    if (step == PhoneStep.EnterOtp) {
                        step = PhoneStep.EnterPhone
                        otp = ""
                        error = null
                    } else {
                        onBack()
                    }
                })
            }

            Spacer(Modifier.height(24.dp))

            // ── Step indicator ────────────────────────────────────────────────
            if (isLinking) {
                StepIndicator(
                    current = if (step == PhoneStep.EnterPhone) 1 else 2,
                    total = 2
                )
                Spacer(Modifier.height(24.dp))
            }

            // ── Animated content per step ─────────────────────────────────────
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    slideInHorizontally { it / 2 } + fadeIn(tween(300)) togetherWith
                    slideOutHorizontally { -it / 2 } + fadeOut(tween(200))
                },
                label = "step"
            ) { currentStep ->
                Column(modifier = Modifier.alpha(alpha)) {
                    when (currentStep) {
                        PhoneStep.EnterPhone -> PhoneEntryContent(
                            phone = phone,
                            onPhoneChange = {
                                phone = it.filter { c -> c.isDigit() }.take(10)
                                error = null
                            },
                            isLinking = isLinking,
                            isLoading = isLoading,
                            error = error,
                            onSend = ::sendOtp
                        )

                        PhoneStep.EnterOtp -> OtpEntryContent(
                            phone = phone,
                            otp = otp,
                            onOtpChange = {
                                otp = it.filter { c -> c.isDigit() }.take(6)
                                error = null
                                if (otp.length == 6) verifyOtp()
                            },
                            isLoading = isLoading,
                            error = error,
                            resendCountdown = resendCountdown,
                            onResend = {
                                otp = ""
                                sendOtp()
                            },
                            onVerify = ::verifyOtp
                        )
                    }
                }
            }
        }
    }
}

// ── Phone entry step ──────────────────────────────────────────────────────────

@Composable
private fun PhoneEntryContent(
    phone: String,
    onPhoneChange: (String) -> Unit,
    isLinking: Boolean,
    isLoading: Boolean,
    error: String?,
    onSend: () -> Unit
) {
    Column {
        Text(
            text = if (isLinking) "Link your phone number" else "Enter your phone number",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp
            ),
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isLinking)
                "We need your mobile number to verify your identity (KYC). This is a one-time step."
            else
                "We'll send you a one-time password to sign you in.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceLight.copy(alpha = 0.5f),
            lineHeight = 21.sp
        )

        Spacer(Modifier.height(36.dp))

        // Phone input
        Text(
            "Mobile number",
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceLight.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        PhoneInputField(
            value = phone,
            onValueChange = onPhoneChange,
            onDone = onSend
        )

        // Error
        error?.let {
            Spacer(Modifier.height(10.dp))
            Text(
                text = it,
                color = ErrorRed,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onSend,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = phone.length == 10 && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                disabledContainerColor = GreenPrimary.copy(alpha = 0.35f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Send OTP", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── OTP entry step ────────────────────────────────────────────────────────────

@Composable
private fun OtpEntryContent(
    phone: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    resendCountdown: Int,
    onResend: () -> Unit,
    onVerify: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column {
        Text(
            "Verify your number",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp
            ),
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        Text(
            buildString {
                append("OTP sent to ")
                append("+91 ")
                append(phone.take(5))
                append(" ")
                append(phone.takeLast(5))
            },
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceLight.copy(alpha = 0.5f)
        )

        Spacer(Modifier.height(40.dp))

        OtpInputField(
            value = otp,
            onValueChange = onOtpChange,
            focusRequester = focusRequester
        )

        // Error
        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text = it,
                color = ErrorRed,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = otp.length == 6 && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                disabledContainerColor = GreenPrimary.copy(alpha = 0.35f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Verify & Continue", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Resend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Didn't receive it? ",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceLight.copy(alpha = 0.4f)
            )
            if (resendCountdown > 0) {
                Text(
                    "Resend in ${resendCountdown}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceLight.copy(alpha = 0.35f)
                )
            } else {
                TextButton(
                    onClick = onResend,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Resend OTP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GreenLight,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── Phone input field ─────────────────────────────────────────────────────────

@Composable
private fun PhoneInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit
) {
    val focused = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.5.dp,
                color = if (focused.value) GreenPrimary else Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(Color.White.copy(alpha = 0.04f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Country code pill
        Box(
            modifier = Modifier
                .padding(start = 14.dp, top = 16.dp, bottom = 16.dp)
                .background(GreenContainer, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                "🇮🇳  +91",
                style = MaterialTheme.typography.bodyMedium,
                color = GreenLight,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.width(10.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp, top = 16.dp, bottom = 16.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            cursorBrush = SolidColor(GreenPrimary),
            singleLine = true,
            decorationBox = { innerField ->
                if (value.isEmpty()) {
                    Text(
                        "98765 43210",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            letterSpacing = 1.5.sp
                        ),
                        color = Color.White.copy(alpha = 0.2f)
                    )
                }
                innerField()
            }
        )
    }
}

// ── OTP 6-box input ───────────────────────────────────────────────────────────

@Composable
private fun OtpInputField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        cursorBrush = SolidColor(Color.Transparent),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(6) { index ->
                    val char = value.getOrNull(index)?.toString() ?: ""
                    val isCurrent = index == value.length

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.85f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = when {
                                    char.isNotEmpty() -> GreenPrimary.copy(alpha = 0.8f)
                                    isCurrent -> GreenPrimary
                                    else -> Color.White.copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(
                                if (char.isNotEmpty()) GreenPrimary.copy(alpha = 0.08f)
                                else Color.White.copy(alpha = 0.03f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}

// ── Step indicator ────────────────────────────────────────────────────────────

@Composable
private fun StepIndicator(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { index ->
            val active = index + 1 <= current
            Box(
                modifier = Modifier
                    .height(3.dp)
                    .width(if (active) 28.dp else 16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (active) GreenPrimary else Color.White.copy(alpha = 0.2f))
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "Step $current of $total",
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceLight.copy(alpha = 0.4f)
        )
    }
}
