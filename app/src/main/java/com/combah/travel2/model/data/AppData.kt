package com.combah.travel2.model.data

data class Trip(
    val id: String,
    val name: String?,
    val coverImage: String?,
    val flights: List<Flight>,
    val lodgings: List<Lodging>,
    val places: List<Place>,
)

sealed interface TripEntity

data class Flight(
    val id: String,
    val segments: List<FlightSegment>,
    val price: Double?,
) : TripEntity

data class FlightSegment(
    val airportFrom: Airport,
    val departure: Time,
    val airportTo: Airport,
    val arrival: Time,
)

data class Airport(
    val iata: String,
    val name: String,
    val city: Place,
)

data class Lodging(
    val name: String?,
    val address: String,
    val city: Place,
    val checkIn: Time,
    val checkout: Time,
) : TripEntity

data class Place(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val coverImage: String?,
    val externalId: String,
    val source: String,
)