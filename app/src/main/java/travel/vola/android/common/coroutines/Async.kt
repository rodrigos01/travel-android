package travel.vola.android.common.coroutines

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun <T, R> Iterable<T>.mapAsync(transform: suspend (T) -> R): List<R> = coroutineScope {
    map {
        async {
            transform(
                it
            )
        }
    }.awaitAll()
}

suspend fun <K, V, R> Map<K, V>.mapAsync(transform: suspend (Map.Entry<K, V>) -> R): List<R> =
    coroutineScope {
        map {
            async {
                transform(
                    it
                )
            }
        }.awaitAll()
    }