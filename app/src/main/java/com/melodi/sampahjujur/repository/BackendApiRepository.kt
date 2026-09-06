package com.melodi.sampahjujur.repository

import com.melodi.sampahjujur.api.BackendApiService
import com.melodi.sampahjujur.api.BackendAuthData
import com.melodi.sampahjujur.api.BackendLoginRequest
import com.melodi.sampahjujur.api.BackendRegisterRequest
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
        if (!response.isSuccessful || response.body()?.success != true) {
            return Result.failure(IllegalStateException(response.body()?.message ?: "Backend logout failed"))
        }
        session.clear()
        return Result.success(Unit)
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
