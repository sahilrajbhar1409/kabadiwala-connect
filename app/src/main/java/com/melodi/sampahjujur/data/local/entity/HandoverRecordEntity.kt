package com.melodi.sampahjujur.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.melodi.sampahjujur.data.local.converter.CollectionConverters
import com.melodi.sampahjujur.model.HandoverRecord
import com.melodi.sampahjujur.model.ScrapMaterial

/**
 * Room entity for caching Digital Handover Records (SIH 26229 - Person 4).
 * Provides offline-accessible digital handover receipts with verifiable timestamps.
 */
@Entity(
    tableName = "handover_records",
    indices = [
        Index(value = ["lotId"]),
        Index(value = ["handoverReference"], unique = true),
        Index(value = ["collectorId"])
    ]
)
@TypeConverters(CollectionConverters::class)
data class HandoverRecordEntity(
    @PrimaryKey
    val handoverId: String,
    val handoverReference: String,
    val lotId: String,
    val collectorId: String,
    val recyclerId: String,
    val recyclerName: String,
    val materials: List<ScrapMaterial>,
    val weight: Double,
    val collectionLocation: String,
    val handoverLocation: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val quotedPrice: Double,
    val finalSaleValue: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val recyclerConfirmed: Boolean,
    val recyclerConfirmedAt: Long?,
    val transactionStatus: String,
    val isSynced: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toHandoverRecord(): HandoverRecord = HandoverRecord(
        handoverId = handoverId,
        handoverReference = handoverReference,
        lotId = lotId,
        collectorId = collectorId,
        recyclerId = recyclerId,
        recyclerName = recyclerName,
        materials = materials,
        weight = weight,
        collectionLocation = collectionLocation,
        handoverLocation = handoverLocation,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        quotedPrice = quotedPrice,
        finalSaleValue = finalSaleValue,
        paymentMethod = paymentMethod,
        paymentStatus = paymentStatus,
        recyclerConfirmed = recyclerConfirmed,
        recyclerConfirmedAt = recyclerConfirmedAt,
        transactionStatus = transactionStatus,
        isSynced = isSynced
    )

    companion object {
        fun fromHandoverRecord(record: HandoverRecord, isSynced: Boolean = record.isSynced): HandoverRecordEntity =
            HandoverRecordEntity(
                handoverId = record.handoverId,
                handoverReference = record.handoverReference,
                lotId = record.lotId,
                collectorId = record.collectorId,
                recyclerId = record.recyclerId,
                recyclerName = record.recyclerName,
                materials = record.materials,
                weight = record.weight,
                collectionLocation = record.collectionLocation,
                handoverLocation = record.handoverLocation,
                latitude = record.latitude,
                longitude = record.longitude,
                timestamp = record.timestamp,
                quotedPrice = record.quotedPrice,
                finalSaleValue = record.finalSaleValue,
                paymentMethod = record.paymentMethod,
                paymentStatus = record.paymentStatus,
                recyclerConfirmed = record.recyclerConfirmed,
                recyclerConfirmedAt = record.recyclerConfirmedAt,
                transactionStatus = record.transactionStatus,
                isSynced = isSynced,
                cachedAt = System.currentTimeMillis()
            )
    }
}
