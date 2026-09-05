package com.kabadiwalaconnect.data.model

data class AiPrediction(
    val id: String,
    val materialId: String,
    val predictedWeight: Double,
    val predictedValue: Double,
    val confidence: Double,
    val createdAt: String,
    val updatedAt: String,
    val imageReference: String = "demo-upload",
    val modelVersion: String = "deterministic-demo-v1"
)
