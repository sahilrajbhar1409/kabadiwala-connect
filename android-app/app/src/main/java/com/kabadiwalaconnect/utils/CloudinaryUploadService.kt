package com.kabadiwalaconnect.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.kabadiwalaconnect.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service for uploading images to Cloudinary.
 * Handles initialization and upload operations for lot/waste item photos.
 * 
 * Credentials are loaded from BuildConfig (sourced from local.properties).
 */
object CloudinaryUploadService {
    private const val TAG = "CloudinaryUploadService"
    private var isInitialized = false

    /**
     * Initialize Cloudinary MediaManager with credentials from BuildConfig.
     * Must be called before any upload operations.
     * 
     * @param context Android application context
     * @throws CloudinaryException if initialization fails
     */
    fun initialize(context: Context) {
        if (isInitialized) {
            Log.d(TAG, "Cloudinary already initialized")
            return
        }

        try {
            if (BuildConfig.CLOUDINARY_CLOUD_NAME.isBlank()) {
                throw CloudinaryException("Cloudinary cloud name not configured in local.properties")
            }

            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME
            )

            MediaManager.init(context, config)
            isInitialized = true
            Log.d(TAG, "Cloudinary initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Cloudinary", e)
            throw CloudinaryException("Failed to initialize Cloudinary: ${e.message}", e)
        }
    }

    /**
     * Upload a single image to Cloudinary.
     * 
     * @param context Android context
     * @param imageUri URI of the image to upload
     * @param folder Optional folder path in Cloudinary
     * @return Cloudinary secure URL of the uploaded image
     * @throws CloudinaryException if upload fails
     */
    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        folder: String = BuildConfig.CLOUDINARY_UPLOAD_FOLDER
    ): String = suspendCancellableCoroutine { continuation ->
        if (!isInitialized) {
            initialize(context)
        }

        try {
            val file = getFileFromUri(context, imageUri)
                ?: throw CloudinaryException("Failed to convert URI to file")

            val uploadOptions = mapOf(
                "folder" to folder,
                "upload_preset" to BuildConfig.CLOUDINARY_UPLOAD_PRESET,
                "resource_type" to "image",
                "quality" to "auto:good",
                "fetch_format" to "auto"
            )

            val requestId = MediaManager.get().upload(file.absolutePath)
                .options(uploadOptions)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d(TAG, "Upload started: $requestId")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes.toDouble() / totalBytes * 100).toInt()
                        Log.d(TAG, "Upload progress: $progress%")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val url = resultData["secure_url"] as? String
                            ?: resultData["url"] as? String
                            ?: ""

                        Log.d(TAG, "Upload successful: $url")
                        file.delete()

                        if (continuation.isActive) {
                            continuation.resume(url)
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e(TAG, "Upload failed: ${error.description}")
                        file.delete()

                        if (continuation.isActive) {
                            continuation.resumeWithException(
                                CloudinaryException("Upload failed: ${error.description}")
                            )
                        }
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w(TAG, "Upload rescheduled: ${error.description}")
                    }
                })
                .dispatch()

            continuation.invokeOnCancellation {
                MediaManager.get().cancelRequest(requestId)
                file.delete()
                Log.d(TAG, "Upload cancelled: $requestId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during upload", e)
            if (continuation.isActive) {
                continuation.resumeWithException(
                    CloudinaryException("Upload error: ${e.message}", e)
                )
            }
        }
    }

    /**
     * Convert URI to File for Cloudinary upload.
     * Creates a temporary file in cache directory by copying content from URI.
     * 
     * @param context Android context
     * @param uri URI of the image
     * @return File object or null if conversion fails
     */
    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                val tempFile = File.createTempFile(
                    "lot_image_${System.currentTimeMillis()}",
                    ".jpg",
                    context.cacheDir
                )

                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }

                tempFile
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to file", e)
            null
        }
    }
}

/**
 * Custom exception for Cloudinary operations.
 */
class CloudinaryException(message: String, cause: Throwable? = null) : Exception(message, cause)
