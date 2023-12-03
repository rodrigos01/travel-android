package com.combah.travel2.model.data

data class Lodging(
    val name: String? = null,
    val address: String,
    val city: Place,
    val checkIn: Time,
    val checkout: Time,
)