package com.kabadiwalaconnect.data.auth

import android.app.Activity
import android.content.Context
import android.content.Intent

data class AuthenticatedUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val phoneNumber: String?
)

interface AuthRepository {
    fun currentUser(): AuthenticatedUser?

    fun signInWithGoogleIdToken(
        idToken: String,
        callback: (Result<AuthenticatedUser>) -> Unit
    )

    fun googleSignInIntent(context: Context): Intent

    fun startPhoneVerification(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onVerificationCompleted: (AuthenticatedUser) -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun verifyPhoneCode(
        verificationId: String,
        code: String,
        callback: (Result<AuthenticatedUser>) -> Unit
    )

    fun signOut()
}
