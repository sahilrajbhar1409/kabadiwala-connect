package com.kabadiwalaconnect.data.repository

import com.kabadiwalaconnect.data.model.AiPrediction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Small, deterministic seam for the demo AI. The interface can be replaced by
 * a network implementation without changing the pickup UI or ViewModel.
 */
interface AiDemoService {
    fun analyze(imageReference: String): AiPrediction
}

class DeterministicAiDemoService(
    private val prices: PriceService = PriceServiceProvider.instance
) : AiDemoService {
    override fun analyze(imageReference: String): AiPrediction {
        val reference = imageReference.trim().ifBlank { "demo-upload" }
        val materialIds = prices.supportedMaterials().map { it.id }
        val requestedMaterial = materialIds.firstOrNull { reference.contains(it, ignoreCase = true) }
        val index = requestedMaterial?.let(materialIds::indexOf)
            ?: (abs(reference.hashCode()) % materialIds.size)
        val materialId = materialIds[index]
        val weight = 1.5 + (abs(reference.hashCode().toLong()) % 70) / 10.0
        val value = prices.estimateValue(materialId, weight)
        val timestamp = now()
        return AiPrediction(
            id = "AI-${reference.hashCode().toUInt().toString(16).uppercase(Locale.US)}",
            materialId = materialId,
            predictedWeight = weight,
            predictedValue = value,
            confidence = 0.91,
            createdAt = timestamp,
            updatedAt = timestamp,
            imageReference = reference,
            modelVersion = "deterministic-demo-v1"
        )
    }
}

object AiDemoServiceProvider {
    val instance: AiDemoService = DeterministicAiDemoService()
}

private fun now(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
