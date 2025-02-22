package com.combah.travel2.ui.trip.creation.usecase

import kotlinx.coroutines.flow.Flow

interface InputUseCase<T> {
    val state: Flow<Map<String, T>>
}
