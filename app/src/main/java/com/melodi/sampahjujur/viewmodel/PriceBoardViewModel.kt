package com.melodi.sampahjujur.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melodi.sampahjujur.data.local.entity.PriceEntity
import com.melodi.sampahjujur.repository.CachedPrice
import com.melodi.sampahjujur.repository.Person3PriceCategories
import com.melodi.sampahjujur.repository.PriceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

 data class PriceBoardUiState(
    val prices: List<PriceEntity> = emptyList(),
    val selectedCategory: String = Person3PriceCategories.all.first().backendId,
    val weightInput: String = "",
    val selectedPrice: CachedPrice? = null,
    val trendPoints: List<Double> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val isOffline: Boolean = false
)

class PriceBoardViewModel @Inject constructor(
    private val repository: PriceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PriceBoardUiState())
    val uiState: StateFlow<PriceBoardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeCachedPrices().collect { cached ->
                _uiState.update { it.copy(prices = cached) }
                selectCategory(_uiState.value.selectedCategory)
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            repository.refresh()
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = error.message ?: "Current prices unavailable; using cached prices when available",
                            isOffline = true
                        )
                    }
                }
                .onSuccess { _uiState.update { it.copy(isOffline = false) } }
            loadTrends(_uiState.value.selectedCategory)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun selectCategory(category: String) {
        viewModelScope.launch {
            val price = repository.getPrice(category)
            _uiState.update { it.copy(selectedCategory = category, selectedPrice = price) }
            loadTrends(category)
        }
    }

    fun setWeight(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*(\\.\\d*)?$"))) {
            _uiState.update { it.copy(weightInput = value) }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    private suspend fun loadTrends(category: String) {
        repository.trends(category)
            .onSuccess { rows -> _uiState.update { it.copy(trendPoints = rows.map { row -> row.price }) } }
            .onFailure { _uiState.update { it.copy(trendPoints = emptyList()) } }
    }
}
