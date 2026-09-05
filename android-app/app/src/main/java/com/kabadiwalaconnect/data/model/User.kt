package com.kabadiwalaconnect.data.model

data class User(
    val uid: String,
    val name: String,
    val phoneNumber: String,
    val email: String? = null,
    val role: UserRole,
    val profileImageUrl: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: String,
    val updatedAt: String
)

enum class UserRole {
    CITIZEN,
    COLLECTOR,
    RECYCLER,
    ADMIN
}
