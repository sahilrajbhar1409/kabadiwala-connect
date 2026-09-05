package com.kabadiwalaconnect.presentation.citizen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kabadiwalaconnect.data.SessionState
import com.kabadiwalaconnect.data.model.AiPrediction
import com.kabadiwalaconnect.data.model.CollectionRequest
import com.kabadiwalaconnect.data.model.CollectionRequestStatus
import com.kabadiwalaconnect.data.model.Lot
import com.kabadiwalaconnect.data.model.LotStatus
import com.kabadiwalaconnect.data.repository.AiDemoService
import com.kabadiwalaconnect.data.repository.AiDemoServiceProvider
import com.kabadiwalaconnect.data.repository.CollectionRepository
import com.kabadiwalaconnect.data.repository.CollectionRepositoryProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class PickupResult(
    val lot: Lot,
    val prediction: AiPrediction?
)

/**
 * Owns the citizen pickup use case. Screens only collect input and render
 * state; persistence always goes through CollectionRepository.
 */
class PickupViewModel(
    private val repository: CollectionRepository = CollectionRepositoryProvider.instance,
    private val aiService: AiDemoService = AiDemoServiceProvider.instance
) {
    var prediction by mutableStateOf<AiPrediction?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun clearPrediction() {
        prediction = null
    }

    fun analyzeUpload(imageReference: String): AiPrediction? {
        return try {
            val generated = aiService.analyze(imageReference)
            prediction = repository.getAiPrediction(generated.id)
                ?: repository.createAiPrediction(generated)
            errorMessage = null
            prediction
        } catch (exception: Exception) {
            errorMessage = exception.message ?: "Unable to analyze this upload."
            null
        }
    }

    fun submit(
        materialId: String,
        estimatedWeight: Double,
        estimatedValue: Double,
        pickupAddress: String
    ): PickupResult? {
        return try {
            require(materialId.isNotBlank()) { "Select a material." }
            require(estimatedWeight.isFinite() && estimatedWeight > 0) {
                "Weight must be greater than zero."
            }
            require(estimatedValue.isFinite() && estimatedValue >= 0) {
                "Estimated value is invalid."
            }
            require(pickupAddress.isNotBlank()) { "Pickup address is required." }
            val timestamp = now()
            val requestId = "REQ-${UUID.randomUUID()}"
            val lotId = repository.nextLotId()
            val request = CollectionRequest(
                id = requestId,
                citizenId = SessionState.CITIZEN_ID,
                materialId = materialId,
                estimatedWeight = estimatedWeight,
                estimatedValue = estimatedValue,
                pickupAddress = pickupAddress.trim(),
                latitude = 0.0,
                longitude = 0.0,
                preferredDate = timestamp.substringBefore("T"),
                preferredTime = "Any time",
                status = CollectionRequestStatus.REQUESTED,
                createdAt = timestamp,
                updatedAt = timestamp,
                aiPredictionId = prediction?.id,
                imageReference = prediction?.imageReference
            )
            repository.createCollectionRequest(request)
            val lot = repository.createLot(
                Lot(
                    lotId = lotId,
                    requestId = requestId,
                    citizenId = SessionState.CITIZEN_ID,
                    collectorId = "",
                    materialId = materialId,
                    estimatedWeight = estimatedWeight,
                    estimatedValue = estimatedValue,
                    pickupLocation = pickupAddress.trim(),
                    status = LotStatus.REQUESTED,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                    aiPredictionId = prediction?.id,
                    imageReference = prediction?.imageReference
                )
            )
            errorMessage = null
            PickupResult(lot, prediction)
        } catch (exception: Exception) {
            errorMessage = exception.message ?: "We couldn't save your pickup request."
            null
        }
    }
}

private fun now(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
