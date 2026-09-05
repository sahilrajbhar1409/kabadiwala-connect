package com.kabadiwalaconnect.data.profile

import com.google.firebase.firestore.FirebaseFirestore
import com.kabadiwalaconnect.data.model.User
import com.kabadiwalaconnect.data.model.UserRole

class FirebaseUserProfileRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserProfileRepository {

    override fun getProfile(uid: String, callback: (Result<User?>) -> Unit) {
        if (uid.isBlank()) {
            callback(Result.failure(IllegalArgumentException("User ID cannot be empty")))
            return
        }

        firestore.collection(USERS_COLLECTION).document(uid).get()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    callback(Result.failure(task.exception ?: IllegalStateException("Profile lookup failed")))
                    return@addOnCompleteListener
                }

                val snapshot = task.result
                if (snapshot == null || !snapshot.exists()) {
                    callback(Result.success(null))
                    return@addOnCompleteListener
                }

                callback(runCatching { Result.success(snapshot.toUser()) }
                    .getOrElse { Result.failure(it) })
            }
    }

    override fun saveProfile(profile: User, callback: (Result<Unit>) -> Unit) {
        if (profile.uid.isBlank()) {
            callback(Result.failure(IllegalArgumentException("User ID cannot be empty")))
            return
        }

        firestore.collection(USERS_COLLECTION).document(profile.uid)
            .set(profile.toFirestoreMap())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(Result.success(Unit))
                } else {
                    callback(Result.failure(task.exception ?: IllegalStateException("Profile save failed")))
                }
            }
    }

    private fun User.toFirestoreMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "name" to name,
        "email" to email,
        "phoneNumber" to phoneNumber,
        "role" to role.name,
        "profileImageUrl" to profileImageUrl,
        "address" to address,
        "latitude" to latitude,
        "longitude" to longitude,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User {
        val roleName = getString("role")
            ?: throw IllegalStateException("Profile is missing a role")
        return User(
            uid = getString("uid") ?: id,
            name = getString("name").orEmpty(),
            phoneNumber = getString("phoneNumber").orEmpty(),
            email = getString("email"),
            role = UserRole.valueOf(roleName),
            profileImageUrl = getString("profileImageUrl"),
            address = getString("address"),
            latitude = getDouble("latitude"),
            longitude = getDouble("longitude"),
            createdAt = getString("createdAt").orEmpty(),
            updatedAt = getString("updatedAt").orEmpty()
        )
    }

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
