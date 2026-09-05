package com.kabadiwalaconnect.data.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.util.concurrent.TimeUnit

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override fun currentUser(): AuthenticatedUser? = auth.currentUser?.toAuthenticatedUser()

    override fun signInWithGoogleIdToken(
        idToken: String,
        callback: (Result<AuthenticatedUser>) -> Unit
    ) {
        if (idToken.isBlank()) {
            callback(Result.failure(IllegalArgumentException("Google ID token cannot be empty")))
            return
        }

        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user == null) {
                        callback(Result.failure(IllegalStateException("Firebase returned no signed-in user")))
                    } else {
                        callback(Result.success(user.toAuthenticatedUser()))
                    }
                } else {
                    callback(Result.failure(task.exception ?: IllegalStateException("Google sign-in failed")))
                }
            }

    }

    override fun googleSignInIntent(context: Context): Intent {
        val resourceId = context.resources.getIdentifier(
            "default_web_client_id", "string", context.packageName
        )
        require(resourceId != 0) {
            "Firebase Google sign-in client ID is missing from google-services configuration"
        }
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(resourceId))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, options).signInIntent
    }

    override fun startPhoneVerification(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onVerificationCompleted: (AuthenticatedUser) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (phoneNumber.isBlank()) {
            onFailure(IllegalArgumentException("Phone number cannot be empty"))
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user == null) {
                            onFailure(IllegalStateException("Firebase returned no signed-in user"))
                        } else {
                            onVerificationCompleted(user.toAuthenticatedUser())
                        }
                    } else {
                        onFailure(task.exception ?: IllegalStateException("Phone sign-in failed"))
                    }
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                onFailure(exception)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        }

        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
        )
    }

    override fun verifyPhoneCode(
        verificationId: String,
        code: String,
        callback: (Result<AuthenticatedUser>) -> Unit
    ) {
        if (verificationId.isBlank() || code.isBlank()) {
            callback(Result.failure(IllegalArgumentException("Verification ID and code are required")))
            return
        }

        auth.signInWithCredential(
            PhoneAuthProvider.getCredential(verificationId, code)
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = task.result?.user
                if (user == null) {
                    callback(Result.failure(IllegalStateException("Firebase returned no signed-in user")))
                } else {
                    callback(Result.success(user.toAuthenticatedUser()))
                }
            } else {
                callback(Result.failure(task.exception ?: IllegalStateException("Phone sign-in failed")))
            }
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    private fun FirebaseUser.toAuthenticatedUser() = AuthenticatedUser(
        uid = uid,
        displayName = displayName,
        email = email,
        phoneNumber = phoneNumber
    )
}
