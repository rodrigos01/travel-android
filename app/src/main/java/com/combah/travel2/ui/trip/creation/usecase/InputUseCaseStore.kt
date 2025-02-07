package com.combah.travel2.ui.trip.creation.usecase

import com.combah.travel2.extensions.MutableMapStateFlow
import com.combah.travel2.extensions.get
import com.combah.travel2.extensions.set
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class InputUseCaseStore<T : InputUseCaseStore.UseCaseSet<S>, S>(
    private val useCaseSetFactory: UseCaseSetFactory<T, S>,
    private val inputUseCaseFactory: InputUseCaseFactory,
) {

    fun interface UseCaseSetFactory<T : UseCaseSet<S>, S> {
        fun createUseCaseSet(useCaseFactory: InputUseCaseFactory): T
    }

    interface UseCaseSet<S> {
        val state: Flow<S>
    }

    private val useCaseSets = MutableMapStateFlow<String, T>()
    val inputStates = useCaseSets.flatMapMerge { inputUseCaseSetMap ->
        combine(inputUseCaseSetMap.entries.map { (key, value) ->
            value.state.map { key to it }
        }) { it.toMap() }
    }

    fun register(key: String) {
        useCaseSets[key] = useCaseSetFactory.createUseCaseSet(inputUseCaseFactory)
    }

    fun get(key: String): T? = useCaseSets[key]

    class Factory<T : UseCaseSet<S>, S> {
        fun create(setFactory: UseCaseSetFactory<T, S>) =
            InputUseCaseStore(setFactory, InputUseCaseFactory())
    }
}
