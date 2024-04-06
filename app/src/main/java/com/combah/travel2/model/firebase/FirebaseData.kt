package com.combah.travel2.model.firebase

import com.google.firebase.firestore.Exclude

sealed interface FirebaseData {
    data class Trip(
        @Exclude
        val id: String = "",
        val name: String? = null,
        val coverImage: String? = null,
        val flights: List<Flight> = emptyList(),
        val lodgings: List<Lodging> = emptyList(),
        val places: List<Place> = emptyList(),
    ) : FirebaseData

    data class Flight(
        val id: String = "",
        val segments: List<FlightSegment> = emptyList(),
        val price: Double? = null
    ) : FirebaseData

    data class FlightSegment(
        val airportFrom: Airport? = null,
        val departure: String = "",
        val airportTo: Airport? = null,
        val arrival: String = "",
        val cityFrom: Place? = null,
        val cityTo: Place? = null,
    ) : FirebaseData

    data class Airport(
        val iata: String? = null,
        val name: String? = null,
        val timezone: String? = null,
        val city: Place? = null,
    )

    data class Lodging(
        val name: String? = null,
        val address: String? = null,
        val city: Place? = null,
        val checkIn: String? = null,
        val checkout: String? = null,
    ) : FirebaseData

    data class Place(
        val id: String = "",
        val name: String = "",
        val address: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val timeZone: String = "",
        val coverImage: String? = null,
        val externalId: String = "",
        val source: String = "",
    )
}
