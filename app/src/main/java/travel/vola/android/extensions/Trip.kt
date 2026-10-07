package travel.vola.android.extensions

import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.data.LegType
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.Trip
import travel.vola.android.model.data.TripEvent
import travel.vola.android.model.data.WithCity
import java.time.ZonedDateTime

data class TripDestination(
    val place: Place,
    val startDateTime: ZonedDateTime,
    val endDateTime: ZonedDateTime?,
)

/**
 * The places the trip stays in, in order: the legs of the itinerary the backend built. A trip
 * the backend hasn't processed yet has none.
 */
fun Trip.getDestinations(): List<TripDestination> =
    itinerary?.legs.orEmpty().filter { it.type != LegType.TRANSIT }.mapNotNull { leg ->
        leg.place?.let { place ->
            val zone = place.timeZone.toZoneId()
            TripDestination(
                place = place,
                startDateTime = leg.startDate.atStartOfDay(zone),
                endDateTime = leg.endDate.atStartOfDay(zone),
            )
        }
    }

fun TripEvent.getPlace(referenceTime: ZonedDateTime) = when (this) {
    is FlightSegment -> if (referenceTime == departure) {
        airportFrom.city
    } else {
        airportTo.city
    }

    is WithCity -> city
}
