package com.kabadiwalaconnect.data.profile

import com.kabadiwalaconnect.data.model.User

interface UserProfileRepository {
    fun getProfile(uid: String, callback: (Result<User?>) -> Unit)

    fun saveProfile(profile: User, callback: (Result<Unit>) -> Unit)

    fun updateProfile(profile: User, callback: (Result<Unit>) -> Unit) =
        saveProfile(profile, callback)
}
