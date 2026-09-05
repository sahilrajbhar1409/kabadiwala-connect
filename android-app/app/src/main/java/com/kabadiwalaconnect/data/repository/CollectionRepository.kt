package com.kabadiwalaconnect.data.repository

import com.kabadiwalaconnect.data.model.CollectionRequest
import com.kabadiwalaconnect.data.model.CollectionRequestStatus
import com.kabadiwalaconnect.data.model.HandoverRecord
import com.kabadiwalaconnect.data.model.HandoverStatus
import com.kabadiwalaconnect.data.model.Lot
import com.kabadiwalaconnect.data.model.LotStatus
import com.kabadiwalaconnect.data.model.RecyclingRecord
import com.kabadiwalaconnect.data.model.RecyclingStatus
import com.kabadiwalaconnect.data.model.Transaction
import com.kabadiwalaconnect.data.model.TransactionStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

interface CollectionRepository {
    fun createCollectionRequest(request: CollectionRequest): CollectionRequest

    fun getCollectionRequests(): List<CollectionRequest>

    fun getCollectionRequest(id: String): CollectionRequest?

    fun updateCollectionRequest(request: CollectionRequest): CollectionRequest

    fun createLot(lot: Lot): Lot

    fun getLot(id: String): Lot?

    fun getLots(): List<Lot> = emptyList()

    fun getLatestLot(): Lot? = getLots().lastOrNull()

    fun nextLotId(): String = "KC-${SimpleDateFormat("yyyy", Locale.US).format(Date())}-000001"

    fun updateLot(lot: Lot): Lot

    /** Requests which a collector can still claim. */
    fun getPendingCollectionRequests(): List<CollectionRequest> =
        getCollectionRequests().filter {
            it.status == CollectionRequestStatus.REQUESTED ||
                it.status == CollectionRequestStatus.PENDING
        }
    fun getPendingRequests(): List<CollectionRequest> = getPendingCollectionRequests()

    fun getAssignedRequests(collectorId: String): List<CollectionRequest> {
        val assignedRequestIds = getCollectorLots(collectorId)
            .filter { it.status != LotStatus.CANCELLED }
            .map { it.requestId }
            .toSet()
        return getCollectionRequests().filter { it.id in assignedRequestIds }
    }

    fun assignCollector(lotId: String, collectorId: String): Lot {
        val lot = getLot(lotId) ?: error("Lot not found: $lotId")
        require(lot.status == LotStatus.REQUESTED) {
            "Only a requested lot can be assigned."
        }
        return acceptCollectionRequest(
            requestId = lot.requestId,
            collectorId = collectorId
        )
    }

    fun updateLotStatus(lotId: String, status: LotStatus): Lot

    fun acceptCollectionRequest(requestId: String, collectorId: String): Lot
    fun rejectCollectionRequest(requestId: String): CollectionRequest
    fun rejectCollectionRequest(requestId: String, collectorId: String): CollectionRequest =
        rejectCollectionRequest(requestId)
    fun acceptRequest(requestId: String, collectorId: String): Lot =
        acceptCollectionRequest(requestId, collectorId)
    fun rejectRequest(requestId: String): CollectionRequest =
        rejectCollectionRequest(requestId)
    fun startPickup(lotId: String, collectorId: String): Lot
    fun startCollection(lotId: String, collectorId: String): Lot =
        startPickup(lotId, collectorId)
    fun completePickup(
        lotId: String,
        collectorId: String,
        actualWeight: Double,
        actualValue: Double? = null
    ): Lot
    fun collectLot(
        lotId: String,
        collectorId: String,
        actualWeight: Double,
        actualValue: Double? = null
    ): Lot = completePickup(lotId, collectorId, actualWeight, actualValue)

