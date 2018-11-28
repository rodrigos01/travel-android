package com.combah.travel2

import io.reactivex.Observable
import org.junit.Assert.assertEquals

fun <T> assertObservableEquals(expected: T?, actual: Observable<T>) {
    var value: T? = null
    actual.subscribe {
        value = it
    }
    assertEquals(expected, value)
}