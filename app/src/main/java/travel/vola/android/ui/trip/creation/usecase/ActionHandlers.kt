package travel.vola.android.ui.trip.creation.usecase

import com.vola.android.model.data.Time
import com.vola.android.ui.trip.state.AddPlanItemState

interface AddPlanItemActionHandler : AddLodgingItemActionHandler,
    AddFlightItemActionHandler {
    fun addPlanTypeChanged(itemId: String, newType: AddPlanItemState.Type)
    fun save(itemId: String)
    fun cancelEdit(itemId: String)
    fun delete(type: AddPlanItemState.Type, itemId: String)
}

interface AddLodgingItemActionHandler : AddLodgingItemActionHandlerBase {
    fun onSwitchToManualButtonTapped(itemId: String)
}

interface ManualAddPlanItemActionHandler : AddLodgingItemActionHandlerBase

interface LodgingSearchItemActionHandler : AddLodgingItemActionHandlerBase

interface AddLodgingItemActionHandlerBase {
    fun setCheckInTime(itemId: String, time: Time)
    fun setCheckOutTime(itemId: String, time: Time)
    fun locationTextChanged(itemId: String, content: CharSequence)
    fun locationSearchResultTapped(itemId: String, index: Int)
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
