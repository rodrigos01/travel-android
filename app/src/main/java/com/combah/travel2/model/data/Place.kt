package com.combah.travel2.model.data

data class Place(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val coverImage: String? = null,
    val externalId: String = "",
    val source: String = ""
)