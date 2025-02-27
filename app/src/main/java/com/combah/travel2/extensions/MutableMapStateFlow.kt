package com.combah.travel2.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

inline fun <K, V, reified T : V> Map<K, V>.filterValueInstanceOf(): Map<K, T> {
    return entries.fold(mutableMapOf()) { newMap, (key, value) ->
        if (value is T) {
            newMap[key] = value
        }
        newMap
    }
}
