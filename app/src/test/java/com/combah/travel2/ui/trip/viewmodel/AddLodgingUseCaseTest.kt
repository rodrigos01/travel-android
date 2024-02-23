package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.extensions.get
import com.combah.travel2.extensions.toMidnight
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.SimplePlace
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.repository.AddLodgingRepository
import com.combah.travel2.test.Captor.getUpdateResult
import com.combah.travel2.test.Mocks.mockTime
import com.combah.travel2.test.UnconfinedDispatcherTestRule
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemStore
import com.combah.travel2.ui.trip.creation.usecase.AutoCompleteUseCase
import com.combah.travel2.ui.trip.creation.usecase.AutoCompleteUseCase.AutoCompleteState
import com.combah.travel2.ui.trip.creation.usecase.InputUseCaseStore
import com.combah.travel2.ui.trip.viewmodel.AddLodgingUseCase.AddLodgingItem
import com.combah.travel2.ui.trip.viewmodel.AddLodgingUseCase.InputState
import com.combah.travel2.ui.trip.viewmodel.AddLodgingUseCase.InputUseCaseSet
import com.combah.travel2.ui.trip.viewmodel.AddLodgingUseCase.PendingLodging
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
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import java.util.concurrent.TimeUnit

class AddLodgingUseCaseTest {
    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val repository: AddLodgingRepository = mock()
    private val formatter: TimeFormatter = mock {
        on { dayOfMonthString(any()) } doReturn ""
        on { dayOfWeekString(any()) } doReturn ""
    }
    private val itemFlow = MutableStateFlow(mapOf<String, AddLodgingItem>())
    private val itemStore =
        mock<AddPlanItemStore<PendingLodging, AddLodgingItem>> {
            on { items } doReturn itemFlow
        }
    private val autoCompleteState =
        MutableStateFlow<AutoCompleteState<SimplePlace>>(mock {
            on { searchResults } doReturn emptyList()
        })
    private val autoCompleteUseCase: AutoCompleteUseCase<SimplePlace> = mock {
        on { state } doReturn autoCompleteState
    }
    private val inputUseCaseSet = InputUseCaseSet(autoCompleteUseCase)
    private val inputUseCaseStates =
        MutableStateFlow<Map<String, InputState>>(emptyMap())
    private val inputUseCaseStore =
        mock<InputUseCaseStore<InputUseCaseSet, InputState>> {
            on { get(any()) } doReturn inputUseCaseSet
            on { inputStates } doReturn inputUseCaseStates
        }
    private val subject = AddLodgingUseCase(
        repository, formatter,
        itemStoreFactory = mock {
            on { create(any(), any()) } doReturn itemStore
        },
        inputUseCaseStoreFactory = mock {
            on { create(any()) } doReturn inputUseCaseStore
        },
    )

    private val items = subject.items.stateIn(
        TestScope(rule.dispatcher), started = SharingStarted.Eagerly, initialValue = emptyMap()
    )

    @Test
    fun `itemStore items updated should update items`() {
        val item = AddLodgingItem(
            "lodging_id",
            timestamp = mock(),
            checkOutDayOfMonth = "",
            checkOutDayOfWeek = "",
            minCheckOutTimeMillis = 0L,
        )
        itemFlow.value = mapOf("lodging_id" to item)
        assertThat(items.value["lodging_id"]).isEqualTo(item)
    }

    @Test
    fun `created data should be initialized empty`() {
        val data = subject.createData(mockTime())
        assertThat(data.name).isNull()
        assertThat(data.address).isNull()
        assertThat(data.city).isNull()
    }

    @Test
    fun `created data should be initialized with initial time as check-in`() {
        val initialTime = mockTime()
        val data = subject.createData(initialTime)
        assertThat(data.checkIn).isEqualTo(initialTime)
    }

    @Test
    fun `created data should be initialized with day after initial time as check-out`() {
        val expected = mockTime()
        val midnightTime = mockTime {
            on { plus(TimeUnit.DAYS.toMillis(1)) } doReturn expected
        }
        mockkStatic("com.combah.travel2.extensions.TimeKt")
        val initialTime = mockk<Time>()
        every { initialTime.toMidnight() } returns midnightTime
        val data = subject.createData(initialTime)
        assertThat(data.checkOut).isEqualTo(expected)
    }

    @Test
    fun `addItem should return itemStore item`() {
        val time = mock<Time>()
        val item = mock<AddLodgingItem>()
        itemStore.stub { on { addItem(time) } doReturn item }
        assertThat(subject.addItem(time)).isEqualTo(item)
    }

    @Test
    fun `remove should call itemStore remove`() {
        val item = mock<AddLodgingItem>()
        subject.remove(item)
        verify(itemStore).remove(item)
    }

    @Test
    fun `InputState updated should update items`() {
        val item = AddLodgingItem(
            "lodging_id",
            timestamp = mock(),
            checkOutDayOfMonth = "",
            checkOutDayOfWeek = "",
            minCheckOutTimeMillis = 0L,
        )
        itemFlow.value = mapOf("lodging_id" to item)
        val results = listOf<SimplePlace>(
            mock { on { name } doReturn "Hotel Novotel Paris Les Halles" },
            mock { on { name } doReturn "Romantik Istanbul Hotel" },
            mock { on { name } doReturn "Hotel Romantik Schwaizerhoff Grindewald" },
        )
        val expected = listOf(
            "Hotel Novotel Paris Les Halles",
            "Romantik Istanbul Hotel",
            "Hotel Romantik Schwaizerhoff Grindewald",
        )
        inputUseCaseStates.value = mapOf(
            "lodging_id" to InputState(
                lodgingSearchResults = results
            )
        )
        assertThat(items["lodging_id"]?.lodgingSearchResults).hasSameElementsAs(expected)
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
        autoCompleteState.value = mock {
            on { searchResults } doReturn listOf(
                mock(),
                expected,
                mock(),
            )
        }
        val originalData =
            PendingLodging(
                id = "lodging_id",
                checkIn = mock(),
                checkOut = mock(),
            )
        subject.lodgingSearchResultTapped("lodging_id", 1)
        val result = getUpdateResult(originalData)
        assertThat(result.name).isEqualTo("Hotel Novotel Paris Les Halles")
        assertThat(result.address).isEqualTo("Blvd Les Halles, 45")
        assertThat(result.city).isEqualTo(paris)
    }

    private fun getUpdateResult(originalData: PendingLodging): PendingLodging {
        return getUpdateResult(itemStore, originalData)
    }
}
