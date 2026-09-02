package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.test.TestScope
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Lodging
import travel.vola.android.test.UnconfinedDispatcherTestRule
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState
import java.time.ZonedDateTime

class AddPlanUseCaseTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val testScope = TestScope(rule.dispatcher)

    private val addFlightUseCase: AddFlightUseCase = mock()
    private val addLodgingUseCase: AddLodgingUseCase = mock()
    private val addPlaceUseCase: AddPlaceUseCase = mock()
    private val addRestaurantUseCase: AddRestaurantUseCase = mock()
    private val flexibleSectionUseCase: FlexibleSectionUseCase = mock()

    private val subject = AddPlanUseCase(
        placeRepository = mock(),
        coroutineScope = testScope,
        flexibleSectionUseCase = flexibleSectionUseCase,
        addFlightUseCase = addFlightUseCase,
        addLodgingUseCase = addLodgingUseCase,
        addPlaceUseCase = addPlaceUseCase,
        addRestaurantUseCase = addRestaurantUseCase,
    )

    @Test
    fun `added plan item should be initialized as Flight`() {
        val initialTime: ZonedDateTime = mock()
        addFlightUseCase.stub {
            on {
                createItem(
                    eq("item_id"),
                    eq(initialTime),
                    any()
                )
            } doReturn mock<AddFlightItemState>()
        }
        subject.createAddPlanItem("item_id", initialTime)
        verify(addFlightUseCase).createItem(eq("item_id"), eq(initialTime), any())
    }

    @Test
    fun `added plan item with entity should be initialized as entity type`() {
        val flight = mock<Flight>()
        addFlightUseCase.stub {
            on { createItem(eq("item_id"), eq(flight), any()) } doReturn mock<AddFlightItemState>()
        }
        subject.createAddPlanItem("item_id", flight)
        verify(addFlightUseCase).createItem(eq("item_id"), eq(flight), any())

        val lodging = mock<Lodging>()
        addLodgingUseCase.stub {
            on {
                createItem(
                    eq("item_id"),
                    eq(lodging),
                    any()
                )
            } doReturn mock<ManualAddLodgingItemState>()
        }
        subject.createAddPlanItem("item_id", lodging)
        verify(addLodgingUseCase).createItem(eq("item_id"), eq(lodging), any())
    }

    @Test
    fun `createAddPlanItem should clear any previously pending item`() {
        val flight = mock<Flight>()
        addFlightUseCase.stub {
            on { createItem(any(), eq(flight), any()) } doReturn mock<AddFlightItemState>()
        }
        subject.createAddPlanItem("item_id", flight)
        assertThat(subject.state.value).isNotNull()

        val lodging = mock<Lodging>()
        val lodgingItem = mock<ManualAddLodgingItemState> { on { id } doReturn "lodging_item" }
        addLodgingUseCase.stub {
            on { createItem(any(), eq(lodging), any()) } doReturn lodgingItem
        }
        subject.createAddPlanItem("other_id", lodging)
        assertThat(subject.state.value?.id).isEqualTo("lodging_item")
    }

    @Test
    fun `updated flight item should route to addFlightUseCase and update items`() {
        val flight = mock<Flight>()
        val flightItem = mock<AddFlightItemState> { on { id } doReturn "flight_item" }
        addFlightUseCase.stub {
            on { createItem(any(), eq(flight), any()) } doReturn flightItem
        }
        subject.createAddPlanItem("item_id", flight)
        val next: AddFlightItemState = mock { on { id } doReturn "flight_item" }
        val corrected: AddFlightItemState = mock { on { id } doReturn "flight_item" }
        addFlightUseCase.stub {
            onBlocking { onUpdated(next) } doReturn corrected
        }
        subject.onUpdated(next)
        assertThat(subject.state.value).isEqualTo(corrected)
    }

    @Test
    fun `saveItem should return flight from use case`() {
        val expected: Flight = mock()
        val item: AddFlightItemState = mock { on { id } doReturn "item_id" }
        addFlightUseCase.stub {
            on { createItem(any(), any<ZonedDateTime>(), any()) } doReturn item
            on { createEntity(item) } doReturn expected
        }
        subject.createAddPlanItem("item_id", mock<ZonedDateTime>())
        val entity = subject.saveItem()
        assertThat(entity).isEqualTo(expected)
        assertThat(subject.state.value).isNull()
    }

    @Test
    fun `saveItem should return lodging from use case`() {
        val expected: Lodging = mock()
        val item: ManualAddLodgingItemState = mock { on { id } doReturn "item_id" }
        addLodgingUseCase.stub {
            on { createItem(any(), any<Lodging>(), any()) } doReturn item
            on { createEntity(item) } doReturn expected
        }
        subject.createAddPlanItem("item_id", mock<Lodging>())
        val entity = subject.saveItem()
        assertThat(entity).isEqualTo(expected)
    }
}
