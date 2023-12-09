@file:OptIn(ExperimentalContracts::class)

package com.combah.travel2.ui

import com.combah.travel2.model.firebase.toAppDataModel
import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.model.repository.mock.MockData.jfk
import com.combah.travel2.model.repository.mock.MockData.lis
import com.combah.travel2.model.repository.mock.MockData.lisbon
import com.combah.travel2.model.repository.mock.MockData.lisbonAirBnB
import com.combah.travel2.model.repository.mock.MockData.milan
import com.combah.travel2.model.repository.mock.MockData.milanHotel
import com.combah.travel2.model.repository.mock.MockData.nap
import com.combah.travel2.model.repository.mock.MockData.nice
import com.combah.travel2.model.repository.mock.MockData.niceHotel
import com.combah.travel2.model.repository.mock.MockData.nyc
import com.combah.travel2.model.repository.mock.MockData.opo
import com.combah.travel2.model.repository.mock.MockData.ory
import com.combah.travel2.model.repository.mock.MockData.paris
import com.combah.travel2.model.repository.mock.MockData.parisAirBnB
import com.combah.travel2.model.repository.mock.MockData.porto
import com.combah.travel2.model.repository.mock.MockData.portoHotel
import com.combah.travel2.model.repository.mock.MockData.sorento
import com.combah.travel2.model.repository.mock.MockData.sorentoHotel
import com.combah.travel2.model.repository.mock.MockData.trip
import com.combah.travel2.test.UnconfinedDispatcherTestRule
import com.combah.travel2.ui.trip.TripViewModel
import com.combah.travel2.ui.trip.TripViewModel.AddPlanType
import com.combah.travel2.ui.trip.TripViewModel.TripItem.AddFlightItem
import com.combah.travel2.ui.trip.TripViewModel.TripItem.DateRangeItem
import com.combah.travel2.ui.trip.TripViewModel.TripItem.FlightArrivalItem
import com.combah.travel2.ui.trip.TripViewModel.TripItem.FlightDepartureItem
import com.combah.travel2.ui.trip.TripViewModel.TripItem.HotelCheckInItem
import com.combah.travel2.ui.trip.TripViewModel.TripItem.HotelCheckOutItem
import com.combah.travel2.ui.trip.TripViewModel.TripItem.MonthItem
import com.combah.travel2.ui.trip.TripViewModel.TripItem.PlaceItem
import kotlinx.coroutines.flow.flowOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import kotlin.contracts.ExperimentalContracts

