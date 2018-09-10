package com.combah.travel2.model.data

data class Flight(
    val id: String = "",
    val segments: List<FlightSegment> = emptyList(),
    val price: Double? = null
)