package com.combah.travel2.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

context(ViewModel)
fun <T> Flow<T>.asStateFlow(initialValue: T, started: SharingStarted = SharingStarted.Eagerly) =
    stateIn(scope = viewModelScope, started = started, initialValue = initialValue)