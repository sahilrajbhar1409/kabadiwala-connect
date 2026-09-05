package com.melodi.sampahjujur.model

import com.google.firebase.firestore.PropertyName

/**
 * Domain model for a Collector's formal collection request (SIH 26229 - Person 4).
 * Links the entire chain: Lot ID -> Collector -> Scrap Material -> Recycler -> Status -> Payment.
 */
data class CollectionRequest(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("lotId") @set:PropertyName("lotId")
    var lotId: String = "",

    @get:PropertyName("collectorId") @set:PropertyName("collectorId")
    var collectorId: String = "",

    @get:PropertyName("recyclerId") @set:PropertyName("recyclerId")
    var recyclerId: String? = null,

    @get:PropertyName("recyclerName") @set:PropertyName("recyclerName")
    var recyclerName: String = "",

    @get:PropertyName("materials") @set:PropertyName("materials")
    var materials: List<ScrapMaterial> = emptyList(),

    @get:PropertyName("totalWeight") @set:PropertyName("totalWeight")
    var totalWeight: Double = 0.0,

    @get:PropertyName("quotedPrice") @set:PropertyName("quotedPrice")
    var quotedPrice: Double = 0.0,

    @get:PropertyName("finalSaleValue") @set:PropertyName("finalSaleValue")
    var finalSaleValue: Double = 0.0,

    @get:PropertyName("status") @set:PropertyName("status")
    var status: String = CollectionStatus.CREATED.name,

    @get:PropertyName("collectionLocation") @set:PropertyName("collectionLocation")
    var collectionLocation: String = "",

    @get:PropertyName("handoverLocation") @set:PropertyName("handoverLocation")
    var handoverLocation: String = "",

    @get:PropertyName("latitude") @set:PropertyName("latitude")
    var latitude: Double = 0.0,

    @get:PropertyName("longitude") @set:PropertyName("longitude")
    var longitude: Double = 0.0,

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis(),

    @get:PropertyName("scheduledAt") @set:PropertyName("scheduledAt")
    var scheduledAt: Long? = null,

    @get:PropertyName("collectedAt") @set:PropertyName("collectedAt")
    var collectedAt: Long? = null,

    @get:PropertyName("handedOverAt") @set:PropertyName("handedOverAt")
    var handedOverAt: Long? = null,

    @get:PropertyName("paymentMethod") @set:PropertyName("paymentMethod")
    var paymentMethod: String? = null,

    @get:PropertyName("paymentStatus") @set:PropertyName("paymentStatus")
    var paymentStatus: String = PaymentStatus.PENDING.name,

    @get:PropertyName("paymentReference") @set:PropertyName("paymentReference")
    var paymentReference: String? = null,

    @get:PropertyName("notes") @set:PropertyName("notes")
    var notes: String = "",

    @get:PropertyName("isSynced") @set:PropertyName("isSynced")
    var isSynced: Boolean = false
) {
    fun getCollectionStatus(): CollectionStatus =
        try {
            CollectionStatus.valueOf(status)
        } catch (e: Exception) {
            CollectionStatus.CREATED
        }

    fun getPaymentMethodEnum(): PaymentMethod? =
        paymentMethod?.let {
            try {
                PaymentMethod.valueOf(it)
            } catch (e: Exception) {
                null
            }
        }

    fun getPaymentStatusEnum(): PaymentStatus =
        try {
            PaymentStatus.valueOf(paymentStatus)
        } catch (e: Exception) {
            PaymentStatus.PENDING
        }

    fun isPaid(): Boolean = paymentStatus == PaymentStatus.PAID.name
    fun isCompleted(): Boolean = status == CollectionStatus.COMPLETED.name
}
