package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.runtime.Composable
import travel.vola.android.ui.trip.creation.composable.AddPlanType
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemActionHandler
import travel.vola.android.ui.trip.state.AddFlexibleSectionItemState
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.AddPlaceItemState
import travel.vola.android.ui.trip.state.AddPlanItemState
import travel.vola.android.ui.trip.state.AddRestaurantItemState
import travel.vola.android.ui.trip.state.LodgingSearchItemState
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState

@Composable
fun AddPlanContent(
    state: AddPlanItemState,
    actionHandler: AddPlanItemActionHandler,
) {
    when (state) {
        is AddFlightItemState -> AddFlightListItem(
            uiState = state,
            onUpdated = actionHandler::onUpdated,
        )

        is ManualAddLodgingItemState -> AddLodgingListItem(
            uiState = state,
            onFindLodgingButtonTapped = actionHandler::onFindLodgingButtonTapped,
            onUpdated = actionHandler::onUpdated,
        )

        is LodgingSearchItemState -> LodgingSearchListItem(
            uiState = state,
            onSwitchToManualButtonTapped = actionHandler::onSwitchToManualButtonTapped,
            onUpdated = actionHandler::onUpdated,
        )

        is AddPlaceItemState -> AddPlaceListItem(
            uiState = state,
            onUpdated = actionHandler::onUpdated,
        )

        is AddRestaurantItemState -> AddRestaurantListItem(
            uiState = state,
            onUpdated = actionHandler::onUpdated,
        )

        is AddFlexibleSectionItemState -> AddFlexibleSectionListItem(
            state = state,
            onGenerateTapped = actionHandler::onGenerateSectionTapped,
            onUpdated = actionHandler::onUpdated,
        )
    }
}

fun AddPlanType.toState() = when (this) {
    AddPlanType.Flight -> AddPlanItemState.Type.Flight
    AddPlanType.Lodging -> AddPlanItemState.Type.Lodging
    AddPlanType.Place -> AddPlanItemState.Type.Place
    AddPlanType.Restaurant -> AddPlanItemState.Type.Restaurant
    AddPlanType.FlexibleSection -> AddPlanItemState.Type.FlexibleSection
}

val AddPlanItemState.uiType
    get() = when (this) {
        is AddFlightItemState -> AddPlanType.Flight
        is ManualAddLodgingItemState, is LodgingSearchItemState -> AddPlanType.Lodging
        is AddPlaceItemState -> AddPlanType.Place
        is AddRestaurantItemState -> AddPlanType.Restaurant
        is AddFlexibleSectionItemState -> AddPlanType.FlexibleSection
    }

object NoOpActionHandler : AddPlanItemActionHandler {
    override fun onUpdated(state: AddPlanItemState) = Unit
    override fun onSwitchToManualButtonTapped() = Unit
    override fun onFindLodgingButtonTapped() = Unit
    override fun onGenerateSectionTapped() = Unit
    override fun save() = Unit
    override fun cancelEdit() = Unit
    override fun delete(type: AddPlanItemState.Type, itemId: String) = Unit
    override fun onFlexibleCategoryAdded(itemId: String, category: String) = Unit
    override fun onFlexibleItemSearchTextChanged(itemId: String, content: CharSequence) = Unit
    override fun onFlexibleItemSearchResultSelected(itemId: String, resultId: String, categoryIndex: Int) = Unit
    override fun onFlexibleItemNoteAdded(itemId: String, index: Int, categoryIndex: Int, note: String) = Unit
}
