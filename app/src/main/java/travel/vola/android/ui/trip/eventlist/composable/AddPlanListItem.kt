package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.extensions.Time
import travel.vola.android.model.data.Time
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.composable.AddPlanType
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemActionHandler
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.AddPlaceItemState
import travel.vola.android.ui.trip.state.AddPlanItemState
import travel.vola.android.ui.trip.state.LodgingSearchItemState
import travel.vola.android.ui.trip.state.ManualAddLodgingItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState
import travel.vola.android.ui.trip.state.ManualStartEndAddPlanState
import travel.vola.android.ui.trip.state.type
import java.time.ZonedDateTime

@Composable
fun AddPlanListItem(
    state: AddPlanItemState,
    actionHandler: AddPlanItemActionHandler,
) {
    val primaryButtonLabel = when (state.buttonConfiguration) {
        AddPlanItemState.ButtonConfiguration.Save -> "Save"
        AddPlanItemState.ButtonConfiguration.Search -> "Search"
    }
    AddPlanScaffold(
        state.uiType,
        onTypeSelected = { actionHandler.addPlanTypeChanged(state.id, it.toState()) },
        typeSelectionEnabled = state.typeSelectionEnabled,
        deleteButtonEnabled = state.deleteButtonEnabled,
        onDeleteConfirmed = { actionHandler.delete(state.type, state.id) },
        primaryButtonEnabled = state.saveButtonEnabled,
        primaryButtonLabel = primaryButtonLabel,
        onPrimaryButtonTapped = { actionHandler.save(state.id) },
        secondaryButtonLabel = "Cancel",
        onSecondaryButtonTapped = { actionHandler.cancelEdit(state.id) },
    ) {
        when (state) {
            is ManualStartEndAddPlanState -> {
                val itemState = rememberStartEndAddPlanListItemState(
                    startState = rememberAddPlanRowState(
                        key = state.startState.searchResults,
                        selectedDateTime = state.startState.dateTime,
                        timeSelected = state.startState.isTimeSet,
                    ), endState = rememberAddPlanRowState(
                        key = state.endState.searchResults,
                        selectedDateTime = state.endState.dateTime,
                        timeSelected = state.endState.isTimeSet,
                    )
                )
                when (state) {
                    is AddFlightItemState -> {
                        AddFlightListItem(
                            startEndAddPlanState = itemState,
                            uiState = state,
                            onAirportFromTextChanged = {
                                actionHandler.airportFromSearchTextChanged(
                                    state.id, it
                                )
                            },
                            onAirportToTextChanged = {
                                actionHandler.airportToSearchTextChanged(
                                    state.id, it
                                )
                            },
                            onUpdated = {
                                    departureDateTime,
                                    departureTimeSelected,
                                    selectedDepartureSearchResultIndex,
                                    arrivalDateTime,
                                    arrivalTimeSelected,
                                    selectedArrivalSearchResultIndex,
                                ->
                                actionHandler.onUpdated(
                                    state.id,
                                    departureDateTime,
                                    departureTimeSelected,
                                    selectedDepartureSearchResultIndex,
                                    arrivalDateTime,
                                    arrivalTimeSelected,
                                    selectedArrivalSearchResultIndex,
                                )
                            },
                        )
                    }

                    is ManualAddLodgingItemState -> {
                        AddLodgingListItem(
                            startEndAddPlanState = itemState,
                            uiState = state,
                            onLodgingTextChanged = {
                                actionHandler.lodgingTextChanged(
                                    state.id, it
                                )
                            },
                            onFindLodgingButtonTapped = {
                                actionHandler.onFindLodgingButtonTapped(state.id)
                            },
                            onUpdated = {
                                    checkIn,
                                    checkInTimeSelected,
                                    checkOut,
                                    checkOutTimeSelected,
                                    selectedSearchResultIndex,
                                ->
                                actionHandler.onUpdated(
                                    state.id,
                                    checkIn,
                                    checkInTimeSelected,
                                    checkOut,
                                    checkOutTimeSelected,
                                    selectedSearchResultIndex,
                                )
                            },
                        )
                    }
                }
            }

            is LodgingSearchItemState -> {
                LodgingSearchListItem(
                    checkIn = state.checkIn,
                    checkOut = state.checkOut,
                    minCheckOut = state.minCheckOutTime,
                    locationText = state.locationText,
                    searchResults = state.searchResults,
                    onLocationSearchTextChanged = {
                        actionHandler.lodgingTextChanged(
                            state.id, it
                        )
                    },
                    onSwitchToManualButtonTapped = {
                        actionHandler.onSwitchToManualButtonTapped(state.id)
                    },
                    onUpdated = {
                            checkIn,
                            checkOut,
                            selectedSearchResultIndex,
                        ->
                        actionHandler.onUpdated(
                            state.id,
                            checkIn,
                            checkInTimeSelected = false,
                            checkOut,
                            checkOutTimeSelected = false,
                            selectedSearchResultIndex,
                        )
                    },
                )
            }

            is AddPlaceItemState -> {
                val addPlaceListItemState = rememberAddPlaceListItemState(
                    startState = rememberAddPlanRowState(
                        key = state.searchResults,
                        selectedDateTime = state.timestamp,
                        timeSelected = state.startTimeSelected,
                    ),
                    endState = rememberAddPlanRowState(
                        key = state.timestamp,
                        selectedDateTime = state.endDateTime ?: state.minEndTime,
                        timeSelected = state.endTimeSelected,
                    ),
                    hasEnd = state.endDateTime != null,
                )
                AddPlaceListItem(
                    placeName = state.placeName,
                    state = addPlaceListItemState,
                    searchResults = state.searchResults,
                    minEndTime = state.minEndTime,
                    onTextChanged = {
                        actionHandler.locationTextChanged(state.id, it)
                    },
                    onUpdated = { startDateTime, startTimeSelected, endDateTime, endTimeSelected, selectedSearchResultIndex ->
                        actionHandler.onUpdated(
                            state.id,
                            startDateTime,
                            startTimeSelected,
                            endDateTime,
                            endTimeSelected,
                            selectedSearchResultIndex,
                        )
                    },
                )
            }
        }
    }
}

