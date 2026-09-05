package com.kabadiwalaconnect.data.repository

import com.kabadiwalaconnect.data.model.CollectionRequest
import com.kabadiwalaconnect.data.model.Lot

interface CollectionRepository {
    fun createCollectionRequest(request: CollectionRequest): CollectionRequest

    fun getCollectionRequests(): List<CollectionRequest>

    fun getCollectionRequest(id: String): CollectionRequest?

    fun updateCollectionRequest(request: CollectionRequest): CollectionRequest

    fun createLot(lot: Lot): Lot

    fun getLot(id: String): Lot?

    fun updateLot(lot: Lot): Lot
}

class InMemoryCollectionRepository : CollectionRepository {
    private val collectionRequests = LinkedHashMap<String, CollectionRequest>()
    private val lots = LinkedHashMap<String, Lot>()

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
    override fun updateLot(lot: Lot): Lot {
        require(lots.containsKey(lot.lotId)) {
            "Lot not found: ${lot.lotId}"
        }
        lots[lot.lotId] = lot
        return lot
    }
}
