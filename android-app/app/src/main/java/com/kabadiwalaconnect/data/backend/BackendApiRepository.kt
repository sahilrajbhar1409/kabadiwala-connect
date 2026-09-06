package com.kabadiwalaconnect.data.backend

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class BackendApiRepository(
    private val api: BackendApiService,
    private val session: BackendSessionRepository
) {
    fun isAuthenticated(): Boolean = !session.token().isNullOrBlank()

    suspend fun login(identifier: String, password: String): Result<BackendAuthData> =
        executeAuth { api.login(BackendLoginRequest(identifier.trim(), password)) }

    suspend fun lots(
        status: String? = null,
        materialCategory: String? = null
    ): Result<List<Map<String, Any?>>> = execute { api.lots(status, materialCategory) }

    suspend fun createLot(request: BackendCreateLotRequest): Result<Map<String, Any?>> =
        execute { api.createLot(request) }

    suspend fun offers(): Result<List<Map<String, Any?>>> = execute { api.offers() }

    suspend fun createOffer(request: BackendCreateOfferRequest): Result<Map<String, Any?>> =
        execute { api.createOffer(request) }

    suspend fun transactions(): Result<List<Map<String, Any?>>> =
        execute { api.transactions() }

    suspend fun createHandover(request: BackendHandoverRequest): Result<Map<String, Any?>> =
        execute { api.createHandover(request) }

    suspend fun confirmHandover(handoverId: String): Result<Map<String, Any?>> =
        execute { api.confirmHandover(handoverId) }

    suspend fun createPayment(request: BackendPaymentRequest): Result<Map<String, Any?>> =
        execute { api.createPayment(request) }

    suspend fun dashboard(role: String): Result<Map<String, Any?>> =
        execute { api.dashboard(role) }

    suspend fun trace(referenceId: String): Result<Map<String, Any?>> =
        execute { api.trace(referenceId) }

    private suspend fun <T> execute(
        call: suspend () -> Response<BackendEnvelope<T>>
    ): Result<T> = withContext(Dispatchers.IO) {
        val response = call()
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.data == null) {
            Result.failure(IllegalStateException(body?.message ?: "Backend request failed"))
        } else {
            Result.success(body.data)
        }
    }

    private suspend fun executeAuth(
        call: suspend () -> Response<BackendEnvelope<BackendAuthData>>
    ): Result<BackendAuthData> = withContext(Dispatchers.IO) {
        val response = call()
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.data?.token.isNullOrBlank()) {
            Result.failure(IllegalStateException(body?.message ?: "Backend authentication failed"))
        } else {
            val data = body.data ?: return@withContext Result.failure(
                IllegalStateException("Backend returned no session")
            )
            session.saveToken(data.token)
            Result.success(data)
        }
    }

    fun logFailure(operation: String, error: Throwable) {
        Log.w(TAG, "$operation unavailable: ${error.message}")
    }

    private companion object {
        const val TAG = "BackendApiRepository"
    }
}
