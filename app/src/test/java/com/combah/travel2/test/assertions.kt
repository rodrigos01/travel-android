package com.combah.travel2.test

import org.assertj.core.api.Assertions
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@ExperimentalContracts
inline fun <reified T> assertType(obj: Any?) {
    contract { returns() implies (obj is T) }
    Assertions.assertThat(obj).isInstanceOf(T::class.java)
}