package com.combah.travel2.ui

import com.combah.travel2.extensions.dateFromString
import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.model.repository.mock.MockData.bru
import com.combah.travel2.model.repository.mock.MockData.brussels
import com.combah.travel2.model.repository.mock.MockData.cdg
import com.combah.travel2.model.repository.mock.MockData.flightToBruxelsArrivalDate
import com.combah.travel2.model.repository.mock.MockData.flightToBruxelsDepartureDate
import com.combah.travel2.model.repository.mock.MockData.flightToParisArrivalDate
import com.combah.travel2.model.repository.mock.MockData.flightToParisDepartureDate
import com.combah.travel2.model.repository.mock.MockData.jfk
import com.combah.travel2.model.repository.mock.MockData.nyc
import com.combah.travel2.model.repository.mock.MockData.paris
import com.combah.travel2.model.repository.mock.MockData.parisHotel
import com.combah.travel2.model.repository.mock.MockData.trip
import com.combah.travel2.test.UnconfinedDispatcherTestRule
import com.combah.travel2.ui.data.ArrivalEvent
import com.combah.travel2.ui.data.CheckinEvent
import com.combah.travel2.ui.data.CheckoutEvent
import com.combah.travel2.ui.data.EmptyDateRangeEvent
import com.combah.travel2.ui.data.FlightEvent
import com.combah.travel2.ui.data.MonthEvent
import com.combah.travel2.ui.data.PlaceEvent
import com.combah.travel2.ui.trip.TripViewModel
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
import kotlinx.coroutines.flow.flowOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.TimeZone

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
    fun eventsShouldHaveOneDepartureEventPerFlight() {
        val events = subject.viewState.value.events

        assertTrue(events.contains(FlightEvent(nyc, paris, jfk, flightToParisDepartureDate)))
        assertTrue(events.contains(FlightEvent(paris, brussels, cdg, flightToBruxelsDepartureDate)))
    }

    @Test
    fun eventsShouldHaveOneArrivalEventPerFlight() {
        val events = subject.viewState.value.events

        assertTrue(events.contains(ArrivalEvent(cdg, flightToParisArrivalDate, paris)))
        assertTrue(events.contains(ArrivalEvent(bru, flightToBruxelsArrivalDate, brussels)))
    }

    @Test
    fun eventsShouldHaveOneCheckinEventPerHotel() {
        val events = subject.viewState.value.events

        assertTrue(events.contains(CheckinEvent(parisHotel)))
    }

    @Test
    fun eventsShouldHaveOneCheckoutEventPerHotel() {
        val events = subject.viewState.value.events

        assertTrue(events.contains(CheckoutEvent(parisHotel)))
    }

    @Test
    fun eventsShouldHaveOnePlaceEventForEachPlace() {
        val events = subject.viewState.value.events

        assertTrue(events.contains(PlaceEvent(paris, flightToParisArrivalDate)))
        assertTrue(events.contains(PlaceEvent(brussels, flightToBruxelsArrivalDate)))
    }

    @Test
    fun eventsShouldHaveOneMonthEventForEachMonth() {
        val events = subject.viewState.value.events

        assertTrue(events.contains(MonthEvent(9, 2018)))
        assertTrue(events.contains(MonthEvent(10, 2018)))
    }

    @Test
    fun firstEventsShouldBeFirstOfEachDay() {
        val events = subject.viewState.value.firstEvents

        if (events == null) {
            fail()
            return
        }

        assertEquals(3, events.size)
        assertTrue(events.contains(FlightEvent(nyc, paris, jfk, flightToParisDepartureDate)))
        assertTrue(events.contains(ArrivalEvent(cdg, flightToParisArrivalDate, paris)))
        assertTrue(events.contains(CheckoutEvent(parisHotel)))
    }

    @Test
    fun eventsShouldBeSortedByTypeAndTimestamp() {
        val events = subject.viewState.value.events

        events.forEachIndexed { index, event ->
            val previous = events.getOrNull(index - 1) ?: return@forEachIndexed

            assertTrue(previous.timestamp < event.timestamp || previous is PlaceEvent)
        }
    }

    @Test
    fun eventsShouldHaveEmptyDateRangeForAllDatesWithoutEvents() {
        val events = subject.viewState.value.events
        val dateStart = dateFromString("2018-10-26T00:00") ?: error("")
        val dateEnd = dateFromString("2018-10-31T23:59:59") ?: error("")
        assertThat(events).containsOnlyOnce(EmptyDateRangeEvent(dateStart, dateEnd))
    }
}