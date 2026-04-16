package com.sports.turfbook.ui.navigation

/**
 * Lightweight screen identifier — no nav library required for the initial
 * splash → landing → auth flow.  Replace with full NavHost once we have
 * more routes.
 */
sealed interface Screen {
    data object Splash   : Screen
    data object Landing  : Screen
    data object Login    : Screen
    data object Home     : Screen
}
