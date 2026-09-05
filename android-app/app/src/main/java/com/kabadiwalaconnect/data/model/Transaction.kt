package com.kabadiwalaconnect.data.model

data class Transaction(
    val id: String,
    val lotId: String,
    val payerId: String,
    val payeeId: String,
    val amount: Double,
    val status: TransactionStatus,
    val createdAt: String,
    val updatedAt: String,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI
)

enum class PaymentMethod {
    CASH,
    UPI,
    BANK
}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}
