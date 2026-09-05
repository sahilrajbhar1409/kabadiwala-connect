package com.melodi.sampahjujur.model

import android.net.Uri

/**
 * Payment methods supported in Kabadiwala Connect (SIH 26229 - Person 4).
 * CASH is fully offline-capable; UPI launches genuine UPI intents.
 */
enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    UPI("UPI")
}

/**
 * Payment lifecycle status.
 */
enum class PaymentStatus(val displayName: String) {
    PENDING("Pending"),
    PAID("Paid"),
    FAILED("Failed")
}

/**
 * Helper to construct standard Android UPI intent URIs without fake gateways.
 * Scheme: upi://pay?pa={vpa}&pn={name}&mc={code}&tr={ref}&am={amount}&cu=INR&tn={note}
 */
object UpiHelper {
    fun buildUpiUri(
        payeeVpa: String,
        payeeName: String,
        amount: Double,
        transactionRef: String,
        transactionNote: String
    ): Uri {
        val amountStr = String.format(java.util.Locale.US, "%.2f", amount)
        return Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", payeeVpa)
            .appendQueryParameter("pn", payeeName)
            .appendQueryParameter("tr", transactionRef)
            .appendQueryParameter("am", amountStr)
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", transactionNote)
            .build()
    }
}