    fun createHandoverRecord(record: HandoverRecord): HandoverRecord
    fun getHandoverRecords(collectorId: String): List<HandoverRecord>
    fun recordHandover(
        lotId: String,
        collectorId: String,
        recyclerId: String,
        location: String,
        actualWeight: Double,
        actualValue: Double
    ): HandoverRecord
    fun handoverLot(
        lotId: String,
        collectorId: String,
        recyclerId: String,
        location: String,
        actualWeight: Double,
        actualValue: Double
    ): HandoverRecord = recordHandover(
        lotId, collectorId, recyclerId, location, actualWeight, actualValue
    )

    fun getCollectorLots(collectorId: String): List<Lot> =
        getLots().filter { it.collectorId == collectorId }

    fun getCollectorEarnings(collectorId: String): Double =
        getCollectorLots(collectorId)
            .filter { it.status.isCompletedCollection() }
            .sumOf { it.actualValue ?: 0.0 }

    fun getCollectorHistory(collectorId: String): List<Lot> =
        getCollectorLots(collectorId).filter { it.status.isCompletedCollection() }.asReversed()
    fun getEarnings(collectorId: String): Double = getCollectorEarnings(collectorId)
    fun getHistory(collectorId: String): List<Lot> = getCollectorHistory(collectorId)

    /** Lots which have left the collector chain and can be received by a recycler. */
    fun getAvailableHandedOverLots(): List<Lot> =
        getLots().filter { it.status == LotStatus.HANDED_OVER }.asReversed()

    fun getAvailableRecyclerLots(): List<Lot> = getAvailableHandedOverLots()

    /** Lots that are assigned to, or already processed by, this recycler. */
    fun getIncomingLots(recyclerId: String): List<Lot> =
        getLots().filter {
            it.recyclerId == recyclerId &&
                it.status != LotStatus.CANCELLED &&
                it.status != LotStatus.RECYCLED
        }.asReversed()

    fun getRecyclerLots(recyclerId: String): List<Lot> =
        getLots().filter { it.recyclerId == recyclerId }.asReversed()

    fun assignRecycler(lotId: String, recyclerId: String): Lot
    fun confirmRecyclerReceipt(lotId: String, recyclerId: String): Lot
    fun confirmHandover(lotId: String, recyclerId: String): Lot =
        confirmRecyclerReceipt(lotId, recyclerId)

    fun createRecyclingRecord(record: RecyclingRecord): RecyclingRecord
    fun getRecyclingRecords(recyclerId: String): List<RecyclingRecord>
    fun recordRecycling(
        lotId: String,
        recyclerId: String,
        processedWeight: Double,
        actualMaterial: String = "",
        facility: String = "",
        notes: String = "",
        recyclingDate: String = ""
    ): RecyclingRecord

    fun createTransaction(transaction: Transaction): Transaction
    fun getRecyclerTransactions(recyclerId: String): List<Transaction>
    fun payRecycler(
        lotId: String,
        recyclerId: String,
        paymentMethod: com.kabadiwalaconnect.data.model.PaymentMethod =
            com.kabadiwalaconnect.data.model.PaymentMethod.UPI
    ): Transaction

    fun getRecyclerPayments(recyclerId: String): List<Transaction> =
        getRecyclerTransactions(recyclerId)

    fun getRecyclerHistory(recyclerId: String): List<Lot> =
        getRecyclerLots(recyclerId).filter { it.status == LotStatus.RECYCLED }
}

class InMemoryCollectionRepository : CollectionRepository {
    private val collectionRequests = LinkedHashMap<String, CollectionRequest>()
    private val lots = LinkedHashMap<String, Lot>()
    private val handovers = LinkedHashMap<String, HandoverRecord>()
    private val recyclingRecords = LinkedHashMap<String, RecyclingRecord>()
    private val transactions = LinkedHashMap<String, Transaction>()
    private var lotSequence = 0

    @Synchronized
    override fun createCollectionRequest(request: CollectionRequest): CollectionRequest {
        require(!collectionRequests.containsKey(request.id)) {
            "Collection request already exists: ${request.id}"
        }
        collectionRequests[request.id] = request
        return request
    }

