package travel.vola.android.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import okhttp3.internal.toImmutableMap

typealias MapFlow<K, V> = Flow<Map<K, V>>
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

operator fun <K, V> MapStateFlow<K, V>.get(key: K): V? = value[key]

fun <K, V> mergeMaps(vararg flows: MapFlow<K, V>): MapFlow<K, V> =
    combine(flows.map { it.onStart { emit(emptyMap()) } }) {
        it.fold(emptyMap()) { acc, map -> acc + map }
    }
