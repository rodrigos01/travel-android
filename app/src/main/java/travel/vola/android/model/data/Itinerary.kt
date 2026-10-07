package travel.vola.android.model.data

import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * The trip's events organised into legs and days, built by the backend every time the trip is
 * edited (see `buildItinerary` in travel-node). The app decides how to present it - month
 * headers, date labels, borders - but not what is in it.
 *
 * [version] is the builder version that produced it; a trip that hasn't been edited or
 * backfilled since the itinerary was introduced has none ([Trip.itinerary] is null).
 */
data class Itinerary(
    val version: Int,
    val legs: List<ItineraryLeg>,
)

/**
 * A stretch of the trip with a common base, today always a place. Legs are generic so other
 * kinds can follow: a [LegType.TRANSIT] leg has no header (it holds the departure from where
 * the trip starts, the flight home, and so on).
 *
 * [startDate] and [endDate] bound the leg's header; [days] is sparse and only has days with
 * something in them, including the empty days and date ranges after the leg's last event.
 */
data class ItineraryLeg(
    val id: String,
    val type: LegType,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val thumbnailUrl: String?,
    val place: Place?,
    /** The timed place that started this leg, when it was one; null otherwise. */
    val entityRef: EntityRef?,
    val days: List<ItineraryDay>,
)

enum class LegType {
    PLACE,
    TRANSIT,

    /** A type this version of the app doesn't know. Its events are still shown. */
    UNKNOWN,
}

data class ItineraryDay(
    val date: LocalDate,
    val events: List<ItineraryEvent>,
)

sealed interface ItineraryEvent {
    val id: String

    /** An event that is a moment of one of the trip's entities, edited through [entityRef]. */
    data class OfEntity(
        override val id: String,
        val type: EntityEventType,
        val entityRef: EntityRef,
        /** In the offset of the place it happens in. */
        val timestamp: ZonedDateTime,
    ) : ItineraryEvent

    /** One day with nothing planned. */
    data class EmptyDay(
        override val id: String,
        val date: LocalDate,
    ) : ItineraryEvent

    /** Several consecutive days with nothing planned, [start] to [end] inclusive. */
    data class EmptyDateRange(
        override val id: String,
        val start: LocalDate,
        val end: LocalDate,
    ) : ItineraryEvent
}

enum class EntityEventType {
    FLIGHT_DEPARTURE,
    FLIGHT_ARRIVAL,
    LODGING_CHECK_IN,
    LODGING_CHECK_OUT,
    TIMED_PLACE,
    RESTAURANT,
    FLEXIBLE_SECTION,
}

/** Points at an entity on the trip: [segmentIndex] says which segment, for flights. */
data class EntityRef(
    val type: EntityType,
    val id: String,
    val segmentIndex: Int? = null,
)

enum class EntityType {
    FLIGHT,
    LODGING,
    PLACE,
    RESTAURANT,
    FLEXIBLE_SECTION,
}