    @Synchronized
    override fun getCollectionRequests(): List<CollectionRequest> =
        collectionRequests.values.toList()

    @Synchronized
    override fun getCollectionRequest(id: String): CollectionRequest? =
        collectionRequests[id]

    @Synchronized
    override fun updateCollectionRequest(request: CollectionRequest): CollectionRequest {
        require(collectionRequests.containsKey(request.id)) {
            "Collection request not found: ${request.id}"
        }
        collectionRequests[request.id] = request
        return request
    }

    @Synchronized
    override fun createLot(lot: Lot): Lot {
        require(!lots.containsKey(lot.lotId)) {
            "Lot already exists: ${lot.lotId}"
        }
        lots[lot.lotId] = lot
        return lot
    }

    @Synchronized
    override fun getLot(id: String): Lot? = lots[id]

    @Synchronized
    override fun getLots(): List<Lot> = lots.values.toList()

    @Synchronized
    override fun getLatestLot(): Lot? = lots.values.lastOrNull()

    @Synchronized
    override fun nextLotId(): String {
        lotSequence += 1
        val year = SimpleDateFormat("yyyy", Locale.US).format(Date())
        return "KC-$year-${lotSequence.toString().padStart(6, '0')}"
    }

    @Synchronized
    override fun updateLot(lot: Lot): Lot {
        require(lots.containsKey(lot.lotId)) {
            "Lot not found: ${lot.lotId}"
        }
        lots[lot.lotId] = lot
        return lot
    }

    @Synchronized
    override fun getPendingCollectionRequests(): List<CollectionRequest> =
        collectionRequests.values.filter {
            it.status == CollectionRequestStatus.REQUESTED ||
                it.status == CollectionRequestStatus.PENDING
        }

    @Synchronized
    override fun getAssignedRequests(collectorId: String): List<CollectionRequest> {
        require(collectorId.isNotBlank()) { "Collector id is required." }
        val assignedRequestIds = lots.values
            .filter { it.collectorId == collectorId && it.status != LotStatus.CANCELLED }
            .map { it.requestId }
            .toSet()
        return collectionRequests.values.filter { it.id in assignedRequestIds }
    }

    @Synchronized
    override fun assignCollector(lotId: String, collectorId: String): Lot {
        require(collectorId.isNotBlank()) { "Collector id is required." }
        val lot = lots[lotId] ?: error("Lot not found: $lotId")
        require(lot.status == LotStatus.REQUESTED) {
            "Only a requested lot can be assigned."
        }
        return acceptCollectionRequest(lot.requestId, collectorId)
    }

    @Synchronized
    override fun updateLotStatus(lotId: String, status: LotStatus): Lot {
        val lot = lots[lotId] ?: error("Lot not found: $lotId")
        require(isValidTransition(lot.status, status)) {
            "Invalid lot status transition: ${lot.status} to $status"
        }
        val updated = lot.copy(status = status, updatedAt = now())
        lots[lotId] = updated
        return updated
    }

    @Synchronized
    override fun acceptCollectionRequest(requestId: String, collectorId: String): Lot {
        require(collectorId.isNotBlank()) { "Collector id is required." }
        val request = collectionRequests[requestId] ?: error("Collection request not found: $requestId")
        require(request.status == CollectionRequestStatus.REQUESTED ||
            request.status == CollectionRequestStatus.PENDING) {
            "This request is no longer available."
        }
        val lot = lots.values.firstOrNull { it.requestId == requestId }
            ?: error("Lot not found for request: $requestId")
        val timestamp = now()
        collectionRequests[requestId] = request.copy(
            status = CollectionRequestStatus.ASSIGNED,
            updatedAt = timestamp
        )
        val acceptedLot = lot.copy(
            collectorId = collectorId,
            status = LotStatus.ACCEPTED,
            updatedAt = timestamp
        )
        lots[lot.lotId] = acceptedLot
        return acceptedLot
    }

