package com.combah.travel2.test

import com.combah.travel2.model.data.Time
import org.assertj.core.api.Assertions
import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object Assertions {
    @ExperimentalContracts
    inline fun <reified T> assertType(obj: Any?) {
        contract { returns() implies (obj is T) }
        Assertions.assertThat(obj).isInstanceOf(T::class.java)
    }

    private fun mockTime(stubbing: KStubbing<Time>.(Time) -> Unit = {}): Time = mock {
        on { midnightTime() } doReturn it
        on { minus(any()) } doReturn it
        on { timeInMillis } doReturn 0L
        stubbing(it)
    }
}
