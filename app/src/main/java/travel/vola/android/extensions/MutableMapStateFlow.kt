package travel.vola.android.extensions

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.internal.toImmutableMap

typealias MapStateFlow<K, V> = StateFlow<Map<K, V>>
typealias MutableMapStateFlow<K, V> = MutableStateFlow<Map<K, V>>

fun <K, V> MutableMapStateFlow(): MutableMapStateFlow<K, V> = MutableStateFlow(emptyMap())

operator fun <K, V> MutableMapStateFlow<K, V>.set(key: K, value: V) {
    this.value = this.value.toMutableMap().also {
        it[key] = value
    }.toImmutableMap()
}

fun <K, V> MutableMapStateFlow<K, V>.remove(key: K): V? {
    val newMap = value.toMutableMap()
    val removed = newMap.remove(key)
    value = newMap.toImmutableMap()
    return removed
}

fun <K, V> MutableMapStateFlow<K, V>.updateOrSet(key: K, updater: (V?) -> V) {
    val entry = get(key)
    set(key, updater(entry))
}

fun <K, V> MutableMapStateFlow<K, V>.update(
    key: K,
    updater: (V) -> V,
) {
    updateOrSet(key) { entry ->
        entry ?: throw IllegalArgumentException("Entry with key $key not found")
        updater(entry)
    }
}

operator fun <K, V> MapStateFlow<K, V>.get(key: K): V? = value[key]
