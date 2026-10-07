package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.test.UnconfinedDispatcherTestRule
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.AddPlanItemState
import travel.vola.android.ui.trip.state.TripItemState
import travel.vola.android.ui.trip.state.TripItemState.DateRangeItemState
import travel.vola.android.ui.trip.state.TripItemState.EmptyDateItemState
import travel.vola.android.ui.trip.state.TripItemState.HotelCheckInItemState
import travel.vola.android.ui.trip.state.TripItemState.HotelCheckOutItemState
import travel.vola.android.ui.trip.state.TripItemState.PlaceItemState

class TripViewModelTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val tripFlow = MutableStateFlow<Trip>(mock())
    private val repository = mock<TripRepository> {
        on { findTripById("tripId") } doReturn tripFlow
    }

    private val addPlanItems = MutableStateFlow<Map<String, AddPlanItemState>>(mapOf())
    private val addPlanUseCase: AddPlanUseCase = mock {
        on { items } doReturn addPlanItems
    }
    private val suggestionsUseCase: SuggestionsUseCase = mock {
        on { state } doReturn MutableStateFlow(SuggestionsUseCase.DailyItineraryState(emptyList(), emptyList()))
    }
    private val flexibleSectionUseCase: FlexibleSectionUseCase = mock {
        on { flexibleSectionItems } doReturn MutableStateFlow(emptyList())
    }

    private val subject = TripViewModel(
        repository = repository,
        placeRepository = mock(),
        tripId = "tripId",
        navController = mock(),
        useCaseScope = TestScope(rule.dispatcher),
        suggestionsUseCase = suggestionsUseCase,
        flexibleSectionUseCase = flexibleSectionUseCase,
        addPlanUseCase = addPlanUseCase,
    )

    private val newYork = place("New York")
    private val porto = place("Porto")
    private val lisbon = place("Lisbon")

    private val outbound = flight("out", newYork, "2024-05-10T22:05:00-04:00", porto, "2024-05-11T10:00:00+01:00")
    private val portoHotel = lodging("hotel", porto, "2024-05-11T13:00:00+01:00", "2024-05-19T11:00:00+01:00")

    /** Four nights in Porto, then eight in Lisbon. */
    private fun twoCities(): Trip {
        val lisbonHotel = lodging("lisbon-hotel", lisbon, "2024-05-19T13:00:00+01:00", "2024-05-21T11:00:00+01:00")
        return trip {
            transit(newYork, "2024-05-10") { day("2024-05-10") { departure(outbound) } }
            leg(porto, "2024-05-11", "2024-05-19") {
                day("2024-05-11") {
                    arrival(outbound)
                    checkIn(portoHotel)
                }
                emptyRange("2024-05-12", "2024-05-18")
                day("2024-05-19") { checkOut(portoHotel) }
            }
            leg(lisbon, "2024-05-19", "2024-05-21") {
                day("2024-05-19") { checkIn(lisbonHotel) }
                emptyDay("2024-05-20")
                day("2024-05-21") { checkOut(lisbonHotel) }
            }
        }
    }

    private fun stayInPorto() = trip {
        leg(porto, "2024-05-11", "2024-05-19") {
            day("2024-05-11") { checkIn(portoHotel) }
            emptyRange("2024-05-12", "2024-05-18")
            day("2024-05-19") { checkOut(portoHotel) }
        }
    }

    // ---- State ----

    @Test
    fun `the screen shows the trip's itinerary`() {
        tripFlow.value = twoCities()

        val items = subject.viewState.value.items
        assertThat(items.filterIsInstance<PlaceItemState>().map { it.placeName }).containsExactly("Porto", "Lisbon")
        assertThat(items.filterIsInstance<DateRangeItemState>()).hasSize(1)
        assertThat(items.filterIsInstance<EmptyDateItemState>()).hasSize(1)
    }

    @Test
    fun `each place is found at its header`() {
        tripFlow.value = twoCities()

        val places = subject.viewState.value.places
        val headers = subject.viewState.value.items
        assertThat(places.single { it.place.name == "Porto" }.listIndex)
            .isEqualTo(headers.indexOfFirst { it is PlaceItemState && it.placeName == "Porto" })
        assertThat(places.single { it.place.name == "Lisbon" }.listIndex)
            .isEqualTo(headers.indexOfFirst { it is PlaceItemState && it.placeName == "Lisbon" })
    }

    // ---- Adding ----

    @Test
    fun `add plan tapped on last item in place should add add plan item with last item time and start date selection disabled`() {
        tripFlow.value = twoCities()
        val checkOutItem =
            subject.viewState.value.items.first { it is HotelCheckOutItemState && it.hotelName == "Hotel Porto" } as TripItemState.EventItemState

        subject.addButtonTapped(checkOutItem.id)

        verify(addPlanUseCase).createAddPlanItem(
            any(),
            eq(zonedDateTime("2024-05-19T11:00:00+01:00")),
            dateSelectionEnabled = eq(false),
            type = any<AddPlanItemState.Type>(),
            place = eq(porto),
        )
    }

    @Test
    fun `a plan added from a row is in the place of that row`() {
        tripFlow.value = twoCities()
        val lisbonCheckIn = subject.viewState.value.items.filterIsInstance<HotelCheckInItemState>()
            .first { it.hotelName == "Hotel Lisbon" }

        subject.addButtonTapped(lisbonCheckIn.id)

        // The 19th is the day Porto ends and Lisbon begins.
        verify(addPlanUseCase).createAddPlanItem(
            any(),
            any(),
            dateSelectionEnabled = any(),
            type = any<AddPlanItemState.Type>(),
            place = eq(lisbon),
        )
    }

    @Test
    fun `add Plan tapped on date range should add add plan item below tapped item`() {
        tripFlow.value = stayInPorto()
        val addPlanItemId = "originalItemId"
        val expected: AddFlightItemState = mock {
            on { id } doReturn addPlanItemId
        }
        mockAddPlanItem(expected)
        val originalItem =
            subject.viewState.value.items.filterIsInstance<DateRangeItemState>().first()
        val originalItemIndex = subject.viewState.value.items.indexOf(originalItem)
        subject.addButtonTapped(originalItem.id)
        val addedItem = subject.viewState.value.items[originalItemIndex]
        assertThat(addedItem).isEqualTo(expected)
    }

    @Test
    fun `add Plan tapped on date range should add add plan item with start date and start date selection enabled`() {
        tripFlow.value = stayInPorto()
        val originalItem =
            subject.viewState.value.items.filterIsInstance<DateRangeItemState>().first()
        subject.addButtonTapped(originalItem.id)
        verify(addPlanUseCase).createAddPlanItem(
            id = eq(originalItem.id),
            time = any(),
            dateSelectionEnabled = eq(true),
            type = any<AddPlanItemState.Type>(),
            place = eq(porto),
        )
    }

    @Test
    fun `empty date row tapped on date range should replace tapped item with add plan item`() {
        tripFlow.value = twoCities()
        val addPlanItemId = "originalItemId"
        val expected: AddFlightItemState = mock {
            on { id } doReturn addPlanItemId
        }
        mockAddPlanItem(expected)
        val originalItem =
            subject.viewState.value.items.filterIsInstance<EmptyDateItemState>().first()
        val originalItemIndex = subject.viewState.value.items.indexOf(originalItem)
        subject.emptyDateRowTapped(originalItem.id)
        val addedItem = subject.viewState.value.items[originalItemIndex]
        assertThat(addedItem).isEqualTo(expected)
    }

    @Test
    fun `addPlanUseCase items changed invalid item should ignore`() {
        tripFlow.value = stayInPorto()
        val newItemId = "originalItemId"
        val newAddPlanItem = mock<AddFlightItemState> {
            on { id } doReturn newItemId
        }
        addPlanItems.value = mapOf(newItemId to newAddPlanItem)
        assertThat(subject.viewState.value.items).doesNotContain(newAddPlanItem)
    }

    @Test
    fun `cancel should remove item from addPlanUseCase`() {
        tripFlow.value = stayInPorto()
        val addPlanItemId = "originalItemId"
        val expected: AddFlightItemState = mock {
            on { id } doReturn addPlanItemId
        }
        mockAddPlanItem(expected)
        val originalItem =
            subject.viewState.value.items.filterIsInstance<DateRangeItemState>().first()
        subject.addButtonTapped(originalItem.id)
        subject.cancelEdit(addPlanItemId)
        verify(addPlanUseCase).removeItem(addPlanItemId)
    }

    @Test
    fun `cancel should remove item from use case`() {
        tripFlow.value = stayInPorto()
        val addPlanItemId = "originalItemId"
        val originalItem =
            subject.viewState.value.items.filterIsInstance<DateRangeItemState>().first()
        subject.addButtonTapped(originalItem.id)
        subject.cancelEdit(addPlanItemId)
        verify(addPlanUseCase).removeItem(addPlanItemId)
    }

    // ---- Editing, through the reference the itinerary holds ----

    @Test
    fun `edit tapped on a row edits the entity it refers to`() {
        tripFlow.value = twoCities()
        val checkIn = subject.viewState.value.items.filterIsInstance<HotelCheckInItemState>()
            .first { it.hotelName == "Hotel Porto" }

        subject.editTapped(checkIn.id)

        verify(addPlanUseCase).createAddPlanItem(checkIn.id, portoHotel)
    }

    @Test
    fun `edit tapped on a place header started by a timed place edits that place`() {
        val stay = timedPlace("stay", porto, porto, "2024-05-11T00:00:00+01:00", "2024-05-19T00:00:00+01:00")
        tripFlow.value = trip { leg(porto, "2024-05-11", "2024-05-19", startedBy = stay) { } }
        val header = subject.viewState.value.items.filterIsInstance<PlaceItemState>().single()

        subject.editTapped(header.id)

        verify(addPlanUseCase).createAddPlanItem(header.id, stay)
    }

    @Test
    fun `edit tapped on a place header no timed place started makes one for the place`() {
        tripFlow.value = twoCities()
        val header = subject.viewState.value.items.filterIsInstance<PlaceItemState>().first { it.placeName == "Porto" }

        subject.editTapped(header.id)

        val captor = argumentCaptor<TimedPlace>()
        verify(addPlanUseCase).createAddPlanItem(eq(header.id), captor.capture(), eq(false))
        assertThat(captor.firstValue.place).isEqualTo(porto)
        assertThat(captor.firstValue.city).isEqualTo(porto)
        assertThat(captor.firstValue.hasStartTime).isFalse()
    }

    @Test
    fun `deleting an edited row deletes the entity it refers to`() {
        tripFlow.value = twoCities()
        val checkIn = subject.viewState.value.items.filterIsInstance<HotelCheckInItemState>()
            .first { it.hotelName == "Hotel Porto" }
        val editing: AddFlightItemState = mock { on { id } doReturn checkIn.id }
        addPlanItems.value = mapOf(checkIn.id to editing)

        subject.delete(AddPlanItemState.Type.Lodging, checkIn.id)

        verifyBlocking(repository) { deleteLodging("tripId", "hotel") }
    }

    private fun mockAddPlanItem(addPlanItem: AddPlanItemState) {
        addPlanUseCase.stub {
            on {
                createAddPlanItem(
                    anyOrNull(),
                    any(),
                    any(),
                    any<AddPlanItemState.Type>(),
                    anyOrNull(),
                )
            } doAnswer {
                val id = it.getArgument<String?>(0) ?: "adding"
                addPlanItems.value = mapOf(id to addPlanItem)
            }
        }
    }
}
