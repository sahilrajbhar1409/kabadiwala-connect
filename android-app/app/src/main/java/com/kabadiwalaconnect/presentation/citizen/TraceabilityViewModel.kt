package com.kabadiwalaconnect.presentation.citizen

import com.kabadiwalaconnect.data.model.AiPrediction
import com.kabadiwalaconnect.data.model.Lot
import com.kabadiwalaconnect.data.model.RecyclingRecord
import com.kabadiwalaconnect.data.model.Transaction
import com.kabadiwalaconnect.data.repository.CollectionRepository
import com.kabadiwalaconnect.data.repository.CollectionRepositoryProvider

data class TraceabilityState(
    val lot: Lot?,
    val prediction: AiPrediction?,
    val recycling: RecyclingRecord?,
    val payment: Transaction?,
    val repositoryLots: List<Lot>,
    val paymentByLot: Map<String, Transaction>
)

class TraceabilityViewModel(
    private val repository: CollectionRepository = CollectionRepositoryProvider.instance
) {
    fun load(lotId: String?): TraceabilityState {
        val lot = lotId?.let(repository::getLot) ?: repository.getLatestLot()
        return TraceabilityState(
            lot = lot,
            prediction = lot?.let { repository.getAiPredictionForLot(it.lotId) },
            recycling = lot?.let { repository.getRecyclingRecordForLot(it.lotId) },
            payment = lot?.let { repository.getTransactionForLot(it.lotId) },
            repositoryLots = repository.getLots(),
            paymentByLot = repository.getLots().mapNotNull { currentLot ->
                repository.getTransactionForLot(currentLot.lotId)?.let { currentLot.lotId to it }
            }.toMap()
        )
    }
}
