package com.kabadiwalaconnect.data.auth

import android.app.Activity

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
