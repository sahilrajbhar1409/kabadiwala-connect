package com.kabadiwalaconnect.data.backend

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class BackendEnvelope<T>(
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null
)

data class BackendAuthData(
    val token: String = "",
    val user: BackendUser = BackendUser()
)

data class BackendUser(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: String = ""
)

data class BackendLoginRequest(val identifier: String, val password: String)

data class BackendCreateLotRequest(
    val materialCategory: String,
    val materialDescription: String = "",
    val approximateWeight: Double,
    val city: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = "",
    val clientGeneratedId: String? = null
)

data class BackendCreateOfferRequest(
    val lotId: String,
    val quotedPrice: Double,
    val message: String = "",
    val pickupAvailable: Boolean = true
)

data class BackendHandoverRequest(
    val transactionId: String,
    val weight: Double,
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class BackendPaymentRequest(
    val transactionId: String,
    val amount: Double,
    val paymentMethod: String,
    val paymentReference: String? = null
)

interface BackendApiService {
    @POST("auth/login")
    suspend fun login(@Body request: BackendLoginRequest): Response<BackendEnvelope<BackendAuthData>>

    @GET("lots")
    suspend fun lots(
        @Query("status") status: String? = null,
        @Query("materialCategory") materialCategory: String? = null
    ): Response<BackendEnvelope<List<Map<String, Any?>>>>

    @POST("lots")
    suspend fun createLot(
        @Body request: BackendCreateLotRequest
    ): Response<BackendEnvelope<Map<String, Any?>>>

    @GET("offers")
    suspend fun offers(): Response<BackendEnvelope<List<Map<String, Any?>>>>

    @POST("offers")
    suspend fun createOffer(
        @Body request: BackendCreateOfferRequest
    ): Response<BackendEnvelope<Map<String, Any?>>>

    @GET("transactions")
    suspend fun transactions(): Response<BackendEnvelope<List<Map<String, Any?>>>>

    @POST("handovers")
    suspend fun createHandover(
        @Body request: BackendHandoverRequest
    ): Response<BackendEnvelope<Map<String, Any?>>>

    @POST("handovers/{id}/confirm")
    suspend fun confirmHandover(
        @Path("id") handoverId: String
    ): Response<BackendEnvelope<Map<String, Any?>>>

    @POST("payments")
    suspend fun createPayment(
        @Body request: BackendPaymentRequest
    ): Response<BackendEnvelope<Map<String, Any?>>>

    @GET("dashboard/{role}")
    suspend fun dashboard(
        @Path("role") role: String
    ): Response<BackendEnvelope<Map<String, Any?>>>

    @GET("trace/{referenceId}")
    suspend fun trace(
        @Path("referenceId") referenceId: String
    ): Response<BackendEnvelope<Map<String, Any?>>>
}
