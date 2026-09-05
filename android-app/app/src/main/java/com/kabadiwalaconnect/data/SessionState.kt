package com.kabadiwalaconnect.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kabadiwalaconnect.data.model.UserRole

/**
 * Lightweight session used by the prototype until authentication is backed by
 * the API. Keeping the role here lets both citizen and collector flows share
 * the same navigation entry point.
 */
object SessionState {
    var role by mutableStateOf(UserRole.CITIZEN)
        private set

    const val CITIZEN_ID = "citizen-session"
    const val COLLECTOR_ID = "collector-session"
    const val RECYCLER_ID = "recycler-session"

    fun signInAs(newRole: UserRole) {
        role = newRole
    }

    fun signOut() {
        role = UserRole.CITIZEN
    }

    fun savedRole(context: Context, uid: String): UserRole? {
        val value = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .getString(roleKey(uid), null)
        return value?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
    }

    fun persistRole(context: Context, uid: String, newRole: UserRole) {
        signInAs(newRole)
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(roleKey(uid), newRole.name)
            .apply()
    }

    fun clearPersistedSession(context: Context) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().clear().apply()
        signOut()
    }

    private fun roleKey(uid: String) = "role_$uid"

    private const val PREFERENCES = "kabadiwala_session"
}
