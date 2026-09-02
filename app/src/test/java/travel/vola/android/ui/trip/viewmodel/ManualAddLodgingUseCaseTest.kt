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
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.SimplePlace
import travel.vola.android.model.repository.LodgingSearchRepository
import travel.vola.android.ui.trip.creation.usecase.PendingDataStore
import travel.vola.android.ui.trip.state.AutoCompleteResultState

class ManualAddLodgingUseCaseTest {

    private val repository: LodgingSearchRepository = mock()
    private val pendingDataStore = PendingDataStore()
    private val subject = ManualAddLodgingUseCase(pendingDataStore, repository)

    @Test
    fun `created item should be initialized empty`() {
        val item = subject.createItem("lodging_id", zonedDateTime("2025-10-16T15:23:00+01:00"), mock())
        assertThat(item.startState.locationText).isNull()
        assertThat(item.startState.searchResults).isEmpty()
        assertThat(item.saveButtonEnabled).isFalse()
    }

    @Test
    fun `created item should have params`() {
        val params = AddPlanUseCase.StateParams(
            dateSelectionEnabled = true,
            deleteEnabled = false,
            typeSelectionEnabled = true,
        )
        val item = subject.createItem("lodging_id", zonedDateTime("2025-10-16T15:23:00+01:00"), params)
        assertThat(item.dateSelectionEnabled).isTrue()
        assertThat(item.deleteButtonEnabled).isFalse()
        assertThat(item.typeSelectionEnabled).isTrue()
    }

    @Test
    fun `created item from entity should be initialized with entity data`() {
        val entity = mock<Lodging> {
            on { id } doReturn "hotel_id"
            on { name } doReturn "Hotel Novotel Paris Les Halles"
            on { address } doReturn "Blvd Les Halles, 45"
            on { city } doReturn mock()
            on { checkIn } doReturn zonedDateTime("2025-10-16T15:00:00+01:00")
            on { checkout } doReturn zonedDateTime("2025-10-17T11:00:00+01:00")
        }
        val item = subject.createItem("lodging_id", entity, mock())
        assertThat(item.startState.locationText).isEqualTo("Hotel Novotel Paris Les Halles")
        assertThat(item.startState.dateTime).isEqualTo(entity.checkIn)
        assertThat(item.endState.dateTime).isEqualTo(entity.checkout)
    }

    @Test
    fun `created item should be initialized with initial time at full hour as check-in`() {
        val expected = zonedDateTime("2025-10-16T15:00:00+01:00")
        val initialTime = zonedDateTime("2025-10-16T15:23:00+01:00")
        val item = subject.createItem("lodging_id", initialTime, mock())
        assertThat(item.startState.dateTime).isEqualTo(expected)
    }

    @Test
    fun `created item should be initialized with day after initial time at 10am as check-out`() {
        val expected = zonedDateTime("2025-10-17T10:00:00+01:00")
        val initialTime = zonedDateTime("2025-10-16T15:43:00+01:00")
        val item = subject.createItem("lodging_id", initialTime, mock())
        assertThat(item.endState.dateTime).isEqualTo(expected)
    }

    @Test
    fun `set check-in time should update check-in time`() = runTest {
        val newTime = zonedDateTime("2025-10-17T10:52:00+01:00")
        val originalTime = zonedDateTime("2025-10-17T15:23:00+01:00")
        val original = subject.createItem("lodging_id", originalTime, mock())
        val updated = subject.onUpdated(
            original.copy(startState = original.startState.copy(dateTime = newTime, isTimeSet = true)),
        )
        assertThat(updated.startState.dateTime).isEqualTo(newTime)
    }

    @Test
    fun `set check-out time should update check-out time`() = runTest {
        val newTime = zonedDateTime("2025-10-17T10:52:00+01:00")
        val checkInTime = zonedDateTime("2025-10-16T15:23:00+01:00")
        val original = subject.createItem("lodging_id", checkInTime, mock())
        val updated = subject.onUpdated(
            original.copy(endState = original.endState.copy(dateTime = newTime, isTimeSet = true)),
        )
        assertThat(updated.endState.dateTime).isEqualTo(newTime)
    }

    @Test
    fun `location text change should update search results with repository results`() = runTest {
        val results = List(3) { index ->
            SimplePlace(id = "hotel_$index", name = "Hotel $index", address = "Address $index")
        }
        repository.stub {
            onBlocking { autocomplete("hotel", autocompleteKey = "lodging_id") } doReturn results
        }
        val original = subject.createItem("lodging_id", zonedDateTime("2025-10-16T15:23:00+01:00"), mock())
        val updated = subject.onUpdated(original.copy(startState = original.startState.copy(locationText = "hotel")))
        assertThat(updated.startState.searchResults).isEqualTo(
            List(3) { index ->
                AutoCompleteResultState(
                    id = "hotel_$index",
                    title = "Hotel $index",
                    subtitle = "Address $index",
                )
            },
        )
    }

