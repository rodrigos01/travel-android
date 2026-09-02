package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.clickable
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.request.SuccessResult
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemActionHandler
import travel.vola.android.ui.trip.state.AddPlanItemState
import travel.vola.android.ui.trip.state.TripItemState

@Composable
fun TripDetailItem(
    state: TripItemState,
    addPlanItemActionHandler: AddPlanItemActionHandler,
    highlightDate: Boolean = false,
    onAddButonTapped: (String) -> Unit,
    onEmptyAddRowTapped: (String) -> Unit,
    onInitialAddButonTapped: (String) -> Unit,
    onEditTapped: (String) -> Unit,
    onImageLoaded: (SuccessResult) -> Unit,
    onGenerateTapped: (String) -> Unit,
    onFlexibleSuggestionConfirmed: (String) -> Unit,
    onFlexibleSuggestionDismissed: (String) -> Unit,
) {
    when (state) {
        is TripItemState.MonthItemState -> MonthEventListItem(state.month, state.year)
        is TripItemState.DateRangeItemState -> DateRangeListItem(
            dayOfMonthStart = state.dayOfMonthStart,
            dayOfWeekStart = state.dayOfWeekStart,
            dayOfMonthEnd = state.dayOfMonthEnd,
            dayOfWeekEnd = state.dayOfWeekEnd,
            focused = highlightDate,
            isGeneratingSuggestions = state.isGeneratingPlans,
            onAddButtonClick = { onAddButonTapped(state.id) },
            onGenerateButtonClick = { onGenerateTapped(state.id) },
        )

        is TripItemState.EmptyDateItemState -> EmptyDateListItem(
            dayOfMonth = state.dayOfMonth,
            dayOfWeek = state.dayOfWeek,
            highlightDate = highlightDate,
            isGeneratingSuggestions = state.isGeneratingPlans,
            onTap = { onEmptyAddRowTapped(state.id) },
            onGenerateTapped = { onGenerateTapped(state.id) },
        )

        is TripItemState.PlaceItemState -> PlaceEventListItem(
            state.imageUrl,
            state.placeName,
            state.dateStart,
            state.dateEnd,
            onImageLoaded,
            modifier = Modifier.Companion.clickable { onEditTapped(state.id) },
        )

        is TripItemState.FlexibleDaySectionState -> FlexibleDaySectionListItem(
            state,
            highlightDate = highlightDate,
            position = state.backgroundStyle.asEventItemPosition(),
            onEditTapped = {
                onEditTapped(state.id)
            },
            onDeleteConfirmed = {
                addPlanItemActionHandler.delete(
                    AddPlanItemState.Type.FlexibleSection,
                    state.id,
                )
            },
            onCategoryAdded = {
                addPlanItemActionHandler.onFlexibleCategoryAdded(
                    state.id,
                    it,
                )
            },
            onLocationSearchTextChanged = {
                addPlanItemActionHandler.onFlexibleItemSearchTextChanged(
                    state.id,
                    it,
                )
            },
            onLocationSearchResultSelected = { index, categoryIndex ->
                state.searchResults.getOrNull(index)?.let { result ->
                    addPlanItemActionHandler.onFlexibleItemSearchResultSelected(
                        state.id,
                        result.id,
                        categoryIndex,
                    )
                }
            },
            onNoteAdded = { index, categoryIndex, note ->
                addPlanItemActionHandler.onFlexibleItemNoteAdded(
                    state.id,
                    index,
                    categoryIndex,
                    note,
                )
            },
            onSuggestionConfirmed = {
                onFlexibleSuggestionConfirmed(state.id)
            },
            onSuggestionDismissed = {
                onFlexibleSuggestionDismissed(state.id)
            },
        )

        is TripItemState.SuggestionPlaceholderItemState -> SuggestionPlaceholderListItem(
            state,
            highlightDate,
        )

        is TripItemState.EventItemState -> Surface(
            onClick = { onEditTapped(state.id) },
        ) {
            when (state) {
                is TripItemState.FlightDepartureItemState -> FlightEventListItem(
                    state.showDate,
                    highlightDate,
                    state.dayOfMonth,
                    state.dayOfWeek,
                    state.time,
                    state.destination,
                    state.airport,
                    state.backgroundStyle.asEventItemPosition(),
                )

                is TripItemState.FlightArrivalItemState -> ArrivalEventListItem(
                    state.showDate,
                    highlightDate,
                    state.dayOfMonth,
                    state.dayOfWeek,
                    state.time,
                    state.airport,
                    state.backgroundStyle.asEventItemPosition(),
                )

                is TripItemState.HotelCheckInItemState -> CheckinListItem(
                    state.showDate,
                    highlightDate,
                    state.dayOfMonth,
                    state.dayOfWeek,
                    state.time,
                    state.hotelName,
                    state.backgroundStyle.asEventItemPosition(),
                )

                is TripItemState.HotelCheckOutItemState -> CheckoutListItem(
                    state.showDate,
                    highlightDate,
                    state.dayOfMonth,
                    state.dayOfWeek,
                    state.time,
                    state.hotelName,
                    state.backgroundStyle.asEventItemPosition(),
                )

                is TripItemState.TimedPlaceItemState -> TimedPlaceListItem(state, highlightDate)
                is TripItemState.RestaurantReservationItemState -> RestaurantListItem(
                    state,
                    highlightDate,
                )

                is TripItemState.FlexibleDaySectionState -> {}
                is TripItemState.SuggestionPlaceholderItemState -> {}
            }
        }

        is TripItemState.InitialAddPlanItemState -> EmptyAddPlanListItem(
            onAddButtonClick = { onInitialAddButonTapped(state.id) },
        )
    }
}
