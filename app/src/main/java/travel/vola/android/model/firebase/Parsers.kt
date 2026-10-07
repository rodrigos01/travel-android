package travel.vola.android.model.firebase

import travel.vola.android.extensions.asISO8601String
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.AnsweredQuestion
import travel.vola.android.model.data.BasicInformation
import travel.vola.android.model.data.EntityEventType
import travel.vola.android.model.data.EntityRef
import travel.vola.android.model.data.EntityType
import travel.vola.android.model.data.FlexibleDayCategory
import travel.vola.android.model.data.FlexibleDayItem
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.data.GroupType
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
import travel.vola.android.model.data.TripParameters
import travel.vola.android.model.data.TripPreferences
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.TimeZone

fun FirebaseData.Trip.toAppDataModel(): Trip {
    val appFlights = flights.map { it.toAppDataModel() }
    val appLodgings = lodgings.map { it.toAppDataModel() }
    val appPlaces = places.map { it.toAppDataModel() }
    val appRestaurants = restaurants.map { it.toAppDataModel() }
    val image =
        coverImage ?: appLodgings.firstOrNull()?.city?.coverImage
            ?: appFlights.firstOrNull()?.segments?.firstOrNull()?.airportTo?.city?.coverImage
            ?: appPlaces.firstOrNull()?.city?.coverImage
            ?: appRestaurants.firstOrNull()?.city?.coverImage
    return Trip(
        id = id,
        name = name,
        coverImage = image,
        preferences = preferences?.toAppDataModel(),
        flights = appFlights,
        lodgings = appLodgings,
        places = appPlaces,
        restaurants = appRestaurants,
        flexibleSections = flexibleSections.map { it.toAppDataModel() },
        // Derived data: one the app can't read is the same as none, and must
        // not take the trip's real contents down with it.
        itinerary = itinerary?.let { runCatching { it.toAppDataModel() }.getOrNull() },
    )
}

fun FirebaseData.Flight.toAppDataModel() = Flight(
    id = id,
    segments = segments.map { it.toAppDataModel() },
    price = price,
)

fun FirebaseData.FlightSegment.toAppDataModel() = FlightSegment(
    airportFrom = airportFrom?.toAppDataModel(cityFrom) ?: error("airportFrom is required"),
    departure = departure.toTime(),
    airportTo = airportTo?.toAppDataModel(cityTo) ?: error("address is required"),
    arrival = arrival.toTime(),
)

fun FirebaseData.Airport.toAppDataModel(city: FirebaseData.Place? = null) = Airport(
    iata = iata ?: error("iata is required"),
    name = name ?: error("name is required"),
    timeZone = (timezone ?: city?.timeZone)?.let { TimeZone.getTimeZone(it) }
        ?: TimeZone.getDefault(),
    city = this.city?.toAppDataModel() ?: city?.toAppDataModel() ?: error("city is required"),
)

fun FirebaseData.Lodging.toAppDataModel() = Lodging(
    id = id,
    name = name,
    address = address ?: error("address is required"),
    latitude = latitude,
    longitude = longitude,
    city = city?.toAppDataModel() ?: error("city is required"),
    checkIn = checkIn?.toTime() ?: error("checkin is required"),
    checkout = checkout?.toTime() ?: error("checkout is required"),
)

fun FirebaseData.Place.toAppDataModel() = Place(
    id = externalId,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    timeZone = TimeZone.getTimeZone(timeZone),
    coverImage = coverImage,
    externalId = externalId,
    source = source,
)

fun FirebaseData.TimedPlace.toAppDataModel() = TimedPlace(
    id = id,
    place = place.toAppDataModel(),
    startDateTime = time?.toTime() ?: error("time is required"),
    hasStartTime = hasTime,
    endDateTime = endTime?.toTime(),
    hasEndTime = hasEndTime,
    city = city?.toAppDataModel() ?: error("city is required"),
)

fun FirebaseData.RestaurantReservation.toAppDataModel() = RestaurantReservation(
    id = id,
    dateTime = time?.toTime() ?: error("time is required"),
    place = place.toAppDataModel(),
    city = city?.toAppDataModel() ?: error("city is required"),
)

