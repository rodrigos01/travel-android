package com.combah.travel2.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

context(ViewModel)
fun <T> Flow<T>.asStateFlow(initialValue: T, started: SharingStarted = SharingStarted.Eagerly) =
    stateIn(scope = viewModelScope, started = started, initialValue = initialValue)

context (CoroutineScope)
suspend fun <T> Flow<T>.expectItem(): T {
    return suspendCoroutine { continuation ->
        launch {
            collect {
                continuation.resume(it)
                cancel()
            }
        }
    }
}