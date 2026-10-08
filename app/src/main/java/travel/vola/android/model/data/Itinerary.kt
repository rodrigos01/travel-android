package travel.vola.android.model.data

import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * The trip's events organised into legs and days, built by the backend every time the trip is
 * edited (see `buildItinerary` in travel-node). Events hold the entities they are about, so
 * nothing needs to look anything up. How it is presented - month headers, date labels,
 * borders - is up to the app; what is in it, and in what order, is not.
 *
 * A trip that hasn't been edited or backfilled since the itinerary was introduced has none
 * ([Trip.itinerary] is null).
 */
data class Itinerary(val legs: List<ItineraryLeg>) {

    val events: Sequence<ItineraryEvent>
        get() = legs.asSequence().flatMap { it.days }.flatMap { it.events }
}

/**
 * A stretch of the trip with a common base. A [LegType.PLACE] leg is a stay in [place]; a
 * [LegType.TRANSIT] leg has no stay of its own - it holds the departure from where the trip
 * starts, the flight home, and so on - and is not given a header.
 *
 * [startDate] and [endDate] bound the leg's header; [days] only has days with something in
 * them, including the empty days after the leg's last event.
 */
data class ItineraryLeg(
    val id: String,
    val type: LegType,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val thumbnailUrl: String?,
    val place: Place?,
    /** The timed place that started this leg, when one did. */
    val startedBy: TimedPlace?,
    val days: List<ItineraryDay>,
)

enum class LegType {
    PLACE,
    TRANSIT,
}

data class ItineraryDay(
    val date: LocalDate,
    val events: List<ItineraryEvent>,
)

sealed interface ItineraryEvent {
    val id: String

    /** A moment of one of the trip's entities; [entity] is what editing it edits. */
    sealed interface OfEntity : ItineraryEvent {
        val entity: TripEntity
        val timestamp: ZonedDateTime
    }

    data class FlightDeparture(
        override val id: String,
        val flight: Flight,
        val segment: FlightSegment,
    ) : OfEntity {
        override val entity get() = flight
        override val timestamp get() = segment.departure
    }

    data class FlightArrival(
        override val id: String,
        val flight: Flight,
        val segment: FlightSegment,
    ) : OfEntity {
        override val entity get() = flight
        override val timestamp get() = segment.arrival
    }

    data class LodgingCheckIn(override val id: String, val lodging: Lodging) : OfEntity {
        override val entity get() = lodging
        override val timestamp get() = lodging.checkIn
    }

    data class LodgingCheckOut(override val id: String, val lodging: Lodging) : OfEntity {
        override val entity get() = lodging
        override val timestamp get() = lodging.checkout
    }

    data class TimedPlaceVisit(override val id: String, val place: TimedPlace) : OfEntity {
        override val entity get() = place
        override val timestamp get() = place.startDateTime
    }

    data class Restaurant(override val id: String, val reservation: RestaurantReservation) : OfEntity {
        override val entity get() = reservation
        override val timestamp get() = reservation.dateTime
    }

    data class FlexibleSection(override val id: String, val section: FlexibleDaySection) : OfEntity {
        override val entity get() = section
        override val timestamp get() = section.date
    }

    /**
     * A suggestion the app is showing in the place of an empty day, before the user decides on
     * it. Never read from the backend.
     */
    data class Placeholder(val placeholder: SuggestionPlaceholder) : ItineraryEvent {
        override val id get() = placeholder.timestamp.toString()
    }

    /** Days with nothing planned, [start] to [end] inclusive; one day when they are the same. */
    data class EmptyDays(
        override val id: String,
        val start: LocalDate,
        val end: LocalDate,
    ) : ItineraryEvent
}
