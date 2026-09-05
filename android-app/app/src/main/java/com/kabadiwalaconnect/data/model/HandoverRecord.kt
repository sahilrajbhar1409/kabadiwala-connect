package com.kabadiwalaconnect.data.model

data class HandoverRecord(
    val id: String,
    val lotId: String,
    val collectorId: String,
    val recyclerId: String,
    val location: String,
    val timestamp: String,
    val status: HandoverStatus,
    val createdAt: String,
    val updatedAt: String
)

enum class HandoverStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}
