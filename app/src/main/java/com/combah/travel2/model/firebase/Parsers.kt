package com.combah.travel2.model.firebase

import com.combah.travel2.extensions.Time
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.data.Trip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun FirebaseData.Trip.toAppDataModel(): Trip {
    val appFlights = flights.map { it.toAppDataModel() }
    val appLodgings = lodgings.map { it.toAppDataModel() }
    val image = coverImage ?: appLodgings.firstOrNull()?.city?.coverImage
    ?: appFlights.firstOrNull()?.segments?.firstOrNull()?.airportTo?.city?.coverImage
    return Trip(
        id = id,
        name = name,
        coverImage = image,
        flights = appFlights,
        lodgings = appLodgings,
        places = places.map { it.toAppDataModel() },
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
    timeZone = (timezone ?: city?.timeZone)?.let { TimeZone.getTimeZone(it) },
    city = this.city?.toAppDataModel() ?: city?.toAppDataModel() ?: error("city is required"),
)

fun FirebaseData.Lodging.toAppDataModel() = Lodging(
    name = name,
    address = address ?: error("address is required"),
    city = city?.toAppDataModel() ?: error("city is required"),
    checkIn = checkIn?.toTime()
        ?: error("checkin is required"),
    checkout = checkout?.toTime()
        ?: error("checkout is required"),
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
    timezone = timeZone?.id,
    city = city.toFirebaseDataModel(),
)

fun Lodging.toFirebaseDataModel() = FirebaseData.Lodging(
    name = name,
    address = address,
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

fun String.toTime(): Time = Time(this)

fun Time.toFirebaseDataModel() =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm Z", Locale.getDefault()).also {
        it.timeZone = timeZone
    }.format(Date(timeInMillis))
