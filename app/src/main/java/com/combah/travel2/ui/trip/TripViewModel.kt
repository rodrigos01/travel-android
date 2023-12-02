package com.combah.travel2.ui.trip

import androidx.lifecycle.ViewModel
import com.combah.travel2.extensions.asCalendar
import com.combah.travel2.extensions.asStateFlow
import com.combah.travel2.extensions.dayOfMonthString
import com.combah.travel2.extensions.dayOfWeekString
import com.combah.travel2.extensions.midnightTime
import com.combah.travel2.extensions.plus
import com.combah.travel2.extensions.timeString
import com.combah.travel2.model.data.Trip
import com.combah.travel2.model.repository.TripRepository
import kotlinx.coroutines.flow.map
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class TripViewModel(repository: TripRepository, tripId: String) : ViewModel() {

    data class ViewState(
        val items: List<TripItem>,
    )

    sealed interface TripItem {

        val timestamp: Date

        data class MonthItem(override val timestamp: Date, val month: String, val year: String) :
            TripItem

        data class DateRangeItem(
            override val timestamp: Date,
            val dayOfMonthStart: String,
            val dayOfWeekStart: String,
            val dayOfMonthEnd: String,
            val dayOfWeekEnd: String,
        ) : TripItem

        data class PlaceItem(
            override val timestamp: Date,
            val placeName: String,
            val imageUrl: String,
        ) : TripItem

        sealed interface EventItem : TripItem {
            val showDate: Boolean
            val dayOfMonth: String?
            val dayOfWeek: String?
            val time: String
            val title: String?
            val subtitle: String?
        }

        data class FlightDepartureItem(
            override val timestamp: Date,
            override val showDate: Boolean,
            override val dayOfMonth: String,
            override val dayOfWeek: String,
            override val time: String,
            val destination: String,
            val airport: String
        ) : EventItem {
            override val title = destination
            override val subtitle = airport
        }

        data class FlightArrivalItem(
            override val timestamp: Date,
            override val showDate: Boolean,
            override val dayOfMonth: String,
            override val dayOfWeek: String,
            override val time: String,
            val airport: String
        ) : EventItem {
            override val title = null
            override val subtitle = airport
        }

        data class HotelCheckInItem(
            override val timestamp: Date,
            override val showDate: Boolean,
            override val dayOfMonth: String,
            override val dayOfWeek: String,
            override val time: String,
            val hotelName: String,
            val hotelAddress: String,
        ) : EventItem {
            override val title = null
            override val subtitle = hotelAddress
        }

        data class HotelCheckOutItem(
            override val timestamp: Date,
            override val showDate: Boolean,
            override val dayOfMonth: String,
            override val dayOfWeek: String,
            override val time: String,
            val hotelName: String
        ) : EventItem {
            override val title = null
            override val subtitle = hotelName
        }
    }

    private val trip = repository.findTripById(tripId)

    val viewState = trip.map {
        ViewState(
            items = genItems(it),
        )
    }.asStateFlow(initialValue = ViewState(emptyList()))

    private fun genItems(trip: Trip): List<TripItem> {
        val flights = trip.flights.flatMap { flight ->
            flight.segments.flatMap {
                listOf(
                    TripItem.FlightDepartureItem(
                        timestamp = it.departure,
                        showDate = false,
                        dayOfMonth = it.departure.dayOfMonthString(),
                        dayOfWeek = it.departure.dayOfWeekString(),
                        time = it.departure.timeString(),
                        destination = it.cityTo.name,
                        airport = it.airportFrom.name,
                    ), TripItem.FlightArrivalItem(
                        timestamp = it.arrival,
                        showDate = false,
                        dayOfMonth = it.arrival.dayOfMonthString(),
                        dayOfWeek = it.arrival.dayOfWeekString(),
                        time = it.arrival.timeString(),
                        airport = it.airportTo.name,
                    )
                )
            }
        }
        val hotels = trip.hotels.flatMap {
            listOf(
                TripItem.HotelCheckInItem(
                    timestamp = it.checkin,
                    showDate = false,
                    dayOfWeek = it.checkin.dayOfWeekString(),
                    dayOfMonth = it.checkin.dayOfMonthString(),
                    time = it.checkin.timeString(),
                    hotelName = it.name,
                    hotelAddress = it.place.address,
                ), TripItem.HotelCheckOutItem(
                    timestamp = it.checkout,
                    showDate = false,
                    dayOfWeek = it.checkout.dayOfWeekString(),
                    dayOfMonth = it.checkout.dayOfMonthString(),
                    time = it.checkout.timeString(),
                    hotelName = it.name,
                )
            )
        }
        val events = flights + hotels
        val months = events.distinctBy { it.timestamp.asCalendar()[Calendar.MONTH] }.map {
            TripItem.MonthItem(
                timestamp = Date(it.timestamp.midnightTime - TimeUnit.DAYS.toMillis(it.timestamp.asCalendar()[Calendar.DAY_OF_MONTH].toLong())),
                month = DateFormatSymbols.getInstance().months[it.timestamp.asCalendar()[Calendar.MONTH]],
                year = it.timestamp.asCalendar()[Calendar.YEAR].toString(),
            )
        }
        val emptyDateRanges = events.zipWithNext { previous, next ->
            val start = (previous.timestamp + TimeUnit.DAYS.toMillis(1)).midnightTime
            val end = next.timestamp.midnightTime - TimeUnit.MINUTES.toMillis(1)
            if (end > start) {
                val dateStart = Date(start)
                val dateEnd = Date(end)
                TripItem.DateRangeItem(
                    timestamp = dateStart,
                    dayOfMonthStart = dateStart.dayOfMonthString(),
                    dayOfWeekStart = dateStart.dayOfWeekString(),
                    dayOfMonthEnd = dateEnd.dayOfMonthString(),
                    dayOfWeekEnd = dateEnd.dayOfWeekString(),
                )
            } else {
                null
            }
        }.filterNotNull().distinct()
        val places =
            (trip.hotels.map { it.checkin.midnightTime to it.place } +
                    trip.flights.flatMap { flight -> flight.segments.map { it.arrival.midnightTime to it.cityTo } })
                .distinct()
                .map {
                    TripItem.PlaceItem(
                        Date(it.first),
                        it.second.name,
                        it.second.coverImage ?: ""
                    )
                }
        val allItems = (events + months + emptyDateRanges + places).sortedBy { it.timestamp }
        return allItems.mapIndexed { index, item ->
            val prev = allItems.getOrNull(index - 1)
            if (item is TripItem.EventItem && (prev !is TripItem.EventItem || prev.timestamp.midnightTime < item.timestamp.midnightTime)) {
                val newItem = when (item) {
                    is TripItem.FlightDepartureItem -> item.copy(showDate = true)
                    is TripItem.FlightArrivalItem -> item.copy(showDate = true)
                    is TripItem.HotelCheckInItem -> item.copy(showDate = true)
                    is TripItem.HotelCheckOutItem -> item.copy(showDate = true)
                }
                newItem
            } else {
                item
            }
        }
    }
}