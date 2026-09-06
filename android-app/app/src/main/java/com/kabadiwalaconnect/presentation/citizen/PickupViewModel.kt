package com.kabadiwalaconnect.presentation.citizen

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kabadiwalaconnect.data.SessionState
import com.kabadiwalaconnect.data.api.BackendApiClient
import com.kabadiwalaconnect.data.api.CreateLotRequest
import com.kabadiwalaconnect.data.model.AiPrediction
import com.kabadiwalaconnect.data.model.CollectionRequest
import com.kabadiwalaconnect.data.model.CollectionRequestStatus
import com.kabadiwalaconnect.data.model.Lot
import com.kabadiwalaconnect.data.model.LotStatus
import com.kabadiwalaconnect.data.repository.AiDemoService
import com.kabadiwalaconnect.data.repository.AiDemoServiceProvider
import com.kabadiwalaconnect.data.repository.CollectionRepository
import com.kabadiwalaconnect.data.repository.CollectionRepositoryProvider
import com.kabadiwalaconnect.utils.CloudinaryException
import com.kabadiwalaconnect.utils.CloudinaryUploadService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class PickupResult(
    val lot: Lot,
    val prediction: AiPrediction?
)

/**
 * Image upload state for tracking individual image uploads.
 */
data class ImageUploadState(
    val uri: Uri,
    val isUploading: Boolean = false,
    val uploadedUrl: String? = null,
    val error: String? = null
)

/**
 * Owns the citizen pickup use case including image upload handling.
 * Screens only collect input and render state; persistence always goes through
 * CollectionRepository and backend APIs.
 */
class PickupViewModel(
    private val repository: CollectionRepository = CollectionRepositoryProvider.instance,
    private val aiService: AiDemoService = AiDemoServiceProvider.instance,
    private val backendRepository: BackendApiClient? = null
) {
    var prediction by mutableStateOf<AiPrediction?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
    
    // Image upload state management
    var photoUris by mutableStateOf<List<ImageUploadState>>(emptyList())
        private set
    var isUploadingPhotos by mutableStateOf(false)
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

    /**
     * Add a photo to the upload queue.
     * @param uri The URI of the image to upload
     */
    fun addPhoto(uri: Uri) {
        if (photoUris.size < 6) {
            photoUris = photoUris + ImageUploadState(uri)
            errorMessage = null
        } else {
            errorMessage = "Maximum 6 photos allowed"
        }
    }

    /**
     * Remove a photo from the queue by its index.
     * @param index The index of the photo to remove
     */
    fun removePhoto(index: Int) {
        if (index in photoUris.indices) {
            photoUris = photoUris.toMutableList().apply { removeAt(index) }
            errorMessage = null
        }
    }

    /**
     * Upload all selected photos to Cloudinary.
     * @param context Android context for file operations
     * @return List of uploaded photo URLs, or null if upload fails
     */
    suspend fun uploadPhotos(context: Context): List<String>? {
        if (photoUris.isEmpty()) {
            return emptyList()
        }

        isUploadingPhotos = true
        val uploadedUrls = mutableListOf<String>()

        try {
            CloudinaryUploadService.initialize(context)

            for (i in photoUris.indices) {
                val photoState = photoUris[i]
                if (photoState.uploadedUrl != null) {
                    uploadedUrls.add(photoState.uploadedUrl)
                    continue
                }

                try {
                    // Update state to show uploading
                    photoUris = photoUris.toMutableList().apply {
                        set(i, get(i).copy(isUploading = true, error = null))
                    }

                    // Upload to Cloudinary
                    val url = CloudinaryUploadService.uploadImage(context, photoState.uri)

                    // Update state with successful upload
                    photoUris = photoUris.toMutableList().apply {
                        set(i, get(i).copy(isUploading = false, uploadedUrl = url))
                    }
                    uploadedUrls.add(url)
                } catch (e: CloudinaryException) {
                    val errorMsg = e.message ?: "Upload failed"
                    photoUris = photoUris.toMutableList().apply {
                        set(i, get(i).copy(isUploading = false, error = errorMsg))
                    }
                    errorMessage = "Failed to upload photo ${i + 1}: $errorMsg"
                    // Continue with other photos instead of failing completely
                }
            }

            isUploadingPhotos = false
            return if (uploadedUrls.isNotEmpty()) uploadedUrls else null
        } catch (e: Exception) {
            isUploadingPhotos = false
            errorMessage = "Photo upload failed: ${e.message}"
            return null
        }
    }

    fun submit(
        materialId: String,
        estimatedWeight: Double,
        estimatedValue: Double,
        pickupAddress: String,
        latitude: Double,
        longitude: Double
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
                latitude = latitude,
                longitude = longitude,
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

    suspend fun submitToBackend(
        context: Context,
        materialId: String,
        estimatedWeight: Double,
        estimatedValue: Double,
        pickupAddress: String,
        latitude: Double,
        longitude: Double
    ): PickupResult? {
        val result = submit(
            materialId,
            estimatedWeight,
            estimatedValue,
            pickupAddress,
            latitude,
            longitude
        ) ?: return null

        val backend = backendRepository
        if (backend != null) {
            // Upload photos first if any are selected
            val photoUrls = uploadPhotos(context) ?: emptyList()

            val response = backend.createLot(
                CreateLotRequest(
                    materialCategory = materialId.uppercase().replace(' ', '_'),
                    materialDescription = "Pickup request from Kabadiwala Connect",
                    approximateWeight = estimatedWeight,
                    address = pickupAddress,
                    latitude = latitude,
                    longitude = longitude,
                    notes = "AI prediction: ${prediction?.modelVersion ?: "not available"}",
                    clientGeneratedId = result.lot.lotId,
                    photos = photoUrls
                )
            )
            response.exceptionOrNull()?.let {
                backend.logFailure("lot creation", it)
            }
        }
        return result
    }
}


private fun now(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
