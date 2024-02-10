package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddFlightRepository
import com.combah.travel2.test.assertType
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
class AddFlightUseCaseTest {
    private val repository: AddFlightRepository = mock()
    private val formatter: TimeFormatter = mock {
        on { dayOfMonthString(any()) } doReturn ""
        on { dayOfWeekString(any()) } doReturn ""
    }
    private val subject = AddFlightUseCase(repository, formatter)

    @Test
    fun `created item should be initialized empty`() {
        val addedItem = subject.createItem(mock())
        assertType<AddFlightUseCase.AddFlightItem>(addedItem)
        assertThat(addedItem.airportFromName).isNull()
        assertThat(addedItem.arrivalTime).isNull()
        assertThat(addedItem.airportToName).isNull()
    }

    @Test
    fun `created item should be initialized with initial time as departure`() {
        val initialTime: Time = mock()
        formatter.stub {
            on { dayOfMonthString(initialTime) } doReturn "16"
            on { dayOfWeekString(initialTime) } doReturn "Fri"
        }
        val addedItem = subject.createItem(initialTime)
        assertType<AddFlightUseCase.AddFlightItem>(addedItem)
        assertThat(addedItem.arrivalDayOfMonth).isEqualTo("16")
        assertThat(addedItem.arrivalDayOfWeek).isEqualTo("Fri")
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
        val new = subject.setDepartureTime(original.id, hour = 9, minute = 15)
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
        val new = subject.setArrivalDate(original.id, receivedTime)
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
        val new = subject.setArrivalTime(original.id, hour = 16, minute = 15)
        assertThat(new.arrivalTime).isEqualTo("16:15")
    }

    @Test
    fun `airport from search text changed should trigger repository autocomplete`() = runTest {
        repository.stub {
            onBlocking { autocomplete("par") } doReturn emptyList()
        }
        val original = subject.createItem(mock())
        subject.airportFromSearchTextChanged(original.id, "par")
        verify(repository).autocomplete("par")
    }

    @Test
    fun `airport from search text changed should update item with repository results`() = runTest {
        val expected = listOf(
            "Charles de Gaule",
            "Orly Airport",
            "Beauvais Airport",
        )
        val results = expected.map { airportName ->
            mock<Airport> {
                on { name } doReturn airportName
            }
        }
        repository.stub {
            onBlocking { autocomplete("par") } doReturn results
        }
        val original = subject.createItem(mock())
        val newItem = subject.airportFromSearchTextChanged(original.id, "par")
        assertThat(newItem.airportFromSearchResults).isEqualTo(expected)
    }

    @Test
    fun `airport from search result tapped should update item with selected airport`() = runTest {
        val expected = listOf(
            "Charles de Gaule",
            "Orly Airport",
            "Beauvais Airport",
        )
        val results = expected.map { airportName ->
            mock<Airport> {
                on { name } doReturn airportName
            }
        }
        repository.stub {
            onBlocking { autocomplete("par") } doReturn results
        }
        val original = subject.createItem(mock())
        val newItem = subject.airportFromSearchTextChanged(original.id, "par")
        val selectedItem = subject.airportFromSearchResultTapped(newItem.id, 1)
        assertThat(selectedItem.airportFromName).isEqualTo("Orly Airport")
    }

    @Test
    fun `airport to search text changed should trigger repository autocomplete`() = runTest {
        repository.stub {
            onBlocking { autocomplete("par") } doReturn emptyList()
        }
        val original = subject.createItem(mock())
        subject.airportToSearchTextChanged(original.id, "par")
        verify(repository).autocomplete("par")
    }

    @Test
    fun `airport to search text changed should update item with repository results`() = runTest {
        val expected = listOf(
            "Charles de Gaule",
            "Orly Airport",
            "Beauvais Airport",
        )
        val results = expected.map { airportName ->
            mock<Airport> {
                on { name } doReturn airportName
            }
        }
        repository.stub {
            onBlocking { autocomplete("par") } doReturn results
        }
        val original = subject.createItem(mock())
        val newItem = subject.airportToSearchTextChanged(original.id, "par")
        assertThat(newItem.airportToSearchResults).isEqualTo(expected)
    }

    @Test
    fun `airport to search result tapped should update item with selected airport`() = runTest {
        val expected = listOf(
            "Charles de Gaule",
            "Orly Airport",
            "Beauvais Airport",
        )
        val results = expected.map { airportName ->
            mock<Airport> {
                on { name } doReturn airportName
            }
        }
        repository.stub {
            onBlocking { autocomplete("par") } doReturn results
        }
        val original = subject.createItem(mock())
        val newItem = subject.airportToSearchTextChanged(original.id, "par")
        val selectedItem = subject.airportToSearchResultTapped(newItem.id, 1)
        assertThat(selectedItem.airportToName).isEqualTo("Orly Airport")
    }

    @Test
    fun `save should return a Flight with pending values`() = runTest {
        val expected = listOf(
            "Charles de Gaule",
            "Orly Airport",
            "Beauvais Airport",
        )
        val results = expected.map { airportName ->
            mock<Airport> {
                on { name } doReturn airportName
            }
        }
        val toResults = listOf(mock<Airport> {
            on { name } doReturn "Brussels Airport"
        })
        repository.stub {
            onBlocking { autocomplete("par") } doReturn results
            onBlocking { autocomplete("bru") } doReturn toResults
        }
        val newArrival = mock<Time>()
        val arrivalDay = mock<Time> {
            on { dayOfMonth } doReturn 21
            on { month } doReturn 4
            on { year } doReturn 2024
            on { copy(hour = 11, minute = 5) } doReturn newArrival
        }
        val newDeparture = mock<Time> {
            on { copy(dayOfMonth = 21, month = 4, year = 2024) } doReturn arrivalDay
        }
        val initialTime = mock<Time> {
            on { copy(hour = 22, minute = 35) } doReturn newDeparture
        }
        val item = subject.createItem(initialTime)
        subject.setDepartureTime(item.id, 22, 35)
        subject.airportFromSearchTextChanged(item.id, "par")
        subject.airportFromSearchResultTapped(item.id, 1)
        subject.setArrivalDate(item.id, arrivalDay)
        subject.setArrivalTime(item.id, 11, 5)
        subject.airportToSearchTextChanged(item.id, "bru")
        subject.airportToSearchResultTapped(item.id, 0)
        val result = subject.save(item)
        assertThat(result.id).isEqualTo(item.id)
        assertThat(result.segments[0].departure).isEqualTo(newDeparture)
        assertThat(result.segments[0].airportFrom).isEqualTo(results[1])
        assertThat(result.segments[0].arrival).isEqualTo(newArrival)
        assertThat(result.segments[0].airportTo).isEqualTo(toResults[0])
    }
}