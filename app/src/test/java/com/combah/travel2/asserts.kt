package com.combah.travel2

import androidx.lifecycle.LiveData
import io.reactivex.Observable
import org.junit.Assert.assertEquals

fun <T> assertObservableEquals(expected: T?, actual: Observable<T>) {
    var value: T? = null
    actual.subscribe {
        value = it
    }
    assertEquals(expected, value)
}

val <T> LiveData<T>.observedValue: T?
    get() {
        var value: T? = null
        observeForever {
            value = it
        }
        return value
    }