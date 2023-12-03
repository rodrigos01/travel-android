package com.combah.travel2.model.data

data class FlightSegment(
    val airportFrom: Airport,
    val departure: Time,
    val airportTo: Airport,
    val arrival: Time,
)