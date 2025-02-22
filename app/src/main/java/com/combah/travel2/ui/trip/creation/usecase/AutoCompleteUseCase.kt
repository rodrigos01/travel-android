package com.combah.travel2.ui.trip.creation.usecase

import com.combah.travel2.extensions.MapStateFlow
import com.combah.travel2.extensions.MutableMapStateFlow
import com.combah.travel2.extensions.set
import com.combah.travel2.model.repository.AutoCompleteRepository
import kotlinx.coroutines.flow.asStateFlow

class AutoCompleteUseCase<T>(private val repository: AutoCompleteRepository<T>) :
    InputUseCase<AutoCompleteUseCase.AutoCompleteState<T>> {
    data class AutoCompleteState<T>(
        val searchResults: List<T>,
    )

    private val _state = MutableMapStateFlow<String, AutoCompleteState<T>>()
    override val state: MapStateFlow<String, AutoCompleteState<T>> = _state.asStateFlow()

    suspend fun setQuery(itemId: String, query: String) {
        val results = repository.autocomplete(query)
        _state[itemId] = AutoCompleteState(results)
    }

    fun clearResults(itemId: String) {
        _state[itemId] = AutoCompleteState(emptyList())
    }
}
