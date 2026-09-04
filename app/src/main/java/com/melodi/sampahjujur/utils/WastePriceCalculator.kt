package com.melodi.sampahjujur.utils

import com.melodi.sampahjujur.model.PriceHistoryRecord
import com.melodi.sampahjujur.model.PriceInfo

/**
 * Indicative buying rates, market ranges, recycler offers, and trend calculation
 * for e-waste materials.
 */
object WastePriceCalculator {

    // Buying price per kg in INR (₹) for the 8 e-waste materials
    private val pricePerKg = mapOf(
        "crt" to 45.0,
        "lcd" to 110.0,
        "pcb" to 320.0,
        "cables" to 180.0,
        "batteries" to 95.0,
        "motors" to 160.0,
        "magnets" to 210.0,
        "mixed plastics" to 28.0,
        "electronics" to 320.0,
        "plastic" to 28.0
    )

    // Recycler offered price per kg in INR (₹)
    private val recyclerOffers = mapOf(
        "crt" to 50.0,
        "lcd" to 120.0,
        "pcb" to 330.0,
        "cables" to 195.0,
        "batteries" to 105.0,
        "motors" to 175.0,
        "magnets" to 225.0,
        "mixed plastics" to 32.0
    )

    // Market ranges (min, max) per kg in INR (₹)
    private val marketRanges = mapOf(
        "crt" to Pair(35.0, 55.0),
        "lcd" to Pair(95.0, 130.0),
        "pcb" to Pair(280.0, 350.0),
        "cables" to Pair(150.0, 210.0),
        "batteries" to Pair(80.0, 115.0),
        "motors" to Pair(140.0, 190.0),
        "magnets" to Pair(180.0, 240.0),
        "mixed plastics" to Pair(20.0, 35.0)
    )

    fun normalizeType(type: String): String {
        return when (type.lowercase().trim()) {
            "crt" -> "crt"
            "lcd" -> "lcd"
            "pcb", "electronics", "e-waste" -> "pcb"
            "cables", "cable" -> "cables"
            "batteries", "battery" -> "batteries"
            "motors", "motor" -> "motors"
            "magnets", "magnet", "magnets / magnet-bearing assemblies" -> "magnets"
            "mixed plastics", "plastic", "plastics" -> "mixed plastics"
            else -> type.lowercase().trim()
        }
    }

    fun getDisplayName(type: String): String {
        return when (normalizeType(type)) {
            "crt" -> "CRT"
            "lcd" -> "LCD"
            "pcb" -> "PCB"
            "cables" -> "Cables"
            "batteries" -> "Batteries"
            "motors" -> "Motors"
            "magnets" -> "Magnets"
            "mixed plastics" -> "Mixed Plastics"
            else -> type.replaceFirstChar { it.uppercase() }
        }
    }

    fun formatMoney(amount: Double): String {
        return "₹${String.format("%,.0f", amount)}"
    }

    /**
     * Calculates deterministic estimated value: Weight × Current Buying Rate
     */
    fun calculateValue(type: String, weight: Double): Double {
        if (weight <= 0) return 0.0
        val value = weight * getPricePerKg(type)
        return kotlin.math.round(value)
    }

    fun getPricePerKg(type: String): Double {
        val normalized = normalizeType(type)
        return pricePerKg[normalized] ?: 50.0
    }

    fun getRecyclerOfferPrice(type: String): Double {
        val normalized = normalizeType(type)
        return recyclerOffers[normalized] ?: (getPricePerKg(type) + 10.0)
    }

    fun getMarketRange(type: String): Pair<Double, Double> {
        val normalized = normalizeType(type)
        return marketRanges[normalized] ?: Pair(getPricePerKg(type) * 0.8, getPricePerKg(type) * 1.2)
    }

    fun getWasteTypes(): List<String> {
        return listOf(
            "CRT",
            "LCD",
            "PCB",
            "Cables",
            "Batteries",
            "Motors",
            "Magnets",
            "Mixed Plastics"
        )
    }

    fun getWasteTypeWithPrice(type: String): String {
        val price = getPricePerKg(type)
        return "${getDisplayName(type)} (₹${String.format("%,.0f", price)}/kg)"
    }

    /**
     * Retrieves full PriceInfo for a material category
     */
    fun getPriceInfo(type: String, location: String = "Mumbai"): PriceInfo {
        val normalized = normalizeType(type)
        val rate = getPricePerKg(normalized)
        val recyclerOffer = getRecyclerOfferPrice(normalized)
        val (min, max) = getMarketRange(normalized)
        val history = getMockPriceHistory(normalized, location)
        val trend = calculateTrend(history)

        return PriceInfo(
            material = getDisplayName(normalized),
            subcategory = "Standard Grade",
            location = location,
            buyingPrice = rate,
            unit = "kg",
            marketMin = min,
            marketMax = max,
            recyclerOffer = recyclerOffer,
            trend = trend,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Rule-based deterministic price trend evaluation.
     * Evaluates sequence of historical prices.
     * e.g. ₹280 -> ₹295 -> ₹310 -> ₹320 = Increasing
     */
    fun calculateTrend(history: List<PriceHistoryRecord>): String {
        if (history.size < 2) return PriceInfo.TREND_STABLE
        val sorted = history.sortedBy { it.timestamp }
        val firstPrice = sorted.first().buyingPrice
        val lastPrice = sorted.last().buyingPrice

        val diff = lastPrice - firstPrice
        return when {
            diff > 2.0 -> PriceInfo.TREND_INCREASING
            diff < -2.0 -> PriceInfo.TREND_DECREASING
            else -> PriceInfo.TREND_STABLE
        }
    }

    /**
     * Returns mock historical price records over the last 4 weeks for offline cache initialization.
     */
    fun getMockPriceHistory(type: String, location: String = "Mumbai"): List<PriceHistoryRecord> {
        val normalized = normalizeType(type)
        val current = getPricePerKg(normalized)
        val (min, max) = getMarketRange(normalized)
        val recycler = getRecyclerOfferPrice(normalized)
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L

        // Generate deterministic history series e.g. for PCB: 280 -> 295 -> 310 -> 320 (Increasing)
        val deltas = when (normalized) {
            "pcb" -> listOf(-40.0, -25.0, -10.0, 0.0) // 280, 295, 310, 320
            "cables" -> listOf(-20.0, -15.0, -5.0, 0.0) // Increasing
            "lcd" -> listOf(20.0, 15.0, 5.0, 0.0) // Decreasing
            "crt" -> listOf(0.0, 1.0, -1.0, 0.0) // Stable
            "batteries" -> listOf(-15.0, -10.0, -5.0, 0.0) // Increasing
            "motors" -> listOf(10.0, 5.0, 2.0, 0.0) // Decreasing
            "magnets" -> listOf(-30.0, -20.0, -10.0, 0.0) // Increasing
            else -> listOf(-5.0, 0.0, -2.0, 0.0) // Stable
        }

        return deltas.mapIndexed { index, delta ->
            val timestamp = now - ((3 - index) * 7 * dayMs)
            val price = (current + delta).coerceAtLeast(5.0)
            PriceHistoryRecord(
                id = "${normalized}_hist_$index",
                material = getDisplayName(normalized),
                subcategory = "Standard Grade",
                location = location,
                timestamp = timestamp,
                buyingPrice = price,
                marketMin = (min + delta).coerceAtLeast(5.0),
                marketMax = (max + delta).coerceAtLeast(10.0),
                recyclerOffer = (recycler + delta).coerceAtLeast(5.0),
                unit = "kg"
            )
        }
    }
}
