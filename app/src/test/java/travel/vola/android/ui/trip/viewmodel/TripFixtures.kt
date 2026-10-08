package travel.vola.android.ui.trip.viewmodel

import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.data.Itinerary
import travel.vola.android.model.data.ItineraryDay
import travel.vola.android.model.data.ItineraryEvent
import travel.vola.android.model.data.ItineraryLeg
import travel.vola.android.model.data.LegType
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.ui.trip.state.TripItemState
import java.time.LocalDate
import java.util.TimeZone

/**
 * A trip and the itinerary the backend would build for it, written down by hand: say which legs
 * and days there are and which entity each event is. The entities are added to the trip as they
 * are mentioned. What the backend decides - which events go in which leg - is what the test is
 * given, not what it checks.
 *
 *     val trip = trip {
 *         leg(porto, "2024-05-11", "2024-05-21") {
 *             day("2024-05-11") { arrival(flight); checkIn(hotel) }
 *             emptyRange("2024-05-12", "2024-05-18")
 *         }
 *     }
 */
fun trip(id: String = "tripId", block: TripBuilder.() -> Unit): Trip = TripBuilder(id).apply(block).build()

class TripBuilder(private val id: String) {
    internal val flights = linkedMapOf<String, Flight>()
    internal val lodgings = linkedMapOf<String, Lodging>()
    internal val places = linkedMapOf<String, TimedPlace>()
    internal val restaurants = linkedMapOf<String, RestaurantReservation>()
    internal val sections = linkedMapOf<String, FlexibleDaySection>()
    private val legs = mutableListOf<ItineraryLeg>()

    fun leg(
        place: Place?,
        start: String,
        end: String,
        type: LegType = LegType.PLACE,
        startedBy: TimedPlace? = null,
        thumbnailUrl: String? = null,
        block: LegBuilder.() -> Unit = {},
    ) {
        startedBy?.let { places[it.id] = it }
        val builder = LegBuilder(this).apply(block)
        legs += ItineraryLeg(
            id = "${place?.id ?: "none"}_$start",
            type = type,
            title = place?.name ?: "",
            startDate = LocalDate.parse(start),
            endDate = LocalDate.parse(end),
            thumbnailUrl = thumbnailUrl,
            place = place,
            startedBy = startedBy,
            days = builder.days,
        )
    }

    fun transit(place: Place?, start: String, end: String = start, block: LegBuilder.() -> Unit) =
        leg(place, start, end, type = LegType.TRANSIT, block = block)

    fun build() = Trip(
        id = id,
        name = null,
        coverImage = null,
        preferences = null,
        flights = flights.values.toList(),
        lodgings = lodgings.values.toList(),
        places = places.values.toList(),
        restaurants = restaurants.values.toList(),
        flexibleSections = sections.values.toList(),
        itinerary = Itinerary(legs),
    )
}

class LegBuilder(private val trip: TripBuilder) {
    internal val days = mutableListOf<ItineraryDay>()

    fun day(date: String, block: DayBuilder.() -> Unit) {
        days += ItineraryDay(LocalDate.parse(date), DayBuilder(trip).apply(block).events)
    }

    fun emptyDay(date: String) = day(date) { emptyDay(date) }

    fun emptyRange(start: String, end: String) = day(start) { emptyRange(start, end) }
}

class DayBuilder(private val trip: TripBuilder) {
    internal val events = mutableListOf<ItineraryEvent>()

    fun departure(flight: Flight, segment: Int = 0) {
        trip.flights[flight.id] = flight
        events += ItineraryEvent.FlightDeparture("${flight.id}:$segment:departure", flight, flight.segments[segment])
    }

    fun arrival(flight: Flight, segment: Int = 0) {
        trip.flights[flight.id] = flight
        events += ItineraryEvent.FlightArrival("${flight.id}:$segment:arrival", flight, flight.segments[segment])
    }

    fun checkIn(lodging: Lodging) {
        trip.lodgings[lodging.id] = lodging
        events += ItineraryEvent.LodgingCheckIn("${lodging.id}:checkIn", lodging)
    }

    fun checkOut(lodging: Lodging) {
        trip.lodgings[lodging.id] = lodging
        events += ItineraryEvent.LodgingCheckOut("${lodging.id}:checkOut", lodging)
    }

    fun timedPlace(place: TimedPlace) {
        trip.places[place.id] = place
        events += ItineraryEvent.TimedPlaceVisit(place.id, place)
    }

