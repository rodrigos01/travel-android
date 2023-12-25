package com.combah.travel2.model.firebase

import com.google.firebase.firestore.Exclude
import java.util.Date

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
        val airportFrom: Airport,
        val departure: Date,
        val airportTo: Airport,
        val arrival: Date,
    ) : FirebaseData

    data class Airport(
        val iata: String? = null,
        val name: String? = null,
        val city: Place? = null,
    )

    data class Lodging(
        val name: String? = null,
        val address: String? = null,
        val city: Place? = null,
        val checkIn: Date? = null,
        val checkout: Date? = null,
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
