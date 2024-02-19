package com.combah.travel2.ui.trip.creation.usecase

import com.combah.travel2.model.repository.AutoCompleteRepository
import kotlinx.coroutines.flow.Flow

interface InputUseCase<T> {
    val state: Flow<T>
}

class InputUseCaseFactory {
    fun <T> createAutoCompleteUseCase(repository: AutoCompleteRepository<T>) =
        AutoCompleteUseCase(repository)
}
