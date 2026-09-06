package com.kabadiwalaconnect.data.api

import android.content.Context

class ApiSessionRepository(context: Context) {
    private val preferences = context.getSharedPreferences("kabadiwala_backend_session", Context.MODE_PRIVATE)
    fun token(): String? = preferences.getString("jwt", null)
    fun saveToken(token: String) { preferences.edit().putString("jwt", token).apply() }
    fun clear() { preferences.edit().remove("jwt").apply() }
}
