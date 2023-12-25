package com.combah.travel2.model.data

data class Trip(
    val id: String,
    val name: String?,
    val coverImage: String?,
    val flights: List<Flight>,
    val lodgings: List<Lodging>,
    val places: List<Place>,
)