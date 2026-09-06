package com.kabadiwalaconnect.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class BackendApiClient(private val api: ApiService, private val session: ApiSessionRepository) {
    fun isAuthenticated() = !session.token().isNullOrBlank()
    suspend fun login(identifier: String, password: String): Result<AuthData> =
        executeAuth { api.login(LoginRequest(identifier.trim(), password)) }
    suspend fun lots(status: String? = null, category: String? = null) = execute { api.lots(status, category) }
    suspend fun myLots() = execute { api.myLots() }
    suspend fun createLot(request: CreateLotRequest) = execute { api.createLot(request) }
    suspend fun uploadImages(context: Context, uris: List<Uri>): Result<List<String>> {
        val parts = uris.take(6).mapIndexedNotNull { index, uri ->
            val file = File.createTempFile("lot-$index-", ".jpg", context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { input -> file.outputStream().use(input::copyTo) }
                ?: return@mapIndexedNotNull null
            MultipartBody.Part.createFormData("photos", file.name, file.asRequestBody("image/*".toMediaType()))
        }
        return execute { api.uploadImages(parts) }.map { it["urls"] as? List<String> ?: emptyList() }
    }
    suspend fun estimatePrice(category: String, location: String, weight: Double): Result<Double> =
        execute { api.prices(category, location) }.map { payload ->
            (payload as? Map<*, *>)?.get("estimatedValue")?.toString()?.toDoubleOrNull()
                ?: throw IllegalStateException("Backend price response missing estimatedValue")
        }
    suspend fun offers() = execute { api.offers() }
    suspend fun createOffer(request: CreateOfferRequest) = execute { api.createOffer(request) }
    suspend fun transactions() = execute { api.transactions() }
    suspend fun createHandover(request: HandoverRequest) = execute { api.createHandover(request) }
    suspend fun createPayment(request: PaymentRequest) = execute { api.createPayment(request) }
    suspend fun dashboard(role: String) = execute { api.dashboard(role) }
    suspend fun trace(referenceId: String) = execute { api.trace(referenceId) }

    private suspend fun <T> execute(call: suspend () -> Response<ApiEnvelope<T>>): Result<T> =
        withContext(Dispatchers.IO) {
            val response = call()
            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.data == null) {
                Result.failure(IllegalStateException(body?.message ?: "Backend request failed"))
            } else Result.success(body.data)
        }

    private suspend fun executeAuth(call: suspend () -> Response<ApiEnvelope<AuthData>>): Result<AuthData> =
        withContext(Dispatchers.IO) {
            val response = call()
            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.data?.token.isNullOrBlank()) {
                Result.failure(IllegalStateException(body?.message ?: "Backend authentication failed"))
            } else {
                val data = body.data ?: return@withContext Result.failure(IllegalStateException("Backend returned no session"))
                session.saveToken(data.token)
                Result.success(data)
            }
        }

    fun logFailure(operation: String, error: Throwable) {
        Log.w("BackendApiClient", "$operation unavailable: ${error.message}")
    }
}
