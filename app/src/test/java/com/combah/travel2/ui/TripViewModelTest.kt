package com.combah.travel2.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.combah.travel2.extensions.dateFromString
import com.combah.travel2.model.data.*
import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.ui.data.*
import com.combah.travel2.ui.trip.TripViewModel
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
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

    val jfk = Airport("JFK", "JFK International Airport")
    val nyc = Place(name = "New York")
    val cdg = Airport("CDG", "Charles de Gaule International Airport")
    val paris = Place(name = "Paris")
    val flightToParisDepartureDate = dateFromString("2018-10-24T10:25")
    val flightToParisArrivalDate = dateFromString("2018-10-25T05:15")
    val parisHotelName = "Hôtel Europe Saint-Séverin"
    val parisHotelCheckinDate = dateFromString("2018-10-25T13:00")
    val parisHotelCheckoutDate = dateFromString("2018-10-29T12:00")
    val bru = Airport("BRU", "Brussels Airport (BRU)")
    val bruxels = Place(name = "Bruxels")
    val flightToBruxelsDepartureDate = dateFromString("2018-10-29T15:00")
    val flightToBruxelsArrivalDate = dateFromString("2018-10-29T19:10")
    val trip = Trip(
        id = "minhaTrip",
        flights = listOf(
            Flight(
                segments = listOf(
                    FlightSegment(
                        airportFrom = jfk,
                        cityFrom = nyc,
                        airportTo = cdg,
                        cityTo = paris,
                        departure = flightToParisDepartureDate,
                        arrival = flightToParisArrivalDate
                    )
                )
            ),
            Flight(
                segments = listOf(
                    FlightSegment(
                        airportFrom = cdg,
                        cityFrom = paris,
                        airportTo = bru,
                        cityTo = bruxels,
                        departure = flightToBruxelsDepartureDate,
                        arrival = flightToBruxelsArrivalDate
                    )
                )
            )
        ),
        hotels = listOf(
            Hotel(
                name = parisHotelName,
                place = paris,
                checkin = parisHotelCheckinDate,
                checkout = parisHotelCheckoutDate
            )
        )
    )

    private val tripObservable = Observable.just(trip)
    private val tripListObservable = PublishSubject.create<List<Trip>>()
    private val repository = mock<TripRepository>()

    @Before
    fun setup() {
        whenever(repository.trips).thenReturn(tripListObservable)
        whenever(repository.findTripById(any())).thenReturn(tripObservable)
    }

    @Test
    fun shouldGetTripWithIdProvided() {
        val viewModel = TripViewModel(repository, "minhaTrip")
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
                assertEquals(FlightEvent(paris, jfk, flightToParisDepartureDate), it[0])
                assertEquals(ArrivalEvent(cdg, flightToParisArrivalDate), it[1])
                assertEquals(CheckinEvent(parisHotelName, parisHotelCheckinDate), it[2])
                assertEquals(CheckoutEvent(parisHotelName, parisHotelCheckoutDate), it[3])
                assertEquals(FlightEvent(bruxels, cdg, flightToBruxelsDepartureDate), it[4])
                assertEquals(ArrivalEvent(bru, flightToBruxelsArrivalDate), it[5])
            }
        }
    }
}