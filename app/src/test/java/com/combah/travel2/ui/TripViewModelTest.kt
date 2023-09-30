package com.combah.travel2.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import com.combah.travel2.observedValue
import com.combah.travel2.ui.data.*
import com.combah.travel2.ui.trip.TripViewModel
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class TripViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = mock<TripRepository> {
        on { findTripById(any()) } doAnswer {
            flowOf(trip)
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @Test
    fun eventsShouldHaveOneDepartureEventPerFlight() {
        val viewModel = TripViewModel(repository, "minhaTrip")
        val events = viewModel.events.observedValue

        if (events == null) {
            fail()
            return
        }

        assertTrue(events.contains(FlightEvent(nyc, paris, jfk, flightToParisDepartureDate)))
        assertTrue(events.contains(FlightEvent(paris, brussels, cdg, flightToBruxelsDepartureDate)))
    }

    @Test
    fun eventsShouldHaveOneArrivalEventPerFlight() {
        val viewModel = TripViewModel(repository, "minhaTrip")
        val events = viewModel.events.observedValue

        if (events == null) {
            fail()
            return
        }

        assertTrue(events.contains(ArrivalEvent(cdg, flightToParisArrivalDate, paris)))
        assertTrue(events.contains(ArrivalEvent(bru, flightToBruxelsArrivalDate, brussels)))
    }

    @Test
    fun eventsShouldHaveOneCheckinEventPerHotel() {
        val viewModel = TripViewModel(repository, "minhaTrip")
        val events = viewModel.events.observedValue

        if (events == null) {
            fail()
            return
        }

        assertTrue(events.contains(CheckinEvent(parisHotel)))
    }

    @Test
    fun eventsShouldHaveOneCheckoutEventPerHotel() {
        val viewModel = TripViewModel(repository, "minhaTrip")
        val events = viewModel.events.observedValue

        if (events == null) {
            fail()
            return
        }

        assertTrue(events.contains(CheckoutEvent(parisHotel)))
    }

    @Test
    fun eventsShouldHaveOnePlaceEventForEachPlace() {
        val viewModel = TripViewModel(repository, "minhaTrip")
        val events = viewModel.events.observedValue

        if (events == null) {
            fail()
            return
        }

        assertTrue(events.contains(PlaceEvent(paris, flightToParisArrivalDate)))
        assertTrue(events.contains(PlaceEvent(brussels, flightToBruxelsArrivalDate)))
    }

    @Test
    fun eventsShouldHaveOneMonthEventForEachMonth() {
        val viewModel = TripViewModel(repository, "minhaTrip")
        val events = viewModel.events.observedValue

        if (events == null) {
            fail()
            return
        }

        assertTrue(events.contains(MonthEvent(9, 2018)))
        assertTrue(events.contains(MonthEvent(10, 2018)))
    }

    @Test
    fun firstEventsShouldBeFirstOfEachDay() {
        val viewModel = TripViewModel(repository, "minhaTrip")
        val events = viewModel.firstEvents.observedValue

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
        val viewModel = TripViewModel(repository, "minhaTrip")
        val events = viewModel.events.observedValue

        if (events == null) {
            fail()
            return
        }

        events.forEachIndexed { index, event ->
            val previous = events.getOrNull(index - 1) ?: return@forEachIndexed

            assertTrue(previous.timestamp < event.timestamp || previous is PlaceEvent)
        }
    }
}