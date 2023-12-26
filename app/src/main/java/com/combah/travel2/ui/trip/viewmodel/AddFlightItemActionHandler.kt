package com.combah.travel2.ui.trip.viewmodel

import com.combah.travel2.model.data.Time

interface AddFlightItemActionHandler {
    fun setDepartureTime(itemId: String, hour: Int, minute: Int): AddFlightUseCase.AddFlightItem
    fun setArrivalDay(itemId: String, day: Time): AddFlightUseCase.AddFlightItem
    fun setArrivalTime(itemId: String, hour: Int, minute: Int): AddFlightUseCase.AddFlightItem

    suspend fun airportFromSearchTextChanged(
        itemId: String, content: CharSequence
    ): AddFlightUseCase.AddFlightItem

    fun airportFromSearchResultTapped(itemId: String, index: Int): AddFlightUseCase.AddFlightItem

    suspend fun airportToSearchTextChanged(
        itemId: String, content: CharSequence
    ): AddFlightUseCase.AddFlightItem

    fun airportToSearchResultTapped(itemId: String, index: Int): AddFlightUseCase.AddFlightItem
}