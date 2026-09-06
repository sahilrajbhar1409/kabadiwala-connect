package com.melodi.sampahjujur.repository

import com.melodi.sampahjujur.api.BackendApiService
import com.melodi.sampahjujur.api.BackendAuthData
import com.melodi.sampahjujur.api.BackendLoginRequest
import com.melodi.sampahjujur.api.BackendRegisterRequest
import com.melodi.sampahjujur.api.BackendCreateLotRequest
import com.melodi.sampahjujur.api.BackendCreateOfferRequest
import com.melodi.sampahjujur.api.BackendHandoverRequest
import com.melodi.sampahjujur.api.BackendPaymentRequest
import com.melodi.sampahjujur.api.BackendEnvelope
import com.melodi.sampahjujur.api.BackendAnalyzeLotRequest
import com.melodi.sampahjujur.api.BackendLotAnalysis
import com.melodi.sampahjujur.api.BackendFirebaseLoginRequest
import com.melodi.sampahjujur.api.BackendPriceHistory
import com.melodi.sampahjujur.api.BackendPriceQuote
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@Singleton
class BackendApiRepository @Inject constructor(
    private val api: BackendApiService,
    private val session: BackendSessionRepository
) {
    val sessionInvalidated = session.sessionInvalidated

    suspend fun login(identifier: String, password: String): Result<BackendAuthData> =
        executeAuth { api.login(BackendLoginRequest(identifier.trim(), password)) }

    suspend fun register(request: BackendRegisterRequest): Result<BackendAuthData> =
        executeAuth { api.register(request) }

    suspend fun firebaseLogin(idToken: String, name: String, phone: String, role: String): Result<BackendAuthData> =
        executeAuth {
            api.firebaseLogin(BackendFirebaseLoginRequest(idToken, name, phone, role))
        }

    suspend fun logout(): Result<Unit> {
        val response = api.logout()
        session.clear()
        if (!response.isSuccessful || response.body()?.success != true) {
            return Result.failure(IllegalStateException(response.body()?.message ?: "Backend logout failed"))
        }
        return Result.success(Unit)
    }

    fun isAuthenticated(): Boolean = session.token() != null

    suspend fun recyclers(): Result<List<Map<String, Any?>>> = execute { api.recyclers() }
    suspend fun price(category: String, location: String? = null): Result<BackendPriceQuote> =
        execute { api.prices(category = category, location = location) }
    suspend fun priceTrends(category: String, limit: Int = 12): Result<List<BackendPriceHistory>> =
        execute { api.priceTrends(category = category, limit = limit) }
    suspend fun createLot(request: BackendCreateLotRequest): Result<Map<String, Any?>> =
        execute { api.createLot(request) }
<<<<<<< HEAD
    suspend fun analyzeLot(
        lotId: String,
        imageUrl: String,
        weightKg: Double,
        actualPrice: Double
    ): Result<BackendLotAnalysis> = execute {
        api.analyzeLot(
            lotId,
            BackendAnalyzeLotRequest(
                imageUrl = imageUrl,
                weightKg = weightKg,
                actualPrice = actualPrice
            )
        )
    }
=======
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
        execute { api.prices(category, location, weight) }.map { payload ->
            (payload as? Map<*, *>)?.get("estimatedValue")?.toString()?.toDoubleOrNull()
                ?: throw IllegalStateException("Backend price response missing estimatedValue")
        }
    suspend fun prices(category: String? = null): Result<Any> = execute { api.prices(category = category) }
    suspend fun priceTrends(category: String, limit: Int = 12): Result<Any> =
        execute { api.priceTrends(category, limit) }
>>>>>>> f8f13893033af90e6bddcbdb82ab63c12f831ffd
    suspend fun lots(): Result<List<Map<String, Any?>>> = execute { api.lots() }
    suspend fun myLots(): Result<List<Map<String, Any?>>> = execute { api.myLots() }
    suspend fun createOffer(request: BackendCreateOfferRequest): Result<Map<String, Any?>> =
        execute { api.createOffer(request) }
    suspend fun offers(): Result<List<Map<String, Any?>>> = execute { api.offers() }
<<<<<<< HEAD
    suspend fun acceptOffer(offerId: String, scheduledAt: String = ""): Result<Map<String, Any?>> =
        execute { api.acceptOffer(offerId, com.melodi.sampahjujur.api.BackendScheduleRequest(scheduledAt)) }
    suspend fun rejectOffer(offerId: String): Result<Any> = execute { api.rejectOffer(offerId) }
    suspend fun scheduleTransaction(transactionId: String, scheduledAt: String): Result<Map<String, Any?>> =
        execute { api.scheduleTransaction(transactionId, com.melodi.sampahjujur.api.BackendScheduleRequest(scheduledAt)) }
=======
    suspend fun notifications(): Result<List<Map<String, Any?>>> = execute { api.notifications() }
    suspend fun markNotificationRead(id: String): Result<Map<String, Any?>> =
        execute { api.markNotificationRead(id) }
>>>>>>> f8f13893033af90e6bddcbdb82ab63c12f831ffd
    suspend fun transactions(): Result<List<Map<String, Any?>>> = execute { api.transactions() }
    suspend fun createHandover(request: BackendHandoverRequest): Result<Map<String, Any?>> =
        execute { api.createHandover(request) }
    suspend fun createPayment(request: BackendPaymentRequest): Result<Map<String, Any?>> =
        execute { api.createPayment(request) }
    suspend fun payments(): Result<List<Map<String, Any?>>> = execute { api.payments() }
    suspend fun confirmHandover(handoverId: String): Result<Map<String, Any?>> =
        execute { api.confirmHandover(handoverId) }
    suspend fun dashboard(role: String): Result<Map<String, Any?>> =
        execute { api.dashboard(role) }
    suspend fun trace(referenceId: String): Result<Map<String, Any?>> =
        execute { api.trace(referenceId) }

    private suspend fun <T> execute(
        call: suspend () -> retrofit2.Response<BackendEnvelope<T>>
    ): Result<T> {
        val response = call()
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.data == null) {
            return Result.failure(responseFailure(response.code(), body?.message ?: "Backend request failed"))
        }
        return Result.success(body.data)
    }

    private suspend fun executeAuth(
        call: suspend () -> retrofit2.Response<com.melodi.sampahjujur.api.BackendEnvelope<BackendAuthData>>
    ): Result<BackendAuthData> {
        val response = call()
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.data?.token.isNullOrBlank()) {
            return Result.failure(responseFailure(response.code(), body?.message ?: "Backend authentication failed"))
        }
        val data = body.data ?: return Result.failure(IllegalStateException("Backend returned no session"))
        session.saveToken(data.token)
        return Result.success(data)
    }

    private fun responseFailure(statusCode: Int, message: String): BackendApiException {
        if (statusCode == 401) session.invalidate()
        return BackendApiException(statusCode, message)
    }
}

class BackendApiException(val statusCode: Int, message: String) : IllegalStateException(message)
