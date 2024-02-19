package com.combah.travel2.ui.trip.creation.usecase

import com.combah.travel2.model.repository.AutoCompleteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AutoCompleteUseCase<T>(private val repository: AutoCompleteRepository<T>) :
    InputUseCase<AutoCompleteUseCase.AutoCompleteState<T>> {
    data class AutoCompleteState<T>(
        val searchResults: List<T>,
    )

    private val _state = MutableStateFlow(AutoCompleteState<T>(emptyList()))
    override val state: Flow<AutoCompleteState<T>> = _state.asStateFlow()

    suspend fun setQuery(query: String) {
        val results = repository.autocomplete(query)
        _state.value = AutoCompleteState(results)
    }
}
