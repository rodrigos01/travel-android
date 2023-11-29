@file:OptIn(ExperimentalContracts::class)

package com.combah.travel2.ui

import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.model.repository.mock.MockData.bru
import com.combah.travel2.model.repository.mock.MockData.brussels
import com.combah.travel2.model.repository.mock.MockData.cdg
import com.combah.travel2.model.repository.mock.MockData.jfk
import com.combah.travel2.model.repository.mock.MockData.paris
import com.combah.travel2.model.repository.mock.MockData.parisHotelName
import com.combah.travel2.model.repository.mock.MockData.trip
import com.combah.travel2.test.UnconfinedDispatcherTestRule
import com.combah.travel2.ui.trip.TripViewModel
import com.combah.travel2.ui.trip.TripViewModel.TripItem.DateRangeItem
import com.combah.travel2.ui.trip.TripViewModel.TripItem.FlightArrivalItem
import com.combah.travel2.ui.trip.TripViewModel.TripItem.FlightDepartureItem
import com.combah.travel2.ui.trip.TripViewModel.TripItem.HotelCheckInItem
import com.combah.travel2.ui.trip.TripViewModel.TripItem.HotelCheckOutItem
import com.combah.travel2.ui.trip.TripViewModel.TripItem.MonthItem
import com.combah.travel2.ui.trip.TripViewModel.TripItem.PlaceItem
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.TimeZone
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
class TripViewModelTest {

    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val repository = mock<TripRepository> {
        on { findTripById(any()) } doAnswer {
            flowOf(trip)
        }
    }
    private val subject = TripViewModel(repository, "minhaTrip")

    @Before
    fun setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @Test
    fun eventsShouldHaveOneDepartureEventPerFlight() = runTest {
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<FlightDepartureItem>(item)
            assertThat(item.airport).isEqualTo(jfk.name)
            assertThat(item.destination).isEqualTo(paris.name)
            assertThat(item.dayOfMonth).isEqualTo("24")
            assertThat(item.time).isEqualTo("10:25 AM")
        }
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<FlightDepartureItem>(item)
            assertThat(item.airport).isEqualTo(cdg.name)
            assertThat(item.destination).isEqualTo(brussels.name)
            assertThat(item.dayOfMonth).isEqualTo("01")
            assertThat(item.time).isEqualTo("3:00 PM")
        }
    }

    @Test
    fun eventsShouldHaveOneArrivalEventPerFlight() {
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<FlightArrivalItem>(item)
            assertThat(item.airport).isEqualTo(cdg.name)
            assertThat(item.dayOfMonth).isEqualTo("25")
            assertThat(item.time).isEqualTo("5:15 AM")
        }
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<FlightArrivalItem>(item)
            assertThat(item.airport).isEqualTo(bru.name)
            assertThat(item.dayOfMonth).isEqualTo("01")
            assertThat(item.time).isEqualTo("7:10 PM")
        }
    }

    @Test
    fun eventsShouldHaveOneCheckinEventPerHotel() {
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<HotelCheckInItem>(item)
            assertThat(item.hotelName).isEqualTo(parisHotelName)
            assertThat(item.dayOfMonth).isEqualTo("25")
            assertThat(item.time).isEqualTo("1:00 PM")
        }
    }

    @Test
    fun eventsShouldHaveOneCheckoutEventPerHotel() {
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<HotelCheckOutItem>(item)
            assertThat(item.hotelName).isEqualTo(parisHotelName)
            assertThat(item.dayOfMonth).isEqualTo("01")
            assertThat(item.time).isEqualTo("12:00 PM")
        }
    }

    @Test
    fun eventsShouldHaveOnePlaceEventForEachPlace() {
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<PlaceItem>(item)
            assertThat(item.placeName).isEqualTo(paris.name)
        }
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<PlaceItem>(item)
            assertThat(item.placeName).isEqualTo(brussels.name)
        }
    }

    @Test
    fun eventsShouldHaveOneMonthEventForEachMonth() {
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<MonthItem>(item)
            assertThat(item.month).isEqualTo("October")
            assertThat(item.year).isEqualTo("2018")
        }
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<MonthItem>(item)
            assertThat(item.month).isEqualTo("November")
            assertThat(item.year).isEqualTo("2018")
        }
    }

    @Test
    fun firstEventsShouldBeFirstOfEachDay() {
        val items = subject.viewState.value.items

        val flightDepartureItem = items.first { it is FlightDepartureItem } as FlightDepartureItem
        assertThat(flightDepartureItem.destination).isEqualTo(paris.name)
        assertThat(flightDepartureItem.showDate).isTrue
        val flightArrivalItem = items.first { it is FlightArrivalItem } as FlightArrivalItem
        assertThat(flightArrivalItem.airport).isEqualTo(cdg.name)
        assertThat(flightArrivalItem.showDate).isTrue
        val hotelCheckOutItem = items.first { it is HotelCheckOutItem } as HotelCheckOutItem
        assertThat(hotelCheckOutItem.hotelName).isEqualTo(parisHotelName)
        assertThat(hotelCheckOutItem.showDate).isTrue
    }

    @Test
    fun eventsShouldBeSortedByTypeAndTimestamp() {
        val items = subject.viewState.value.items

        items.forEachIndexed { index, event ->
            val previous = items.getOrNull(index - 1) ?: return@forEachIndexed
            assertThat(previous.timestamp.time).isLessThanOrEqualTo(event.timestamp.time)
        }
    }

    @Test
    fun eventsShouldHaveEmptyDateRangeForAllDatesWithoutEvents() {
        assertThat(subject.viewState.value.items).satisfiesOnlyOnce { item ->
            assertType<DateRangeItem>(item)
            assertThat(item.dayOfMonthStart).isEqualTo("26")
            assertThat(item.dayOfMonthEnd).isEqualTo("31")
        }
    }
}

@ExperimentalContracts
private inline fun <reified T> assertType(obj: Any?) {
    contract { returns() implies (obj is T) }
    assertThat(obj).isInstanceOf(T::class.java)
}