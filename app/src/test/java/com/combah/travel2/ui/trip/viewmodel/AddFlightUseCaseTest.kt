package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.data.Time
import com.combah.travel2.test.assertType
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.stub
import org.assertj.core.api.Assertions
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
        Assertions.assertThat(addedItem.airportFromName).isNull()
        Assertions.assertThat(addedItem.arrivalDayOfMonth).isNull()
        Assertions.assertThat(addedItem.arrivalDayOfWeek).isNull()
        Assertions.assertThat(addedItem.arrivalTime).isNull()
        Assertions.assertThat(addedItem.airportToName).isNull()
    }

    @Test
    fun `created item should be initialized with initial time as departure`() {
        val initialTime: Time = mock()
        formatter.stub {
            on { timeString(initialTime) } doReturn "6:15"
        }
        val addedItem = subject.createItem(initialTime)
        assertType<AddFlightUseCase.AddFlightItem>(addedItem)
        Assertions.assertThat(addedItem.departureTime).isEqualTo("6:15")
    }

    @Test
    fun `created pending data should be initialized with initial time as departure`() {
        val initialTime: Time = mock()
        val pendingData = subject.createPendingData("id", initialTime)
        assertType<AddFlightUseCase.PendingFlight>(pendingData)
        Assertions.assertThat(pendingData.departure).isEqualTo(initialTime)
    }
}