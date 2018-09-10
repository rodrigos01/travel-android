package com.combah.travel2.model.data

import java.util.*

data class FlightSegment(
    val id: String = "",
    val airportFrom: Airport = Airport(),
    val cityFrom: Place = Place(),
    val departure: Date = Date(),
    val airportTo: Airport = Airport(),
    val cityTo: Place = Place(),
    val arrival: Date = Date()
)