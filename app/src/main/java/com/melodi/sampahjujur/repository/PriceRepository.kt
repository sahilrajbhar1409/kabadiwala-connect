package com.melodi.sampahjujur.repository

<<<<<<< HEAD
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
=======
import com.melodi.sampahjujur.data.local.dao.PriceDao
import com.melodi.sampahjujur.data.local.entity.PriceEntity
import com.melodi.sampahjujur.model.ScrapMaterial
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class PricePoint(
    val category: String,
    val name: String,
    val price: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val unit: String,
    val location: String,
    val source: String,
    val recordedAt: String,
    val cachedAt: Long
>>>>>>> f8f13893033af90e6bddcbdb82ab63c12f831ffd
)

@Singleton
class PriceRepository @Inject constructor(
    private val backend: BackendApiRepository,
<<<<<<< HEAD
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
=======
    private val dao: PriceDao
) {
    companion object {
        val categories = listOf("CRT", "LCD_PANEL", "PCB", "CABLE", "BATTERY", "MOTOR", "MAGNET_ASSEMBLY", "MIXED_PLASTIC")
    }

    fun observeLatest(): Flow<List<PricePoint>> = dao.observeAll().map { rows ->
        rows.groupBy { it.category }.values.mapNotNull { it.maxByOrNull { row -> row.recordedAt } }.map(::toPoint)
    }

    fun observeHistory(category: String): Flow<List<PricePoint>> =
        dao.observeHistory(category).map { it.map(::toPoint) }

    suspend fun refresh(): Result<Unit> {
        val rows = mutableListOf<PriceEntity>()
        var firstError: Throwable? = null
        for (category in categories) {
            backend.prices(category).fold(
                onSuccess = { payload -> parse(category, payload)?.let(rows::add) },
                onFailure = { if (firstError == null) firstError = it }
            )
        }
        if (rows.isNotEmpty()) {
            dao.upsertAll(rows)
            return Result.success(Unit)
        }
        return Result.failure(firstError ?: IllegalStateException("No price data available"))
    }

    suspend fun refreshHistory(category: String): Result<Unit> =
        backend.priceTrends(category).map { payload ->
            val rows = (payload as? List<*>)?.mapNotNull { parse(category, it as? Map<*, *> ?: return@mapNotNull null) }.orEmpty()
            if (rows.isNotEmpty()) dao.upsertAll(rows)
        }

    private fun parse(category: String, payload: Any?): PriceEntity? {
        val map = payload as? Map<*, *> ?: return null
        val price = map["currentPrice"] ?: map["price"] ?: return null
        val recordedAt = map["recordedAt"]?.toString() ?: return null
        return PriceEntity(
            category = map["category"]?.toString() ?: category,
            name = displayName(map["category"]?.toString() ?: category),
            price = price.toString().toDoubleOrNull() ?: return null,
            minPrice = (map["minPrice"] ?: 0).toString().toDoubleOrNull() ?: 0.0,
            maxPrice = (map["maxPrice"] ?: 0).toString().toDoubleOrNull() ?: 0.0,
            unit = map["unit"]?.toString() ?: "kg",
            location = map["location"]?.toString() ?: "India",
            source = map["source"]?.toString() ?: "backend",
            recordedAt = recordedAt
        )
    }

    private fun toPoint(row: PriceEntity) = PricePoint(
        row.category, row.name, row.price, row.minPrice, row.maxPrice,
        row.unit, row.location, row.source, row.recordedAt, row.cachedAt
    )

    private fun displayName(category: String) = when (category) {
        "LCD_PANEL" -> "LCD"
        "CABLE" -> "Cables"
        "BATTERY" -> "Batteries"
        "MOTOR" -> "Motors"
        "MAGNET_ASSEMBLY" -> "Magnets"
        "MIXED_PLASTIC" -> "Mixed Plastics"
        else -> category
    }
>>>>>>> f8f13893033af90e6bddcbdb82ab63c12f831ffd
}