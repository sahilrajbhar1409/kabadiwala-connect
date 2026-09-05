package com.kabadiwalaconnect.data.model

data class User(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String? = null,
    val role: UserRole,
    val address: String? = null,
    val createdAt: String,
    val updatedAt: String
)

enum class UserRole {
    CITIZEN,
    COLLECTOR,
    RECYCLER,
    ADMIN
}
