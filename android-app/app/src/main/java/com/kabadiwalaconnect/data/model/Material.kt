package com.kabadiwalaconnect.data.model

data class Material(
    val id: String,
    val name: String,
    val category: MaterialCategory,
    val unit: String = "kg",
    val pricePerUnit: Double,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

enum class MaterialCategory {
    PAPER,
    PLASTIC,
    METAL,
    E_WASTE,
    GLASS,
    CARDBOARD,
    OTHER
}
