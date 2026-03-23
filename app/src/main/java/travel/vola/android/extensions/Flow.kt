package travel.vola.android.extensions

import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun <T> Flow<T>.expectItem(): T {
    return coroutineScope {
        suspendCoroutine { continuation ->
            launch {
                collect {
                    continuation.resume(it)
                    cancel()
                }
            }
        }
    }
}

fun <T, R, S> combineWithoutWaiting(
    flow1: Flow<T>,
    initialValue1: T,
    flow2: Flow<R>,
    initialValue2: R,
    transform: (T, R) -> S,
) = channelFlow {
    val stateFlow1 = flow1.stateIn(
        scope = this,
        initialValue = initialValue1,
        started = SharingStarted.WhileSubscribed(),
    )
    val stateFlow2 = flow2.stateIn(
        scope = this,
        initialValue = initialValue2,
        started = SharingStarted.WhileSubscribed(),
    )
    launch {
        combine(stateFlow1, stateFlow2, transform).collect {
            send(it)
        }
    }
}
