package com.melodi.sampahjujur.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.melodi.sampahjujur.data.local.dao.CollectionRequestDao
import com.melodi.sampahjujur.data.local.dao.HandoverRecordDao
import com.melodi.sampahjujur.data.local.entity.CollectionRequestEntity
import com.melodi.sampahjujur.data.local.entity.HandoverRecordEntity
import com.melodi.sampahjujur.data.sync.SyncManager
import com.melodi.sampahjujur.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class CollectionEarningsSummary(
    val totalEarnings: Double = 0.0,
    val completedEarnings: Double = 0.0,
    val pendingEarnings: Double = 0.0,
    val transactionCount: Int = 0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val totalWeightKg: Double = 0.0,
    val cashEarnings: Double = 0.0,
    val upiEarnings: Double = 0.0
)

/**
 * Primary repository for Person 4 (SIH 26229: Kabadiwala Connect).
 * Implements offline-first collection request creation, Lot ID generation,
 * authorized recycler assignment, status state machine, GPS single-event capture,
 * digital handover, cash/UPI payment, and lot traceability.
 */
@Singleton
class CollectionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
    private val collectionRequestDao: CollectionRequestDao,
    private val handoverRecordDao: HandoverRecordDao,
    private val syncManager: SyncManager,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CollectionRepository"
        const val COLLECTION_REQUESTS = "collection_requests"
        const val HANDOVER_RECORDS = "handover_records"
        const val RECYCLERS_COLLECTION = "recyclers"
    }

    /**
     * Creates a new collection request (offline-first).
     * 1. Generates unique collision-resistant Lot ID (KC-YYYY-XXXXXX).
     * 2. Captures single-event GPS + address safely (handles offline / permission denied without crashing).
     * 3. Stores in Room immediately.
     * 4. Synchronizes to Firestore if online.
     */
    suspend fun createCollectionRequest(
        collectorId: String,
        materials: List<ScrapMaterial>,
        approximateWeight: Double,
        quotedPrice: Double,
        notes: String = ""
    ): Result<CollectionRequest> {
        return try {
            val lotId = LotIdGenerator.generateLotId()
            val requestId = UUID.randomUUID().toString()

            // Safe single-event GPS capture
            var lat = 0.0
            var lng = 0.0
            var address = "Location not captured"

            try {
                val locationResult = locationRepository.getLastKnownLocation()
                if (locationResult.isSuccess) {
                    val geoPoint = locationResult.getOrNull()
                    if (geoPoint != null) {
                        lat = geoPoint.latitude
                        lng = geoPoint.longitude
                        val addrResult = locationRepository.getAddressFromLocation(geoPoint)
                        if (addrResult.isSuccess) {
                            address = addrResult.getOrNull() ?: address
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "GPS capture bypassed gracefully: ${e.message}")
            }

            val request = CollectionRequest(
                id = requestId,
                lotId = lotId,
                collectorId = collectorId,
                materials = materials,
                totalWeight = approximateWeight,
                quotedPrice = quotedPrice,
                finalSaleValue = quotedPrice,
                status = CollectionStatus.CREATED.name,
                collectionLocation = address,
                latitude = lat,
                longitude = lng,
                createdAt = System.currentTimeMillis(),
                notes = notes,
                isSynced = false
            )

            // 1. Save to Room database immediately
            val entity = CollectionRequestEntity.fromCollectionRequest(request, isSynced = false)
            collectionRequestDao.insert(entity)

            // 2. Sync to Firestore if online
            if (syncManager.isOnline()) {
                try {
                    firestore.collection(COLLECTION_REQUESTS)
                        .document(lotId)
                        .set(request)
                        .await()
                    collectionRequestDao.markAsSynced(lotId)
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore sync deferred: ${e.message}")
                }
            }

            Result.success(request)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create collection request", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves suitable authorized recyclers.
     * Consumes Person 2's authorized recyclers if available, with robust fallback.
     */
    suspend fun getAuthorizedRecyclers(): List<Recycler> {
        return try {
            if (syncManager.isOnline()) {
                val snapshot = firestore.collection(RECYCLERS_COLLECTION)
                    .whereEqualTo("authorizationStatus", "AUTHORIZED")
                    .get()
                    .await()

                val fromFirestore = snapshot.documents.mapNotNull { it.toObject(Recycler::class.java) }
                if (fromFirestore.isNotEmpty()) {
                    return fromFirestore
                }
            }
            Recycler.getDefaultAuthorizedRecyclers()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch remote recyclers, using verified catalog: ${e.message}")
            Recycler.getDefaultAuthorizedRecyclers()
        }
    }

    /**
     * Assigns an authorized recycler to a collection request.
     * Validates transition: CREATED -> RECYCLER_ASSIGNED.
     */
    suspend fun assignRecycler(lotId: String, recycler: Recycler): Result<CollectionRequest> {
        return try {
            val entity = collectionRequestDao.getByLotId(lotId)
                ?: throw IllegalStateException("Collection request not found for Lot ID: $lotId")

            val currentStatus = CollectionStatus.valueOf(entity.status)
            if (!currentStatus.canTransitionTo(CollectionStatus.RECYCLER_ASSIGNED)) {
                throw IllegalStateException("Cannot assign recycler from status: ${currentStatus.name}")
            }

            val updatedRequest = entity.toCollectionRequest().copy(
                recyclerId = recycler.id,
                recyclerName = recycler.name,
                handoverLocation = recycler.facilityLocation,
                status = CollectionStatus.RECYCLER_ASSIGNED.name,
                isSynced = false
            )

            collectionRequestDao.update(CollectionRequestEntity.fromCollectionRequest(updatedRequest))

            if (syncManager.isOnline()) {
                try {
                    firestore.collection(COLLECTION_REQUESTS).document(lotId)
                        .set(updatedRequest, SetOptions.merge())
                        .await()
                    collectionRequestDao.markAsSynced(lotId)
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore sync deferred on assignRecycler: ${e.message}")
                }
            }

            Result.success(updatedRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to assign recycler", e)
            Result.failure(e)
        }
    }

    /**
     * Transitions the collection request status safely using the state machine.
     */
    suspend fun updateCollectionStatus(
        lotId: String,
        newStatus: CollectionStatus,
        notes: String? = null
    ): Result<CollectionRequest> {
        return try {
            val entity = collectionRequestDao.getByLotId(lotId)
                ?: throw IllegalStateException("Collection request not found: $lotId")

            val currentStatus = CollectionStatus.valueOf(entity.status)
            if (!currentStatus.canTransitionTo(newStatus)) {
                throw IllegalStateException("Invalid transition from ${currentStatus.name} to ${newStatus.name}")
            }

            val now = System.currentTimeMillis()
            var req = entity.toCollectionRequest().copy(
                status = newStatus.name,
                isSynced = false
            )

            if (notes != null) {
                req = req.copy(notes = notes)
            }

            when (newStatus) {
                CollectionStatus.SCHEDULED -> req = req.copy(scheduledAt = now)
                CollectionStatus.COLLECTED -> req = req.copy(collectedAt = now)
                CollectionStatus.HANDED_OVER -> req = req.copy(handedOverAt = now)
                else -> Unit
            }

            collectionRequestDao.update(CollectionRequestEntity.fromCollectionRequest(req))

            if (syncManager.isOnline()) {
                try {
                    firestore.collection(COLLECTION_REQUESTS).document(lotId)
                        .set(req, SetOptions.merge())
                        .await()
                    collectionRequestDao.markAsSynced(lotId)
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore sync deferred on updateStatus: ${e.message}")
                }
            }

            Result.success(req)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update collection status", e)
            Result.failure(e)
        }
    }

    /**
     * Initiates digital handover for a collected lot.
     * Records handover GPS, timestamp, actual weight, final agreed value, and generates Handover Reference.
     */
    suspend fun initiateHandover(
        lotId: String,
        actualWeight: Double,
        finalSaleValue: Double,
        handoverLocationText: String = ""
    ): Result<HandoverRecord> {
        return try {
            val reqEntity = collectionRequestDao.getByLotId(lotId)
                ?: throw IllegalStateException("Collection request not found: $lotId")

            val currentRequest = reqEntity.toCollectionRequest()
            val handoverReference = LotIdGenerator.generateHandoverReference()
            val handoverId = UUID.randomUUID().toString()

            // Safe handover GPS capture
            var lat = currentRequest.latitude
            var lng = currentRequest.longitude
            var loc = handoverLocationText.ifBlank { currentRequest.handoverLocation }

            try {
                val locRes = locationRepository.getCurrentLocation()
                if (locRes.isSuccess) {
                    val gp = locRes.getOrNull()
                    if (gp != null) {
                        lat = gp.latitude
                        lng = gp.longitude
                        if (loc.isBlank()) {
                            val addrRes = locationRepository.getAddressFromLocation(gp)
                            loc = addrRes.getOrNull() ?: loc
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Handover GPS capture bypassed: ${e.message}")
            }

            val now = System.currentTimeMillis()

            val record = HandoverRecord(
                handoverId = handoverId,
                handoverReference = handoverReference,
                lotId = lotId,
                collectorId = currentRequest.collectorId,
                recyclerId = currentRequest.recyclerId ?: "",
                recyclerName = currentRequest.recyclerName,
                materials = currentRequest.materials,
                weight = actualWeight,
                collectionLocation = currentRequest.collectionLocation,
                handoverLocation = loc,
                latitude = lat,
                longitude = lng,
                timestamp = now,
                quotedPrice = currentRequest.quotedPrice,
                finalSaleValue = finalSaleValue,
                paymentMethod = PaymentMethod.CASH.name,
                paymentStatus = PaymentStatus.PENDING.name,
                recyclerConfirmed = false,
                transactionStatus = CollectionStatus.HANDED_OVER.name,
                isSynced = false
            )

            // Save HandoverRecord in Room
            handoverRecordDao.insert(HandoverRecordEntity.fromHandoverRecord(record))

            // Update CollectionRequest in Room
            val updatedReq = currentRequest.copy(
                status = CollectionStatus.HANDED_OVER.name,
                totalWeight = actualWeight,
                finalSaleValue = finalSaleValue,
                handedOverAt = now,
                handoverLocation = loc,
                isSynced = false
            )
            collectionRequestDao.update(CollectionRequestEntity.fromCollectionRequest(updatedReq))

            // Sync both to Firestore if online
            if (syncManager.isOnline()) {
                try {
                    firestore.collection(HANDOVER_RECORDS).document(handoverReference)
                        .set(record)
                        .await()
                    handoverRecordDao.markAsSynced(handoverId)

                    firestore.collection(COLLECTION_REQUESTS).document(lotId)
                        .set(updatedReq, SetOptions.merge())
                        .await()
                    collectionRequestDao.markAsSynced(lotId)
                } catch (e: Exception) {
                    Log.w(TAG, "Handover sync deferred: ${e.message}")
                }
            }

            Result.success(record)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate handover", e)
            Result.failure(e)
        }
    }

    /**
     * Recycler accepts incoming collection request.
     */
    suspend fun acceptRequest(lotId: String): Result<CollectionRequest> {
        return updateCollectionStatus(lotId, CollectionStatus.ACCEPTED)
    }

    /**
     * Recycler rejects incoming collection request.
     */
    suspend fun rejectRequest(lotId: String, reason: String): Result<CollectionRequest> {
        return updateCollectionStatus(lotId, CollectionStatus.REJECTED, reason)
    }

    /**
     * Recycler confirms the weighbridge handover of the lot.
     * Updates verified weight, agreed settlement value, and generates Handover Reference if not yet present.
     */
    suspend fun confirmRecyclerHandover(
        lotId: String,
        recyclerId: String,
        verifiedWeight: Double? = null,
        finalSaleValue: Double? = null
    ): Result<HandoverRecord> {
        return try {
            val reqEntity = collectionRequestDao.getByLotId(lotId)
                ?: throw IllegalStateException("Collection request not found for Lot ID: $lotId")
            val currentReq = reqEntity.toCollectionRequest()

            val now = System.currentTimeMillis()
            val existingHandover = handoverRecordDao.getByLotId(lotId)

            val finalWeight = verifiedWeight ?: existingHandover?.weight ?: currentReq.totalWeight
            val finalAmount = finalSaleValue ?: existingHandover?.finalSaleValue ?: currentReq.quotedPrice

            val record = if (existingHandover != null) {
                existingHandover.toHandoverRecord().copy(
                    recyclerConfirmed = true,
                    recyclerConfirmedAt = now,
                    recyclerId = recyclerId.ifBlank { existingHandover.recyclerId },
                    weight = finalWeight,
                    finalSaleValue = finalAmount,
                    transactionStatus = CollectionStatus.PAYMENT_PENDING.name,
                    isSynced = false
                )
            } else {
                HandoverRecord(
                    handoverId = UUID.randomUUID().toString(),
                    handoverReference = LotIdGenerator.generateHandoverReference(),
                    lotId = lotId,
                    collectorId = currentReq.collectorId,
                    recyclerId = recyclerId.ifBlank { currentReq.recyclerId ?: "" },
                    recyclerName = currentReq.recyclerName,
                    materials = currentReq.materials,
                    weight = finalWeight,
                    collectionLocation = currentReq.collectionLocation,
                    handoverLocation = currentReq.handoverLocation,
                    latitude = currentReq.latitude,
                    longitude = currentReq.longitude,
                    timestamp = now,
                    quotedPrice = currentReq.quotedPrice,
                    finalSaleValue = finalAmount,
                    paymentMethod = PaymentMethod.CASH.name,
                    paymentStatus = PaymentStatus.PENDING.name,
                    recyclerConfirmed = true,
                    recyclerConfirmedAt = now,
                    transactionStatus = CollectionStatus.PAYMENT_PENDING.name,
                    isSynced = false
                )
            }

            handoverRecordDao.insert(HandoverRecordEntity.fromHandoverRecord(record))

            // Advance collection request status to PAYMENT_PENDING
            val updatedReq = currentReq.copy(
                status = CollectionStatus.PAYMENT_PENDING.name,
                totalWeight = finalWeight,
                finalSaleValue = finalAmount,
                handedOverAt = now,
                isSynced = false
            )
            collectionRequestDao.update(CollectionRequestEntity.fromCollectionRequest(updatedReq))

            if (syncManager.isOnline()) {
                try {
                    firestore.collection(HANDOVER_RECORDS).document(record.handoverReference)
                        .set(record, SetOptions.merge())
                        .await()
                    handoverRecordDao.markAsSynced(record.handoverId)

                    firestore.collection(COLLECTION_REQUESTS).document(lotId)
                        .set(updatedReq, SetOptions.merge())
                        .await()
                    collectionRequestDao.markAsSynced(lotId)
                } catch (e: Exception) {
                    Log.w(TAG, "Sync deferred during confirmRecyclerHandover: ${e.message}")
                }
            }

            Result.success(record)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to confirm recycler handover", e)
            Result.failure(e)
        }
    }

    /**
     * Records cash payment (100% offline capable).
     * Finalizes the collection and handover as COMPLETED and PAID.
     */
    suspend fun recordCashPayment(
        lotId: String,
        amount: Double,
        paymentReference: String? = null
    ): Result<CollectionRequest> {
        return try {
            val reqEntity = collectionRequestDao.getByLotId(lotId)
                ?: throw IllegalStateException("Collection request not found: $lotId")

            val now = System.currentTimeMillis()
            val updatedReq = reqEntity.toCollectionRequest().copy(
                status = CollectionStatus.COMPLETED.name,
                paymentMethod = PaymentMethod.CASH.name,
                paymentStatus = PaymentStatus.PAID.name,
                paymentReference = paymentReference ?: "CASH-${System.currentTimeMillis()}",
                finalSaleValue = amount,
                isSynced = false
            )

            collectionRequestDao.update(CollectionRequestEntity.fromCollectionRequest(updatedReq))

            // Update HandoverRecord if exists
            val handoverEntity = handoverRecordDao.getByLotId(lotId)
            if (handoverEntity != null) {
                val updatedHandover = handoverEntity.toHandoverRecord().copy(
                    paymentMethod = PaymentMethod.CASH.name,
                    paymentStatus = PaymentStatus.PAID.name,
                    transactionStatus = CollectionStatus.COMPLETED.name,
                    finalSaleValue = amount,
                    isSynced = false
                )
                handoverRecordDao.update(HandoverRecordEntity.fromHandoverRecord(updatedHandover))

                if (syncManager.isOnline()) {
                    firestore.collection(HANDOVER_RECORDS).document(updatedHandover.handoverReference)
                        .set(updatedHandover, SetOptions.merge())
                        .await()
                    handoverRecordDao.markAsSynced(updatedHandover.handoverId)
                }
            }

            if (syncManager.isOnline()) {
                firestore.collection(COLLECTION_REQUESTS).document(lotId)
                    .set(updatedReq, SetOptions.merge())
                    .await()
                collectionRequestDao.markAsSynced(lotId)
            }

            Result.success(updatedReq)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record cash payment", e)
            Result.failure(e)
        }
    }

    /**
     * Completes UPI payment after confirmation.
     * Never fakes payment; marks as PAID only when confirmed by collector/recycler.
     */
    suspend fun recordUpiPaymentSuccess(
        lotId: String,
        amount: Double,
        upiReference: String
    ): Result<CollectionRequest> {
        return try {
            val reqEntity = collectionRequestDao.getByLotId(lotId)
                ?: throw IllegalStateException("Collection request not found: $lotId")

            val updatedReq = reqEntity.toCollectionRequest().copy(
                status = CollectionStatus.COMPLETED.name,
                paymentMethod = PaymentMethod.UPI.name,
                paymentStatus = PaymentStatus.PAID.name,
                paymentReference = upiReference,
                finalSaleValue = amount,
                isSynced = false
            )

            collectionRequestDao.update(CollectionRequestEntity.fromCollectionRequest(updatedReq))

            val handoverEntity = handoverRecordDao.getByLotId(lotId)
            if (handoverEntity != null) {
                val updatedHandover = handoverEntity.toHandoverRecord().copy(
                    paymentMethod = PaymentMethod.UPI.name,
                    paymentStatus = PaymentStatus.PAID.name,
                    transactionStatus = CollectionStatus.COMPLETED.name,
                    finalSaleValue = amount,
                    isSynced = false
                )
                handoverRecordDao.update(HandoverRecordEntity.fromHandoverRecord(updatedHandover))

                if (syncManager.isOnline()) {
                    firestore.collection(HANDOVER_RECORDS).document(updatedHandover.handoverReference)
                        .set(updatedHandover, SetOptions.merge())
                        .await()
                    handoverRecordDao.markAsSynced(updatedHandover.handoverId)
                }
            }

            if (syncManager.isOnline()) {
                firestore.collection(COLLECTION_REQUESTS).document(lotId)
                    .set(updatedReq, SetOptions.merge())
                    .await()
                collectionRequestDao.markAsSynced(lotId)
            }

            Result.success(updatedReq)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record UPI payment", e)
            Result.failure(e)
        }
    }

    /**
     * Observes all collection requests for a collector in real time from Room (offline-first).
     */
    fun observeCollectionRequests(collectorId: String): Flow<List<CollectionRequest>> {
        return collectionRequestDao.observeByCollector(collectorId)
            .map { list -> list.map { it.toCollectionRequest() } }
    }

    /**
     * Observes all collection requests assigned to a recycler facility (offline-first).
     */
    fun observeRequestsForRecycler(recyclerId: String): Flow<List<CollectionRequest>> {
        return collectionRequestDao.observeByRecycler(recyclerId)
            .map { list -> list.map { it.toCollectionRequest() } }
    }

    /**
     * Observes all collection requests in the system (useful for recycler demo / overview).
     */
    fun observeAllCollectionRequests(): Flow<List<CollectionRequest>> {
        return collectionRequestDao.observeAllRequests()
            .map { list -> list.map { it.toCollectionRequest() } }
    }

    /**
     * Observes a single collection request by Lot ID.
     */
    fun observeCollectionRequestByLotId(lotId: String): Flow<CollectionRequest?> {
        return collectionRequestDao.observeByLotId(lotId)
            .map { it?.toCollectionRequest() }
    }

    /**
     * Observes a handover record by Lot ID.
     */
    fun observeHandoverRecordByLotId(lotId: String): Flow<HandoverRecord?> {
        return handoverRecordDao.observeByLotId(lotId)
            .map { it?.toHandoverRecord() }
    }

    /**
     * Calculates collector earnings summary directly from local database (offline capable).
     */
    fun observeEarningsSummary(collectorId: String): Flow<CollectionEarningsSummary> {
        return collectionRequestDao.observeByCollector(collectorId).map { entities ->
            val requests = entities.map { it.toCollectionRequest() }

            val completed = requests.filter { it.status == CollectionStatus.COMPLETED.name && it.paymentStatus == PaymentStatus.PAID.name }
            val pending = requests.filter { it.paymentStatus == PaymentStatus.PENDING.name && it.status != CollectionStatus.CANCELLED.name && it.status != CollectionStatus.REJECTED.name }

            val completedEarnings = completed.sumOf { it.finalSaleValue }
            val pendingEarnings = pending.sumOf { if (it.finalSaleValue > 0) it.finalSaleValue else it.quotedPrice }
            val totalEarnings = completedEarnings + pendingEarnings

            val cash = completed.filter { it.paymentMethod == PaymentMethod.CASH.name }.sumOf { it.finalSaleValue }
            val upi = completed.filter { it.paymentMethod == PaymentMethod.UPI.name }.sumOf { it.finalSaleValue }
            val weight = completed.sumOf { it.totalWeight }

            CollectionEarningsSummary(
                totalEarnings = totalEarnings,
                completedEarnings = completedEarnings,
                pendingEarnings = pendingEarnings,
                transactionCount = requests.size,
                completedCount = completed.size,
                pendingCount = pending.size,
                totalWeightKg = weight,
                cashEarnings = cash,
                upiEarnings = upi
            )
        }
    }

    /**
     * Reconstructs the complete audit trail and traceability chain for a Lot ID.
     */
    suspend fun traceLot(lotId: String): Result<LotTraceabilityChain> {
        return try {
            val reqEntity = collectionRequestDao.getByLotId(lotId)
                ?: throw IllegalStateException("No collection request found for Lot: $lotId")

            val request = reqEntity.toCollectionRequest()
            val handover = handoverRecordDao.getByLotId(lotId)?.toHandoverRecord()
            val collector = authRepository.getCurrentUser()

            val recycler = request.recyclerId?.let { rId ->
                getAuthorizedRecyclers().firstOrNull { it.id == rId }
            }

            val timeline = mutableListOf<TraceabilityEvent>()

            timeline.add(
                TraceabilityEvent(
                    stage = CollectionStatus.CREATED,
                    title = "Lot Created",
                    description = "Lot generated with initial quoted price: Rs ${request.quotedPrice}",
                    timestamp = request.createdAt,
                    location = request.collectionLocation
                )
            )

            if (request.recyclerId != null) {
                timeline.add(
                    TraceabilityEvent(
                        stage = CollectionStatus.RECYCLER_ASSIGNED,
                        title = "Recycler Assigned",
                        description = "Assigned to authorized facility: ${request.recyclerName}",
                        timestamp = request.scheduledAt ?: request.createdAt,
                        location = request.handoverLocation
                    )
                )
            }

            if (request.collectedAt != null) {
                timeline.add(
                    TraceabilityEvent(
                        stage = CollectionStatus.COLLECTED,
                        title = "Material Collected",
                        description = "Collected ${request.totalWeight} kg of scrap materials",
                        timestamp = request.collectedAt!!,
                        location = request.collectionLocation
                    )
                )
            }

            if (handover != null) {
                timeline.add(
                    TraceabilityEvent(
                        stage = CollectionStatus.HANDED_OVER,
                        title = "Handed Over",
                        description = "Handover Ref: ${handover.handoverReference}, Weight: ${handover.weight} kg",
                        timestamp = handover.timestamp,
                        location = handover.handoverLocation
                    )
                )

                if (handover.recyclerConfirmed) {
                    timeline.add(
                        TraceabilityEvent(
                            stage = CollectionStatus.PAYMENT_PENDING,
                            title = "Recycler Confirmed",
                            description = "Authorized recycler verified and stamped digital handover",
                            timestamp = handover.recyclerConfirmedAt ?: handover.timestamp,
                            location = handover.handoverLocation
                        )
                    )
                }
            }

            if (request.isPaid()) {
                timeline.add(
                    TraceabilityEvent(
                        stage = CollectionStatus.COMPLETED,
                        title = "Payment Completed",
                        description = "Settled via ${request.paymentMethod ?: "Cash"}: Rs ${request.finalSaleValue}",
                        timestamp = request.handedOverAt ?: System.currentTimeMillis(),
                        location = request.handoverLocation
                    )
                )
            }

            val chain = LotTraceabilityChain(
                lotId = lotId,
                collectionRequest = request,
                handoverRecord = handover,
                collector = collector,
                recycler = recycler,
                materials = request.materials,
                timeline = timeline
            )

            Result.success(chain)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to trace Lot: $lotId", e)
            Result.failure(e)
        }
    }

    /**
     * Synchronizes all unsynced collection requests and handover records to Firestore.
     */
    suspend fun syncPendingData(): Result<Unit> {
        if (!syncManager.isOnline()) {
            return Result.failure(Exception("Device is currently offline"))
        }

        return try {
            val unsyncedReqs = collectionRequestDao.getUnsynced()
            for (entity in unsyncedReqs) {
                val req = entity.toCollectionRequest()
                firestore.collection(COLLECTION_REQUESTS).document(req.lotId)
                    .set(req)
                    .await()
                collectionRequestDao.markAsSynced(req.lotId)
            }

            val unsyncedHandovers = handoverRecordDao.getUnsynced()
            for (entity in unsyncedHandovers) {
                val rec = entity.toHandoverRecord()
                firestore.collection(HANDOVER_RECORDS).document(rec.handoverReference)
                    .set(rec)
                    .await()
                handoverRecordDao.markAsSynced(rec.handoverId)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