    @Synchronized
    override fun rejectCollectionRequest(requestId: String): CollectionRequest {
        val request = collectionRequests[requestId] ?: error("Collection request not found: $requestId")
        require(request.status == CollectionRequestStatus.REQUESTED ||
            request.status == CollectionRequestStatus.PENDING) {
            "This request is no longer available."
        }
        val timestamp = now()
        val rejected = request.copy(
            status = CollectionRequestStatus.REJECTED,
            updatedAt = timestamp
        )
        collectionRequests[requestId] = rejected
        lots.values.firstOrNull { it.requestId == requestId }?.let { lot ->
            lots[lot.lotId] = lot.copy(status = LotStatus.CANCELLED, updatedAt = timestamp)
        }
        return rejected
    }

    @Synchronized
    override fun startPickup(lotId: String, collectorId: String): Lot {
        val lot = requireCollectorLot(lotId, collectorId)
        require(lot.status == LotStatus.ACCEPTED) { "Only an accepted pickup can be started." }
        val timestamp = now()
        val started = lot.copy(
            status = LotStatus.PICKUP_IN_PROGRESS,
            pickupTimestamp = timestamp,
            updatedAt = timestamp
        )
        lots[lotId] = started
        val request = collectionRequests[lot.requestId]
        if (request != null) {
            collectionRequests[request.id] = request.copy(
                status = CollectionRequestStatus.IN_PROGRESS,
                updatedAt = timestamp
            )
        }
        return started
    }

    @Synchronized
    override fun completePickup(
        lotId: String,
        collectorId: String,
        actualWeight: Double,
        actualValue: Double?
    ): Lot {
        require(actualWeight.isFinite() && actualWeight > 0) { "Actual weight must be greater than zero." }
        val lot = requireCollectorLot(lotId, collectorId)
        require(lot.status == LotStatus.PICKUP_IN_PROGRESS) { "Start the pickup before recording weight." }
        val calculatedValue = actualValue ?: if (lot.estimatedWeight > 0) {
            lot.estimatedValue * actualWeight / lot.estimatedWeight
        } else {
            lot.estimatedValue
        }
        require(calculatedValue.isFinite() && calculatedValue >= 0) { "Actual value is invalid." }
        val updated = lot.copy(
            status = LotStatus.COLLECTED,
            actualWeight = actualWeight,
            actualValue = calculatedValue,
            updatedAt = now()
        )
        lots[lotId] = updated
        return updated
    }

    @Synchronized
    override fun createHandoverRecord(record: HandoverRecord): HandoverRecord {
        require(!handovers.containsKey(record.id)) { "Handover already exists: ${record.id}" }
        handovers[record.id] = record
        return record
    }

    @Synchronized
    override fun getHandoverRecords(collectorId: String): List<HandoverRecord> =
        handovers.values.filter { it.collectorId == collectorId }.toList()

    @Synchronized
    override fun recordHandover(
        lotId: String,
        collectorId: String,
        recyclerId: String,
        location: String,
        actualWeight: Double,
        actualValue: Double
    ): HandoverRecord {
        require(recyclerId.isNotBlank()) { "Recycler is required." }
        require(location.isNotBlank()) { "Handover location is required." }
        require(actualWeight.isFinite() && actualWeight > 0) { "Actual weight must be greater than zero." }
        require(actualValue.isFinite() && actualValue >= 0) { "Actual value is invalid." }
        val lot = requireCollectorLot(lotId, collectorId)
        require(lot.status == LotStatus.COLLECTED) { "Only collected material can be handed over." }
        val timestamp = now()
        val record = HandoverRecord(
            id = "HANDOVER-${UUID.randomUUID()}",
            lotId = lotId,
            collectorId = collectorId,
            recyclerId = recyclerId.trim(),
            location = location.trim(),
            timestamp = timestamp,
            status = HandoverStatus.COMPLETED,
            createdAt = timestamp,
            updatedAt = timestamp,
            actualWeight = actualWeight,
            actualValue = actualValue
        )
        handovers[record.id] = record
        lots[lotId] = lot.copy(
            recyclerId = recyclerId.trim(),
            actualWeight = actualWeight,
            actualValue = actualValue,
            handoverLocation = location.trim(),
            handoverTimestamp = timestamp,
            status = LotStatus.HANDED_OVER,
            updatedAt = timestamp
        )
        collectionRequests[lot.requestId]?.let { request ->
            collectionRequests[request.id] = request.copy(
                status = CollectionRequestStatus.COMPLETED,
                updatedAt = timestamp
            )
        }
        return record
    }

