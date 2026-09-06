package com.melodi.sampahjujur.repository

import com.melodi.sampahjujur.api.BackendPriceHistory
import com.melodi.sampahjujur.data.local.dao.PriceDao
import com.melodi.sampahjujur.data.local.entity.PriceEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

data class CanonicalPriceCategory(
    val backendId: String,
    val displayName: String
)

object Person3PriceCategories {
    val all = listOf(
        CanonicalPriceCategory("CRT", "CRT"),
        CanonicalPriceCategory("LCD", "LCD"),
        CanonicalPriceCategory("PCB", "PCB"),
        CanonicalPriceCategory("CABLE", "Cables"),
        CanonicalPriceCategory("BATTERY", "Batteries"),
        CanonicalPriceCategory("MOTOR", "Motors"),
        CanonicalPriceCategory("MAGNET_ASSEMBLY", "Magnets"),
        CanonicalPriceCategory("MIXED_PLASTIC", "Mixed Plastics")
    )

    fun find(id: String): CanonicalPriceCategory? = all.firstOrNull { it.backendId == id }
}

data class CachedPrice(
    val category: CanonicalPriceCategory,
    val currentPrice: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val unit: String,
    val source: String,
    val recordedAt: String?
)

@Singleton
class PriceRepository @Inject constructor(
    private val backend: BackendApiRepository,
    private val priceDao: PriceDao
) {
    fun observeCachedPrices(): Flow<List<PriceEntity>> = priceDao.observeAll()

    suspend fun refresh(): Result<List<PriceEntity>> {
        return try {
            val prices = Person3PriceCategories.all.map { category ->
                val quote = backend.price(category.backendId).getOrThrow()
                PriceEntity(
                    category = category.backendId,
                    displayName = category.displayName,
                    currentPrice = quote.currentPrice,
                    minPrice = quote.minPrice,
                    maxPrice = quote.maxPrice,
                    unit = quote.unit,
                    source = quote.source,
                    recordedAt = quote.recordedAt
                )
            }
            priceDao.upsertAll(prices)
            Result.success(prices)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun getPrice(category: String): CachedPrice? {
        val canonical = Person3PriceCategories.find(category)
            ?: error("Unsupported pricing category: $category")
        val cached = priceDao.get(canonical.backendId) ?: return null
        return CachedPrice(
            category = canonical,
            currentPrice = cached.currentPrice,
            minPrice = cached.minPrice,
            maxPrice = cached.maxPrice,
            unit = cached.unit,
            source = cached.source,
            recordedAt = cached.recordedAt
        )
    }

    suspend fun trends(category: String, limit: Int = 12): Result<List<BackendPriceHistory>> =
        backend.priceTrends(category, limit)
}