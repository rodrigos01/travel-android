package com.combah.travel2.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.combah.travel2.extensions.asCalendar
import com.combah.travel2.extensions.asLiveData
import com.combah.travel2.extensions.map
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.ui.data.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class TripViewModel(repository: TripRepository, tripId: String) : ViewModel() {

    private val earlierEventTimes = HashMap<Date, Date>()

    private val trip = repository.findTripById(tripId)


    val events = trip.filter { it.flights != null }
            .map(this::getEventsFromTrip)
            .asLiveData()

    val firstEvents = events.map(this::getFirstEvents)

    private fun getEventsFromTrip(trip: Trip) = getFlightEventsFromTrip(trip)
            ?.asSequence()
            ?.plus(getHotelEventsFromTrip(trip) ?: emptyList())
            ?.sortedBy { it.timestamp }
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