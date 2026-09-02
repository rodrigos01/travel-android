package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.AirportSearchResult
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.repository.AddFlightRepository
import travel.vola.android.ui.trip.creation.usecase.PendingDataStore
import travel.vola.android.ui.trip.state.AutoCompleteResultState
import java.time.ZoneId
import java.util.TimeZone

class AddFlightUseCaseTest {

    private val repository: AddFlightRepository = mock()
    private val pendingDataStore = PendingDataStore()
    private val subject = AddFlightUseCase(pendingDataStore, repository)

    @Test
    fun `created item should be initialized empty`() {
        val item = subject.createItem("flight_id", zonedDateTime("2024-10-16T18:25+02:00"), mock())
        assertThat(item.startState.locationText).isNull()
        assertThat(item.startState.searchResults).isEmpty()
        assertThat(item.saveButtonEnabled).isFalse()
    }

    @Test
    fun `created item should be initialized with initial time as departure`() {
        val initialTime = zonedDateTime("2024-10-16T18:25+02:00")
        val item = subject.createItem("flight_id", initialTime, mock())
        assertThat(item.startState.dateTime).isEqualTo(initialTime)
    }

    @Test
    fun `created item should have params`() {
        val params = AddPlanUseCase.StateParams(
            dateSelectionEnabled = true,
            deleteEnabled = false,
            typeSelectionEnabled = true,
        )
        val item = subject.createItem("flight_id", zonedDateTime("2025-10-16T15:23:00+01:00"), params)
        assertThat(item.dateSelectionEnabled).isTrue()
        assertThat(item.deleteButtonEnabled).isFalse()
        assertThat(item.typeSelectionEnabled).isTrue()
    }

