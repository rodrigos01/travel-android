package travel.vola.android.ui.trip.creation.usecase

import travel.vola.android.model.data.Time
import travel.vola.android.ui.trip.state.AddPlanItemState
import java.time.ZonedDateTime

interface AddPlanItemActionHandler : AddLodgingItemActionHandler, AddFlightItemActionHandler,
    AddPlaceItemActionHandler {
    fun addPlanTypeChanged(itemId: String, newType: AddPlanItemState.Type)
    fun save(itemId: String)
    fun cancelEdit(itemId: String)
    fun delete(type: AddPlanItemState.Type, itemId: String)
}

interface AddLodgingItemActionHandlerBase {
    fun setCheckInTime(itemId: String, time: Time)
    fun setCheckOutTime(itemId: String, time: Time)
    fun lodgingTextChanged(itemId: String, content: CharSequence)
    fun lodgingSearchResultTapped(itemId: String, index: Int)
}

interface AddLodgingItemActionHandler : AddLodgingItemActionHandlerBase {
    fun onSwitchToManualButtonTapped(itemId: String)
    fun onFindLodgingButtonTapped(itemId: String)
}

interface ManualAddPlanItemActionHandler : AddLodgingItemActionHandlerBase

interface LodgingSearchItemActionHandler : AddLodgingItemActionHandlerBase

interface AddPlaceItemActionHandler {
    fun setPlaceStartDateTime(itemId: String, dateTime: ZonedDateTime, timeSelected: Boolean)
    fun setPlaceEndDateTime(itemId: String, dateTime: ZonedDateTime?, timeSelected: Boolean)
    fun locationTextChanged(itemId: String, content: CharSequence)
    fun locationSearchResultTapped(itemId: String, index: Int)
}

interface AddFlightItemActionHandler {
    fun setDepartureTime(itemId: String, time: Time)
    fun setArrivalTime(itemId: String, time: Time)

    fun airportFromSearchTextChanged(
        itemId: String, content: CharSequence,
    )

    fun airportFromSearchResultTapped(itemId: String, index: Int)

    fun airportToSearchTextChanged(
        itemId: String, content: CharSequence,
    )

    fun airportToSearchResultTapped(itemId: String, index: Int)
}
