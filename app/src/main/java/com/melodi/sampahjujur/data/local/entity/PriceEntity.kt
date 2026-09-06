package com.melodi.sampahjujur.data.local.entity

import androidx.room.Entity
<<<<<<< HEAD
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
=======

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
>>>>>>> f8f13893033af90e6bddcbdb82ab63c12f831ffd
