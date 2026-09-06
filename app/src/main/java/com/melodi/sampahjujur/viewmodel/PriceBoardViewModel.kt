package com.melodi.sampahjujur.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melodi.sampahjujur.repository.PricePoint
import com.melodi.sampahjujur.repository.PriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PriceBoardState(
    val prices: List<PricePoint> = emptyList(),
    val history: List<PricePoint> = emptyList(),
    val selectedCategory: String = PriceRepository.categories.first(),
    val loading: Boolean = true,
    val offline: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PriceBoardViewModel @Inject constructor(
    private val repository: PriceRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PriceBoardState())
    val state: StateFlow<PriceBoardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeLatest().collect { rows -> _state.update { it.copy(prices = rows, loading = false) } }
        }
        refresh()
    }

    fun select(category: String) {
        _state.update { it.copy(selectedCategory = category) }
        viewModelScope.launch {
            repository.observeHistory(category).collect { rows -> _state.update { it.copy(history = rows) } }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val result = repository.refresh()
            _state.update { it.copy(loading = false, offline = result.isFailure, error = result.exceptionOrNull()?.message) }
            repository.refreshHistory(_state.value.selectedCategory)
        }
    }
}