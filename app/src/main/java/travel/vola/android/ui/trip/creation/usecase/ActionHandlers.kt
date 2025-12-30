package travel.vola.android.ui.trip.creation.usecase

import travel.vola.android.ui.trip.state.AddPlanItemState
import java.time.ZonedDateTime

interface AddPlanItemActionHandler : AddLodgingItemActionHandler, AddFlightItemActionHandler,
    AddPlaceItemActionHandler, AddRestaurantItemActionHandler, AddFlexibleSectionItemActionHandler {
    fun addPlanTypeChanged(itemId: String, newType: AddPlanItemState.Type)
    fun save(itemId: String)
    fun cancelEdit(itemId: String)
    fun delete(type: AddPlanItemState.Type, itemId: String)
}

interface AddLodgingItemActionHandlerBase {
    fun lodgingTextChanged(itemId: String, content: CharSequence)
    fun onLodgingUpdated(
        itemId: String,
        checkIn: ZonedDateTime,
        checkInTimeSelected: Boolean,
        checkOut: ZonedDateTime?,
        checkOutTimeSelected: Boolean,
        selectedSearchResultIndex: Int,
    )
}

interface AddLodgingItemActionHandler : AddLodgingItemActionHandlerBase {
    fun onSwitchToManualButtonTapped(itemId: String)
    fun onFindLodgingButtonTapped(itemId: String)
}

interface ManualAddPlanItemActionHandler : AddLodgingItemActionHandlerBase

interface LodgingSearchItemActionHandler : AddLodgingItemActionHandlerBase

interface AddPlaceItemActionHandler {
    fun locationTextChanged(itemId: String, content: CharSequence)
    fun onUpdated(
        itemId: String,
        startDateTime: ZonedDateTime?,
        startTimeSelected: Boolean,
        endDateTime: ZonedDateTime?,
        endTimeSelected: Boolean,
        selectedSearchResultIndex: Int,
    )
}

interface AddRestaurantItemActionHandler {
    fun restaurantTextChanged(itemId: String, content: CharSequence)
    fun onUpdated(
        itemId: String,
        dateTime: ZonedDateTime?,
        timeSelected: Boolean,
        selectedSearchResultIndex: Int,
    )
}

interface AddFlightItemActionHandler {
    fun airportFromSearchTextChanged(
        itemId: String, content: CharSequence,
    )

    fun airportToSearchTextChanged(
        itemId: String, content: CharSequence,
    )

    fun onUpdated(
        itemId: String,
        departureTime: ZonedDateTime,
        departureTimeSelected: Boolean,
        selectedDepartureSearchResultIndex: Int,
        arrivalTime: ZonedDateTime?,
        arrivalTimeSelected: Boolean,
        selectedArrivalSearchResultIndex: Int,
    )
}

interface AddFlexibleSectionItemActionHandler {
    fun sectionNameChanged(itemId: String, content: CharSequence)
    fun onFlexibleCategoryAdded(itemId: String, category: String)
    fun onFlexibleItemSearchTextChanged(itemId: String, content: CharSequence)
    fun onFlexibleItemSearchResultSelected(itemId: String, index: Int, categoryIndex: Int)
    fun onFlexibleItemDateTimeUpdated(
        itemId: String,
        dateTime: ZonedDateTime,
        timeSelected: Boolean,
    )
}
