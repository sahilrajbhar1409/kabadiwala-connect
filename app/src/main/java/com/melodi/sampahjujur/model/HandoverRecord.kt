package com.melodi.sampahjujur.model

import com.google.firebase.firestore.PropertyName

/**
 * Immutable Digital Handover Record (SIH 26229 - Person 4).
 * Generated after handover verification between informal collector and authorized recycler.
 * Fully traceable using Lot ID.
 */
data class HandoverRecord(
    @get:PropertyName("handoverId") @set:PropertyName("handoverId")
    var handoverId: String = "",

    @get:PropertyName("handoverReference") @set:PropertyName("handoverReference")
    var handoverReference: String = "",

    @get:PropertyName("lotId") @set:PropertyName("lotId")
    var lotId: String = "",

    @get:PropertyName("collectorId") @set:PropertyName("collectorId")
    var collectorId: String = "",

    @get:PropertyName("recyclerId") @set:PropertyName("recyclerId")
    var recyclerId: String = "",

    @get:PropertyName("recyclerName") @set:PropertyName("recyclerName")
    var recyclerName: String = "",

    @get:PropertyName("materials") @set:PropertyName("materials")
    var materials: List<ScrapMaterial> = emptyList(),

    @get:PropertyName("weight") @set:PropertyName("weight")
    var weight: Double = 0.0,

    @get:PropertyName("collectionLocation") @set:PropertyName("collectionLocation")
    var collectionLocation: String = "",

    @get:PropertyName("handoverLocation") @set:PropertyName("handoverLocation")
    var handoverLocation: String = "",

    @get:PropertyName("latitude") @set:PropertyName("latitude")
    var latitude: Double = 0.0,

    @get:PropertyName("longitude") @set:PropertyName("longitude")
    var longitude: Double = 0.0,

    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis(),

    @get:PropertyName("quotedPrice") @set:PropertyName("quotedPrice")
    var quotedPrice: Double = 0.0,

    @get:PropertyName("finalSaleValue") @set:PropertyName("finalSaleValue")
    var finalSaleValue: Double = 0.0,

    @get:PropertyName("paymentMethod") @set:PropertyName("paymentMethod")
    var paymentMethod: String = PaymentMethod.CASH.name,

    @get:PropertyName("paymentStatus") @set:PropertyName("paymentStatus")
    var paymentStatus: String = PaymentStatus.PENDING.name,

    @get:PropertyName("recyclerConfirmed") @set:PropertyName("recyclerConfirmed")
    var recyclerConfirmed: Boolean = false,

    @get:PropertyName("recyclerConfirmedAt") @set:PropertyName("recyclerConfirmedAt")
    var recyclerConfirmedAt: Long? = null,

    @get:PropertyName("transactionStatus") @set:PropertyName("transactionStatus")
    var transactionStatus: String = CollectionStatus.HANDED_OVER.name,

    @get:PropertyName("isSynced") @set:PropertyName("isSynced")
    var isSynced: Boolean = false
) {
    fun getCollectionStatus(): CollectionStatus =
        try {
            CollectionStatus.valueOf(transactionStatus)
        } catch (e: Exception) {
            CollectionStatus.HANDED_OVER
        }

    fun getPaymentMethodEnum(): PaymentMethod =
        try {
            PaymentMethod.valueOf(paymentMethod)
        } catch (e: Exception) {
            PaymentMethod.CASH
        }

    fun getPaymentStatusEnum(): PaymentStatus =
        try {
            PaymentStatus.valueOf(paymentStatus)
        } catch (e: Exception) {
            PaymentStatus.PENDING
        }
}
