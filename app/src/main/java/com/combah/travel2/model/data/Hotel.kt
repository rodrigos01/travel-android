package com.combah.travel2.model.data

import java.util.*

data class Hotel(
    val name: String = "",
    val place: Place = Place(),
    val checkin: Date = Date(),
    val checkout: Date = Date()
)