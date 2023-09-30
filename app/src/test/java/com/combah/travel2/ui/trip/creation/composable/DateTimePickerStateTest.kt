package com.combah.travel2.ui.trip.creation.composable

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.stub
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

private const val NOV_15_MIDNIGHT = 1700024400000
private const val NOV_16_MIDNIGHT = 1700110800000
private const val NOV_15_2_35_PM = 1700076900000


@OptIn(ExperimentalMaterial3Api::class)
class DateTimePickerStateTest {

    private val datePickerState: DatePickerState = mock()
    private val timePickerState: TimePickerState = mock()
    private val subject = DateTimePickerState(null, datePickerState, timePickerState)

    @Test
    fun `step should start as Date`() {
        assertEquals(PickerStep.Date, subject.step)
    }

    @Test
    fun `selectedTimestamp should be null when date picker state date is null`() {
        datePickerState.stub {
            on { selectedDateMillis } doReturn null
        }
        assertNull(subject.selectedTimestamp)
    }

    @Test
    fun `selectedTimestamp should be date picker date when time picker state time is zero`() {
        val expectedTimestamp = NOV_15_MIDNIGHT
        datePickerState.stub {
            on { selectedDateMillis } doReturn expectedTimestamp
        }
        timePickerState.stub {
            on { hour } doReturn 0
            on { minute } doReturn 0
        }
        assertEquals(expectedTimestamp, subject.selectedTimestamp)
    }

    @Test
    fun `dateConfirmEnabled should be false when date picker date is null`() {
        datePickerState.stub {
            on { selectedDateMillis } doReturn null
        }
        assertFalse(subject.dateConfirmEnabled)
    }

    @Test
    fun `dateConfirmEnabled should be true when date picker date is not null`() {
        datePickerState.stub {
            on { selectedDateMillis } doReturn NOV_15_MIDNIGHT
        }
        assertTrue(subject.dateConfirmEnabled)
    }

    @Test
    fun `selectedTimestamp should be date picker date plus picker state time`() {
        val expectedTimestamp = NOV_15_2_35_PM
        datePickerState.stub {
            on { selectedDateMillis } doReturn NOV_15_MIDNIGHT
        }
        timePickerState.stub {
            on { hour } doReturn 14
            on { minute } doReturn 35
        }
        assertEquals(expectedTimestamp, subject.selectedTimestamp)
    }

    @Test
    fun `timeConfirmEnabled should be false when date picker date is equal minDate and time picker time is before minDate time`() {
        val localSubject = DateTimePickerState(
            minDate = Date(NOV_15_2_35_PM),
            datePickerState, timePickerState
        )
        datePickerState.stub {
            on { selectedDateMillis } doReturn NOV_15_MIDNIGHT
        }
        timePickerState.stub {
            on { hour } doReturn 0
            on { minute } doReturn 0
        }
        assertFalse(localSubject.timeConfirmEnabled)
    }

    @Test
    fun `timeConfirmEnabled should be false when date picker date is equal minDate and time picker time is equal minDate time`() {
        val localSubject = DateTimePickerState(
            minDate = Date(NOV_15_2_35_PM),
            datePickerState, timePickerState
        )
        datePickerState.stub {
            on { selectedDateMillis } doReturn NOV_15_MIDNIGHT
        }
        timePickerState.stub {
            on { hour } doReturn 14
            on { minute } doReturn 35
        }
        assertFalse(localSubject.timeConfirmEnabled)
    }

    @Test
    fun `timeConfirmEnabled should be true when date picker date is equal minDate and time picker time is after minDate time`() {
        val localSubject = DateTimePickerState(
            minDate = Date(NOV_15_2_35_PM),
            datePickerState, timePickerState
        )
        datePickerState.stub {
            on { selectedDateMillis } doReturn NOV_15_MIDNIGHT
        }
        timePickerState.stub {
            on { hour } doReturn 14
            on { minute } doReturn 36
        }
        assertTrue(localSubject.timeConfirmEnabled)
    }

    @Test
    fun `timeConfirmEnabled should be true when date picker date is after minDate`() {
        val localSubject = DateTimePickerState(
            minDate = Date(NOV_15_2_35_PM), // 11.5.2023 2:35 P.M.
            datePickerState, timePickerState
        )
        datePickerState.stub {
            on { selectedDateMillis } doReturn NOV_16_MIDNIGHT
        }
        timePickerState.stub {
            on { hour } doReturn 0
            on { minute } doReturn 0
        }
        assertTrue(localSubject.timeConfirmEnabled)
    }
}