package com.combah.travel2.ui.trip.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface MapStateFlow<K, V> : Map<K, V>, StateFlow<Map<K, V>>

class MutableMapStateFlow<K, V> private constructor(
    private val map: MutableMap<K, V>,
    val stateFlow: MutableStateFlow<Map<K, V>> = MutableStateFlow(map),
) : MapStateFlow<K, V>, Map<K, V> by map, StateFlow<Map<K, V>> by stateFlow {

    constructor() : this(mutableMapOf())

    operator fun set(key: K, value: V) {
        stateFlow.value = map.also {
            it[key] = value
        }
    }

    fun remove(key: K): V? {
        val value = map.remove(key)
        stateFlow.value = map
        return value
    }
}