fun FirebaseData.TripPreferences.toAppDataModel() = TripPreferences(
    basicInformation = BasicInformation(
        groupType = basicInformation.groupType.toAppDataModel(),
        travelers = basicInformation.travelers,
    ),
    initialParameters = TripParameters(
        occasions = initialParameters.occasions,
        interests = initialParameters.interests,
        vibe = initialParameters.vibe,
        focus = initialParameters.focus,
        mustHave = initialParameters.mustHave,
        duration = initialParameters.duration,
        anythingElse = initialParameters.anythingElse,
    ),
    questionsAnswers = questionsAnswers.map { AnsweredQuestion(it.question, it.answer) },
)

fun FirebaseData.GroupType.toAppDataModel() = when (this) {
    FirebaseData.GroupType.SOLO -> GroupType.SOLO
    FirebaseData.GroupType.FAMILY -> GroupType.FAMILY
    FirebaseData.GroupType.FRIENDS -> GroupType.FRIENDS
    FirebaseData.GroupType.COWORKERS -> GroupType.COWORKERS
    FirebaseData.GroupType.COUPLE -> GroupType.COUPLE
}

fun FirebaseData.FlexibleDaySection.toAppDataModel(): FlexibleDaySection {
    return FlexibleDaySection(
        id = id,
        name = name,
        date = date.toTime(),
        city = city.toAppDataModel(),
        categories = categories.map { category ->
            FlexibleDayCategory(
                name = category.name,
                items = category.items.map {
                    FlexibleDayItem(
                        id = it.id,
                        place = it.place.toAppDataModel(),
                        note = it.note,
                    )
                },
            )
        },
    )
}

fun FirebaseData.Itinerary.toAppDataModel() = Itinerary(
    version = version,
    legs = legs.map { it.toAppDataModel() },
)

fun FirebaseData.ItineraryLeg.toAppDataModel() = ItineraryLeg(
    id = id,
    type = when (type) {
        "place" -> LegType.PLACE
        "transit" -> LegType.TRANSIT
        else -> LegType.UNKNOWN
    },
    title = title,
    startDate = LocalDate.parse(startDate),
    endDate = LocalDate.parse(endDate),
    thumbnailUrl = thumbnailUrl,
    place = place?.toAppDataModel(),
    entityRef = entityRef?.toAppDataModel(),
    days = days.map { it.toAppDataModel() },
)

fun FirebaseData.ItineraryDay.toAppDataModel(): ItineraryDay {
    val day = LocalDate.parse(date)
    // An event type this version of the app doesn't know is left out; the
    // rest of the day is still shown.
    return ItineraryDay(day, events.mapNotNull { it.toAppDataModel(day) })
}

private fun FirebaseData.ItineraryEvent.toAppDataModel(day: LocalDate): ItineraryEvent? {
    val entityType = when (type) {
        "flightDeparture" -> EntityEventType.FLIGHT_DEPARTURE
        "flightArrival" -> EntityEventType.FLIGHT_ARRIVAL
        "lodgingCheckIn" -> EntityEventType.LODGING_CHECK_IN
        "lodgingCheckOut" -> EntityEventType.LODGING_CHECK_OUT
        "timedPlace" -> EntityEventType.TIMED_PLACE
        "restaurant" -> EntityEventType.RESTAURANT
        "flexibleSection" -> EntityEventType.FLEXIBLE_SECTION
        else -> null
    }
    return when {
        entityType != null -> ItineraryEvent.OfEntity(
            id = id,
            type = entityType,
            entityRef = entityRef?.toAppDataModel() ?: error("entityRef is required"),
            timestamp = timestamp?.toTime() ?: error("timestamp is required"),
        )

        type == "emptyDay" -> ItineraryEvent.EmptyDay(id, day)
        type == "emptyDateRange" -> ItineraryEvent.EmptyDateRange(
            id = id,
            start = day,
            end = LocalDate.parse(endDate ?: error("endDate is required")),
        )

        else -> null
    }
}

fun FirebaseData.EntityRef.toAppDataModel() = EntityRef(
    type = when (type) {
        "flight" -> EntityType.FLIGHT
        "lodging" -> EntityType.LODGING
        "place" -> EntityType.PLACE
        "restaurant" -> EntityType.RESTAURANT
        "flexibleSection" -> EntityType.FLEXIBLE_SECTION
        else -> error("Unknown entity type $type")
    },
    id = id,
    segmentIndex = segmentIndex,
)

fun String.toTime(): ZonedDateTime = zonedDateTime(this)

fun ZonedDateTime.toFirebaseDataModel() = this.asISO8601String()
