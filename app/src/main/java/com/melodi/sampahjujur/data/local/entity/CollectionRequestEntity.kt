package com.melodi.sampahjujur.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.melodi.sampahjujur.data.local.converter.CollectionConverters
import com.melodi.sampahjujur.model.CollectionRequest
import com.melodi.sampahjujur.model.ScrapMaterial

/**
 * Room entity for caching Collection Requests locally (SIH 26229 - Person 4).
 * Enables offline creation, offline cash recording, and fast local queries.
 */
@Entity(
    tableName = "collection_requests",
    indices = [Index(value = ["lotId"], unique = true), Index(value = ["collectorId"])]
)
@TypeConverters(CollectionConverters::class)
data class CollectionRequestEntity(
    @PrimaryKey
    val id: String,
    val lotId: String,
    val collectorId: String,
    val recyclerId: String?,
    val recyclerName: String,
    val materials: List<ScrapMaterial>,
    val totalWeight: Double,
    val quotedPrice: Double,
    val finalSaleValue: Double,
    val status: String,
    val collectionLocation: String,
    val handoverLocation: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: Long,
    val scheduledAt: Long?,
    val collectedAt: Long?,
    val handedOverAt: Long?,
    val paymentMethod: String?,
    val paymentStatus: String,
    val paymentReference: String?,
    val notes: String,
    val isSynced: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toCollectionRequest(): CollectionRequest = CollectionRequest(
        id = id,
        lotId = lotId,
        collectorId = collectorId,
        recyclerId = recyclerId,
        recyclerName = recyclerName,
        materials = materials,
        totalWeight = totalWeight,
        quotedPrice = quotedPrice,
        finalSaleValue = finalSaleValue,
        status = status,
        collectionLocation = collectionLocation,
        handoverLocation = handoverLocation,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt,
        scheduledAt = scheduledAt,
        collectedAt = collectedAt,
        handedOverAt = handedOverAt,
        paymentMethod = paymentMethod,
        paymentStatus = paymentStatus,
        paymentReference = paymentReference,
        notes = notes,
        isSynced = isSynced
    )

    companion object {
        fun fromCollectionRequest(request: CollectionRequest, isSynced: Boolean = request.isSynced): CollectionRequestEntity =
            CollectionRequestEntity(
                id = request.id.ifBlank { request.lotId },
                lotId = request.lotId,
                collectorId = request.collectorId,
                recyclerId = request.recyclerId,
                recyclerName = request.recyclerName,
                materials = request.materials,
                totalWeight = request.totalWeight,
                quotedPrice = request.quotedPrice,
                finalSaleValue = request.finalSaleValue,
                status = request.status,
                collectionLocation = request.collectionLocation,
                handoverLocation = request.handoverLocation,
                latitude = request.latitude,
                longitude = request.longitude,
                createdAt = request.createdAt,
                scheduledAt = request.scheduledAt,
                collectedAt = request.collectedAt,
                handedOverAt = request.handedOverAt,
                paymentMethod = request.paymentMethod,
                paymentStatus = request.paymentStatus,
                paymentReference = request.paymentReference,
                notes = request.notes,
                isSynced = isSynced,
                cachedAt = System.currentTimeMillis()
            )
    }
}
