package com.combah.travel2.extensions

import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.util.Date

class DateTest {

    private val subject = Date(1696713541000) // 10/7/2023 9:19:01 PM UTC

    @Test
    fun `midnightTime should return timestamp for same day at midnight`() {
        val expectedTime = 1696636800000L // 10/7/2023 12 AM UTC
        assertEquals(expectedTime, subject.midnightTime)
    }

    @Test
    fun `hour should be hours since midnight in the day`() {
        assertEquals(21, subject.hour)
    }

    @Test
    fun `minute should be minutes since full-hour`() {
        assertEquals(19, subject.minute)
    }
}