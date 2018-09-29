package com.combah.travel2.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.combah.travel2.extensions.asCalendar
import com.combah.travel2.extensions.asLiveData
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.ui.data.*
import java.util.*
import javax.inject.Inject

class TripViewModel(repository: TripRepository, tripId: String) : ViewModel() {

    private val trip = repository.findTripById(tripId)

    val events = trip.filter { it.flights != null }
            .map(this::getEventsFromTrip)
            .asLiveData()

    private fun getEventsFromTrip(trip: Trip) = getFlightEventsFromTrip(trip)
            ?.asSequence()
            ?.plus(getHotelEventsFromTrip(trip) ?: emptyList())
            ?.let { it.plus(getPlaceEventsFromEvents(it.asIterable())) }
            ?.let { it.plus(getMonthEventsFromEvents(it.asIterable())) }
            ?.sortedWith(eventComparator)
            ?.toList()
            ?: emptyList()

    private fun getFlightEventsFromTrip(trip: Trip) = trip.flights
            ?.flatMap { it.segments.flatMap(this::getFlightEventsFromSegment) }

    private fun getFlightEventsFromSegment(segment: FlightSegment) = listOf(
            FlightEvent(
                    segment.cityFrom,
                    segment.cityTo,
                    segment.airportFrom,
                    segment.departure
            ),
            ArrivalEvent(
                    segment.airportTo,
                    segment.arrival,
                    segment.cityTo
            )
    )

    private fun getHotelEventsFromTrip(trip: Trip) = trip.hotels?.flatMap {
        listOf(
                CheckinEvent(it),
                CheckoutEvent(it)
        )
    }

    private fun getMonthEventsFromEvents(events: Iterable<TripEvent>) = events
            .sortedBy { it.timestamp }
            .distinctBy { it.timestamp.asCalendar()[Calendar.MONTH] }
            .map { event ->
                event.timestamp.asCalendar().let {
                    MonthEvent(
                            it[Calendar.MONTH],
                            it[Calendar.YEAR]
                    )
                }
            }

    private fun getPlaceEventsFromEvents(events: Iterable<TripEvent>) = events
            .sortedBy { it.timestamp }
            .distinctBy { it.place }
            .map {
                PlaceEvent(
                        it.place,
                        it.timestamp
                )
            }.let { it.takeLast(it.size - 1) }

    private val eventComparator = Comparator<TripEvent> { event1, event2 ->
        val timeComparison = event1.timestamp.compareTo(event2.timestamp)
        if (timeComparison == 0 && event1 is PlaceEvent) {
            -1
        } else {
            timeComparison
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(private val repository: TripRepository) : ViewModelProvider.Factory {

        lateinit var tripId: String

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TripViewModel(repository, tripId) as T
        }

    }
}