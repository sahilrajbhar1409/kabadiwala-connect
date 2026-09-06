package com.melodi.sampahjujur.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class BackendEnvelope<T>(
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null
)

data class BackendUser(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: String = "",
    val preferredLanguage: String = "english",
    val profileImage: String = "",
    val generalLocation: String = "",
    val isActive: Boolean = true
)

data class BackendAuthData(
    val token: String = "",
    val user: BackendUser = BackendUser()
)

data class BackendLoginRequest(
    val identifier: String,
    val password: String
)

data class BackendRegisterRequest(
    val name: String,
    val phone: String,
    val email: String? = null,
    val password: String,
    val role: String,
    val preferredLanguage: String? = null,
    val generalLocation: String? = null,
    val companyName: String? = null,
    val authorizationNumber: String? = null,
    val acceptedMaterials: List<String>? = null,
    val serviceAreas: List<String>? = null,
    val pickupAvailable: Boolean? = null
)

data class BackendCreateLotRequest(
    val materialCategory: String,
    val materialDescription: String = "",
    val approximateWeight: Double,
    val weightUnit: String = "kg",
    val city: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = "",
    val photos: List<String> = emptyList()
)

data class BackendCreateOfferRequest(
    val lotId: String,
    val quotedPrice: Double,
    val message: String = "",
    val pickupAvailable: Boolean = true
)

data class BackendScheduleRequest(val scheduledAt: String)
data class BackendPaymentRequest(
    val transactionId: String,
    val amount: Double,
    val paymentMethod: String,
    val paymentStatus: String = "PAID",
    val paymentReference: String? = null
)

data class BackendHandoverRequest(
    val transactionId: String,
    val weight: Double,
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photos: List<String> = emptyList()
)

data class BackendSyncRequest(val items: List<BackendSyncItem>)
data class BackendSyncItem(
    val clientGeneratedId: String,
    val action: String,
    val payload: Map<String, Any?> = emptyMap()
)

interface BackendApiService {
    @POST("auth/register")
    suspend fun register(@Body request: BackendRegisterRequest): Response<BackendEnvelope<BackendAuthData>>

    @POST("auth/login")
    suspend fun login(@Body request: BackendLoginRequest): Response<BackendEnvelope<BackendAuthData>>

    @GET("auth/me")
    suspend fun currentUser(): Response<BackendEnvelope<Map<String, Any?>>>

    @POST("auth/logout")
    suspend fun logout(): Response<BackendEnvelope<Any>>

    @GET("users/me")
    suspend fun profile(): Response<BackendEnvelope<BackendUser>>

    @PATCH("users/me")
    suspend fun updateProfile(@Body fields: Map<String, Any?>): Response<BackendEnvelope<BackendUser>>

    @GET("materials")
    suspend fun materials(): Response<BackendEnvelope<List<Map<String, Any?>>>>

    @GET("prices")
    suspend fun prices(
        @Query("category") category: String? = null,
        @Query("location") location: String? = null,
        @Query("weight") weight: Double? = null
    ): Response<BackendEnvelope<Any>>

    @GET("prices/trends")
    suspend fun priceTrends(
        @Query("category") category: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<BackendEnvelope<Any>>

    @POST("lots")
    suspend fun createLot(@Body request: BackendCreateLotRequest): Response<BackendEnvelope<Map<String, Any?>>>

    @GET("lots")
    suspend fun lots(): Response<BackendEnvelope<List<Map<String, Any?>>>>

    @GET("lots/my-lots")
    suspend fun myLots(): Response<BackendEnvelope<List<Map<String, Any?>>>>

    @GET("lots/{id}")
    suspend fun lot(@Path("id") id: String): Response<BackendEnvelope<Map<String, Any?>>>

    @GET("lots/{id}/matches")
    suspend fun lotMatches(@Path("id") id: String): Response<BackendEnvelope<Any>>

    @PATCH("lots/{id}")
    suspend fun updateLot(
        @Path("id") id: String,
        @Body fields: Map<String, Any?>
    ): Response<BackendEnvelope<Map<String, Any?>>>

    @DELETE("lots/{id}")
    suspend fun deleteLot(@Path("id") id: String): Response<BackendEnvelope<Any>>

    @GET("recyclers")
    suspend fun recyclers(): Response<BackendEnvelope<List<Map<String, Any?>>>>

    @GET("recyclers/nearby")
    suspend fun nearbyRecyclers(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Double? = null
    ): Response<BackendEnvelope<List<Map<String, Any?>>>>

    @POST("offers")
    suspend fun createOffer(@Body request: BackendCreateOfferRequest): Response<BackendEnvelope<Map<String, Any?>>>

    @GET("offers")
    suspend fun offers(): Response<BackendEnvelope<List<Map<String, Any?>>>>

    @POST("offers/{id}/accept")
    suspend fun acceptOffer(
        @Path("id") id: String,
        @Body request: BackendScheduleRequest
    ): Response<BackendEnvelope<Map<String, Any?>>>

    @POST("offers/{id}/reject")
    suspend fun rejectOffer(@Path("id") id: String): Response<BackendEnvelope<Any>>

    @GET("transactions")
    suspend fun transactions(): Response<BackendEnvelope<List<Map<String, Any?>>>>

    @POST("transactions/{id}/schedule")
    suspend fun scheduleTransaction(
        @Path("id") id: String,
        @Body request: BackendScheduleRequest
    ): Response<BackendEnvelope<Map<String, Any?>>>

    @POST("handovers")
    suspend fun createHandover(@Body request: BackendHandoverRequest): Response<BackendEnvelope<Map<String, Any?>>>

    @POST("handovers/{id}/confirm")
    suspend fun confirmHandover(@Path("id") id: String): Response<BackendEnvelope<Map<String, Any?>>>

    @POST("payments")
    suspend fun createPayment(@Body request: BackendPaymentRequest): Response<BackendEnvelope<Map<String, Any?>>>

    @GET("payments")
    suspend fun payments(): Response<BackendEnvelope<List<Map<String, Any?>>>>

    @GET("dashboard/{role}")
    suspend fun dashboard(@Path("role") role: String): Response<BackendEnvelope<Map<String, Any?>>>

    @GET("notifications")
    suspend fun notifications(): Response<BackendEnvelope<List<Map<String, Any?>>>>

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): Response<BackendEnvelope<Map<String, Any?>>>

    @GET("admin/analytics/overview")
    suspend fun analyticsOverview(): Response<BackendEnvelope<Map<String, Any?>>>

    @GET("trace/{referenceId}")
    suspend fun trace(@Path("referenceId") referenceId: String): Response<BackendEnvelope<Map<String, Any?>>>

    @POST("sync")
    suspend fun sync(@Body request: BackendSyncRequest): Response<BackendEnvelope<Map<String, Any?>>>
}
