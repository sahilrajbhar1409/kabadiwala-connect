package com.melodi.sampahjujur.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.melodi.sampahjujur.model.PriceHistoryRecord
import com.melodi.sampahjujur.model.PriceInfo

/**
 * Room entity representing material price data cached for offline access.
 */
@Entity(tableName = "prices")
data class PriceEntity(
    @PrimaryKey
    val id: String,
    val material: String,
    val subcategory: String,
    val location: String,
    val buyingPrice: Double,
    val unit: String,
    val marketMin: Double,
    val marketMax: Double,
    val recyclerOffer: Double,
    val trend: String,
    val timestamp: Long,
    val isCurrent: Boolean = true
) {
    fun toPriceInfo(): PriceInfo {
        return PriceInfo(
            material = material,
            subcategory = subcategory,
            location = location,
            buyingPrice = buyingPrice,
            unit = unit,
            marketMin = marketMin,
            marketMax = marketMax,
            recyclerOffer = recyclerOffer,
            trend = trend,
            updatedAt = timestamp
        )
    }

    fun toPriceHistoryRecord(): PriceHistoryRecord {
        return PriceHistoryRecord(
            id = id,
            material = material,
            subcategory = subcategory,
            location = location,
            timestamp = timestamp,
            buyingPrice = buyingPrice,
            marketMin = marketMin,
            marketMax = marketMax,
            recyclerOffer = recyclerOffer,
            unit = unit
        )
    }

    companion object {
        fun fromPriceInfo(priceInfo: PriceInfo): PriceEntity {
            return PriceEntity(
                id = "${priceInfo.material}_current",
                material = priceInfo.material,
                subcategory = priceInfo.subcategory,
                location = priceInfo.location,
                buyingPrice = priceInfo.buyingPrice,
                unit = priceInfo.unit,
                marketMin = priceInfo.marketMin,
                marketMax = priceInfo.marketMax,
                recyclerOffer = priceInfo.recyclerOffer,
                trend = priceInfo.trend,
                timestamp = priceInfo.updatedAt,
                isCurrent = true
            )
        }

        fun fromPriceHistoryRecord(history: PriceHistoryRecord): PriceEntity {
            return PriceEntity(
                id = history.id.ifBlank { "${history.material}_${history.timestamp}" },
                material = history.material,
                subcategory = history.subcategory,
                location = history.location,
                buyingPrice = history.buyingPrice,
                unit = history.unit,
                marketMin = history.marketMin,
                marketMax = history.marketMax,
                recyclerOffer = history.recyclerOffer,
                trend = PriceInfo.TREND_STABLE,
                timestamp = history.timestamp,
                isCurrent = false
            )
        }
    }
}
