package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.Time
import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.extensions.set
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddFlightRepository
import com.combah.travel2.test.Captor
import com.combah.travel2.test.Mocks.mockTime
import com.combah.travel2.test.UnconfinedDispatcherTestRule
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemStore
import com.combah.travel2.ui.trip.creation.usecase.PendingData.PendingFlight
import com.combah.travel2.ui.trip.state.AddFlightItemState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import java.util.TimeZone

class AddFlightUseCaseTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val repository: AddFlightRepository = mock()
    private val formatter: TimeFormatter = mock {
        on { dayOfMonthString(any()) } doReturn ""
        on { dayOfWeekString(any()) } doReturn ""
    }
    private val itemFlow = MutableStateFlow(mapOf<String, AddFlightItemState>())
    private val itemStore = mock<AddPlanItemStore<PendingFlight, AddFlightItemState>> {
        on { items(any()) } doReturn itemFlow
    }
    private val subject = AddFlightUseCase(itemStore, repository)

    private val items = subject.items.stateIn(
        TestScope(rule.dispatcher), started = SharingStarted.Eagerly, initialValue = emptyMap()
    )

    @Test
    fun `itemStore items updated should update items`() {
        val item = mock<AddFlightItemState> {
            on { id } doReturn "flight_id"
        }
        itemFlow["flight_id"] = item
        assertThat(items.value["flight_id"]).isEqualTo(item)
    }

    @Test
    fun `added item should be initialized empty`() {
        val data = subject.addItem(mockTime(), mock())
        assertThat(data.startState.locationText).isNull()
        assertThat(data.endState.time).isNull()
        assertThat(data.endState.locationText).isNull()
    }

    @Test
    fun `added item should be initialized with initial time as departure`() {
        val initialTime: Time = mockTime()
        val data = subject.addItem(initialTime, mock())
        assertThat(data.startState.time).isEqualTo(initialTime)
    }

    @Test
    fun `remove should call itemStore remove`() {
        val item = mock<AddFlightItemState>()
        subject.removeItem(item)
        verify(itemStore).remove(item)
    }

