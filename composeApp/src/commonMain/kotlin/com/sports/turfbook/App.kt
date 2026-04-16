package com.sports.turfbook

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import com.sports.turfbook.ui.navigation.Screen
import com.sports.turfbook.ui.screens.LandingScreen
import com.sports.turfbook.ui.screens.SplashScreen
import com.sports.turfbook.ui.theme.TurfBookTheme

@Composable
fun App() {
    TurfBookTheme(darkTheme = true) {

        var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                when {
                    // Splash → Landing: fade + slight slide up
                    initialState is Screen.Splash ->
                        fadeIn(tween(600)) + slideInVertically(
                            tween(600), initialOffsetY = { it / 8 }
                        ) togetherWith fadeOut(tween(300))

                    // Landing → Login (future): slide left
                    else ->
                        fadeIn(tween(400)) togetherWith fadeOut(tween(300))
                }
            },
            label = "screenTransition"
        ) { screen ->
            when (screen) {
                Screen.Splash  -> SplashScreen(onFinished = { currentScreen = Screen.Landing })
                Screen.Landing -> LandingScreen(
                    onGetStarted = { currentScreen = Screen.Login },
                    onLogin      = { currentScreen = Screen.Login }
                )
                Screen.Login   -> LandingScreen(   // placeholder until LoginScreen is built
                    onGetStarted = { currentScreen = Screen.Login },
                    onLogin      = { currentScreen = Screen.Login }
                )
                Screen.Home    -> LandingScreen(   // placeholder until HomeScreen is built
                    onGetStarted = {},
                    onLogin      = {}
                )
            }
        }
    }
}
