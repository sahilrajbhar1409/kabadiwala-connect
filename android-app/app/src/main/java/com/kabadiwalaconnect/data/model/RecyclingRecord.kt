package com.kabadiwalaconnect.data.model

data class RecyclingRecord(
    val id: String,
    val lotId: String,
    val recyclerId: String,
    val materialId: String,
    val processedWeight: Double,
    val recycledAt: String,
    val status: RecyclingStatus,
    val createdAt: String,
    val updatedAt: String
)

enum class RecyclingStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}
