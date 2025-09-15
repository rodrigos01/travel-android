package travel.vola.android.model.firebase

import travel.vola.android.extensions.Time
import travel.vola.android.extensions.asISO8601String
import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.FlightSegment
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.Time
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import java.util.TimeZone

fun FirebaseData.Trip.toAppDataModel(): Trip {
    val appFlights = flights.map { it.toAppDataModel() }
    val appLodgings = lodgings.map { it.toAppDataModel() }
    val image =
        coverImage ?: appLodgings.firstOrNull()?.city?.coverImage
        ?: appFlights.firstOrNull()?.segments?.firstOrNull()?.airportTo?.city?.coverImage
    return Trip(
        id = id,
        name = name,
        coverImage = image,
        flights = appFlights,
        lodgings = appLodgings,
        places = places.map { it.toAppDataModel() },
        restaurants = restaurants.map { it.toAppDataModel() },
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

fun Flight.toFirebaseDataModel() = FirebaseData.Flight(
    id = id,
    segments = segments.map { it.toFirebaseDataModel() },
    price = price,
)

fun FlightSegment.toFirebaseDataModel() = FirebaseData.FlightSegment(
    airportFrom = airportFrom.toFirebaseDataModel(),
    departure = departure.toFirebaseDataModel(),
    airportTo = airportTo.toFirebaseDataModel(),
    arrival = arrival.toFirebaseDataModel(),
)

fun Airport.toFirebaseDataModel() = FirebaseData.Airport(
    iata = iata,
    name = name,
    timezone = timeZone.id,
    city = city.toFirebaseDataModel(),
)

fun Lodging.toFirebaseDataModel() = FirebaseData.Lodging(
    id = id,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    city = city.toFirebaseDataModel(),
    checkIn = checkIn.toFirebaseDataModel(),
    checkout = checkout.toFirebaseDataModel(),
)

fun Place.toFirebaseDataModel() = FirebaseData.Place(
    id = id,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    coverImage = coverImage,
    externalId = externalId,
    source = source,
)

fun TimedPlace.toFirebaseDataModel() = FirebaseData.TimedPlace(
    id = id,
    place = place.toFirebaseDataModel(),
    time = startDateTime.toFirebaseDataModel(),
    hasTime = hasStartTime,
    endTime = endDateTime?.toFirebaseDataModel(),
    hasEndTime = hasEndTime,
    city = city.toFirebaseDataModel(),
)

fun RestaurantReservation.toFirebaseDataModel() = FirebaseData.RestaurantReservation(
    id = id,
    time = dateTime.toFirebaseDataModel(),
    place = place.toFirebaseDataModel(),
    city = city.toFirebaseDataModel(),
)

fun String.toTime(): Time = Time(this)

fun Time.toFirebaseDataModel() = this.asISO8601String()
