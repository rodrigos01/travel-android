package com.combah.travel2

import androidx.lifecycle.LiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Assert.assertEquals

@ExperimentalCoroutinesApi
fun <T> TestCoroutineScope.assertFlowEquals(expected: T?, actual: Flow<T>) {
    var value: T? = null
    val job = launch {
        actual.collect {
            value = it
        }
    }
    advanceUntilIdle()
    assertEquals(expected, value)
    job.cancel()
}

fun <T> TestCoroutineScope.getObservedValue(liveData: LiveData<T>): T? {
    var value: T? = null
    liveData.observeForever {
        value = it
    }
    advanceUntilIdle()
    return value
}

val <T> LiveData<T>.observedValue: T?
    get() {
        var value: T? = null
        observeForever {
            value = it
        }
        return value
    }