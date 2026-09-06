package com.kabadiwalaconnect.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part

data class ApiEnvelope<T>(
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null
)

data class AuthData(val token: String = "", val user: BackendUser = BackendUser())
data class BackendUser(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: String = ""
)
data class LoginRequest(val identifier: String, val password: String)
data class RegisterRequest(
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
data class CreateLotRequest(
    val materialCategory: String,
    val materialDescription: String = "",
    val approximateWeight: Double,
    val weightUnit: String = "kg",
    val city: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notes: String = "",
    val clientGeneratedId: String? = null,
    val photos: List<String> = emptyList()
)
data class CreateOfferRequest(
    val lotId: String,
    val quotedPrice: Double,
    val estimatedPickupDate: String? = null,
    val pickupAvailable: Boolean = true,
    val message: String = ""
)
data class ScheduleRequest(val scheduledAt: String)
data class HandoverRequest(
    val transactionId: String,
    val weight: Double,
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)
data class PaymentRequest(
    val transactionId: String,
    val amount: Double,
    val paymentMethod: String,
    val paymentReference: String? = null
)

interface ApiService {
    @Multipart
    @POST("uploads")
    suspend fun uploadImages(@Part photos: List<MultipartBody.Part>): Response<ApiEnvelope<Map<String, Any?>>>

    @GET("health")
    suspend fun health(): Response<ApiEnvelope<Map<String, Any?>>>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiEnvelope<AuthData>>
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiEnvelope<AuthData>>
    @GET("auth/me")
    suspend fun currentUser(): Response<ApiEnvelope<Map<String, Any?>>>
    @POST("auth/logout")
    suspend fun logout(): Response<ApiEnvelope<Any>>

    @GET("dashboard/{role}")
    suspend fun dashboard(@Path("role") role: String): Response<ApiEnvelope<Map<String, Any?>>>

    @POST("lots")
    suspend fun createLot(@Body request: CreateLotRequest): Response<ApiEnvelope<Map<String, Any?>>>
    @GET("lots")
    suspend fun lots(
        @Query("status") status: String? = null,
        @Query("materialCategory") materialCategory: String? = null
    ): Response<ApiEnvelope<List<Map<String, Any?>>>>
    @GET("lots/my-lots")
    suspend fun myLots(): Response<ApiEnvelope<List<Map<String, Any?>>>>
    @GET("lots/{id}")
    suspend fun lot(@Path("id") id: String): Response<ApiEnvelope<Map<String, Any?>>>
    @GET("lots/{id}/matches")
    suspend fun lotMatches(@Path("id") id: String): Response<ApiEnvelope<Any>>
    @PATCH("lots/{id}")
    suspend fun updateLot(@Path("id") id: String, @Body fields: Map<String, Any?>): Response<ApiEnvelope<Map<String, Any?>>>
    @DELETE("lots/{id}")
    suspend fun deleteLot(@Path("id") id: String): Response<ApiEnvelope<Any>>

    @GET("offers")
    suspend fun offers(): Response<ApiEnvelope<List<Map<String, Any?>>>>
    @POST("offers")
    suspend fun createOffer(@Body request: CreateOfferRequest): Response<ApiEnvelope<Map<String, Any?>>>
    @GET("offers/{id}")
    suspend fun offer(@Path("id") id: String): Response<ApiEnvelope<Map<String, Any?>>>
    @PATCH("offers/{id}")
    suspend fun updateOffer(@Path("id") id: String, @Body fields: Map<String, Any?>): Response<ApiEnvelope<Map<String, Any?>>>
    @POST("offers/{id}/accept")
    suspend fun acceptOffer(@Path("id") id: String, @Body request: ScheduleRequest): Response<ApiEnvelope<Map<String, Any?>>>
    @POST("offers/{id}/reject")
    suspend fun rejectOffer(@Path("id") id: String): Response<ApiEnvelope<Any>>

    @GET("transactions")
    suspend fun transactions(): Response<ApiEnvelope<List<Map<String, Any?>>>>
    @GET("transactions/{id}")
    suspend fun transaction(@Path("id") id: String): Response<ApiEnvelope<Map<String, Any?>>>
    @POST("transactions/{id}/schedule")
    suspend fun scheduleTransaction(@Path("id") id: String, @Body request: ScheduleRequest): Response<ApiEnvelope<Map<String, Any?>>>

    @POST("handovers")
    suspend fun createHandover(@Body request: HandoverRequest): Response<ApiEnvelope<Map<String, Any?>>>
    @GET("handovers/{id}")
    suspend fun handover(@Path("id") id: String): Response<ApiEnvelope<Map<String, Any?>>>
    @POST("handovers/{id}/confirm")
    suspend fun confirmHandover(@Path("id") id: String): Response<ApiEnvelope<Map<String, Any?>>>

    @POST("payments")
    suspend fun createPayment(@Body request: PaymentRequest): Response<ApiEnvelope<Map<String, Any?>>>
    @GET("payments")
    suspend fun payments(): Response<ApiEnvelope<List<Map<String, Any?>>>>
    @GET("payments/{id}")
    suspend fun payment(@Path("id") id: String): Response<ApiEnvelope<Map<String, Any?>>>

    @GET("materials")
    suspend fun materials(): Response<ApiEnvelope<List<Map<String, Any?>>>>
    @GET("prices")
    suspend fun prices(@Query("category") category: String? = null, @Query("location") location: String? = null): Response<ApiEnvelope<Any>>
    @GET("prices/trends")
    suspend fun priceTrends(@Query("category") category: String? = null): Response<ApiEnvelope<Any>>
    @GET("prices/material/{materialId}")
    suspend fun materialPrice(@Path("materialId") materialId: String): Response<ApiEnvelope<Any>>

    @GET("recyclers")
    suspend fun recyclers(): Response<ApiEnvelope<List<Map<String, Any?>>>>
    @GET("recyclers/nearby")
    suspend fun nearbyRecyclers(@Query("lat") latitude: Double, @Query("lng") longitude: Double): Response<ApiEnvelope<List<Map<String, Any?>>>>
    @GET("recyclers/{id}")
    suspend fun recycler(@Path("id") id: String): Response<ApiEnvelope<Map<String, Any?>>>
    @PATCH("recyclers/me")
    suspend fun updateRecycler(@Body fields: Map<String, Any?>): Response<ApiEnvelope<Map<String, Any?>>>

    @GET("notifications")
    suspend fun notifications(): Response<ApiEnvelope<List<Map<String, Any?>>>>
    @PATCH("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): Response<ApiEnvelope<Map<String, Any?>>>
    @GET("users/me")
    suspend fun profile(): Response<ApiEnvelope<BackendUser>>
    @PATCH("users/me")
    suspend fun updateProfile(@Body fields: Map<String, Any?>): Response<ApiEnvelope<BackendUser>>
    @GET("admin/analytics/overview")
    suspend fun analyticsOverview(): Response<ApiEnvelope<Map<String, Any?>>>
    @GET("trace/{referenceId}")
    suspend fun trace(@Path("referenceId") referenceId: String): Response<ApiEnvelope<Map<String, Any?>>>
    @POST("trace/sync")
    suspend fun syncTrace(@Body payload: Map<String, Any?>): Response<ApiEnvelope<Map<String, Any?>>>
}
