package com.combah.travel2

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals

@ExperimentalCoroutinesApi
fun <T> TestScope.assertFlowEquals(expected: T?, actual: Flow<T>) {
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