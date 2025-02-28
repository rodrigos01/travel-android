package com.combah.travel2.ui.trip.creation.usecase

import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.trip.state.AddPlanItemState

interface AddPlanItemActionHandler : AddLodgingItemActionHandler,
    AddFlightItemActionHandler {
    fun addPlanTypeChanged(itemId: String, newType: AddPlanItemState.Type)
    fun save(itemId: String)
    fun cancelEdit(itemId: String)
    fun delete(type: AddPlanItemState.Type, itemId: String)
}

interface AddLodgingItemActionHandler {
    fun setCheckInTime(itemId: String, time: Time)
    fun setCheckOutTime(itemId: String, time: Time)

    fun lodgingTextChanged(itemId: String, content: CharSequence)

    fun lodgingSearchResultTapped(itemId: String, index: Int)
}

interface AddFlightItemActionHandler {
    fun setDepartureTime(itemId: String, time: Time)
    fun setArrivalTime(itemId: String, time: Time)

    fun airportFromSearchTextChanged(
        itemId: String, content: CharSequence
    )

    fun airportFromSearchResultTapped(itemId: String, index: Int)

    fun airportToSearchTextChanged(
        itemId: String, content: CharSequence
    )

    fun airportToSearchResultTapped(itemId: String, index: Int)
}
