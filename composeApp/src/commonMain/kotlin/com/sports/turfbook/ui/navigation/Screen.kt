package com.sports.turfbook.ui.navigation

sealed interface Screen {
    data object Splash   : Screen
    data object Landing  : Screen
    data object SignUp   : Screen
    data object Login    : Screen
    /**
     * [isLinking] = true  → user signed in with Google and must now attach a phone (KYC)
     * [isLinking] = false → user is authenticating via phone number only
     */
    data class PhoneVerify(val isLinking: Boolean) : Screen
    data object Home     : Screen
}
