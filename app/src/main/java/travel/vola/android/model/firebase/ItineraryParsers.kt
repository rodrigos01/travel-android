package travel.vola.android.model.firebase

import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.Itinerary
import travel.vola.android.model.data.ItineraryDay
import travel.vola.android.model.data.ItineraryEvent
import travel.vola.android.model.data.ItineraryLeg
import travel.vola.android.model.data.LegType
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.firebase.FirebaseData.EntityTypes
import travel.vola.android.model.firebase.FirebaseData.EventTypes
import travel.vola.android.model.firebase.FirebaseData.LegTypes
import java.time.LocalDate

/**
 * Reads the itinerary the backend wrote. Events refer to the trip's entities by id; they are
 * resolved here, so the rest of the app is given the flights, lodgings and so on themselves.
 *
 * What the app can't make sense of is left out rather than failing the itinerary: a leg or
 * event of a type this version doesn't know, or one whose entity is no longer on the trip.
 */
fun FirebaseData.Itinerary.toAppDataModel(
    flights: List<Flight>,
    lodgings: List<Lodging>,
    places: List<TimedPlace>,
    restaurants: List<RestaurantReservation>,
    sections: List<FlexibleDaySection>,
) = ItineraryParser(
    flights = flights.associateBy(Flight::id),
    lodgings = lodgings.associateBy(Lodging::id),
    places = places.associateBy(TimedPlace::id),
    restaurants = restaurants.associateBy(RestaurantReservation::id),
    sections = sections.associateBy(FlexibleDaySection::id),
).parse(this)

private class ItineraryParser(
    private val flights: Map<String, Flight>,
    private val lodgings: Map<String, Lodging>,
    private val places: Map<String, TimedPlace>,
    private val restaurants: Map<String, RestaurantReservation>,
    private val sections: Map<String, FlexibleDaySection>,
) {

    fun parse(itinerary: FirebaseData.Itinerary) = Itinerary(itinerary.legs.mapNotNull(::parseLeg))

    private fun parseLeg(leg: FirebaseData.ItineraryLeg): ItineraryLeg? {
        val type = when (leg.type) {
            LegTypes.PLACE -> LegType.PLACE
            LegTypes.TRANSIT -> LegType.TRANSIT
            else -> return null
        }
        return ItineraryLeg(
            id = leg.id,
            type = type,
            title = leg.title,
            startDate = LocalDate.parse(leg.startDate),
            endDate = LocalDate.parse(leg.endDate),
            thumbnailUrl = leg.thumbnailUrl,
            place = leg.place?.toAppDataModel(),
            startedBy = leg.entityRef?.let { places[it.id] },
            days = leg.days.map(::parseDay),
        )
    }

    private fun parseDay(day: FirebaseData.ItineraryDay): ItineraryDay {
        val date = LocalDate.parse(day.date)
        return ItineraryDay(date, day.events.mapNotNull { parseEvent(it, date) })
    }

    private fun parseEvent(event: FirebaseData.ItineraryEvent, day: LocalDate): ItineraryEvent? {
        val ref = event.entityRef
        val id = event.id
        return when (event.type) {
            EventTypes.FLIGHT_DEPARTURE -> segment(ref)?.let { (flight, segment) ->
                ItineraryEvent.FlightDeparture(id, flight, segment)
            }

            EventTypes.FLIGHT_ARRIVAL -> segment(ref)?.let { (flight, segment) ->
                ItineraryEvent.FlightArrival(id, flight, segment)
            }

            EventTypes.LODGING_CHECK_IN -> entity(ref, EntityTypes.LODGING, lodgings)
                ?.let { ItineraryEvent.LodgingCheckIn(id, it) }

            EventTypes.LODGING_CHECK_OUT -> entity(ref, EntityTypes.LODGING, lodgings)
                ?.let { ItineraryEvent.LodgingCheckOut(id, it) }

            EventTypes.TIMED_PLACE -> entity(ref, EntityTypes.PLACE, places)
                ?.let { ItineraryEvent.TimedPlaceVisit(id, it) }

            EventTypes.RESTAURANT -> entity(ref, EntityTypes.RESTAURANT, restaurants)
                ?.let { ItineraryEvent.Restaurant(id, it) }

            EventTypes.FLEXIBLE_SECTION -> entity(ref, EntityTypes.FLEXIBLE_SECTION, sections)
                ?.let { ItineraryEvent.FlexibleSection(id, it) }

            EventTypes.EMPTY_DAY -> ItineraryEvent.EmptyDays(id, day, day)
            EventTypes.EMPTY_DATE_RANGE -> ItineraryEvent.EmptyDays(
                id = id,
                start = day,
                end = LocalDate.parse(event.endDate ?: error("endDate is required")),
            )

            else -> null
        }
    }

    private fun <T> entity(ref: FirebaseData.EntityRef?, type: String, entities: Map<String, T>): T? =
        ref?.takeIf { it.type == type }?.let { entities[it.id] }

    private fun segment(ref: FirebaseData.EntityRef?) = entity(ref, EntityTypes.FLIGHT, flights)?.let { flight ->
        flight.segments.getOrNull(ref?.segmentIndex ?: 0)?.let { flight to it }
    }
}
