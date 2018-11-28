package com.combah.travel2.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.combah.travel2.extensions.*
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Hotel
import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.ui.data.*
import java.util.*
import javax.inject.Inject

class TripViewModel(private val repository: TripRepository, tripId: String) : ViewModel() {

    val events = getEventsFromTrip(tripId)
            .asLiveData()

    val firstEvents = events.map(this::getFirstEvents)

    private fun getEventsFromTrip(tripId: String) = getFlightEventForTrip(tripId)
            .plusConcat(getHotelEventsForTrip(tripId))
            .plusMap { getPlaceEventsFromEvents(it) }
            .plusMap { getMonthEventsFromEvents(it) }
            .sortedWith(getEventComparator())

    private fun getFlightEventForTrip(tripId: String) = repository.getTripFlights(tripId)
            .map(this::getFlightEventsFromFlights)

    private fun getFlightEventsFromFlights(flights: List<Flight>) = flights.flatMap {
        it.segments.flatMap(this::getFlightEventsFromSegment)
    }

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

    private fun getHotelEventsForTrip(tripId: String) = repository.getTripHotels(tripId)
            .map(this::getHotelEventsFromHotels)

    private fun getHotelEventsFromHotels(hotels: List<Hotel>) = hotels.flatMap {
        listOf(
                CheckinEvent(it),
                CheckoutEvent(it)
        )
    }

    private fun getMonthEventsFromEvents(events: Iterable<TripEvent>) = events
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

    private fun getFirstEvents(events: Iterable<TripEvent>?) = events
            ?.filter { it !is PlaceEvent && it !is MonthEvent }
            ?.distinctBy { getInitialTimeOfDay(it.timestamp) }
            ?.toSet()

    private fun getInitialTimeOfDay(timestamp: Date): Date {
        val calendar = timestamp.asCalendar()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun getEventComparator() = Comparator<TripEvent> { event1, event2 ->
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