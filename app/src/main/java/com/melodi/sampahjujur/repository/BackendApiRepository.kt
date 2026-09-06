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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendApiRepository @Inject constructor(
    private val api: BackendApiService,
    private val session: BackendSessionRepository
) {
    suspend fun login(identifier: String, password: String): Result<BackendAuthData> =
        executeAuth { api.login(BackendLoginRequest(identifier.trim(), password)) }

    suspend fun register(request: BackendRegisterRequest): Result<BackendAuthData> =
        executeAuth { api.register(request) }

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
    suspend fun createLot(request: BackendCreateLotRequest): Result<Map<String, Any?>> =
        execute { api.createLot(request) }
    suspend fun lots(): Result<List<Map<String, Any?>>> = execute { api.lots() }
    suspend fun myLots(): Result<List<Map<String, Any?>>> = execute { api.myLots() }
    suspend fun createOffer(request: BackendCreateOfferRequest): Result<Map<String, Any?>> =
        execute { api.createOffer(request) }
    suspend fun offers(): Result<List<Map<String, Any?>>> = execute { api.offers() }
    suspend fun transactions(): Result<List<Map<String, Any?>>> = execute { api.transactions() }
    suspend fun createHandover(request: BackendHandoverRequest): Result<Map<String, Any?>> =
        execute { api.createHandover(request) }
    suspend fun createPayment(request: BackendPaymentRequest): Result<Map<String, Any?>> =
        execute { api.createPayment(request) }
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
            return Result.failure(IllegalStateException(body?.message ?: "Backend request failed"))
        }
        return Result.success(body.data)
    }

    private suspend fun executeAuth(
        call: suspend () -> retrofit2.Response<com.melodi.sampahjujur.api.BackendEnvelope<BackendAuthData>>
    ): Result<BackendAuthData> {
        val response = call()
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.data?.token.isNullOrBlank()) {
            return Result.failure(IllegalStateException(body?.message ?: "Backend authentication failed"))
        }
        val data = body.data ?: return Result.failure(IllegalStateException("Backend returned no session"))
        session.saveToken(data.token)
        return Result.success(data)
    }
}
