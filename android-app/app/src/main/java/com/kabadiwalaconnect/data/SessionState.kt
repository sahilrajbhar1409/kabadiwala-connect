package com.kabadiwalaconnect.data

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

    fun signInAs(newRole: UserRole) {
        role = newRole
    }

    fun signOut() {
        role = UserRole.CITIZEN
    }
}
