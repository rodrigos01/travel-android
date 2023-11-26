package com.combah.travel2.ui.trip

import androidx.lifecycle.ViewModel
import com.combah.travel2.extensions.asCalendar
import com.combah.travel2.extensions.asStateFlow
import com.combah.travel2.extensions.midnightTime
import com.combah.travel2.extensions.plus
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import com.combah.travel2.ui.data.ArrivalEvent
import com.combah.travel2.ui.data.CheckinEvent
import com.combah.travel2.ui.data.CheckoutEvent
import com.combah.travel2.ui.data.EmptyDateRangeEvent
import com.combah.travel2.ui.data.FlightEvent
import com.combah.travel2.ui.data.MonthEvent
import com.combah.travel2.ui.data.PlaceEvent
import com.combah.travel2.ui.data.TripEvent
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class TripViewModel(repository: TripRepository, tripId: String) : ViewModel() {

    data class ViewState(
        val events: List<TripEvent>,
        val firstEvents: Set<TripEvent>?,
    )

    private val trip = repository.findTripById(tripId)

    val viewState = trip.filter { it.flights != null }.map {
        val events = getEventsFromTrip(it)
        ViewState(
            events = events,
            firstEvents = getFirstEvents(events)
        )
    }.asStateFlow(initialValue = ViewState(emptyList(), null))

    private fun getEventsFromTrip(trip: Trip) = getFlightEventsFromTrip(trip)
        ?.plus(getHotelEventsFromTrip(trip) ?: emptyList())
        ?.sortedBy { it.timestamp }
        ?.let { it.plus(getPlaceEventsFromEvents(it.asIterable())) }
        ?.let { it.plus(getMonthEventsFromEvents(it.asIterable())) }
        ?.sortedWith(::compareEvents)
        ?.let { it.plus(getEmptyDateRangeEventsFromEvents(it.asIterable())) }
        ?.sortedWith(::compareEvents)
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

    private fun getEmptyDateRangeEventsFromEvents(events: Iterable<TripEvent>) =
        events
            .zipWithNext { previous, next ->
                val dateStart = (previous.timestamp + TimeUnit.DAYS.toMillis(1)).midnightTime
                val dateEnd = next.timestamp.midnightTime - TimeUnit.MINUTES.toMillis(1)
                if (dateEnd > dateStart) {
                    EmptyDateRangeEvent(Date(dateStart), Date(dateEnd))
                } else {
                    null
                }
            }.filterNotNull()
            .toList()

    private fun getPlaceEventsFromEvents(events: Iterable<TripEvent>) = events
        .distinctBy { it.place }
        .map {
            PlaceEvent(
                it.place,
                it.timestamp
            )
        }.let { it.takeLast(it.size - 1) }

    private fun getFirstEvents(events: Iterable<TripEvent>) = events
        .filter { it !is PlaceEvent && it !is MonthEvent }
        .distinctBy { it.timestamp.midnightTime }
        .toSet()

    private fun compareEvents(event1: TripEvent, event2: TripEvent): Int {
        val timeComparison = event1.timestamp.compareTo(event2.timestamp)
        return if (timeComparison == 0 && event1 is PlaceEvent) {
            -1
        } else {
            timeComparison
        }
    }
}