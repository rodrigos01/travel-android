package com.combah.travel2.test

import com.combah.travel2.model.data.Time
import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.TimeZone

object Mocks {
    fun mockTime(stubbing: KStubbing<Time>.(Time) -> Unit = {}): Time {
        val timeZoneMock: TimeZone = mock()
        return mock {
            on { timeZone } doReturn timeZoneMock
            on { minus(any()) } doReturn it
            on { plus(any<Long>()) } doReturn it
            on { timeInMillis } doReturn 0L
            stubbing(it)
        }
    }
}