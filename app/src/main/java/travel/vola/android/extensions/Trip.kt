package travel.vola.android.extensions

import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.model.data.TripEvent
import travel.vola.android.model.data.WithCity
import java.time.ZonedDateTime

data class TripDestination(
    val place: Place,
    val startDateTime: ZonedDateTime,
    val endDateTime: ZonedDateTime?,
)

fun Trip.getDestinations(): List<TripDestination> =
    (flights.flatMap { it.segments } + lodgings + places + restaurants + flexibleSections).flatMap {
        when (it) {
            is FlightSegment -> listOf(
                it.departure to it.getPlace(it.departure),
                it.arrival to it.getPlace(it.arrival)
            )

            is Lodging -> listOf(
                it.checkIn to it.city,
                it.checkout to it.city
            )

            is TimedPlace -> listOf(it.startDateTime to it.city)
            is RestaurantReservation -> listOf(it.dateTime to it.city)
            is FlexibleDaySection -> listOf(it.date to it.city)
        }
    }.sortedBy { it.first }.fold(mutableListOf()) { list, (timestamp, item) ->
        val last = list.lastOrNull()
        if (last?.place == item) {
            list[list.lastIndex] = last.copy(endDateTime = timestamp)
        } else {
            list.add(TripDestination(item, timestamp, timestamp))
        }
        list
    }

fun TripEvent.getPlace(referenceTime: ZonedDateTime) = when (this) {
    is FlightSegment -> if (referenceTime == departure) {
        airportFrom.city
    } else {
        airportTo.city
    }

    is WithCity -> city
}