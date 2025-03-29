package travel.vola.android.ui.trip.viewmodel

import com.vola.android.extensions.toMidnight
import com.vola.android.model.data.Place
import com.vola.android.model.data.SimplePlace
import com.vola.android.model.data.Time
import com.vola.android.model.repository.LodgingSearchRepository
import com.vola.android.test.Mocks.mockTime
import com.vola.android.test.UnconfinedDispatcherTestRule
import com.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import com.vola.android.ui.trip.creation.usecase.PendingData.PendingLodging
import com.vola.android.ui.trip.state.ManualAddLodgingItemState
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.TestScope
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.concurrent.TimeUnit

class AddLodgingDetailsSearchResultUseCaseTest {
    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val repository: LodgingSearchRepository = mock()
    private val itemFlow = MutableStateFlow(mapOf<String, ManualAddLodgingItemState>())
    private val itemStore =
        mock<AddPlanItemStore<PendingLodging, ManualAddLodgingItemState>> {
            on { items(any()) } doReturn itemFlow
        }
    private val subject = AddLodgingUseCase(itemStore, repository)

    private val items = subject.items.stateIn(
        TestScope(rule.dispatcher), started = SharingStarted.Eagerly, initialValue = emptyMap()
    )

    @Test
    fun `itemStore items updated should update items`() {
        val item = mock<ManualAddLodgingItemState> {
            on { id } doReturn "lodging_id"
        }
        itemFlow.value = mapOf("lodging_id" to item)
        assertThat(items.value["lodging_id"]).isEqualTo(item)
    }

    @Test
    fun `added item should be initialized empty`() {
        val time = mockTime()
        val data = subject.addItem(time, mock())
        assertThat(data.startState.locationText).isNull()
        assertThat(data.startState.searchResults).isEmpty()
    }

    @Test
    fun `added item should be initialized with initial time as check-in`() {
        val initialTime = mockTime()
        val data = subject.addItem(initialTime, mock())
        assertThat(data.startState.time).isEqualTo(initialTime)
    }

    @Test
    fun `added item should be initialized with day after initial time as check-out`() {
        val expected = mockTime()
        val midnightTime = mockTime {
            on { plus(TimeUnit.DAYS.toMillis(1)) } doReturn expected
        }
        mockkStatic("com.combah.travel2.extensions.TimeKt")
        val initialTime = mockk<Time>()
        every { initialTime.toMidnight() } returns midnightTime
        val data = subject.addItem(initialTime, mock())
        assertThat(data.endState.time).isEqualTo(expected)
    }

    @Test
    fun `remove should call itemStore remove`() {
        val item = mock<ManualAddLodgingItemState>()
        subject.removeItem(item)
        verify(itemStore).remove(item)
    }

    @Test
    fun `set check-in time should update departure time`() {
        val newTime = mockTime()
        val originalTime = mockTime {
            on { copy(hour = 10, minute = 52) } doReturn newTime
        }
        val originalData =
            PendingLodging(id = "lodging_id", checkIn = originalTime, checkOut = mock())
        subject.setCheckInTime("lodging_id", hour = 10, minute = 52)
        val result = getUpdateResult(originalData)
        assertThat(result.checkIn).isEqualTo(newTime)
    }

    @Test
    fun `set arrival day should update arrival day`() {
        val newTime = mockTime()
        val originalTime = mockTime {
            on { copy(dayOfMonth = 21, month = 4, year = 2024) } doReturn newTime
        }
        val receivedTime = mockTime {
            on { dayOfMonth } doReturn 21
            on { month } doReturn 4
            on { year } doReturn 2024
        }
        val originalData =
            PendingLodging(id = "lodging_id", checkIn = mockTime(), checkOut = originalTime)
        subject.setCheckOutDate("lodging_id", receivedTime)
        val result = getUpdateResult(originalData)
        assertThat(result.checkOut).isEqualTo(newTime)
    }

    @Test
    fun `set arrival time should update arrival time`() {
        val newTime = mockTime()
        val originalTime = mockTime {
            on { copy(hour = 11, minute = 43) } doReturn newTime
        }
        val originalData =
            PendingLodging(id = "lodging_id", checkIn = mockTime(), checkOut = originalTime)
        subject.setCheckoutTime("lodging_id", hour = 11, minute = 43)
        val result = getUpdateResult(originalData)
        assertThat(result.checkOut).isEqualTo(newTime)
    }

    @Test
    fun `lodging search result tapped should update item with selected lodging`() {
        val paris = mock<Place>()
        val expected: SimplePlace = mock {
            on { name } doReturn "Hotel Novotel Paris Les Halles"
            on { address } doReturn "Blvd Les Halles, 45"
            on { city } doReturn paris
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
        subject.locationSearchResultTapped("lodging_id", 1)
        val result = getUpdateResult(originalData)
        assertThat(result.name).isEqualTo("Hotel Novotel Paris Les Halles")
        assertThat(result.address).isEqualTo("Blvd Les Halles, 45")
        assertThat(result.city).isEqualTo(paris)
    }

    private fun getUpdateResult(originalData: PendingLodging): PendingLodging {
        return getUpdateResult(itemStore, originalData)
    }
}