    @Synchronized
    override fun assignRecycler(lotId: String, recyclerId: String): Lot {
        require(recyclerId.isNotBlank()) { "Recycler id is required." }
        val lot = lots[lotId] ?: error("Lot not found: $lotId")
        require(lot.status == LotStatus.HANDED_OVER) {
            "Only a handed-over lot can be assigned to a recycler."
        }
        val updated = lot.copy(recyclerId = recyclerId.trim(), updatedAt = now())
        lots[lotId] = updated
        return updated
    }

    @Synchronized
    override fun confirmRecyclerReceipt(lotId: String, recyclerId: String): Lot {
        require(recyclerId.isNotBlank()) { "Recycler id is required." }
        val lot = lots[lotId] ?: error("Lot not found: $lotId")
        require(lot.status == LotStatus.HANDED_OVER) {
            "Only a handed-over lot can be confirmed."
        }
        require(lot.recyclerId.isNullOrBlank() || lot.recyclerId == recyclerId) {
            "This lot is assigned to another recycler."
        }
        val timestamp = now()
        val updated = lot.copy(
            recyclerId = recyclerId.trim(),
            status = LotStatus.RECYCLER_CONFIRMED,
            updatedAt = timestamp
        )
        lots[lotId] = updated
        return updated
    }

    @Synchronized
    override fun createRecyclingRecord(record: RecyclingRecord): RecyclingRecord {
        require(!recyclingRecords.containsKey(record.id)) {
            "Recycling record already exists: ${record.id}"
        }
        require(record.processedWeight.isFinite() && record.processedWeight > 0) {
            "Processed weight must be greater than zero."
        }
        recyclingRecords[record.id] = record
        return record
    }

    @Synchronized
    override fun getRecyclingRecords(recyclerId: String): List<RecyclingRecord> =
        recyclingRecords.values.filter { it.recyclerId == recyclerId }.toList().asReversed()

    @Synchronized
    override fun recordRecycling(
        lotId: String,
        recyclerId: String,
        processedWeight: Double,
        actualMaterial: String,
        facility: String,
        notes: String,
        recyclingDate: String
    ): RecyclingRecord {
        require(recyclerId.isNotBlank()) { "Recycler id is required." }
        require(processedWeight.isFinite() && processedWeight > 0) {
            "Processed weight must be greater than zero."
        }
        require(actualMaterial.isNotBlank()) { "Actual material is required." }
        val lot = lots[lotId] ?: error("Lot not found: $lotId")
        require(lot.recyclerId == recyclerId) { "This lot is assigned to another recycler." }
        require(lot.status == LotStatus.PAID) {
            "Payment must be completed before recycling is recorded."
        }
        val timestamp = now()
        require(recyclingRecords.values.none { it.lotId == lotId }) {
            "A recycling record already exists for this lot."
        }
        val record = RecyclingRecord(
            id = "RECYCLE-${UUID.randomUUID()}",
            lotId = lotId,
            recyclerId = recyclerId,
            materialId = lot.materialId,
            processedWeight = processedWeight,
            recycledAt = timestamp,
            status = RecyclingStatus.COMPLETED,
            createdAt = timestamp,
            updatedAt = timestamp,
            actualMaterial = actualMaterial.trim(),
            recycledQuantity = processedWeight,
            recyclingDate = recyclingDate.ifBlank { timestamp },
            facility = facility.trim(),
            notes = notes.trim()
        )
        recyclingRecords[record.id] = record
        lots[lotId] = lot.copy(status = LotStatus.RECYCLED, updatedAt = timestamp)
        return record
    }

