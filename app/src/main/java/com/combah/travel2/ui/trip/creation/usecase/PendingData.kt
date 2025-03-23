package com.combah.travel2.ui.trip.creation.usecase

import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.SimplePlace
import com.combah.travel2.model.data.Time

sealed interface PendingData {
    val id: String

    data class PendingFlight(
        override val id: String,
        val entityId: String? = null,
        val departure: Time,
        val airportFrom: Airport? = null,
        val airportTo: Airport? = null,
        val arrival: Time? = null,
        val airportFromSearchResults: List<Airport> = emptyList(),
        val airportToSearchResults: List<Airport> = emptyList(),
    ) : PendingData

    data class PendingLodging(
        override val id: String,
        val entityId: String? = null,
        val checkIn: Time,
        val checkOut: Time,
        val name: String? = null,
        val address: String? = null,
        val city: Place? = null,
        val searchResults: List<SimplePlace> = emptyList(),
    ) : PendingData

    data class LodgingSearchParams(
        override val id: String,
        val checkIn: Time,
        val checkOut: Time? = null,
        val city: Place? = null,
        val searchResults: List<Place> = emptyList(),
    ) : PendingData
}
