package com.combah.travel2.model.data

data class Trip(
    val id: String = "",
    val name: String? = "",
    val flights: List<Flight>? = null,
    val places: List<Place>? = null
)