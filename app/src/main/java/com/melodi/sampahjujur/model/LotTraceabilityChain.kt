package com.melodi.sampahjujur.model

/**
 * Audit trail / traceability chain connecting every step of a recyclable lot (SIH 26229 - Person 4).
 * LOT ID -> Collection Request -> Collector -> Material -> Recycler -> Collection -> Handover -> Confirmation -> Payment -> Completed.
 */
data class LotTraceabilityChain(
    val lotId: String,
    val collectionRequest: CollectionRequest?,
    val handoverRecord: HandoverRecord?,
    val collector: User?,
    val recycler: Recycler?,
    val materials: List<ScrapMaterial>,
    val timeline: List<TraceabilityEvent>
)

data class TraceabilityEvent(
    val stage: CollectionStatus,
    val title: String,
    val description: String,
    val timestamp: Long,
    val location: String = "",
    val isCompleted: Boolean = true
)
