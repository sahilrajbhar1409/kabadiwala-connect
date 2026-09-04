package com.melodi.sampahjujur.model

/**
 * Data class representing current buying rates and market info for an e-waste material.
 *
 * @property material Material category (e.g., "PCB", "CRT", "LCD", "Cables", "Batteries", "Motors", "Magnets", "Mixed Plastics")
 * @property subcategory Optional subcategory details (e.g., "Motherboard Grade A", "Copper Cable")
 * @property location Geographic area or market location (e.g., "Mumbai Central", "All India")
 * @property buyingPrice Current buying price per unit in INR (₹)
 * @property unit Measuring unit (default: "kg")
 * @property marketMin Market minimum price range
 * @property marketMax Market maximum price range
 * @property recyclerOffer Recycler offered price
 * @property trend Price trend direction ("Increasing", "Decreasing", "Stable")
 * @property updatedAt Timestamp (ms) when the price was last updated
 */
data class PriceInfo(
    val material: String = "",
    val subcategory: String = "Standard",
    val location: String = "Mumbai",
    val buyingPrice: Double = 0.0,
    val unit: String = "kg",
    val marketMin: Double = 0.0,
    val marketMax: Double = 0.0,
    val recyclerOffer: Double = 0.0,
    val trend: String = TREND_STABLE,
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TREND_INCREASING = "Increasing"
        const val TREND_DECREASING = "Decreasing"
        const val TREND_STABLE = "Stable"
    }

    /**
     * Formatted string representation of market price range (e.g. "₹280–₹350/kg")
     */
    fun getMarketRangeFormatted(): String {
        return "₹${buyingPrice.toInt() - 20}–₹${buyingPrice.toInt() + 30}/$unit"
    }
}
