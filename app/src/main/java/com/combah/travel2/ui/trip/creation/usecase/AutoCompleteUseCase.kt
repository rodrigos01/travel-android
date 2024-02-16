package com.combah.travel2.ui.trip.creation.usecase

import com.combah.travel2.model.repository.AutoCompleteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AutoCompleteUseCase<T>(repository: AutoCompleteRepository<T>) : InputUseCase<T> {
    data class AutoCompleteState<T>(
        val searchResults: List<T>,
    )

    private val _state = MutableStateFlow(AutoCompleteState<T>(emptyList()))
    val state: StateFlow<AutoCompleteState<T>> = _state.asStateFlow()

    fun setQuery(query: String) = Unit
}
