package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import travel.vola.android.extensions.Time
import travel.vola.android.extensions.get
import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.AirportSearchResult
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.repository.AddFlightRepository
import travel.vola.android.test.Captor.getUpdateResult
import travel.vola.android.test.Mocks.mockItemStore
import travel.vola.android.test.UnconfinedDispatcherTestRule
import travel.vola.android.ui.trip.creation.usecase.PendingData.PendingFlight
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.AutoCompleteResultState
import java.time.ZoneId
import java.util.TimeZone

class AddFlightUseCaseTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val testScope = TestScope(rule.dispatcher)

    private val repository: AddFlightRepository = mock()
    private val itemStore = mockItemStore<PendingFlight, AddFlightItemState>()
    private val subject = AddFlightUseCase(testScope, itemStore, repository)

    private val items = subject.items.stateIn(
        testScope, started = SharingStarted.Eagerly, initialValue = emptyMap()
    )

    @Test
    fun `itemStore items updated should update items`() {
        itemStore.addItem(mock {
            on { id } doReturn "flight_id"
            on { departure } doReturn Time("2024-10-16T18:25+02:00")
            on { arrival } doReturn Time("2024-10-16T16:15+02:00")
        }, mock())
        assertThat(items["flight_id"]?.id).isEqualTo("flight_id")
        assertThat(items["flight_id"]?.startState?.dateTime).isEqualTo(Time("2024-10-16T18:25+02:00"))
    }

    @Test
    fun `added item should be initialized empty`() {
        subject.addItem("flight_id", Time("2024-10-16T18:25+02:00"), mock())
        val item = items.value["flight_id"] ?: fail()
        assertThat(item.startState.locationText).isNull()
        assertThat(item.startState.searchResults).isEmpty()
    }

    @Test
    fun `added item should be initialized with initial time as departure`() {
        val initialTime = Time("2024-10-16T18:25+02:00")
        subject.addItem("flight_id", initialTime, mock())
        val data = items.value["flight_id"] ?: fail()
        assertThat(data.startState.dateTime).isEqualTo(initialTime)
    }

    @Test
    fun `added item should have params`() {
        val params = AddPlanUseCase.StateParams(
            dateSelectionEnabled = true,
            deleteEnabled = false,
            typeSelectionEnabled = true,
        )
        subject.addItem("flight_id", Time("2025-10-16T15:23:00+01:00"), params)
        val item = items.value["flight_id"] ?: fail()
        assertThat(item.dateSelectionEnabled).isTrue()
        assertThat(item.deleteButtonEnabled).isFalse()
        assertThat(item.typeSelectionEnabled).isTrue()
    }

    @Test
    fun `added item from entity should be initialized with entity data`() {
        val origin: Airport = mock {
            on { name } doReturn "John F. Kennedy International Airport"
            on { timeZone } doReturn TimeZone.getTimeZone("America/New_York")
        }
        val destination: Airport = mock {
            on { name } doReturn "Orly Airport"
            on { timeZone } doReturn TimeZone.getTimeZone("Europe/Paris")
        }
        val segment = mock<FlightSegment> {
            on { airportFrom } doReturn origin
            on { departure } doReturn Time("2024-10-16T18:25-05:00")
            on { airportTo } doReturn destination
            on { arrival } doReturn Time("2024-10-17T06:15+02:00")
        }
        val entity = mock<Flight> {
            on { id } doReturn "flight_id"
            on { segments } doReturn listOf(segment)
        }
        subject.addItem("flight_id", entity, mock())
        val item = items.value["flight_id"] ?: fail()
        assertThat(item.startState.locationText).isEqualTo("John F. Kennedy International Airport")
        assertThat(item.endState.locationText).isEqualTo("Orly Airport")
        assertThat(item.startState.dateTime).isEqualTo(Time("2024-10-16T18:25-05:00"))
        assertThat(item.endState.dateTime).isEqualTo(Time("2024-10-17T06:15+02:00"))
    }

    @Test
    fun `remove should call itemStore remove`() {
        val item = mock<AddFlightItemState>()
        subject.removeItem(item)
        verify(itemStore).remove(item)
    }

    @Test
    fun `set departure time should update departure time`() {
        val originalTime = Time("2024-10-16T18:25+02:00")
        subject.addItem("item_id", originalTime, mock())
        subject.setDepartureTime("item_id", Time("2024-10-16T09:15+02:00"))
        val item = items.value["item_id"] ?: fail()
        assertThat(item.startState.dateTime).isEqualTo(Time("2024-10-16T09:15+02:00"))
    }

    @Test
    fun `set arrival time should update arrival time`() {
        val originalTime = Time("2024-10-16T18:25+02:00")
        subject.addItem("item_id", originalTime, mock())
        subject.setArrivalTime("item_id", Time("2024-10-16T20:15+02:00"))
        val item = items.value["item_id"] ?: fail()
        assertThat(item.endState.dateTime).isEqualTo(Time("2024-10-16T20:15+02:00"))
    }

    @Test
    fun `airport from search text changed should fetch results from repository`() = runTest {
        subject.airportFromSearchTextChanged("itemId", "par")
        verify(repository).autocomplete("par")
    }

    @Test
    fun `airport from search text changed should update results with repository data`() {
        val results = listOf(
            AirportSearchResult("CDG", "Charles de Gaule", "Paris, FR"),
            AirportSearchResult("ORY", "Orly Airport", "Paris, FR"),
            AirportSearchResult("BVA", "Beauvais Airport", "Paris, FR"),
        )
        repository.stub {
            onBlocking { autocomplete("par") } doReturn results
        }
        subject.addItem("item_id", Time("2024-10-16T18:25+02:00"), mock())
        subject.airportFromSearchTextChanged("item_id", "par")
        val item = items.value["item_id"] ?: fail()
        assertThat(item.startState.searchResults).isEqualTo(
            listOf(
                AutoCompleteResultState(
                    title = "Charles de Gaule",
                    subtitle = "Paris, FR",
                ),
                AutoCompleteResultState(
                    title = "Orly Airport",
                    subtitle = "Paris, FR",
                ),
                AutoCompleteResultState(
                    title = "Beauvais Airport",
                    subtitle = "Paris, FR",
                ),
            )
        )
    }

    @Test
    fun `airport from search result tapped should update item with selected airport`() {
        val expected = AirportSearchResult("CDG", "Charles de Gaule Airport", "Paris, FR")
        val originalData = PendingFlight(
            id = "item_id",
            departure = Time("2024-10-16T18:25+02:00"),
            airportFromSearchResults = listOf(
                mock(),
                expected,
                mock(),
            )
        )
        val airport: Airport = mock {
            on { name } doReturn "Charles de Gaule Airport"
            on { timeZone } doReturn TimeZone.getTimeZone("Europe/Paris")
        }
        repository.stub {
            onBlocking { details("CDG") } doReturn airport
        }
        itemStore.stub {
            on { getData("item_id") } doReturn originalData
        }
        subject.addItem("item_id", Time("2025-10-16T15:23:00+01:00"), mock())
        subject.airportFromSearchResultTapped("item_id", 1)
        val item = items.value["item_id"] ?: fail()
        assertThat(item.startState.locationText).isEqualTo("Charles de Gaule Airport")
        assertThat(item.startState.searchResults).isEmpty()
    }

    @Test
    fun `airport from search result tapped should update item with new airport's timezone`() {
        val airportTimeZone: ZoneId = ZoneId.of("Europe/Paris")
        val expected: Airport = mock {
            on { timeZone } doReturn TimeZone.getTimeZone(airportTimeZone.id)
        }
        repository.stub {
            onBlocking { details("airport_id") } doReturn expected
        }
        val originalData = PendingFlight(
            id = "flight_id",
            departure = Time("2024-05-17T10:55:00+02:00"),
            airportFromSearchResults = listOf(
                mock(),
                mock { on { iata } doReturn "airport_id" },
                mock(),
            ),
        )
        itemStore.stub {
            on { getData("flight_id") } doReturn originalData
        }
        subject.airportFromSearchResultTapped("flight_id", 1)
        val result = itemStore.getUpdateResult(originalData)
        assertThat(result.departure.zone).isEqualTo(airportTimeZone)
    }

    @Test
    fun `airport to search text changed should update results with repository data`() {
        val results = listOf(
            AirportSearchResult("CDG", "Charles de Gaule Airport", "Paris, FR"),
            AirportSearchResult("ORY", "Orly Airport", "Paris, FR"),
            AirportSearchResult("BVA", "Beauvais Airport", "Paris, FR"),
        )
        repository.stub {
            onBlocking { autocomplete("par") } doReturn results
        }
        subject.addItem("item_id", Time("2024-10-16T18:25+02:00"), mock())
        subject.airportToSearchTextChanged("item_id", "par")
        val item = items.value["item_id"] ?: fail()
        assertThat(item.endState.searchResults).isEqualTo(
            listOf(
                AutoCompleteResultState(
                    title = "Charles de Gaule Airport",
                    subtitle = "Paris, FR",
                ),
                AutoCompleteResultState(
                    title = "Orly Airport",
                    subtitle = "Paris, FR",
                ),
                AutoCompleteResultState(
                    title = "Beauvais Airport",
                    subtitle = "Paris, FR",
                ),
            )
        )
    }

    @Test
    fun `airport to search result tapped should update item with selected airport`() {
        val expected = AirportSearchResult("CDG", "Charles de Gaule Airport", "Paris, FR")
        val originalData = PendingFlight(
            id = "item_id", departure = mock(), airportToSearchResults = listOf(
                mock(),
                expected,
                mock(),
            )
        )
        val airport: Airport = mock {
            on { name } doReturn "Charles de Gaule Airport"
            on { timeZone } doReturn TimeZone.getTimeZone("Europe/Paris")
        }
        repository.stub {
            onBlocking { details("CDG") } doReturn airport
        }
        itemStore.stub {
            on { getData("item_id") } doReturn originalData
        }
        subject.addItem("item_id", Time("2025-10-16T15:23:00+01:00"), mock())
        subject.airportToSearchResultTapped("item_id", 1)
        val item = items.value["item_id"] ?: fail()
        assertThat(item.endState.locationText).isEqualTo("Charles de Gaule Airport")
        assertThat(item.endState.searchResults).isEmpty()
    }

    @Test
    fun `airport to search result tapped should update item with new airport's timezone`() {
        val airportTimeZone: ZoneId = ZoneId.of("Europe/Paris")
        val expected: Airport = mock {
            on { timeZone } doReturn TimeZone.getTimeZone(airportTimeZone.id)
        }
        repository.stub {
            onBlocking { details("airport_id") } doReturn expected
        }
        val originalData = PendingFlight(
            id = "flight_id",
            departure = mock(),
            arrival = Time("2024-05-17T10:55 +02:00"),
            airportToSearchResults = listOf(
                mock(),
                mock { on { iata } doReturn "airport_id" },
                mock(),
            ),
        )
        itemStore.stub {
            on { getData("flight_id") } doReturn originalData
        }
        subject.addItem("flight_id", Time("2025-10-16T15:23:00+01:00"), mock())
        subject.airportToSearchResultTapped("flight_id", 1)
        val result = itemStore.getUpdateResult(originalData)
        assertThat(result.arrival?.zone).isEqualTo(airportTimeZone)
    }
}
