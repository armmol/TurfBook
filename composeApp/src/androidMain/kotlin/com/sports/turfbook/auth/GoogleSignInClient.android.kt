package com.sports.turfbook.auth

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

private class AndroidGoogleSignInClient(private val context: Context) : GoogleSignInClient {

    // TODO: Replace with your Web Client ID from Google Cloud Console (APIs & Services → Credentials)
    //       Must match the SHA-1 fingerprint registered for this app.
    //       Example: "123456789-abc123def456.apps.googleusercontent.com"
    private val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"

    override suspend fun signIn(): GoogleSignInOutcome {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val token = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleSignInOutcome.Success(
                    idToken = token.idToken,
                    email = token.id,
                    displayName = token.displayName
                )
            } else {
                GoogleSignInOutcome.Failure("Unexpected credential type: ${credential.type}")
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleSignInOutcome.Cancelled
        } catch (e: Exception) {
            GoogleSignInOutcome.Failure(e.message ?: "Google sign-in failed")
        }
    }
}

@Composable
actual fun rememberGoogleSignInClient(): GoogleSignInClient {
    val context = LocalContext.current
    return remember(context) { AndroidGoogleSignInClient(context) }
}
