package com.kabadiwalaconnect.data.repository

import com.kabadiwalaconnect.data.model.Material
import com.kabadiwalaconnect.data.model.MaterialCategory

/**
 * Price lookup is deliberately isolated so the controlled table can later be
 * replaced by a server-backed implementation without changing the pickup UI.
 */
interface PriceService {
    fun supportedMaterials(): List<Material>
    fun findMaterial(id: String): Material?
    fun estimateValue(materialId: String, weightKg: Double): Double
}

class ControlledPriceTable : PriceService {
    private val materials = listOf(
        Material("crt", "CRT", MaterialCategory.CRT, pricePerUnit = 18.0, createdAt = "", updatedAt = ""),
        Material("lcd", "LCD", MaterialCategory.LCD, pricePerUnit = 42.0, createdAt = "", updatedAt = ""),
        Material("pcb", "PCB", MaterialCategory.PCB, pricePerUnit = 120.0, createdAt = "", updatedAt = ""),
        Material("cables", "Cables", MaterialCategory.CABLES, pricePerUnit = 85.0, createdAt = "", updatedAt = ""),
        Material("batteries", "Batteries", MaterialCategory.BATTERIES, pricePerUnit = 55.0, createdAt = "", updatedAt = ""),
        Material("motors", "Motors", MaterialCategory.MOTORS, pricePerUnit = 65.0, createdAt = "", updatedAt = ""),
        Material("magnets", "Magnets", MaterialCategory.MAGNETS, pricePerUnit = 75.0, createdAt = "", updatedAt = ""),
        Material("mixed-plastics", "Mixed Plastics", MaterialCategory.MIXED_PLASTICS, pricePerUnit = 30.0, createdAt = "", updatedAt = ""),
        Material("paper", "Paper", MaterialCategory.PAPER, pricePerUnit = 22.0, createdAt = "", updatedAt = ""),
        Material("metal", "Metal", MaterialCategory.METAL, pricePerUnit = 28.0, createdAt = "", updatedAt = ""),
        Material("cardboard", "Cardboard", MaterialCategory.CARDBOARD, pricePerUnit = 16.0, createdAt = "", updatedAt = ""),
        Material("e-waste", "E-waste", MaterialCategory.E_WASTE, pricePerUnit = 75.0, createdAt = "", updatedAt = "")
    )

    override fun supportedMaterials(): List<Material> = materials

    override fun findMaterial(id: String): Material? = materials.find { it.id == id }

    override fun estimateValue(materialId: String, weightKg: Double): Double {
        require(weightKg.isFinite() && weightKg > 0) { "Weight must be greater than zero." }
        val material = findMaterial(materialId) ?: error("Unsupported material.")
        return material.pricePerUnit * weightKg
    }
}

object PriceServiceProvider {
    val instance: PriceService = ControlledPriceTable()
}
