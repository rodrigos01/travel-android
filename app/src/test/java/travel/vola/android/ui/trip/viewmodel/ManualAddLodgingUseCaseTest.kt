package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.TestScope
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
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.SimplePlace
import travel.vola.android.model.repository.LodgingSearchRepository
import travel.vola.android.test.Captor.getUpdateResult
import travel.vola.android.test.Mocks.mockItemStore
import travel.vola.android.test.UnconfinedDispatcherTestRule
import travel.vola.android.ui.trip.creation.usecase.PendingData.PendingLodging
import travel.vola.android.ui.trip.state.AutoCompleteResultState
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState

class ManualAddLodgingUseCaseTest {
    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val testScope = TestScope(rule.dispatcher)

    private val repository: LodgingSearchRepository = mock()
    private val itemStore = mockItemStore<PendingLodging, ManualAddLodgingItemState>()
    private val subject = ManualAddLodgingUseCase(testScope, itemStore, repository)

    private val items = subject.items.stateIn(
        testScope, started = SharingStarted.Eagerly, initialValue = emptyMap()
    )

    @Test
    fun `itemStore data added should update items`() {
        itemStore.addItem("lodging_id", mock {
            on { id } doReturn "lodging_id"
            on { checkIn } doReturn Time("2025-10-16T15:23:00+01:00")
            on { checkOut } doReturn Time("2025-10-17T10:52:00+01:00")
        }, mock())
        assertThat(items["lodging_id"]?.id).isEqualTo("lodging_id")
        assertThat(items["lodging_id"]?.startState?.time).isEqualTo(Time("2025-10-16T15:23:00+01:00"))
        assertThat(items["lodging_id"]?.endState?.time).isEqualTo(Time("2025-10-17T10:52:00+01:00"))
    }

    @Test
    fun `added item should be initialized empty`() {
        subject.addItem("lodging_id", Time("2025-10-16T15:23:00+01:00"), mock())
        val item = items.value["lodging_id"] ?: fail()

        assertThat(item.startState.locationText).isNull()
        assertThat(item.startState.searchResults).isEmpty()
    }

    @Test
    fun `added item should have params`() {
        val params = AddPlanUseCase.StateParams(
            dateSelectionEnabled = true,
            deleteEnabled = false,
            typeSelectionEnabled = true,
        )
        subject.addItem("lodging_id", Time("2025-10-16T15:23:00+01:00"), params)
        val item = items.value["lodging_id"] ?: fail()
        assertThat(item.dateSelectionEnabled).isTrue()
        assertThat(item.deleteButtonEnabled).isFalse()
        assertThat(item.typeSelectionEnabled).isTrue()
    }

    @Test
    fun `added item from entity should be initialized with entity data`() {
        val entity = mock<Lodging> {
            on { id } doReturn "hotel_id"
            on { name } doReturn "Hotel Novotel Paris Les Halles"
            on { address } doReturn "Blvd Les Halles, 45"
            on { city } doReturn mock()
            on { checkIn } doReturn Time("2025-10-16T15:00:00+01:00")
            on { checkout } doReturn Time("2025-10-17T11:00:00+01:00")
        }
        subject.addItem("lodging_id", entity, mock())
        val item = items.value["lodging_id"] ?: fail()
        assertThat(item.startState.locationText).isEqualTo("Hotel Novotel Paris Les Halles")
        assertThat(item.startState.time).isEqualTo(entity.checkIn)
        assertThat(item.endState.time).isEqualTo(entity.checkout)
    }

    @Test
    fun `added item should be initialized with initial time as check-in`() {
        val initialTime = Time("2025-10-16T15:23:00+01:00")
        subject.addItem("lodging_id", initialTime, mock())
        val item = items.value["lodging_id"] ?: fail()
        assertThat(item.startState.time).isEqualTo(initialTime)
    }

    @Test
    fun `added item should be initialized with day after initial time as check-out`() {
        val expected = Time("2025-10-17T00:43:00+01:00")
        val initialTime = Time("2025-10-16T15:43:00+01:00")
        subject.addItem("lodging_id", initialTime, mock())
        val item = items.value["lodging_id"] ?: fail()
        assertThat(item.endState.time).isEqualTo(expected)
    }

    @Test
    fun `remove should call itemStore remove`() {
        val item = mock<ManualAddLodgingItemState> {
            on { id } doReturn "lodging_id"
        }
        subject.removeItem(item)
        verify(itemStore).remove("lodging_id")
    }

