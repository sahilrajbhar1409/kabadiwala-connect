package com.melodi.sampahjujur.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reference_prices")
data class PriceEntity(
    @PrimaryKey val category: String,
    val displayName: String,
    val currentPrice: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val unit: String,
    val source: String,
    val recordedAt: String?
)
