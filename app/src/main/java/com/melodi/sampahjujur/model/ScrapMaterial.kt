package com.melodi.sampahjujur.model

import com.google.firebase.firestore.PropertyName

/**
 * Scrap Material Model (Owned by Person 3; consumed by Person 4).
 * Categories aligned with SIH 26229:
 * CRT, LCD, PCB, Cables, Batteries, Motors, Magnets, Mixed plastics.
 */
data class ScrapMaterial(
    @get:PropertyName("materialId") @set:PropertyName("materialId")
    var materialId: String = "",

    @get:PropertyName("category") @set:PropertyName("category")
    var category: String = "",

    @get:PropertyName("description") @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("approximateWeight") @set:PropertyName("approximateWeight")
    var approximateWeight: Double = 0.0,

    @get:PropertyName("currentBuyingRate") @set:PropertyName("currentBuyingRate")
    var currentBuyingRate: Double = 0.0,

    @get:PropertyName("estimatedValue") @set:PropertyName("estimatedValue")
    var estimatedValue: Double = 0.0,

    @get:PropertyName("quotedPrice") @set:PropertyName("quotedPrice")
    var quotedPrice: Double = 0.0,

    @get:PropertyName("photoReference") @set:PropertyName("photoReference")
    var photoReference: String = ""
) {
    companion object {
        val STANDARD_CATEGORIES = listOf(
            "CRT",
            "LCD",
            "PCB",
            "Cables",
            "Batteries",
            "Motors",
            "Magnets",
            "Mixed plastics"
        )

        val DEFAULT_CATEGORY_RATES = mapOf(
            "CRT" to 15.0,
            "LCD" to 35.0,
            "PCB" to 120.0,
            "Cables" to 65.0,
            "Batteries" to 85.0,
            "Motors" to 70.0,
            "Magnets" to 40.0,
            "Mixed plastics" to 22.0
        )
    }
}
