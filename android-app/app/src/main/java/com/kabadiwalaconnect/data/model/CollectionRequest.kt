package com.kabadiwalaconnect.data.model

data class CollectionRequest(
    val id: String,
    val citizenId: String,
    val materialId: String,
    val estimatedWeight: Double,
    val estimatedValue: Double,
    val pickupAddress: String,
    val latitude: Double,
    val longitude: Double,
    val preferredDate: String,
    val preferredTime: String,
    val status: CollectionRequestStatus,
    val createdAt: String,
    val updatedAt: String,
    val aiPredictionId: String? = null,
    val imageReference: String? = null
)

enum class CollectionRequestStatus {
    REQUESTED,
    PENDING,
    ASSIGNED,
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    REJECTED
}
