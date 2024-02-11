package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Time
import com.combah.travel2.test.UnconfinedDispatcherTestRule
import com.combah.travel2.test.assertType
import com.combah.travel2.ui.trip.viewmodel.AddPlanUseCase.AddPlanItem
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
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
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
class AddPlanUseCaseTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val testScope = TestScope(rule.dispatcher)

    private val addFlightItems =
        MutableStateFlow<Map<String, AddFlightUseCase.AddFlightItem>>(emptyMap())
    private val addFlightUseCase: AddFlightUseCase = mock {
        on { items } doReturn addFlightItems
    }
    private val addLodgingItems =
        MutableStateFlow<Map<String, AddLodgingUseCase.AddLodgingItem>>(emptyMap())
    private val addLodgingUseCase: AddLodgingUseCase = mock {
        on { items } doReturn addLodgingItems
    }
    private val subject = AddPlanUseCase(addFlightUseCase, addLodgingUseCase)

    @Test
    fun `added plan item should be initialized as Flight`() {
        val original = mock<AddFlightUseCase.AddFlightItem>()
        addFlightUseCase.stub {
            on { createItem(any()) } doReturn original
        }
        val addedItem = subject.createAddPlanItem(mock())
        assertType<AddFlightUseCase.AddFlightItem>(addedItem)
    }

    @Test
    fun `Add Plan Items should have all types available`() {
        val original = AddFlightUseCase.AddFlightItem(
            id = "originalItem",
            timestamp = mock(),
            minArrivalTimeMillis = 0L,
            arrivalDayOfMonth = "15",
            arrivalDayOfWeek = "Wed",
        )
        assertThat(original.types).containsExactly(
            AddPlanItem.Type.Flight,
            AddPlanItem.Type.Lodging,
        )
    }

    @Test
    fun `added flight item should be initialized with initial time as departure`() {
        val expected: AddFlightUseCase.AddFlightItem = mock()
        val initialTime: Time = mock()
        addFlightUseCase.stub { on { createItem(initialTime) } doReturn expected }
        val addedItem = subject.createAddPlanItem(initialTime)
        assertThat(addedItem).isEqualTo(expected)
    }

    @Test
    fun `type selected should change item`() {
        val expected: AddLodgingUseCase.AddLodgingItem = mock()
        val initialTime: Time = mock()
        val original = mock<AddFlightUseCase.AddFlightItem> {
            on { id } doReturn "originalId"
            on { timestamp } doReturn initialTime
        }
        addFlightUseCase.stub {
            on { createItem(initialTime) } doReturn original
        }
        addLodgingUseCase.stub { on { createItem(initialTime) } doReturn expected }
        val item = subject.createAddPlanItem(initialTime)
        val newItem = subject.typeChanged(item, AddPlanItem.Type.Lodging)
        assertThat(newItem).isEqualTo(expected)
    }

    @Test
    fun `type selected should keep original item's time`() {
        val initialTime: Time = mock()
        val expected: AddLodgingUseCase.AddLodgingItem = mock {
            on { timestamp } doReturn initialTime
        }
        val original = mock<AddFlightUseCase.AddFlightItem> {
            on { id } doReturn "originalId"
            on { timestamp } doReturn initialTime
        }
        addFlightUseCase.stub {
            on { createItem(initialTime) } doReturn original
        }
        addLodgingUseCase.stub { on { createItem(initialTime) } doReturn expected }
        val addedItem = subject.createAddPlanItem(initialTime)
        val newItem = subject.typeChanged(addedItem, AddPlanItem.Type.Lodging)
        assertType<AddLodgingUseCase.AddLodgingItem>(newItem)
        assertThat(newItem).isEqualTo(expected)
    }

    @Test
    fun `type selected should not change item change item if same item selected`() {
        val expected: AddLodgingUseCase.AddLodgingItem = mock()
        val initialTime: Time = mock()
        val original = mock<AddFlightUseCase.AddFlightItem> {
            on { id } doReturn "originalId"
        }
        addFlightUseCase.stub {
            on { createItem(initialTime) } doReturn original
        }
        addLodgingUseCase.stub { on { createItem(initialTime) } doReturn expected }
        val item = subject.createAddPlanItem(initialTime)
        val newItem = subject.typeChanged(item, AddPlanItem.Type.Flight)
        assertThat(newItem).isEqualTo(item)
    }

    @Test
    fun `addFlightItem items changed should update existing item`() = testScope.runTest {
        val addPlanItemId = "originalItemId"
        val addPlanItem: AddFlightUseCase.AddFlightItem = mock {
            on { id } doReturn addPlanItemId
        }
        val items = subject.items.stateIn(this)
        addFlightItems.value = mapOf(addPlanItemId to addPlanItem)
        assertThat(items.value[addPlanItemId]).isEqualTo(addPlanItem)
        val newFlightItem: AddFlightUseCase.AddFlightItem = mock {
            on { id } doReturn addPlanItemId
        }
        addFlightItems.value = mapOf(addPlanItemId to newFlightItem)
        assertThat(items.value[addPlanItemId]).isEqualTo(newFlightItem)
        testScope.coroutineContext.cancelChildren()
    }

    @Test
    fun `saveItem should return flight from use case`() {
        val original = mock<AddFlightUseCase.AddFlightItem> {
            on { id } doReturn "originalId"
        }
        val expected: Flight = mock()
        addFlightUseCase.stub {
            on { save(original) } doReturn expected
        }
        val entity = subject.saveItem(original)
        assertThat(entity).isEqualTo(expected)
    }

    @Test
    fun `saveItem should return lodging from use case`() {
        val original = mock<AddLodgingUseCase.AddLodgingItem> {
            on { id } doReturn "originalId"
        }
        val expected: Lodging = mock()
        addLodgingUseCase.stub {
            on { save(original) } doReturn expected
        }
        val entity = subject.saveItem(original)
        assertThat(entity).isEqualTo(expected)
    }
}