@OptIn(ExperimentalContracts::class)
class TripViewModelTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val repository = mock<TripRepository> {
        on { findTripById(any()) } doAnswer {
            flowOf(trip.toAppDataModel())
        }
    }
    private val addPlanUseCase: AddPlanUseCase = mock()
    private val subject =
        TripViewModel(repository, "minhaTrip", addPlanUseCase, TimeConverter(), TimeFormatter())

    /*
    Expected List:
    *  May, 2024
    *  May 10 - Flight to Lisbon
    *  Lison
    *  May 11 - Arrival in Lisbon
    *  May 12 - May 18
    *  Porto
    *  May 19 - Check-in Porto
    *  May 20
    *  May 21 - Check-out Porto
    *           Flight to Paris
    *  Paris
    *  May 21 - Arrival in Paris
    *  May 22 - May 28
    *  Nice
    *  May 29 - Check-in Nice
    *  May 30 - May - 31
    *  June, 2024
    *  Jun  1
    *  Jun  2 - Check-out Nice
    *  Milan
    *  Jun  2 - Check-in Milan
    *  Jun  3
    *  Jun  4 - Check-out Milan
    *  Jun  5 - Jun 11
    *  Sorento
    *  Jun 12 - Check-in Sorento
    *  Jun 13
    *  Jun 14 - Check-out Sorento
    *           Flight to Lisbon
    *           Arrival in Lisbon
    *           Flight to NYC
    *           Arrival in NYC
     */

    @Test
    fun `events should have one departure event per flight`() {
        subject.viewState.value.items.find { it is FlightDepartureItem && it.destination == lisbon.name }
            .let {
                val item = it as FlightDepartureItem
                assertThat(item.airport).isEqualTo(jfk.name)
                assertThat(item.dayOfMonth).isEqualTo("10")
                assertThat(item.time).isEqualTo("10:05 PM")
            }
        subject.viewState.value.items.find { it is FlightDepartureItem && it.destination == paris.name }
            .let {
                val item = it as FlightDepartureItem
                assertThat(item.airport).isEqualTo(opo.name)
                assertThat(item.dayOfMonth).isEqualTo("21")
                assertThat(item.time).isEqualTo("4:50 PM")
            }
        subject.viewState.value.items.find { it is FlightDepartureItem && it.airport == nap.name && it.destination == lisbon.name }
            .let {
                val item = it as FlightDepartureItem
                assertThat(item.airport).isEqualTo(nap.name)
                assertThat(item.dayOfMonth).isEqualTo("14")
                assertThat(item.time).isEqualTo("12:30 PM")
            }
        subject.viewState.value.items.find { it is FlightDepartureItem && it.destination == nyc.name }
            .let {
                val item = it as FlightDepartureItem
                assertThat(item.airport).isEqualTo(lis.name)
                assertThat(item.dayOfMonth).isEqualTo("14")
                assertThat(item.time).isEqualTo("5:05 PM")
            }
    }

    @Test
    fun `events should have one arrival event per flight`() {
        subject.viewState.value.items.find { it is FlightArrivalItem && it.airport == lis.name }
            .let {
                val item = it as FlightArrivalItem
                assertThat(item.dayOfMonth).isEqualTo("11")
                assertThat(item.time).isEqualTo("10:00 AM")
            }
        subject.viewState.value.items.find { it is FlightArrivalItem && it.airport == ory.name }
            .let {
                val item = it as FlightArrivalItem
                assertThat(item.dayOfMonth).isEqualTo("21")
                assertThat(item.time).isEqualTo("8:15 PM")
            }
        subject.viewState.value.items.filter { it is FlightArrivalItem && it.airport == lis.name }[1]
            .let {
                val item = it as FlightArrivalItem
                assertThat(item.dayOfMonth).isEqualTo("14")
                assertThat(item.time).isEqualTo("2:50 PM")
            }
        subject.viewState.value.items.find { it is FlightArrivalItem && it.airport == jfk.name }
            .let {
                val item = it as FlightArrivalItem
                assertThat(item.dayOfMonth).isEqualTo("14")
                assertThat(item.time).isEqualTo("8:05 PM")
            }
    }

    @Test
    fun `events should have one check-in event per hotel`() {
        subject.viewState.value.items.find { it is HotelCheckInItem && it.hotelName == portoHotel.name }
            .let {
                val item = it as HotelCheckInItem
                assertThat(item.hotelAddress).isEqualTo(portoHotel.address)
                assertThat(item.dayOfMonth).isEqualTo("19")
                assertThat(item.time).isEqualTo("1:00 PM")
            }
        subject.viewState.value.items.find { it is HotelCheckInItem && it.hotelName == niceHotel.name }
            .let {
                val item = it as HotelCheckInItem
                assertThat(item.hotelAddress).isEqualTo(niceHotel.address)
                assertThat(item.dayOfMonth).isEqualTo("29")
                assertThat(item.time).isEqualTo("1:00 PM")
            }
        subject.viewState.value.items.find { it is HotelCheckInItem && it.hotelName == milanHotel.name }
            .let {
                val item = it as HotelCheckInItem
                assertThat(item.hotelAddress).isEqualTo(milanHotel.address)
                assertThat(item.dayOfMonth).isEqualTo("2")
                assertThat(item.time).isEqualTo("1:00 PM")
            }
        subject.viewState.value.items.find { it is HotelCheckInItem && it.hotelName == sorentoHotel.name }
            .let {
                val item = it as HotelCheckInItem
                assertThat(item.hotelAddress).isEqualTo(sorentoHotel.address)
                assertThat(item.dayOfMonth).isEqualTo("12")
                assertThat(item.time).isEqualTo("1:00 PM")
            }
    }

    @Test
    fun `events should have one checkout event per hotel`() {
        subject.viewState.value.items.find { it is HotelCheckOutItem && it.hotelName == portoHotel.name }
            .let {
                val item = it as HotelCheckOutItem
                assertThat(item.dayOfMonth).isEqualTo("21")
                assertThat(item.time).isEqualTo("11:00 AM")
            }
        subject.viewState.value.items.find { it is HotelCheckOutItem && it.hotelName == niceHotel.name }
            .let {
                val item = it as HotelCheckOutItem
                assertThat(item.dayOfMonth).isEqualTo("2")
                assertThat(item.time).isEqualTo("11:00 AM")
            }
        subject.viewState.value.items.find { it is HotelCheckOutItem && it.hotelName == milanHotel.name }
            .let {
                val item = it as HotelCheckOutItem
                assertThat(item.dayOfMonth).isEqualTo("4")
                assertThat(item.time).isEqualTo("11:00 AM")
            }
        subject.viewState.value.items.find { it is HotelCheckOutItem && it.hotelName == sorentoHotel.name }
            .let {
                val item = it as HotelCheckOutItem
                assertThat(item.dayOfMonth).isEqualTo("14")
                assertThat(item.time).isEqualTo("11:00 AM")
            }
    }

    @Test
    fun `events should have one place event for each place`() {
        subject.viewState.value.items.find { it is PlaceItem && it.placeName == lisbon.name }.let {
            val item = it as PlaceItem
            assertThat(item.dateStart).isEqualTo("May 11")
            assertThat(item.dateEnd).isEqualTo("May 19")
        }
        subject.viewState.value.items.find { it is PlaceItem && it.placeName == porto.name }.let {
            val item = it as PlaceItem
            assertThat(item.dateStart).isEqualTo("May 19")
            assertThat(item.dateEnd).isEqualTo("May 21")
        }
        subject.viewState.value.items.find { it is PlaceItem && it.placeName == paris.name }.let {
            val item = it as PlaceItem
            assertThat(item.dateStart).isEqualTo("May 21")
            assertThat(item.dateEnd).isEqualTo("May 29")
        }
        subject.viewState.value.items.find { it is PlaceItem && it.placeName == nice.name }.let {
            val item = it as PlaceItem
            assertThat(item.dateStart).isEqualTo("May 29")
            assertThat(item.dateEnd).isEqualTo("Jun 2")
        }
        subject.viewState.value.items.find { it is PlaceItem && it.placeName == milan.name }.let {
            val item = it as PlaceItem
            assertThat(item.dateStart).isEqualTo("Jun 2")
            assertThat(item.dateEnd).isEqualTo("Jun 4")
        }
        subject.viewState.value.items.find { it is PlaceItem && it.placeName == sorento.name }.let {
            val item = it as PlaceItem
            assertThat(item.dateStart).isEqualTo("Jun 12")
            assertThat(item.dateEnd).isEqualTo("Jun 14")
        }
    }

    @Test
    fun `events should not have place item for origin`() {
        assertThat(subject.viewState.value.items).noneSatisfy {
            assertType<PlaceItem>(it)
            assertThat(it.placeName).isEqualTo("New York")
        }
    }

    @Test
    fun `events should have one month event for each month`() {
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<MonthItem>(item)
            assertThat(item.month).isEqualTo("May")
            assertThat(item.year).isEqualTo("2024")
        }
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<MonthItem>(item)
            assertThat(item.month).isEqualTo("June")
            assertThat(item.year).isEqualTo("2024")
        }
    }

    @Test
    fun `first events should be first of each day`() {
        subject.viewState.value.items.find { it is FlightDepartureItem && it.destination == lisbon.name }
            .let {
                val item = it as TripViewModel.TripItem.EventItem
                assertThat(item.showDate).isTrue
            }
        subject.viewState.value.items.find { it is FlightArrivalItem && it.airport == lis.name }
            .let {
                val item = it as TripViewModel.TripItem.EventItem
                assertThat(item.showDate).isTrue
            }
        subject.viewState.value.items.find { it is HotelCheckOutItem && it.hotelName == lisbonAirBnB.address }
            .let {
                val item = it as TripViewModel.TripItem.EventItem
                assertThat(item.showDate).isTrue
            }
        subject.viewState.value.items.find { it is HotelCheckOutItem && it.hotelName == portoHotel.name }
            .let {
                val item = it as TripViewModel.TripItem.EventItem
                assertThat(item.showDate).isTrue
            }
        subject.viewState.value.items.find { it is HotelCheckOutItem && it.hotelName == parisAirBnB.address }
            .let {
                val item = it as TripViewModel.TripItem.EventItem
                assertThat(item.showDate).isTrue
            }
        subject.viewState.value.items.find { it is HotelCheckOutItem && it.hotelName == niceHotel.name }
            .let {
                val item = it as TripViewModel.TripItem.EventItem
                assertThat(item.showDate).isTrue
            }
        subject.viewState.value.items.find { it is HotelCheckOutItem && it.hotelName == milanHotel.name }
            .let {
                val item = it as TripViewModel.TripItem.EventItem
                assertThat(item.showDate).isTrue
            }
        subject.viewState.value.items.find { it is HotelCheckInItem && it.hotelName == sorentoHotel.name }
            .let {
                val item = it as TripViewModel.TripItem.EventItem
                assertThat(item.showDate).isTrue
            }
        subject.viewState.value.items.find { it is HotelCheckOutItem && it.hotelName == sorentoHotel.name }
            .let {
                val item = it as TripViewModel.TripItem.EventItem
                assertThat(item.showDate).isTrue
            }
    }

    @Test
    fun `events should have empty date range for all dates without events`() {
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<DateRangeItem>(item)
            assertThat(item.dayOfMonthStart).isEqualTo("12")
            assertThat(item.dayOfMonthEnd).isEqualTo("18")
        }
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<DateRangeItem>(item)
            assertThat(item.dayOfMonthStart).isEqualTo("22")
            assertThat(item.dayOfMonthEnd).isEqualTo("28")
        }
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<DateRangeItem>(item)
            assertThat(item.dayOfMonthStart).isEqualTo("30")
            assertThat(item.dayOfMonthEnd).isEqualTo("1")
        }
    }

    @Test
    fun `add Plan tapped should add add plan item below tapped item`() {
        val expected: AddPlanItem = mock()
        addPlanUseCase.stub {
            on { createAddPlanItem(any()) } doReturn expected
        }
        val eventItem =
            subject.viewState.value.items.find { it is HotelCheckOutItem && it.hotelName == milanHotel.name } as HotelCheckOutItem
        subject.addButtonTapped(eventItem.id)
        val addedItem =
            subject.viewState.value.items.nextAfter(eventItem)
        assertThat(addedItem).isEqualTo(expected)
    }

    @Test
    fun `type selected should change item`() {
        val addPlanItem: AddPlanItem = mock {
            on { id } doReturn "originalItemId"
        }
        val expected: AddPlanItem = mock()
        addPlanUseCase.stub {
            on { createAddPlanItem(any()) } doReturn addPlanItem
            on { typeChanged(eq(addPlanItem), any()) } doReturn expected
        }
        val eventItem =
            subject.viewState.value.items.find { it is HotelCheckOutItem && it.hotelName == milanHotel.name } as HotelCheckOutItem
        subject.addButtonTapped(eventItem.id)
        val newItemIndex = subject.viewState.value.items.indexOf(eventItem) + 1
        subject.addPlanTypeChanged("originalItemId", AddPlanItem.Type.Lodging)
        val newItem = subject.viewState.value.items[newItemIndex]
        assertThat(newItem).isEqualTo(expected)
    }
}

private fun <T> List<T>.nextAfter(item: T, positions: Int = 1) = this[indexOf(item) + positions]