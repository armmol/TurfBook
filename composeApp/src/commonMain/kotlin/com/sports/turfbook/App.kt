package com.sports.turfbook

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import com.sports.turfbook.ui.navigation.Screen
import com.sports.turfbook.ui.screens.LandingScreen
import com.sports.turfbook.ui.screens.SplashScreen
import com.sports.turfbook.ui.screens.auth.AuthMode
import com.sports.turfbook.ui.screens.auth.AuthScreen
import com.sports.turfbook.ui.screens.auth.PhoneVerifyScreen
import com.sports.turfbook.ui.theme.TurfBookTheme

@Composable
fun App() {
    TurfBookTheme(darkTheme = true) {

        var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                when {
                    initialState is Screen.Splash ->
                        fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 8 } togetherWith
                        fadeOut(tween(300))

                    // Slide left for forward navigation
                    targetState is Screen.SignUp || targetState is Screen.Login ||
                    targetState is Screen.PhoneVerify ->
                        slideInHorizontally(tween(350)) { it / 3 } + fadeIn(tween(350)) togetherWith
                        slideOutHorizontally(tween(300)) { -it / 6 } + fadeOut(tween(200))

                    // Slide right for back navigation (to Landing)
                    targetState is Screen.Landing ->
                        slideInHorizontally(tween(350)) { -it / 3 } + fadeIn(tween(350)) togetherWith
                        slideOutHorizontally(tween(300)) { it / 6 } + fadeOut(tween(200))

                    else ->
                        fadeIn(tween(400)) togetherWith fadeOut(tween(300))
                }
            },
            label = "screenTransition"
        ) { screen ->
            when (screen) {

                Screen.Splash -> SplashScreen(
                    onFinished = { currentScreen = Screen.Landing }
                )

                Screen.Landing -> LandingScreen(
                    onGetStarted = { currentScreen = Screen.SignUp },
                    onLogin      = { currentScreen = Screen.Login }
                )

                Screen.SignUp -> AuthScreen(
                    mode = AuthMode.SignUp,
                    onGoogleSuccess = { _, _, _ ->
                        // Google sign-in succeeded — phone KYC always required for new users.
                        // TODO: call POST /api/v1/auth/google with idToken; if server says
                        //       user.phone != null, skip PhoneVerify and go straight to Home.
                        currentScreen = Screen.PhoneVerify(isLinking = true)
                    },
                    onPhoneInstead = { currentScreen = Screen.PhoneVerify(isLinking = false) },
                    onBack         = { currentScreen = Screen.Landing },
                    onSwitchMode   = { currentScreen = Screen.Login }
                )

                Screen.Login -> AuthScreen(
                    mode = AuthMode.Login,
                    onGoogleSuccess = { _, _, _ ->
                        // TODO: call POST /api/v1/auth/google; if server says user.phone == null
                        //       (edge case — existing user without phone) send to PhoneVerify.
                        currentScreen = Screen.Home
                    },
                    onPhoneInstead = { currentScreen = Screen.PhoneVerify(isLinking = false) },
                    onBack         = { currentScreen = Screen.Landing },
                    onSwitchMode   = { currentScreen = Screen.SignUp }
                )

                is Screen.PhoneVerify -> PhoneVerifyScreen(
                    isLinking  = screen.isLinking,
                    onVerified = { _ ->
                        // TODO: if isLinking, call POST /api/v1/user/link-phone;
                        //       store the JWT tokens from the response.
                        currentScreen = Screen.Home
                    },
                    onBack = {
                        currentScreen = if (screen.isLinking) Screen.SignUp else Screen.Landing
                    }
                )

                Screen.Home -> LandingScreen(   // placeholder until HomeScreen is built
                    onGetStarted = {},
                    onLogin      = {}
                )
            }
        }
    }
}
