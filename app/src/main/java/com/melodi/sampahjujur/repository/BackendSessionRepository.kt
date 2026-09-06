package com.melodi.sampahjujur.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Singleton
class BackendSessionRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val _sessionInvalidated = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionInvalidated: SharedFlow<Unit> = _sessionInvalidated

    private val preferences = context.getSharedPreferences("backend_session", Context.MODE_PRIVATE)

    fun token(): String? {
        val encrypted = preferences.getString(KEY_TOKEN_CIPHERTEXT, null)
        val iv = preferences.getString(KEY_TOKEN_IV, null)
        if (!encrypted.isNullOrBlank() && !iv.isNullOrBlank()) {
            return try {
                decrypt(encrypted, iv)
            } catch (_: Exception) {
                clear()
                null
            }
        }

        // Migrate the previous unencrypted value once, then remove it.
        return preferences.getString(KEY_LEGACY_TOKEN, null)?.takeIf { it.isNotBlank() }?.also {
            saveToken(it)
        }
    }

    fun saveToken(token: String) {
        require(token.isNotBlank()) { "Backend token cannot be empty" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val ciphertext = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        preferences.edit()
            .putString(KEY_TOKEN_CIPHERTEXT, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
            .putString(KEY_TOKEN_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .remove(KEY_LEGACY_TOKEN)
            .apply()
    }

    fun clear() {
        preferences.edit()
            .remove(KEY_TOKEN_CIPHERTEXT)
            .remove(KEY_TOKEN_IV)
            .remove(KEY_LEGACY_TOKEN)
            .apply()
    }

    fun invalidate() {
        clear()
        _sessionInvalidated.tryEmit(Unit)
    }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generator.generateKey()
    }

    private fun decrypt(ciphertext: String, iv: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            key(),
            GCMParameterSpec(GCM_TAG_LENGTH, Base64.decode(iv, Base64.NO_WRAP))
        )
        return cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP)).toString(Charsets.UTF_8)
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "kabadiwala_backend_session"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val KEY_TOKEN_CIPHERTEXT = "jwt_ciphertext"
        private const val KEY_TOKEN_IV = "jwt_iv"
        private const val KEY_LEGACY_TOKEN = "jwt"
    }
}
