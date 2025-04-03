package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import travel.vola.android.extensions.Time
import travel.vola.android.extensions.TimeFormatter
import travel.vola.android.extensions.set
import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.AirportSearchResult
import travel.vola.android.model.data.Time
import travel.vola.android.model.repository.AddFlightRepository
import travel.vola.android.test.Captor
import travel.vola.android.test.Mocks.mockTime
import travel.vola.android.test.UnconfinedDispatcherTestRule
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.creation.usecase.PendingData.PendingFlight
import travel.vola.android.ui.trip.state.AddFlightItemState
import java.util.TimeZone

class AddFlightUseCaseTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val testScope = TestScope(rule.dispatcher)

    private val repository: AddFlightRepository = mock()
    private val formatter: TimeFormatter = mock {
        on { dayOfMonthString(any()) } doReturn ""
        on { dayOfWeekString(any()) } doReturn ""
    }
    private val itemFlow = MutableStateFlow(mapOf<String, AddFlightItemState>())
    private val itemStore = mock<AddPlanItemStore<PendingFlight, AddFlightItemState>> {
        on { items(any()) } doReturn itemFlow
    }
    private val subject = AddFlightUseCase(testScope, itemStore, repository)

    private val items = subject.items.stateIn(
        testScope, started = SharingStarted.Eagerly, initialValue = emptyMap()
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
        subject.addItem("flight_id", mockTime(), mock())
        val data = items.value["flight_id"] ?: fail()
        assertThat(data.startState.locationText).isNull()
        assertThat(data.endState.time).isNull()
        assertThat(data.endState.locationText).isNull()
    }

    @Test
    fun `added item should be initialized with initial time as departure`() {
        val initialTime: Time = mockTime()
        subject.addItem("flight_id", initialTime, mock())
        val data = items.value["flight_id"] ?: fail()
        assertThat(data.startState.time).isEqualTo(initialTime)
    }

    @Test
    fun `remove should call itemStore remove`() {
        val item = mock<AddFlightItemState>()
        subject.removeItem(item)
        verify(itemStore).remove(item)
    }

    @Test
    fun `set departure time should update departure time`() {
        val originalTime = Time("2024-10-16T18:25 +0200")
        val originalData = PendingFlight(id = "itemId", departure = originalTime)
        subject.setDepartureTime("itemId", Time("2024-10-16T9:15 +0200"))
        val result = getUpdateResult(originalData)
        assertThat(result.departure).isEqualTo(Time("2024-10-16T9:15 +0200"))
    }

    @Test
    fun `set arrival time should update arrival time`() {
        val originalTime = Time("2024-10-16T18:25 +0200")
        val originalData = PendingFlight(id = "itemId", departure = mock(), arrival = originalTime)
        subject.setArrivalTime("itemId", Time("2024-10-16T16:15 +0200"))
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
                mock<AirportSearchResult> { on { name } doReturn "Charles de Gaule" },
                mock<AirportSearchResult> { on { name } doReturn "Orly Airport" },
                mock<AirportSearchResult> { on { name } doReturn "Beauvais Airport" },
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
        val expected: AirportSearchResult = mock()
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
        repository.stub {
            onBlocking { airportDetails("airport_id") } doReturn expected
        }
        val originalData =
            PendingFlight(
                id = "flight_id",
                departure = mock(),
                arrival = Time("2024-5-17T10:55 +0200"),
                airportFromSearchResults = listOf(
                    mock(),
                    mock { on { iata } doReturn "airport_id" },
                    mock(),
                ),
            )
        subject.airportFromSearchResultTapped("flight_id", 1)
        val result = getUpdateResult(originalData)
        assertThat(result.departure.zone).isEqualTo(airportTimeZone)
    }

    @Test
    fun `airport to search text changed should fetch results to repository`() = runTest {
        subject.airportToSearchTextChanged("itemId", "par")
        verify(repository).autocomplete("par")
    }

    @Test
    fun `airport to search text changed should update results with repository data`() =
        runTest {
            val results = listOf<AirportSearchResult>(
                mock { on { name } doReturn "Charles de Gaule" },
                mock { on { name } doReturn "Orly Airport" },
                mock { on { name } doReturn "Beauvais Airport" },
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
        repository.stub {
            onBlocking { airportDetails("airport_id") } doReturn expected
        }
        val originalData =
            PendingFlight(
                id = "flight_id",
                departure = mock(),
                arrival = Time("2024-5-17T10:55 +0200"),
                airportFromSearchResults = listOf(
                    mock(),
                    mock { on { iata } doReturn "airport_id" },
                    mock(),
                ),
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
        repository.stub {
            onBlocking { airportDetails("airport_id") } doReturn expected
        }
        val originalData =
            PendingFlight(
                id = "flight_id",
                departure = mock(),
                arrival = Time("2024-5-17T10:55 +0200"),
                airportFromSearchResults = listOf(
                    mock(),
                    mock { on { iata } doReturn "airport_id" },
                    mock(),
                ),
            )
        subject.airportToSearchResultTapped("flight_id", 1)
        val result = getUpdateResult(originalData)
        assertThat(result.arrival?.zone).isEqualTo(airportTimeZone)
    }

    private fun getUpdateResult(originalData: PendingFlight): PendingFlight {
        return Captor.getUpdateResult(itemStore, originalData)
    }
}
