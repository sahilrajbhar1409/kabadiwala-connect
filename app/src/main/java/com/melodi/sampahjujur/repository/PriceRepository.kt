package com.melodi.sampahjujur.repository

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
)

@Singleton
class PriceRepository @Inject constructor(
    private val backend: BackendApiRepository,
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
}