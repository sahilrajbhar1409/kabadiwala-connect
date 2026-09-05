package com.melodi.sampahjujur.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melodi.sampahjujur.model.*
import com.melodi.sampahjujur.repository.AuthRepository
import com.melodi.sampahjujur.repository.CollectionEarningsSummary
import com.melodi.sampahjujur.repository.CollectionRepository
import com.melodi.sampahjujur.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionUiState(
    val isLoading: Boolean = false,
    val isCapturingLocation: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val activeRequests: List<CollectionRequest> = emptyList(),
    val filteredRequests: List<CollectionRequest> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: String = "ALL",
    val authorizedRecyclers: List<Recycler> = emptyList(),
    val selectedRecyclerId: String = "",
    val recyclerRequests: List<CollectionRequest> = emptyList(),
    val filteredRecyclerRequests: List<CollectionRequest> = emptyList(),
    val recyclerSearchQuery: String = "",
    val recyclerStatusFilter: String = "ALL",
    val currentTraceabilityChain: LotTraceabilityChain? = null,
    val capturedLocationText: String = "",
    val capturedLatitude: Double = 0.0,
    val capturedLongitude: Double = 0.0,
    val currentLotIdPreview: String = "",
    val selectedCategory: String = ScrapMaterial.STANDARD_CATEGORIES.first(),
    val inputWeight: String = "",
    val inputNotes: String = ""
)

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CollectionUiState(currentLotIdPreview = LotIdGenerator.generateLotId())
    )
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    private val _earningsSummary = MutableStateFlow(CollectionEarningsSummary())
    val earningsSummary: StateFlow<CollectionEarningsSummary> = _earningsSummary.asStateFlow()

    init {
        loadRecyclers()
        observeCollectorData()
        captureCurrentLocation()
    }

    private fun observeCollectorData() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            val collectorId = user?.id ?: "local_collector"

            // Observe requests
            collectionRepository.observeCollectionRequests(collectorId)
                .catch { e -> _uiState.update { it.copy(errorMessage = e.message) } }
                .collect { requests ->
                    _uiState.update { current ->
                        current.copy(
                            activeRequests = requests,
                            filteredRequests = applyFilter(requests, current.searchQuery, current.selectedStatusFilter)
                        )
                    }
                }
        }

        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            val collectorId = user?.id ?: "local_collector"

            collectionRepository.observeEarningsSummary(collectorId)
                .catch { /* use fallback empty */ }
                .collect { summary ->
                    _earningsSummary.value = summary
                }
        }
    }

    fun loadRecyclers() {
        viewModelScope.launch {
            val recyclers = collectionRepository.getAuthorizedRecyclers()
            val defaultId = _uiState.value.selectedRecyclerId.ifBlank { recyclers.firstOrNull()?.id ?: "" }
            _uiState.update { it.copy(authorizedRecyclers = recyclers, selectedRecyclerId = defaultId) }
            if (defaultId.isNotBlank()) {
                observeRecyclerData(defaultId)
            }
        }
    }

    fun selectRecyclerFacility(recyclerId: String) {
        _uiState.update { it.copy(selectedRecyclerId = recyclerId) }
        observeRecyclerData(recyclerId)
    }

    private var recyclerJob: kotlinx.coroutines.Job? = null

    private fun observeRecyclerData(recyclerId: String) {
        recyclerJob?.cancel()
        recyclerJob = viewModelScope.launch {
            val flow = if (recyclerId.isBlank()) {
                collectionRepository.observeAllCollectionRequests()
            } else {
                collectionRepository.observeRequestsForRecycler(recyclerId)
            }
            flow.catch { /* ignore error */ }
                .collect { reqs ->
                    _uiState.update { current ->
                        current.copy(
                            recyclerRequests = reqs,
                            filteredRecyclerRequests = applyFilter(reqs, current.recyclerSearchQuery, current.recyclerStatusFilter)
                        )
                    }
                }
        }
    }

    fun generateNewLotIdPreview() {
        _uiState.update { it.copy(currentLotIdPreview = LotIdGenerator.generateLotId()) }
    }

    fun setCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setInputWeight(weight: String) {
        _uiState.update { it.copy(inputWeight = weight) }
    }

    fun setInputNotes(notes: String) {
        _uiState.update { it.copy(inputNotes = notes) }
    }

    fun captureCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCapturingLocation = true) }
            try {
                val locRes = locationRepository.getCurrentLocation()
                if (locRes.isSuccess) {
                    val gp = locRes.getOrNull()
                    if (gp != null) {
                        var addr = "Lat: ${gp.latitude}, Lng: ${gp.longitude}"
                        val addrRes = locationRepository.getAddressFromLocation(gp)
                        if (addrRes.isSuccess) {
                            addr = addrRes.getOrNull() ?: addr
                        }
                        _uiState.update {
                            it.copy(
                                isCapturingLocation = false,
                                capturedLocationText = addr,
                                capturedLatitude = gp.latitude,
                                capturedLongitude = gp.longitude
                            )
                        }
                        return@launch
                    }
                }
            } catch (e: Exception) {
                // Ignore failure
            }

            // Fallback to last known or default
            val lastRes = locationRepository.getLastKnownLocation()
            val gp = lastRes.getOrNull()
            val defaultAddr = if (gp != null) "Lat: ${gp.latitude}, Lng: ${gp.longitude}" else "Current Location (Offline)"
            _uiState.update {
                it.copy(
                    isCapturingLocation = false,
                    capturedLocationText = defaultAddr,
                    capturedLatitude = gp?.latitude ?: 0.0,
                    capturedLongitude = gp?.longitude ?: 0.0
                )
            }
        }
    }

    fun createCollectionRequest(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val weight = currentState.inputWeight.toDoubleOrNull() ?: 0.0
            if (weight <= 0.0) {
                _uiState.update { it.copy(errorMessage = "Please enter a valid weight (e.g. 5.5 kg)") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val user = authRepository.getCurrentUser()
            val collectorId = user?.id ?: "local_collector"

            val rate = ScrapMaterial.DEFAULT_CATEGORY_RATES[currentState.selectedCategory] ?: 30.0
            val estimatedVal = weight * rate

            val material = ScrapMaterial(
                materialId = "MAT-${System.currentTimeMillis()}",
                category = currentState.selectedCategory,
                description = currentState.inputNotes.ifBlank { "${currentState.selectedCategory} scrap items" },
                approximateWeight = weight,
                currentBuyingRate = rate,
                estimatedValue = estimatedVal,
                quotedPrice = estimatedVal
            )

            val result = collectionRepository.createCollectionRequest(
                collectorId = collectorId,
                materials = listOf(material),
                approximateWeight = weight,
                quotedPrice = estimatedVal,
                notes = currentState.inputNotes
            )

            if (result.isSuccess) {
                val created = result.getOrNull()!!
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        inputWeight = "",
                        inputNotes = "",
                        currentLotIdPreview = LotIdGenerator.generateLotId(),
                        successMessage = "Lot ${created.lotId} created successfully!"
                    )
                }
                onSuccess(created.lotId)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to create request"
                    )
                }
            }
        }
    }

    fun assignRecycler(lotId: String, recycler: Recycler, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = collectionRepository.assignRecycler(lotId, recycler)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Assigned to ${recycler.name}"
                    )
                }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to assign recycler"
                    )
                }
            }
        }
    }

    fun transitionStatus(lotId: String, nextStatus: CollectionStatus, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = collectionRepository.updateCollectionStatus(lotId, nextStatus)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Status updated: ${nextStatus.displayName}"
                    )
                }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Invalid status transition"
                    )
                }
            }
        }
    }

    fun initiateHandover(
        lotId: String,
        actualWeight: Double,
        finalSaleValue: Double,
        locationText: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = collectionRepository.initiateHandover(
                lotId = lotId,
                actualWeight = actualWeight,
                finalSaleValue = finalSaleValue,
                handoverLocationText = locationText
            )
            if (result.isSuccess) {
                val record = result.getOrNull()!!
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Handover initiated: ${record.handoverReference}"
                    )
                }
                onSuccess(record.handoverReference)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Handover initiation failed"
                    )
                }
            }
        }
    }

    fun confirmRecyclerHandover(lotId: String, recyclerId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = collectionRepository.confirmRecyclerHandover(lotId, recyclerId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Handover confirmed by Recycler!"
                    )
                }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Confirmation failed"
                    )
                }
            }
        }
    }

    fun recordCashPayment(lotId: String, amount: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = collectionRepository.recordCashPayment(lotId, amount)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Cash payment of Rs $amount recorded!"
                    )
                }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to record cash payment"
                    )
                }
            }
        }
    }

    fun recordUpiPaymentSuccess(lotId: String, amount: Double, upiRef: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = collectionRepository.recordUpiPaymentSuccess(lotId, amount, upiRef)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "UPI payment confirmed!"
                    )
                }
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to confirm UPI payment"
                    )
                }
            }
        }
    }

    fun fetchTraceabilityChain(lotId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = collectionRepository.traceLot(lotId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentTraceabilityChain = result.getOrNull()
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Could not trace Lot $lotId"
                    )
                }
            }
        }
    }

    fun observeRequest(lotId: String): Flow<CollectionRequest?> =
        collectionRepository.observeCollectionRequestByLotId(lotId)

    fun observeHandover(lotId: String): Flow<HandoverRecord?> =
        collectionRepository.observeHandoverRecordByLotId(lotId)

    fun acceptRequestByRecycler(lotId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = collectionRepository.acceptRequest(lotId)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Lot $lotId accepted by Recycler") }
                onSuccess()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message ?: "Failed to accept request") }
            }
        }
    }

    fun rejectRequestByRecycler(lotId: String, reason: String = "Rejected by Facility", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = collectionRepository.rejectRequest(lotId, reason)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Lot $lotId rejected") }
                onSuccess()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message ?: "Failed to reject request") }
            }
        }
    }

    fun confirmWeighbridgeHandover(
        lotId: String,
        recyclerId: String,
        weight: Double,
        finalValue: Double,
        onSuccess: (HandoverRecord) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = collectionRepository.confirmRecyclerHandover(lotId, recyclerId, weight, finalValue)
            if (result.isSuccess) {
                val record = result.getOrNull()!!
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Weighbridge intake verified: ${record.handoverReference}"
                    )
                }
                onSuccess(record)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Weighbridge intake failed"
                    )
                }
            }
        }
    }

    fun recordPaymentByRecycler(
        lotId: String,
        method: PaymentMethod,
        amount: Double,
        reference: String,
        onSuccess: () -> Unit
    ) {
        if (method == PaymentMethod.CASH) {
            recordCashPayment(lotId, amount, onSuccess)
        } else {
            recordUpiPaymentSuccess(lotId, amount, reference, onSuccess)
        }
    }

    fun filterByQuery(query: String) {
        _uiState.update { current ->
            val filtered = applyFilter(current.activeRequests, query, current.selectedStatusFilter)
            current.copy(searchQuery = query, filteredRequests = filtered)
        }
    }

    fun filterByStatus(statusFilter: String) {
        _uiState.update { current ->
            val filtered = applyFilter(current.activeRequests, current.searchQuery, statusFilter)
            current.copy(selectedStatusFilter = statusFilter, filteredRequests = filtered)
        }
    }

    fun filterRecyclerByQuery(query: String) {
        _uiState.update { current ->
            val filtered = applyFilter(current.recyclerRequests, query, current.recyclerStatusFilter)
            current.copy(recyclerSearchQuery = query, filteredRecyclerRequests = filtered)
        }
    }

    fun filterRecyclerByStatus(statusFilter: String) {
        _uiState.update { current ->
            val filtered = applyFilter(current.recyclerRequests, current.recyclerSearchQuery, statusFilter)
            current.copy(recyclerStatusFilter = statusFilter, filteredRecyclerRequests = filtered)
        }
    }

    private fun applyFilter(
        requests: List<CollectionRequest>,
        query: String,
        statusFilter: String
    ): List<CollectionRequest> {
        return requests.filter { req ->
            val matchesQuery = query.isBlank() ||
                    req.lotId.contains(query, ignoreCase = true) ||
                    req.recyclerName.contains(query, ignoreCase = true) ||
                    req.materials.any { it.category.contains(query, ignoreCase = true) }

            val matchesStatus = when (statusFilter) {
                "ALL" -> true
                "ACTIVE" -> req.getCollectionStatus().isActive()
                "COMPLETED" -> req.getCollectionStatus() == CollectionStatus.COMPLETED
                "PENDING_PAYMENT" -> req.getCollectionStatus() == CollectionStatus.PAYMENT_PENDING
                else -> req.status == statusFilter
            }

            matchesQuery && matchesStatus
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = collectionRepository.syncPendingData()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    successMessage = if (result.isSuccess) "Sync completed successfully" else null,
                    errorMessage = if (result.isFailure) result.exceptionOrNull()?.message else null
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