    @Synchronized
    override fun createTransaction(transaction: Transaction): Transaction {
        require(!transactions.containsKey(transaction.id)) {
            "Transaction already exists: ${transaction.id}"
        }
        require(transaction.amount.isFinite() && transaction.amount >= 0) {
            "Transaction amount is invalid."
        }
        transactions[transaction.id] = transaction
        return transaction
    }

    @Synchronized
    override fun getRecyclerTransactions(recyclerId: String): List<Transaction> =
        transactions.values.filter { it.payeeId == recyclerId }.toList().asReversed()

    @Synchronized
    override fun payRecycler(
        lotId: String,
        recyclerId: String,
        paymentMethod: com.kabadiwalaconnect.data.model.PaymentMethod
    ): Transaction {
        require(recyclerId.isNotBlank()) { "Recycler id is required." }
        val lot = lots[lotId] ?: error("Lot not found: $lotId")
        require(lot.recyclerId == recyclerId) { "This lot is assigned to another recycler." }
        require(lot.status == LotStatus.RECYCLER_CONFIRMED) {
            "Confirm receipt before paying for this lot."
        }
        val timestamp = now()
        val transaction = Transaction(
            id = "KC-TXN-${UUID.randomUUID().toString().take(6).uppercase(Locale.US)}",
            lotId = lotId,
            payerId = "platform",
            payeeId = recyclerId,
            amount = lot.actualValue ?: lot.estimatedValue,
            status = TransactionStatus.COMPLETED,
            createdAt = timestamp,
            updatedAt = timestamp,
            paymentMethod = paymentMethod
        )
        transactions[transaction.id] = transaction
        lots[lotId] = lot.copy(status = LotStatus.PAID, updatedAt = timestamp)
        return transaction
    }

    private fun requireCollectorLot(lotId: String, collectorId: String): Lot {
        require(collectorId.isNotBlank()) { "Collector id is required." }
        val lot = lots[lotId] ?: error("Lot not found: $lotId")
        require(lot.collectorId == collectorId) { "This pickup is assigned to another collector." }
        return lot
    }

    private fun isValidTransition(from: LotStatus, to: LotStatus): Boolean =
        when (from) {
            LotStatus.REQUESTED -> to == LotStatus.ASSIGNED || to == LotStatus.CANCELLED
            LotStatus.ASSIGNED -> to == LotStatus.ACCEPTED || to == LotStatus.CANCELLED
            LotStatus.ACCEPTED -> to == LotStatus.PICKUP_IN_PROGRESS || to == LotStatus.CANCELLED
            LotStatus.PICKUP_IN_PROGRESS -> to == LotStatus.COLLECTED || to == LotStatus.CANCELLED
            LotStatus.COLLECTED -> to == LotStatus.HANDED_OVER
            LotStatus.HANDED_OVER -> to == LotStatus.RECYCLER_CONFIRMED
            LotStatus.RECYCLER_CONFIRMED -> to == LotStatus.PAID
            LotStatus.PAID -> to == LotStatus.RECYCLED
            LotStatus.RECYCLED,
            LotStatus.CANCELLED -> false
        }
}

object CollectionRepositoryProvider {
    val instance: CollectionRepository = InMemoryCollectionRepository()
}

private fun now(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())

private fun LotStatus.isCompletedCollection(): Boolean = when (this) {
    LotStatus.HANDED_OVER,
    LotStatus.RECYCLER_CONFIRMED,
    LotStatus.PAID,
    LotStatus.RECYCLED -> true
    else -> false
}
