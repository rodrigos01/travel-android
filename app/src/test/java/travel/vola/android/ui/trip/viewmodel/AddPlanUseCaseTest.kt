package travel.vola.android.ui.trip.viewmodel

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
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Time
import travel.vola.android.test.Assertions.assertType
import travel.vola.android.test.UnconfinedDispatcherTestRule
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.AddPlanItemState
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
class AddPlanUseCaseTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val testScope = TestScope(rule.dispatcher)

    private val addFlightItems =
        MutableStateFlow<Map<String, AddFlightItemState>>(emptyMap())
    private val addFlightUseCase: AddFlightUseCase = mock {
        on { items } doReturn addFlightItems
    }
    private val addLodgingItems =
        MutableStateFlow<Map<String, ManualAddLodgingItemState>>(emptyMap())
    private val addLodgingUseCase: AddLodgingUseCase = mock {
        on { items } doReturn addLodgingItems
    }
    private val subject = AddPlanUseCase(addFlightUseCase, addLodgingUseCase)

    @Test
    fun `added plan item should be initialized as Flight`() {
        val original = mock<AddFlightItemState>()
        addFlightUseCase.stub {
            on { addItem(any<Time>(), any()) } doReturn original
        }
        val addedItem = subject.createAddPlanItem(mock<Time>())
        assertType<AddFlightItemState>(addedItem)
    }

    @Test
    fun `added plan item with entity should be initialized as entity type`() {
        val flight = mock<Flight>()
        subject.createAddPlanItem(flight)
        verify(addFlightUseCase.addItem(eq(flight), any()))
        val lodging = mock<Lodging>()
        subject.createAddPlanItem(lodging)
        verify(addLodgingUseCase).addItem(eq(lodging), any())
    }

    @Test
    fun `type selected should change item`() {
        val expected: ManualAddLodgingItemState = mock()
        val initialTime: Time = mock()
        val original = mock<AddFlightItemState> {
            on { id } doReturn "originalId"
            on { timestamp } doReturn initialTime
        }
        addFlightUseCase.stub {
            on { addItem(eq(initialTime), any()) } doReturn original
        }
        addLodgingUseCase.stub { on { addItem(eq(initialTime), any()) } doReturn expected }
        val newItem = subject.addPlanTypeChanged("originalId", AddPlanItemState.Type.Lodging)
        verify(addFlightUseCase).removeItem(original)
        verify(addLodgingUseCase).addItem(eq(initialTime), any())
        assertThat(newItem).isEqualTo(expected)
    }

    @Test
    fun `type selected should not change item if same type selected`() {
        val initialTime: Time = mock()
        val original = mock<AddFlightItemState> {
            on { id } doReturn "originalId"
        }
        addFlightUseCase.stub {
            on { addItem(eq(initialTime), any()) } doReturn original
        }
        subject.addPlanTypeChanged("originalId", AddPlanItemState.Type.Flight)
        verifyNoInteractions(addFlightUseCase)
        verifyNoInteractions(addLodgingItems)
    }

    @Test
    fun `addFlightItem items changed should update existing item`() = testScope.runTest {
        val addPlanItemId = "originalItemId"
        val addPlanItem: AddFlightItemState = mock {
            on { id } doReturn addPlanItemId
        }
        val items = subject.items.stateIn(this)
        addFlightItems.value = mapOf(addPlanItemId to addPlanItem)
        assertThat(items.value[addPlanItemId]).isEqualTo(addPlanItem)
        val newFlightItem: AddFlightItemState = mock {
            on { id } doReturn addPlanItemId
        }
        addFlightItems.value = mapOf(addPlanItemId to newFlightItem)
        assertThat(items.value[addPlanItemId]).isEqualTo(newFlightItem)
        testScope.coroutineContext.cancelChildren()
    }

    @Test
    fun `saveItem should return flight from use case`() {
        val original = mock<AddFlightItemState> {
            on { id } doReturn "originalId"
        }
        val expected: Flight = mock()
        addFlightUseCase.stub {
            on { createEntity(original) } doReturn expected
        }
        val entity = subject.saveItem("originalId")
        assertThat(entity).isEqualTo(expected)
    }

    @Test
    fun `saveItem should return lodging from use case`() {
        val original = mock<ManualAddLodgingItemState> {
            on { id } doReturn "originalId"
        }
        val expected: Lodging = mock()
        addLodgingUseCase.stub {
            on { createEntity(original) } doReturn expected
        }
        val entity = subject.saveItem("originalId")
        assertThat(entity).isEqualTo(expected)
    }
}
