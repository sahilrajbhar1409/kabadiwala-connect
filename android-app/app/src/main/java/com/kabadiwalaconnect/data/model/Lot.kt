package com.kabadiwalaconnect.data.model

data class Lot(
    val lotId: String,
    val requestId: String,
    val citizenId: String,
    val collectorId: String,
    val recyclerId: String? = null,
    val materialId: String,
    val estimatedWeight: Double,
    val actualWeight: Double? = null,
    val estimatedValue: Double,
    val actualValue: Double? = null,
    val pickupLocation: String,
    val pickupTimestamp: String? = null,
    val handoverLocation: String? = null,
    val handoverTimestamp: String? = null,
    val status: LotStatus,
    val createdAt: String,
    val updatedAt: String
)

enum class LotStatus {
    REQUESTED,
    ASSIGNED,
    ACCEPTED,
    PICKUP_IN_PROGRESS,
    COLLECTED,
    HANDED_OVER,
    RECYCLER_CONFIRMED,
    PAID,
    RECYCLED,
    CANCELLED,
}
