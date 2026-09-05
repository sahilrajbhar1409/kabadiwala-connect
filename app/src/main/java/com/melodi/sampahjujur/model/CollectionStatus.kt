package com.melodi.sampahjujur.model

/**
 * Lifecycle status for a Collection Request in Kabadiwala Connect (SIH 26229 - Person 4).
 * Enforces valid state transitions to preserve data integrity across the recycling chain.
 */
enum class CollectionStatus(val displayName: String) {
    CREATED("Created"),
    PENDING_RECYCLER("Pending Recycler"),
    RECYCLER_ASSIGNED("Recycler Assigned"),
    ACCEPTED("Accepted by Recycler"),
    PICKUP_SCHEDULED("Pickup Scheduled"),
    SCHEDULED("Scheduled"),
    COLLECTED("Collected"),
    HANDOVER_PENDING("Handover Pending"),
    HANDED_OVER("Handed Over"),
    PAYMENT_PENDING("Payment Pending"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    REJECTED("Rejected");

    /**
     * Guards against invalid transitions in the state machine.
     */
    fun canTransitionTo(next: CollectionStatus): Boolean {
        if (this == next) return true
        return when (this) {
            CREATED -> next in setOf(PENDING_RECYCLER, RECYCLER_ASSIGNED, CANCELLED)
            PENDING_RECYCLER -> next in setOf(RECYCLER_ASSIGNED, CANCELLED)
            RECYCLER_ASSIGNED -> next in setOf(ACCEPTED, REJECTED, CANCELLED)
            ACCEPTED -> next in setOf(PICKUP_SCHEDULED, SCHEDULED, COLLECTED, CANCELLED)
            PICKUP_SCHEDULED, SCHEDULED -> next in setOf(COLLECTED, CANCELLED)
            COLLECTED -> next in setOf(HANDOVER_PENDING, HANDED_OVER, CANCELLED)
            HANDOVER_PENDING -> next in setOf(HANDED_OVER, CANCELLED)
            HANDED_OVER -> next in setOf(PAYMENT_PENDING, COMPLETED, CANCELLED)
            PAYMENT_PENDING -> next in setOf(COMPLETED, CANCELLED)
            COMPLETED, CANCELLED, REJECTED -> false
        }
    }

    fun isTerminal(): Boolean = this in setOf(COMPLETED, CANCELLED, REJECTED)
    fun isActive(): Boolean = !isTerminal()

    companion object {
        fun fromString(name: String?): CollectionStatus {
            if (name.isNullOrBlank()) return CREATED
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: CREATED
        }
    }
}
