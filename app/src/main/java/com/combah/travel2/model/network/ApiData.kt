package com.combah.travel2.model.network

import kotlinx.serialization.Serializable

interface ApiResponse {
    @Serializable
    data class AirportAutoComplete(
        val data: List<ApiData.Airport>,
    )

    @Serializable
    data class PlaceAutoComplete(
        val data: List<ApiData.SimplePlace>
    )
}

interface ApiData {

    @Serializable
    data class Airport(
        val iata: String,
        val name: String,
        val timeZone: String?,
        val city: Place,
    )

    @Serializable
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

    @Serializable
    data class SimplePlace(
        val name: String?,
        val address: String,
        val city: Place
    )
}
