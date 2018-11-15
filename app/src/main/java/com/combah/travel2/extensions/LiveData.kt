package com.combah.travel2.extensions

import androidx.lifecycle.*

fun <T, R> LiveData<T>.map(mapper: (T?) -> R?) = Transformations.map(this, mapper)

inline fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, crossinline observer: (T?) -> Unit) = observe(lifecycleOwner, Observer {
    observer(it)
})

inline fun <T> createLiveData(block: MutableLiveData<T>.() -> Unit): LiveData<T> = MutableLiveData<T>().apply(block)