//    @Test
//    fun `create item should return item with pending flight data`() {
//        val midnightTime: Time = mock()
//        mockkStatic("com.combah.travel2.extensions.TimeKt")
//        val departure = mockk<Time>()
//        every { departure.toMidnight() } returns midnightTime
//
//        val arrival = mock<Time>()
//        val data = PendingFlight(
//            id = "flight_id",
//            departure = departure,
//            airportFrom = mock<Airport> { on { name } doReturn "Airport 15" },
//            arrival = arrival,
//            airportTo = mock<Airport> { on { name } doReturn "Airport 16" },
//        )
//        formatter.stub {
//            on { timeString(departure) } doReturn "9:15"
//            on { dayOfMonthString(departure) } doReturn "20"
//            on { dayOfWeekString(departure) } doReturn "Tue"
//            on { dayOfMonthString(arrival) } doReturn "21"
//            on { dayOfWeekString(arrival) } doReturn "Wed"
//            on { timeString(arrival) } doReturn "16:15"
//        }
//        val item = subject.createItem(data, false)
//        assertThat(item.id).isEqualTo("flight_id")
//        assertThat(item.timestamp).isEqualTo(departure)
//        assertThat(item.minDepartureTime).isEqualTo(midnightTime)
//        assertThat(item.minArrivalTime).isEqualTo(midnightTime)
//        assertThat(item.departureDayOfMonth).isEqualTo("20")
//        assertThat(item.departureDayOfWeek).isEqualTo("Tue")
//        assertThat(item.departureTime).isEqualTo("9:15")
//        assertThat(item.airportFromName).isEqualTo("Airport 15")
//        assertThat(item.arrivalDayOfMonth).isEqualTo("21")
//        assertThat(item.arrivalDayOfWeek).isEqualTo("Wed")
//        assertThat(item.arrivalTime).isEqualTo("16:15")
//        assertThat(item.airportToName).isEqualTo("Airport 16")
//    }

    @Test
    fun `set departure day should update arrival day`() {
        val originalTime = Time("2023-10-16T18:25 +0200")
        val receivedTime = mockTime {
            on { dayOfMonth } doReturn 21
            on { month } doReturn 4
            on { year } doReturn 2024
        }
        val originalData = PendingFlight(id = "itemId", departure = originalTime)
        subject.setDepartureDate("itemId", receivedTime)
        val result = getUpdateResult(originalData)
        assertThat(result.departure).isEqualTo(Time("2024-4-21T18:25 +0200"))
    }

    @Test
    fun `set departure time should update departure time`() {
        val originalTime = Time("2024-10-16T18:25 +0200")
        val originalData = PendingFlight(id = "itemId", departure = originalTime)
        subject.setDepartureTime("itemId", hour = 9, minute = 15)
        val result = getUpdateResult(originalData)
        assertThat(result.departure).isEqualTo(Time("2024-10-16T9:15 +0200"))
    }

    @Test
    fun `set arrival day should update arrival day`() {
        val originalTime = Time("2023-10-16T18:25 +0200")
        val receivedTime = mockTime {
            on { dayOfMonth } doReturn 21
            on { month } doReturn 4
            on { year } doReturn 2024
        }
        val originalData = PendingFlight(id = "itemId", departure = mock(), arrival = originalTime)
        subject.setArrivalDate("itemId", receivedTime)
        val result = getUpdateResult(originalData)
        assertThat(result.arrival).isEqualTo(Time("2024-4-21T18:25 +0200"))
    }

    @Test
    fun `set arrival time should update arrival time`() {
        val originalTime = Time("2024-10-16T18:25 +0200")
        val originalData = PendingFlight(id = "itemId", departure = mock(), arrival = originalTime)
        subject.setArrivalTime("itemId", hour = 16, minute = 15)
        val result = getUpdateResult(originalData)
        assertThat(result.arrival).isEqualTo(Time("2024-10-16T16:15 +0200"))
    }

    @Test
    fun `set arrival time should update arrival time with airportTo timezone`() {
        val originalTime = Time("2024-10-16T18:25 -0400")
        val airportTo: Airport = mock {
            on { timeZone } doReturn TimeZone.getTimeZone("GMT+2:00")
        }
        val originalData = PendingFlight(
            id = "itemId",
            departure = mock(),
            arrival = originalTime,
            airportTo = airportTo
        )
        subject.setArrivalTime("itemId", hour = 16, minute = 15)
        val result = getUpdateResult(originalData)
        assertThat(result.arrival).isEqualTo(Time("2024-10-16T16:15 +0200"))
    }

    @Test
    fun `airport from search text changed should fetch results from repository`() = runTest {
        subject.airportFromSearchTextChanged("itemId", "par")
        verify(repository).autocomplete("par")
    }

    @Test
    fun `airport from search text changed should update results with repository data`() =
        runTest {
            val results = listOf(
                mock<Airport> { on { name } doReturn "Charles de Gaule" },
                mock<Airport> { on { name } doReturn "Orly Airport" },
                mock<Airport> { on { name } doReturn "Beauvais Airport" },
            )
            repository.stub {
                onBlocking { autocomplete("par") } doReturn results
            }
            val originalData = PendingFlight(id = "itemId", departure = mock())
            subject.airportFromSearchTextChanged("itemId", "par")
            val newData = getUpdateResult(originalData)
            assertThat(newData.airportFromSearchResults).isEqualTo(results)
        }

    @Test
    fun `airport from search result tapped should update item with selected airport`() {
        val expected: Airport = mock()
        val originalData =
            PendingFlight(
                id = "flight_id", departure = mock(), airportFromSearchResults = listOf(
                    mock(),
                    expected,
                    mock(),
                )
            )
        subject.airportFromSearchResultTapped("flight_id", 1)
        val result = getUpdateResult(originalData)
        assertThat(result.airportFrom).isEqualTo(expected)
    }

    @Test
    fun `airport from search result tapped should update item with new airport's timezone`() {
        val airportTimeZone: TimeZone = mock()
        val expected: Airport = mock {
            on { timeZone } doReturn airportTimeZone
        }
        val originalData =
            PendingFlight(
                id = "flight_id",
                departure = mock(),
                arrival = Time("2024-5-17T10:55 +0200"),
                airportFromSearchResults = listOf(
                    mock(),
                    expected,
                    mock(),
                ),
            )
        subject.airportFromSearchResultTapped("flight_id", 1)
        val result = getUpdateResult(originalData)
        assertThat(result.departure.timeZone).isEqualTo(airportTimeZone)
    }

    @Test
    fun `airport to search text changed should fetch results to repository`() = runTest {
        subject.airportToSearchTextChanged("itemId", "par")
        verify(repository).autocomplete("par")
    }

    @Test
    fun `airport to search text changed should update results with repository data`() =
        runTest {
            val results = listOf(
                mock<Airport> { on { name } doReturn "Charles de Gaule" },
                mock<Airport> { on { name } doReturn "Orly Airport" },
                mock<Airport> { on { name } doReturn "Beauvais Airport" },
            )
            repository.stub {
                onBlocking { autocomplete("par") } doReturn results
            }
            val originalData = PendingFlight(id = "itemId", departure = mock())
            subject.airportToSearchTextChanged("itemId", "par")
            val newData = getUpdateResult(originalData)
            assertThat(newData.airportToSearchResults).isEqualTo(results)
        }

    @Test
    fun `airport to search result tapped should update item with selected airport`() {
        val expected: Airport = mock()
        val originalData =
            PendingFlight(
                id = "flight_id", departure = mock(), airportToSearchResults = listOf(
                    mock(),
                    expected,
                    mock(),
                )
            )
        subject.airportToSearchResultTapped("flight_id", 1)
        val result = getUpdateResult(originalData)
        assertThat(result.airportTo).isEqualTo(expected)
    }

    @Test
    fun `airport to search result tapped should update item with new airport's timezone`() {
        val airportTimeZone: TimeZone = mock()
        val expected: Airport = mock {
            on { timeZone } doReturn airportTimeZone
        }
        val originalData =
            PendingFlight(
                id = "flight_id",
                departure = mock(),
                arrival = Time("2024-5-17T10:55 +0200"),
                airportToSearchResults = listOf(
                    mock(),
                    expected,
                    mock(),
                ),
            )
        subject.airportToSearchResultTapped("flight_id", 1)
        val result = getUpdateResult(originalData)
        assertThat(result.arrival?.timeZone).isEqualTo(airportTimeZone)
    }

//    @Test
//    fun `createAppData should create flight with data from item store`() {
//        val data = PendingFlight(
//            id = "flight_id",
//            departure = mock(),
//            airportFrom = mock(),
//            arrival = mock(),
//            airportTo = mock(),
//        )
//        itemStore.stub { on { get("flight_id") } doReturn data }
//        val result = subject.createAppData(item = mock { on { id } doReturn "flight_id" })
//        assertThat(result.id).isEqualTo("flight_id")
//        assertThat(result.segments).hasSize(1)
//        val segment = result.segments[0]
//        assertThat(segment.departure).isEqualTo(data.departure)
//        assertThat(segment.airportFrom).isEqualTo(data.airportFrom)
//        assertThat(segment.arrival).isEqualTo(data.arrival)
//        assertThat(segment.airportTo).isEqualTo(data.airportTo)
//    }

    private fun getUpdateResult(originalData: PendingFlight): PendingFlight {
        return Captor.getUpdateResult(itemStore, originalData)
    }
}
