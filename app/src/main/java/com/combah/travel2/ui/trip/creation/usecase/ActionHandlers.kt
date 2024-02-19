package com.combah.travel2.ui.trip.creation.usecase

import com.combah.travel2.model.data.Time

interface AddPlanItemActionHandler : AddLodgingItemActionHandler, AddFlightItemActionHandler

interface AddLodgingItemActionHandler {
    fun setCheckInTime(itemId: String, hour: Int, minute: Int)
    fun setCheckOutDate(itemId: String, date: Time)
    fun setCheckoutTime(itemId: String, hour: Int, minute: Int)

    suspend fun lodgingTextChanged(itemId: String, content: CharSequence)

    fun lodgingSearchResultTapped(itemId: String, index: Int)
}

interface AddFlightItemActionHandler {
    fun setDepartureTime(itemId: String, hour: Int, minute: Int)
    fun setArrivalDate(itemId: String, date: Time)
    fun setArrivalTime(itemId: String, hour: Int, minute: Int)

    suspend fun airportFromSearchTextChanged(
        itemId: String, content: CharSequence
    )

    fun airportFromSearchResultTapped(itemId: String, index: Int)

    suspend fun airportToSearchTextChanged(
        itemId: String, content: CharSequence
    )

    fun airportToSearchResultTapped(itemId: String, index: Int)
}
