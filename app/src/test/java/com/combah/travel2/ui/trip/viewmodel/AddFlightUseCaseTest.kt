package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.data.Time
import com.combah.travel2.test.assertType
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.stub
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
class AddFlightUseCaseTest {
    private val formatter: TimeFormatter = mock()
    private val subject = AddFlightUseCase(formatter)

    @Test
    fun `created item should be initialized empty`() {
        val addedItem = subject.createItem(mock())
        assertType<AddFlightUseCase.AddFlightItem>(addedItem)
        assertThat(addedItem.airportFromName).isNull()
        assertThat(addedItem.arrivalDayOfMonth).isNull()
        assertThat(addedItem.arrivalDayOfWeek).isNull()
        assertThat(addedItem.arrivalTime).isNull()
        assertThat(addedItem.airportToName).isNull()
    }

    @Test
    fun `created item should be initialized with initial time as departure`() {
        val initialTime: Time = mock()
        formatter.stub {
            on { timeString(initialTime) } doReturn "6:15"
        }
        val addedItem = subject.createItem(initialTime)
        assertType<AddFlightUseCase.AddFlightItem>(addedItem)
        assertThat(addedItem.departureTime).isEqualTo("6:15")
    }

    @Test
    fun `set departure time should update departure time`() {
        val newTime = mock<Time>()
        val originalTime = mock<Time> {
            on { copy(hour = 9, minute = 15) } doReturn newTime
        }
        formatter.stub {
            on { timeString(newTime) } doReturn "9:15"
        }
        val original = subject.createItem(originalTime)
        val new = subject.setDepartureTime(original, hour = 9, minute = 15)
        assertThat(new.departureTime).isEqualTo("9:15")
    }

    @Test
    fun `set arrival day should update arrival day`() {
        val newTime = mock<Time>()
        val receivedTime = mock<Time> {
            on { dayOfMonth } doReturn 21
            on { month } doReturn 4
            on { year } doReturn 2024
        }
        val originalTime = mock<Time> {
            on { copy(dayOfMonth = 21, month = 4, year = 2024) } doReturn newTime
        }
        formatter.stub {
            on { dayOfMonthString(newTime) } doReturn "21"
            on { dayOfWeekString(newTime) } doReturn "Wed"
        }
        val original = subject.createItem(originalTime)
        val new = subject.setArrivalDay(original, receivedTime)
        assertThat(new.arrivalDayOfMonth).isEqualTo("21")
        assertThat(new.arrivalDayOfWeek).isEqualTo("Wed")
    }

    @Test
    fun `set arrival time should update arrival time`() {
        val newTime = mock<Time>()
        val originalTime = mock<Time> {
            on { copy(hour = 16, minute = 15) } doReturn newTime
        }
        formatter.stub {
            on { timeString(newTime) } doReturn "16:15"
        }
        val original = subject.createItem(originalTime)
        val new = subject.setArrivalTime(original, hour = 16, minute = 15)
        assertThat(new.arrivalTime).isEqualTo("16:15")
    }
}