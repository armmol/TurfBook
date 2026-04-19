package com.sports.turfbook.auth

import androidx.compose.runtime.Composable

sealed interface GoogleSignInOutcome {
    data class Success(
        val idToken: String,
        val email: String,
        val displayName: String?
    ) : GoogleSignInOutcome

    data class Failure(val error: String) : GoogleSignInOutcome
    data object Cancelled : GoogleSignInOutcome
}

interface GoogleSignInClient {
    suspend fun signIn(): GoogleSignInOutcome
}

@Composable
expect fun rememberGoogleSignInClient(): GoogleSignInClient