    @Test
    fun `lodging search result tapped should update item with selected lodging`() = runTest {
        val hotelPlace: Place = mock {
            on { id } doReturn "hotel_id"
            on { name } doReturn "Hotel Novotel Paris Les Halles"
        }
        repository.stub {
            onBlocking { details("hotel_id", "lodging_id") } doReturn hotelPlace
            onBlocking { placeCity("hotel_id", "lodging_id") } doReturn mock()
        }
        val checkInTime = zonedDateTime("2025-10-16T15:23:00+01:00")
        val original = subject.createItem("lodging_id", checkInTime, mock())
        val withResults = original.copy(
            startState = original.startState.copy(
                searchResults = listOf(AutoCompleteResultState("hotel_id", "Hotel Novotel Paris Les Halles", "")),
            ),
        )
        val updated = subject.onUpdated(
            withResults.copy(startState = withResults.startState.copy(selectedResultId = "hotel_id")),
        )
        assertThat(updated.startState.locationText).isEqualTo("Hotel Novotel Paris Les Halles")
        assertThat(updated.startState.searchResults).isEmpty()
    }

    @Test
    fun `lodging search result tapped should update pending data with repository result`() = runTest {
        val paris = mock<Place>()
        val expected: Place = mock {
            on { id } doReturn "hotel_id"
            on { name } doReturn "Hotel Novotel Paris Les Halles"
            on { address } doReturn "Blvd Les Halles, 45"
            on { latitude } doReturn 48.866667
            on { longitude } doReturn 2.333333
        }
        repository.stub {
            onBlocking { placeCity("hotel_id", "lodging_id") } doReturn paris
            onBlocking { details("hotel_id", "lodging_id") } doReturn expected
        }
        val checkInTime = zonedDateTime("2025-10-16T15:00:00+01:00")
        val original = subject.createItem("lodging_id", checkInTime, mock())
        val withResults = original.copy(
            startState = original.startState.copy(
                searchResults = listOf(AutoCompleteResultState("hotel_id", "Hotel Novotel Paris Les Halles", "")),
            ),
        )
        subject.onUpdated(withResults.copy(startState = withResults.startState.copy(selectedResultId = "hotel_id")))
        val entry = pendingDataStore.current as PendingDataStore.Entry.Lodging
        assertThat(entry.city).isEqualTo(paris)
        assertThat(entry.selectedPlace).isEqualTo(expected)
    }

    @Test
    fun `selecting the same already-resolved lodging again should not re-resolve`() = runTest {
        val expected: Place = mock { on { id } doReturn "hotel_id" }
        repository.stub {
            onBlocking { placeCity("hotel_id", "lodging_id") } doReturn mock()
            onBlocking { details("hotel_id", "lodging_id") } doReturn expected
        }
        val checkInTime = zonedDateTime("2025-10-16T15:00:00+01:00")
        val original = subject.createItem("lodging_id", checkInTime, mock())
        val withResults = original.copy(
            startState = original.startState.copy(
                searchResults = listOf(AutoCompleteResultState("hotel_id", "Hotel", "")),
            ),
        )
        val resolved = subject.onUpdated(
            withResults.copy(startState = withResults.startState.copy(selectedResultId = "hotel_id")),
        )
        subject.onUpdated(resolved)
        verify(repository).details("hotel_id", "lodging_id")
        verify(repository).placeCity("hotel_id", "lodging_id")
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun `create entity should create lodging from item and pending data`() {
        val paris = mock<Place>()
        val entity = mock<Lodging> {
            on { id } doReturn "hotel_id"
            on { name } doReturn "Hotel Novotel Paris Les Halles"
            on { address } doReturn "Blvd Les Halles, 45"
            on { latitude } doReturn 48.866667
            on { longitude } doReturn 2.333333
            on { city } doReturn paris
            on { checkIn } doReturn zonedDateTime("2025-10-16T15:23:00+01:00")
            on { checkout } doReturn zonedDateTime("2025-10-17T10:52:00+01:00")
        }
        val item = subject.createItem("lodging_id", entity, mock())
        val built = subject.createEntity(item)
        assertThat(built.id).isEqualTo("hotel_id")
        assertThat(built.name).isEqualTo("Hotel Novotel Paris Les Halles")
        assertThat(built.address).isEqualTo("Blvd Les Halles, 45")
        assertThat(built.latitude).isEqualTo(48.866667)
        assertThat(built.longitude).isEqualTo(2.333333)
        assertThat(built.checkIn).isEqualTo(zonedDateTime("2025-10-16T15:23:00+01:00"))
        assertThat(built.checkout).isEqualTo(zonedDateTime("2025-10-17T10:52:00+01:00"))
        assertThat(built.city).isEqualTo(paris)
    }
}