    @Test
    fun `set check-in time should update check-in time`() {
        val newTime = Time("2025-10-17T10:52:00+01:00")
        val originalTime = Time("2025-10-17T15:23:00+01:00")
        subject.addItem("lodging_id", originalTime, mock())
        subject.setCheckInTime("lodging_id", newTime)
        val item = items.value["lodging_id"] ?: fail()
        assertThat(item.startState.time).isEqualTo(newTime)
    }

    @Test
    fun `set check-out time should update check-out time`() {
        val newTime = Time("2025-10-17T10:52:00+01:00")
        subject.addItem("lodging_id", Time("2025-10-16T15:23:00+01:00"), mock())
        subject.setCheckOutTime("lodging_id", newTime)
        val item = items.value["lodging_id"] ?: fail()
        assertThat(item.endState.time).isEqualTo(newTime)
    }

    @Test
    fun `location text change should update search results with repository results`() {
        val results = List(3) { index ->
            SimplePlace(
                id = "hotel_$index",
                name = "Hotel $index",
                address = "Address $index",
            )
        }
        repository.stub {
            onBlocking { autocomplete("hotel", autocompleteKey = "lodging_id") } doReturn results
        }
        subject.addItem("lodging_id", Time("2025-10-16T15:23:00+01:00"), mock())
        subject.locationTextChanged("lodging_id", "hotel")
        val item = items.value["lodging_id"] ?: fail()
        assertThat(item.startState.searchResults).isEqualTo(List(3) { index ->
            AutoCompleteResultState(
                title = "Hotel $index",
                subtitle = "Address $index",
            )
        })
    }

    @Test
    fun `lodging search result tapped should update item with selected lodging`() {
        val expected: SimplePlace = mock {
            on { id } doReturn "hotel_id"
            on { name } doReturn "Hotel Novotel Paris Les Halles"
            on { address } doReturn "Blvd Les Halles, 45"
        }
        val originalData = PendingLodging(
            id = "lodging_id", checkIn = mock(), checkOut = mock(), searchResults = listOf(
                mock(),
                expected,
                mock(),
            )
        )
        itemStore.stub {
            on { getData("lodging_id") } doReturn originalData
        }
        subject.addItem("lodging_id", Time("2025-10-16T15:23:00+01:00"), mock())
        subject.locationSearchResultTapped("lodging_id", 1)
        val item = items.value["lodging_id"] ?: fail()
        assertThat(item.startState.locationText).isEqualTo("Hotel Novotel Paris Les Halles")
        assertThat(item.startState.searchResults).isEmpty()
    }

    @Test
    fun `lodging search result tapped should update item with repository result`() {
        val paris = mock<Place>()
        repository.stub {
            onBlocking { placeCity("hotel_id", "lodging_id") } doReturn paris
        }
        val expected: SimplePlace = mock {
            on { id } doReturn "hotel_id"
            on { name } doReturn "Hotel Novotel Paris Les Halles"
            on { address } doReturn "Blvd Les Halles, 45"
        }
        val originalData = PendingLodging(
            id = "lodging_id", checkIn = mock(), checkOut = mock(), searchResults = listOf(
                mock(),
                expected,
                mock(),
            )
        )
        itemStore.stub {
            on { getData("lodging_id") } doReturn originalData
        }
        subject.locationSearchResultTapped("lodging_id", 1)
        val result = itemStore.getUpdateResult(originalData)
        assertThat(result.city).isEqualTo(paris)
    }

    @Test
    fun `create entity should create lodging from item`() {
        val paris = mock<Place>()
        itemStore.stub {
            on { getData("lodging_id") } doReturn PendingLodging(
                id = "lodging_id",
                entityId = "hotel_id",
                name = "Hotel Novotel Paris Les Halles",
                address = "Blvd Les Halles, 45",
                checkIn = Time("2025-10-16T15:23:00+01:00"),
                checkOut = Time("2025-10-17T10:52:00+01:00"),
                city = paris,
            )
        }
        val item = mock<ManualAddLodgingItemState> {
            on { id } doReturn "lodging_id"
        }
        val entity = subject.createEntity(item)
        assertThat(entity.id).isEqualTo("hotel_id")
        assertThat(entity.name).isEqualTo("Hotel Novotel Paris Les Halles")
        assertThat(entity.address).isEqualTo("Blvd Les Halles, 45")
        assertThat(entity.checkIn).isEqualTo(Time("2025-10-16T15:23:00+01:00"))
        assertThat(entity.checkout).isEqualTo(Time("2025-10-17T10:52:00+01:00"))
        assertThat(entity.city).isEqualTo(paris)
    }
}
