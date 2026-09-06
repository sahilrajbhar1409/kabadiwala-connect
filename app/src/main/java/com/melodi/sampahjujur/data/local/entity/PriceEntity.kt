package com.melodi.sampahjujur.data.local.entity

import androidx.room.Entity

@Entity(tableName = "price_cache", primaryKeys = ["category", "recordedAt"])
data class PriceEntity(
    val category: String,
    val name: String,
    val price: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val unit: String,
    val location: String,
    val source: String,
    val recordedAt: String,
    val cachedAt: Long = System.currentTimeMillis()
)