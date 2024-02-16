package com.combah.travel2.ui.trip.creation.usecase

import com.combah.travel2.model.repository.AutoCompleteRepository

interface InputUseCase<T>

class InputUseCaseFactory {
    fun <T> createAutoCompleteUseCase(repository: AutoCompleteRepository<T>) =
        AutoCompleteUseCase(repository)
}
