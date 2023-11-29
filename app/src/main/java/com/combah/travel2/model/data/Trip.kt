package com.combah.travel2.model.data

import com.google.firebase.firestore.Exclude

data class Trip(
    @Exclude
    val id: String = "",
    val name: String? = null,
    val coverImage: String? = null,
    val flights: List<Flight> = emptyList(),
    val hotels: List<Hotel> = emptyList(),
    val places: List<Place> = emptyList(),
)