    @Test
    fun `created item from entity should be initialized with entity data`() {
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
            on { departure } doReturn zonedDateTime("2024-10-16T18:25-05:00")
            on { airportTo } doReturn destination
            on { arrival } doReturn zonedDateTime("2024-10-17T06:15+02:00")
        }
        val entity = mock<Flight> {
            on { id } doReturn "flight_id"
            on { segments } doReturn listOf(segment)
        }
        val item = subject.createItem("flight_id", entity, mock())
        assertThat(item.startState.locationText).isEqualTo("John F. Kennedy International Airport")
        assertThat(item.endState.locationText).isEqualTo("Orly Airport")
        assertThat(item.startState.dateTime).isEqualTo(zonedDateTime("2024-10-16T18:25-05:00"))
        assertThat(item.endState.dateTime).isEqualTo(zonedDateTime("2024-10-17T06:15+02:00"))
    }

    @Test
    fun `set departure time should update departure time`() = runTest {
        val originalTime = zonedDateTime("2024-10-16T18:25+02:00")
        val original = subject.createItem("item_id", originalTime, mock())
        val updated = subject.onUpdated(
            original.copy(
                startState = original.startState.copy(
                    dateTime = zonedDateTime("2024-10-16T09:15+02:00"),
                    isTimeSet = true,
                ),
            ),
        )
        assertThat(updated.startState.dateTime).isEqualTo(zonedDateTime("2024-10-16T09:15+02:00"))
    }

    @Test
    fun `airport from search text changed should fetch results from repository`() = runTest {
        repository.stub {
            onBlocking { autocomplete("par") } doReturn emptyList()
        }
        val original = subject.createItem("item_id", zonedDateTime("2024-10-16T18:25+02:00"), mock())
        subject.onUpdated(original.copy(startState = original.startState.copy(locationText = "par")))
        verify(repository).autocomplete("par")
    }

    @Test
    fun `airport from search text changed should update results with repository data`() = runTest {
        val results = listOf(
            AirportSearchResult("CDG", "Charles de Gaule", "Paris, FR"),
            AirportSearchResult("ORY", "Orly Airport", "Paris, FR"),
            AirportSearchResult("BVA", "Beauvais Airport", "Paris, FR"),
        )
        repository.stub {
            onBlocking { autocomplete("par") } doReturn results
        }
        val original = subject.createItem("item_id", zonedDateTime("2024-10-16T18:25+02:00"), mock())
        val updated = subject.onUpdated(original.copy(startState = original.startState.copy(locationText = "par")))
        assertThat(updated.startState.searchResults).isEqualTo(
            listOf(
                AutoCompleteResultState("CDG", "Charles de Gaule", "Paris, FR"),
                AutoCompleteResultState("ORY", "Orly Airport", "Paris, FR"),
                AutoCompleteResultState("BVA", "Beauvais Airport", "Paris, FR"),
            ),
        )
    }

    @Test
    fun `airport from search result tapped should update item with selected airport`() = runTest {
        val airport: Airport = mock {
            on { name } doReturn "Charles de Gaule Airport"
            on { timeZone } doReturn TimeZone.getTimeZone("Europe/Paris")
        }
        repository.stub {
            onBlocking { details("CDG") } doReturn airport
        }
        val time = zonedDateTime("2025-10-16T15:23:00+01:00")
        val original = subject.createItem("item_id", time, mock())
        val withResults = original.copy(
            startState = original.startState.copy(
                searchResults = listOf(AutoCompleteResultState("CDG", "Charles de Gaule Airport", "Paris, FR")),
            ),
        )
        val updated = subject.onUpdated(
            withResults.copy(startState = withResults.startState.copy(selectedResultId = "CDG")),
        )
        assertThat(updated.startState.locationText).isEqualTo("Charles de Gaule Airport")
        val entry = pendingDataStore.current as PendingDataStore.Entry.Flight
        assertThat(entry.airportFrom).isEqualTo(airport)
    }

    @Test
    fun `selecting the same already-resolved airport again should not re-resolve`() = runTest {
        val airport: Airport = mock {
            on { iata } doReturn "CDG"
            on { name } doReturn "Charles de Gaule Airport"
            on { timeZone } doReturn TimeZone.getTimeZone("Europe/Paris")
        }
        repository.stub {
            onBlocking { details("CDG") } doReturn airport
        }
        val time = zonedDateTime("2025-10-16T15:23:00+01:00")
        val original = subject.createItem("item_id", time, mock())
        val withResults = original.copy(
            startState = original.startState.copy(
                searchResults = listOf(AutoCompleteResultState("CDG", "Charles de Gaule Airport", "Paris, FR")),
            ),
        )
        val resolved = subject.onUpdated(
            withResults.copy(startState = withResults.startState.copy(selectedResultId = "CDG")),
        )
        subject.onUpdated(resolved)
        verify(repository).details("CDG")
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun `airport from search result tapped should update item with new airport's timezone`() = runTest {
        val airportTimeZone: ZoneId = ZoneId.of("Europe/Paris")
        val expected: Airport = mock {
            on { timeZone } doReturn TimeZone.getTimeZone(airportTimeZone.id)
        }
        repository.stub {
            onBlocking { details("airport_id") } doReturn expected
        }
        val departure = zonedDateTime("2024-10-16T18:25:00+02:00")
        val original = subject.createItem("item_id", departure, mock())
        val withResults = original.copy(
            startState = original.startState.copy(
                searchResults = listOf(AutoCompleteResultState("airport_id", "Some Airport", "Paris, FR")),
            ),
        )
        val updated = subject.onUpdated(
            withResults.copy(startState = withResults.startState.copy(selectedResultId = "airport_id")),
        )
        assertThat(updated.startState.dateTime?.zone).isEqualTo(airportTimeZone)
    }

    @Test
    fun `create entity should build flight from item and pending data`() {
        val originAirport: Airport = mock()
        val destinationAirport: Airport = mock { on { timeZone } doReturn TimeZone.getTimeZone("Europe/Paris") }
        val segment = mock<FlightSegment> {
            on { airportFrom } doReturn originAirport
            on { departure } doReturn zonedDateTime("2024-10-16T18:25+02:00")
            on { airportTo } doReturn destinationAirport
            on { arrival } doReturn zonedDateTime("2024-10-16T20:25+02:00")
        }
        val entity = mock<Flight> {
            on { id } doReturn "flight_id"
            on { segments } doReturn listOf(segment)
        }
        val item = subject.createItem("item_id", entity, mock())
        val built = subject.createEntity(item)
        assertThat(built.id).isEqualTo("flight_id")
        assertThat(built.segments.first().departure).isEqualTo(zonedDateTime("2024-10-16T18:25+02:00"))
        assertThat(built.segments.first().arrival).isEqualTo(zonedDateTime("2024-10-16T20:25+02:00"))
    }
}
