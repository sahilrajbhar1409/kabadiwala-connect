package com.kabadiwalaconnect.data.backend

import android.content.Context

class BackendSessionRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun token(): String? = preferences.getString(TOKEN_KEY, null)

    fun saveToken(token: String) {
        preferences.edit().putString(TOKEN_KEY, token).apply()
    }

    fun clear() {
        preferences.edit().remove(TOKEN_KEY).apply()
    }

    private companion object {
        const val PREFERENCES = "kabadiwala_backend_session"
        const val TOKEN_KEY = "jwt"
    }
}
