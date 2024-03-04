package com.combah.travel2.model.data

import java.util.TimeZone

data class Trip(
    val id: String,
    val name: String?,
    val coverImage: String?,
    val flights: List<Flight>,
    val lodgings: List<Lodging>,
    val places: List<Place>,
)

sealed interface TripEntity

sealed interface TripEvent

data class Flight(
    val id: String,
    val segments: List<FlightSegment>,
    val price: Double? = null,
) : TripEntity

data class FlightSegment(
    val airportFrom: Airport,
    val departure: Time,
    val airportTo: Airport,
    val arrival: Time,
) : TripEvent

data class Airport(
    val iata: String,
    val name: String,
    val timeZone: TimeZone?,
    val city: Place,
)

data class Lodging(
    val name: String?,
    val address: String,
    val city: Place,
    val checkIn: Time,
    val checkout: Time,
) : TripEntity, TripEvent

data class Place(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val coverImage: String?,
    val externalId: String,
    val source: String,
) {
    override fun equals(other: Any?): Boolean = other is Place && other.id == this.id
    override fun hashCode(): Int {
        return super.hashCode()
    }
}

data class AirportSearchResult(
    val iata: String,
    val name: String,
    val timeZone: TimeZone,
    val city: Place,
)

data class SimplePlace(
    val name: String?,
    val address: String,
    val city: Place,
)
