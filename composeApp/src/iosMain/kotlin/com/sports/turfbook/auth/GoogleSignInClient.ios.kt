package com.sports.turfbook.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// TODO: Integrate with Google Sign-In for iOS SDK (pod 'GoogleSignIn')
//       Requires GIDConfiguration with clientID in Info.plist and
//       URL scheme handler in AppDelegate.
private class IosGoogleSignInClient : GoogleSignInClient {
    override suspend fun signIn(): GoogleSignInOutcome =
        GoogleSignInOutcome.Failure("Google Sign-In not yet configured for iOS")
}

@Composable
actual fun rememberGoogleSignInClient(): GoogleSignInClient = remember { IosGoogleSignInClient() }
