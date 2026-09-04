package com.melodi.sampahjujur.model

/**
 * Data class representing a historical price data point for e-waste materials.
 *
 * @property id Unique identifier for the history entry
 * @property material Material category (e.g. "PCB", "CRT")
 * @property subcategory Subcategory details
 * @property location Market location
 * @property timestamp Timestamp in ms when this price point was recorded
 * @property buyingPrice Buying price at this historical time in INR (₹)
 * @property marketMin Market minimum price at this time
 * @property marketMax Market maximum price at this time
 * @property recyclerOffer Recycler offer price at this time
 * @property unit Measuring unit (default: "kg")
 */
data class PriceHistoryRecord(
    val id: String = "",
    val material: String = "",
    val subcategory: String = "Standard",
    val location: String = "Mumbai",
    val timestamp: Long = System.currentTimeMillis(),
    val buyingPrice: Double = 0.0,
    val marketMin: Double = 0.0,
    val marketMax: Double = 0.0,
    val recyclerOffer: Double = 0.0,
    val unit: String = "kg"
)
