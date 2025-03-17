package com.combah.travel2.ui.lodgingsearch.state

import com.combah.travel2.model.data.Time

data class LodgingSearchResultState(
    val id: String,
    val name: String,
    val coverImage: String,
    val address: String,
    val rating: Double,
    val lodgingType: String,
    val price: Double,
)

data class LodgingDetailsState(
    val name: String,
    val rating: Double,
    val reviewCountText: String,
    val lodgingType: String,
    val photos: List<String>,
    val checkIn: Time,
    val checkOut: Time,
    val price: Double,
    val rooms: List<LodgingRoomOfferState>,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isLoading: Boolean,
)

data class LodgingRoomOfferState(
    val photos: List<String>,
    val description: String,
    val breakfastIncluded: Boolean,
    val refundable: Boolean,
    val prePaymentRequired: Boolean,
    val isAllInclusive: Boolean,
    val price: Double,
    val bookingUrl: String,
    val bookingAgency: String,
)