fun AddPlanType.toState() = when (this) {
    AddPlanType.Flight -> AddPlanItemState.Type.Flight
    AddPlanType.Lodging -> AddPlanItemState.Type.Lodging
    AddPlanType.Place -> AddPlanItemState.Type.Place
}

val AddPlanItemState.uiType
    get() = when (this) {
        is AddFlightItemState -> AddPlanType.Flight
        is ManualAddLodgingItemState, is LodgingSearchItemState -> AddPlanType.Lodging
        is AddPlaceItemState -> AddPlanType.Place
    }

@Preview
@Composable
fun AddPlanListItemPreview() {
    AppTheme {
        Surface {
            AddPlanListItem(
                state = AddFlightItemState(
                    id = "",
                    timestamp = Time("2025-10-17T18:25 +0200"),
                    startState = ManualAddPlanState(
                        Time("2025-10-17T18:25 +0200"),
                        Time.now(),
                        dateSelectionEnabled = false,
                        locationText = "Charles de Gaule",
                        searchResults = emptyList(),
                        isTimeSet = true
                    ),
                    endState = ManualAddPlanState(
                        Time("2025-10-18T06:00 -0300"),
                        Time.now(),
                        dateSelectionEnabled = true,
                        locationText = "John F. Kennedy",
                        searchResults = emptyList(),
                        isTimeSet = true
                    ),
                    typeSelectionEnabled = true,
                    saveButtonEnabled = true,
                    deleteButtonEnabled = true,
                ),
                actionHandler = NoOpActionHandler,
            )
        }
    }
}

private object NoOpActionHandler : AddPlanItemActionHandler {
    override fun addPlanTypeChanged(itemId: String, newType: AddPlanItemState.Type) = Unit
    override fun delete(type: AddPlanItemState.Type, itemId: String) = Unit
    override fun save(itemId: String) = Unit
    override fun cancelEdit(itemId: String) = Unit

    // Place list item
    override fun airportFromSearchTextChanged(itemId: String, content: CharSequence) = Unit

    override fun airportToSearchTextChanged(itemId: String, content: CharSequence) = Unit
    override fun onUpdated(
        itemId: String,
        startDateTime: ZonedDateTime?,
        startTimeSelected: Boolean,
        endDateTime: ZonedDateTime?,
        endTimeSelected: Boolean,
        selectedSearchResultIndex: Int,
    ) = Unit

    override fun onFindLodgingButtonTapped(itemId: String) = Unit

    // Flight list item
    override fun locationTextChanged(itemId: String, content: CharSequence) = Unit

    override fun onUpdated(
        itemId: String,
        departureTime: ZonedDateTime,
        departureTimeSelected: Boolean,
        selectedDepartureSearchResultIndex: Int,
        arrivalTime: ZonedDateTime?,
        arrivalTimeSelected: Boolean,
        selectedArrivalSearchResultIndex: Int,
    ) = Unit

    // Lodging List Item
    override fun onSwitchToManualButtonTapped(itemId: String) = Unit
    override fun lodgingTextChanged(itemId: String, content: CharSequence) = Unit
    override fun onUpdated(
        itemId: String,
        checkIn: ZonedDateTime,
        checkInTimeSelected: Boolean,
        checkOut: ZonedDateTime?,
        checkOutTimeSelected: Boolean,
        selectedSearchResultIndex: Int,
    ) = Unit
}
