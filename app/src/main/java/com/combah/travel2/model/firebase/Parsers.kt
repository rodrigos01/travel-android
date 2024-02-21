package com.combah.travel2.model.firebase

import com.combah.travel2.extensions.Time
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Flight
import com.combah.travel2.model.data.FlightSegment
import com.combah.travel2.model.data.Lodging
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.data.Trip

fun FirebaseData.Trip.toAppDataModel() = Trip(
    id = id,
    name = name,
    coverImage = coverImage,
    flights = flights.map { it.toAppDataModel() },
    lodgings = lodgings.map { it.toAppDataModel() },
    places = places.map { it.toAppDataModel() },
)

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
