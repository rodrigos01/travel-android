package com.combah.travel2.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.combah.travel2.mock.*
import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.ui.data.*
import com.combah.travel2.ui.trip.TripViewModel
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TripViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val tripObservable = Observable.just(trip)
    private val repository = mock<TripRepository>()

    @Before
    fun setup() {
        whenever(repository.findTripById(any())).thenReturn(tripObservable)
    }

    @Test
    fun shouldGetTripWithIdProvided() {
        TripViewModel(repository, "minhaTrip")
        verify(repository).findTripById("minhaTrip")
    }

    @Test
    fun eventsShouldBeListOfFlightsAndHotels() {
        val viewModel = TripViewModel(repository, "minhaTrip")
        var events: List<TripEvent>? = null
        viewModel.events.observeForever {
            events = it
        }
        if (events == null) fail()
        else {
            events?.let {
                assertEquals(MonthEvent(9, 2018), it[0])
                assertEquals(FlightEvent(nyc, paris, jfk, flightToParisDepartureDate), it[1])
                assertEquals(PlaceEvent(paris, flightToParisArrivalDate), it[2])
                assertEquals(ArrivalEvent(cdg, flightToParisArrivalDate, paris), it[3])
                assertEquals(CheckinEvent(parisHotel), it[4])
                assertEquals(MonthEvent(10, 2018), it[5])
                assertEquals(CheckoutEvent(parisHotel), it[6])
                assertEquals(FlightEvent(paris, brussels, cdg, flightToBruxelsDepartureDate), it[7])
                assertEquals(PlaceEvent(brussels, flightToBruxelsArrivalDate), it[8])
                assertEquals(ArrivalEvent(bru, flightToBruxelsArrivalDate, brussels), it[9])
            }
        }
    }

    @Test
    fun firstEventsShouldBeFirstOfEachDay() {
        val viewModel = TripViewModel(repository, "minhaTrip")
        var events: Set<TripEvent>? = null
        viewModel.firstEvents.observeForever {
            events = it
        }
        if (events == null) fail()
        else {
            events?.let {
                assertEquals(3, it.size)
                assertTrue(it.contains(FlightEvent(nyc, paris, jfk, flightToParisDepartureDate)))
                assertTrue(it.contains(ArrivalEvent(cdg, flightToParisArrivalDate, paris)))
                assertTrue(it.contains(CheckoutEvent(parisHotel)))
            }
        }
    }
}