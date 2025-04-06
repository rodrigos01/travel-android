package travel.vola.android.ui.trip.viewmodel

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.TestScope
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import travel.vola.android.extensions.Time
import travel.vola.android.extensions.get
import travel.vola.android.extensions.set
import travel.vola.android.extensions.toMidnight
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.SimplePlace
import travel.vola.android.model.data.Time
import travel.vola.android.model.repository.LodgingSearchRepository
import travel.vola.android.test.Mocks.mockTime
import travel.vola.android.test.UnconfinedDispatcherTestRule
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.creation.usecase.PendingData.PendingLodging
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState
import java.time.Duration
import java.util.concurrent.TimeUnit

class ManualAddLodgingUseCaseTest {
    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val testScope = TestScope(rule.dispatcher)

    private val repository: LodgingSearchRepository = mock()
    private val itemFlow = MutableStateFlow<Map<String, ManualAddLodgingItemState>>(emptyMap())
    private val itemStore =
        mock<AddPlanItemStore<PendingLodging, ManualAddLodgingItemState>> {
            val captor =
                argumentCaptor<(PendingLodging, AddPlanUseCase.StateParams) -> ManualAddLodgingItemState>()
            on { items(captor.capture()) } doReturn itemFlow
            on { addItem(any(), any()) } doAnswer {
                val data = it.getArgument<PendingLodging>(0)
                val params = it.getArgument<AddPlanUseCase.StateParams>(1)
                itemFlow[data.id] = captor.firstValue(data, params)
            }
        }
    private val subject = ManualAddLodgingUseCase(testScope, itemStore, repository)

    private val items = subject.items.stateIn(
        testScope, started = SharingStarted.Eagerly, initialValue = emptyMap()
    )

    @Test
    fun `itemStore items updated should update items`() {
        val item = mock<ManualAddLodgingItemState> {
            on { id } doReturn "lodging_id"
        }
        itemFlow["lodging_id"] = item
        assertThat(items["lodging_id"]).isEqualTo(item)
    }

    @Test
    fun `added item should be initialized empty`() {
        val time = mockTime()
        subject.addItem("lodging_id", time, mock())
        val item = items.value["lodging_id"] ?: fail()

        assertThat(item.startState.locationText).isNull()
        assertThat(item.startState.searchResults).isEmpty()
    }

    @Test
    fun `added item should be initialized with initial time as check-in`() {
        val initialTime = mockTime()
        subject.addItem("lodging_id", initialTime, mock())
        val item = items.value["lodging_id"] ?: fail()
        assertThat(item.startState.time).isEqualTo(initialTime)
    }

    @Test
    fun `added item should be initialized with day after initial time as check-out`() {
        val expected = mockTime()
        val midnightTime = mockTime {
            on { plus(Duration.ofMillis(TimeUnit.DAYS.toMillis(1))) } doReturn expected
        }
        mockkStatic("travel.vola.android.extensions.TimeKt")
        val initialTime = mockk<Time>()
        every { initialTime.toMidnight() } returns midnightTime
        subject.addItem("lodging_id", initialTime, mock())
        val item = items.value["lodging_id"] ?: fail()
        assertThat(item.endState.time).isEqualTo(expected)
    }

    @Test
    fun `remove should call itemStore remove`() {
        val item = mock<ManualAddLodgingItemState>()
        subject.removeItem(item)
        verify(itemStore).remove(item)
    }

    @Test
    fun `set check-in time should update check-in time`() {
        val newTime = Time("2025-10-17T10:52:00+01:00")
        val originalTime = Time("2025-10-17T15:23:00+01:00")
        val originalData =
            PendingLodging(id = "lodging_id", checkIn = originalTime, checkOut = mock())
        subject.setCheckInTime("lodging_id", Time("2025-10-17T10:52:00+01:00"))
        val result = getUpdateResult(originalData)
        assertThat(result.checkIn).isEqualTo(newTime)
    }

    @Test
    fun `set check-out time should update check-out time`() {
        val newTime = Time("2025-10-17T10:52:00+01:00")
        val originalTime = Time("2025-10-17T15:23:00+01:00")
        val originalData =
            PendingLodging(id = "lodging_id", checkIn = mock(), checkOut = originalTime)
        subject.setCheckOutTime("lodging_id", newTime)
        val result = getUpdateResult(originalData)
        assertThat(result.checkOut).isEqualTo(newTime)
    }

    @Test
    fun `lodging search result tapped should update item with selected lodging`() {
        val expected: SimplePlace = mock {
            on { id } doReturn "hotel_id"
            on { name } doReturn "Hotel Novotel Paris Les Halles"
            on { address } doReturn "Blvd Les Halles, 45"
        }
        val originalData =
            PendingLodging(
                id = "lodging_id",
                checkIn = mock(),
                checkOut = mock(),
                searchResults = listOf(
                    mock(),
                    expected,
                    mock(),
                )
            )
        itemStore.stub {
            on { getData("lodging_id") } doReturn originalData
        }
        subject.locationSearchResultTapped("lodging_id", 1)
        val result = getUpdateResult(originalData)
        assertThat(result.name).isEqualTo("Hotel Novotel Paris Les Halles")
        assertThat(result.address).isEqualTo("Blvd Les Halles, 45")
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
        val originalData =
            PendingLodging(
                id = "lodging_id",
                checkIn = mock(),
                checkOut = mock(),
                searchResults = listOf(
                    mock(),
                    expected,
                    mock(),
                )
            )
        itemStore.stub {
            on { getData("lodging_id") } doReturn originalData
        }
        subject.locationSearchResultTapped("lodging_id", 1)
        val result = getUpdateCaptor().lastValue(originalData)
        assertThat(result.city).isEqualTo(paris)
    }

    private fun getUpdateCaptor() =
        argumentCaptor<(PendingLodging) -> PendingLodging> {
            verify(itemStore, atLeastOnce()).update(any(), capture())
        }

    private fun getUpdateResult(originalData: PendingLodging) =
        getUpdateCaptor().firstValue(originalData)
}
