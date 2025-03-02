package com.combah.travel2.ui.lodgingsearch.state

data class LodgingSearchResultState(
    val id: String,
    val name: String,
    val coverImage: String,
    val address: String,
    val rating: Double,
    val lodgingType: String,
    val price: Double,
)