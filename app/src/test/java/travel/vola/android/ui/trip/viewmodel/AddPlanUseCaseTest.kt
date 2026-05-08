package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import travel.vola.android.extensions.get
import travel.vola.android.extensions.set
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.test.UnconfinedDispatcherTestRule
import travel.vola.android.ui.trip.state.AddFlexibleSectionItemState
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.AddPlanItemState
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState
import java.time.ZonedDateTime

class AddPlanUseCaseTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val testScope = TestScope(rule.dispatcher)

    private val addFlightItems = MutableStateFlow<Map<String, AddFlightItemState>>(emptyMap())
    private val addFlightUseCase: AddFlightUseCase = mock {
        on { items } doReturn addFlightItems
    }
    private val addLodgingItems =
        MutableStateFlow<Map<String, ManualAddLodgingItemState>>(emptyMap())
    private val addLodgingUseCase: AddLodgingUseCase = mock {
        on { items } doReturn addLodgingItems
    }
    private val flexibleSectionItems =
        MutableStateFlow<Map<String, AddFlexibleSectionItemState>>(emptyMap())
    private val flexibleSectionUseCase: FlexibleSectionUseCase = mock {
        on { items } doReturn flexibleSectionItems
    }
    private val subject = AddPlanUseCase(
        mock(), testScope, flexibleSectionUseCase = flexibleSectionUseCase, addFlightUseCase, addLodgingUseCase
    )

    @Test
    fun `added plan item should be initialized as Flight`() {
        val initialTime: ZonedDateTime = mock()
        subject.createAddPlanItem("item_id", initialTime)
        verify(addFlightUseCase).addItem(eq("item_id"), eq(initialTime), any())
    }

    @Test
    fun `added plan item with entity should be initialized as entity type`() {
        val flight = mock<Flight>()
        subject.createAddPlanItem("item_id", flight)
        verify(addFlightUseCase).addItem(eq("item_id"), eq(flight), any())
        val lodging = mock<Lodging>()
        subject.createAddPlanItem("item_id", lodging)
        verify(addLodgingUseCase).addItem(eq("item_id"), eq(lodging), any())
    }

    @Test
    fun `type selected should not change item if same type selected`() {
        val original = mock<AddFlightItemState> {
            on { id } doReturn "originalId"
        }
        addFlightItems["originalId"] = original
        subject.addPlanTypeChanged("originalId", AddPlanItemState.Type.Flight)
        verify(addFlightUseCase, times(0)).removeItem(any())
        verify(addLodgingUseCase, times(0)).addItem(any(), any<ZonedDateTime>(), any())
    }

    @Test
    fun `addFlightItem items changed should update existing item`() {
        val addPlanItemId = "originalItemId"
        val addPlanItem: AddFlightItemState = mock {
            on { id } doReturn addPlanItemId
        }
        addFlightItems[addPlanItemId] = addPlanItem
        assertThat(subject.items[addPlanItemId]).isEqualTo(addPlanItem)
        val newFlightItem: AddFlightItemState = mock {
            on { id } doReturn addPlanItemId
        }
        addFlightItems[addPlanItemId] = newFlightItem
        assertThat(subject.items[addPlanItemId]).isEqualTo(newFlightItem)
    }

    @Test
    fun `saveItem should return flight from use case`() {
        val original = mock<AddFlightItemState> {
            on { id } doReturn "originalId"
        }
        val expected: Flight = mock()
        addFlightItems["originalId"] = original
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
        addLodgingItems["originalId"] = original
        addLodgingUseCase.stub {
            on { createEntity(original) } doReturn expected
        }
        val entity = subject.saveItem("originalId")
        assertThat(entity).isEqualTo(expected)
    }
}