    fun restaurant(restaurant: RestaurantReservation) {
        trip.restaurants[restaurant.id] = restaurant
        events += ItineraryEvent.Restaurant(restaurant.id, restaurant)
    }

    fun flexibleSection(section: FlexibleDaySection) {
        trip.sections[section.id] = section
        events += ItineraryEvent.FlexibleSection(section.id, section)
    }

    fun emptyDay(date: String) {
        events += ItineraryEvent.EmptyDays("empty_$date", LocalDate.parse(date), LocalDate.parse(date))
    }

    fun emptyRange(start: String, end: String) {
        events += ItineraryEvent.EmptyDays("empty_${start}_$end", LocalDate.parse(start), LocalDate.parse(end))
    }
}

// ---- Entities ----

fun place(name: String, coverImage: String? = null) = Place(
    id = name,
    name = name,
    address = "$name address",
    latitude = 0.0,
    longitude = 0.0,
    coverImage = coverImage,
    externalId = name,
    timeZone = TimeZone.getTimeZone("UTC"),
    source = "",
)

fun flight(
    id: String,
    from: Place,
    departure: String,
    to: Place,
    arrival: String,
    fromAirport: String = "${from.name} Airport",
    toAirport: String = "${to.name} Airport",
) = Flight(
    id = id,
    segments = listOf(
        FlightSegment(
            airportFrom = Airport(fromAirport.take(3).uppercase(), fromAirport, TimeZone.getTimeZone("UTC"), from),
            departure = zonedDateTime(departure),
            airportTo = Airport(toAirport.take(3).uppercase(), toAirport, TimeZone.getTimeZone("UTC"), to),
            arrival = zonedDateTime(arrival),
        ),
    ),
)

fun lodging(id: String, city: Place, checkIn: String, checkout: String, name: String? = "Hotel ${city.name}") = Lodging(
    id = id,
    name = name,
    address = "${city.name} hotel address",
    latitude = 0.0,
    longitude = 0.0,
    city = city,
    checkIn = zonedDateTime(checkIn),
    checkout = zonedDateTime(checkout),
)

fun timedPlace(
    id: String,
    place: Place,
    city: Place,
    start: String,
    end: String? = null,
    hasStartTime: Boolean = true,
) = TimedPlace(
    id = id,
    startDateTime = zonedDateTime(start),
    hasStartTime = hasStartTime,
    endDateTime = end?.let { zonedDateTime(it) },
    hasEndTime = end != null,
    place = place,
    city = city,
)

fun restaurant(id: String, place: Place, city: Place, time: String) = RestaurantReservation(
    id = id,
    dateTime = zonedDateTime(time),
    place = place,
    city = city,
)

fun flexibleSection(id: String, city: Place, date: String, name: String = "Day in ${city.name}") = FlexibleDaySection(
    id = id,
    name = name,
    date = zonedDateTime(date),
    categories = emptyList(),
    city = city,
)

// ---- Reading rows ----

/** One short line per row, to compare a screen's worth of rows at a glance. */
fun List<TripItemState>.labels() = map { it.label() }

fun TripItemState.label(): String = when (this) {
    is TripItemState.PlaceItemState -> "place $placeName $dateStart-$dateEnd"
    is TripItemState.MonthItemState -> "month $month $year"
    is TripItemState.FlightDepartureItemState -> "departure $airport${styled()}"
    is TripItemState.FlightArrivalItemState -> "arrival $airport${styled()}"
    is TripItemState.HotelCheckInItemState -> "check-in $hotelName${styled()}"
    is TripItemState.HotelCheckOutItemState -> "check-out $hotelName${styled()}"
    is TripItemState.TimedPlaceItemState -> "place-visit $placeName${styled()}"
    is TripItemState.RestaurantReservationItemState -> "restaurant $restaurantName${styled()}"
    is TripItemState.FlexibleDaySectionState -> "section $name${styled()}"
    is TripItemState.SuggestionPlaceholderItemState -> "placeholder${styled()}"
    is TripItemState.DateRangeItemState -> "range $dayOfMonthStart-$dayOfMonthEnd"
    is TripItemState.EmptyDateItemState -> "empty $dayOfMonth"
    is TripItemState.InitialAddPlanItemState -> "initial"
    else -> error("unexpected $this")
}

private fun TripItemState.EventItemState.styled() = "${if (showDate) " +date" else ""} $backgroundStyle"
