package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.vola.android.extensions.Time
import com.vola.android.extensions.now
import com.vola.android.model.data.Time
import com.vola.android.ui.theme.AppTheme
import com.vola.android.ui.trip.creation.composable.AddPlanType
import com.vola.android.ui.trip.creation.usecase.AddPlanItemActionHandler
import com.vola.android.ui.trip.state.AddFlightItemState
import com.vola.android.ui.trip.state.AddPlanItemState
import com.vola.android.ui.trip.state.LodgingSearchItemState
import com.vola.android.ui.trip.state.ManualAddLodgingItemState
import com.vola.android.ui.trip.state.ManualAddPlanState
import com.vola.android.ui.trip.state.ManualStartEndAddPlanState
import com.vola.android.ui.trip.state.type

@Composable
fun AddPlanListItem(
    state: AddPlanItemState,
    actionHandler: AddPlanItemActionHandler,
) {
    AddPlanScaffold(
        state.uiType,
        onTypeSelected = { actionHandler.addPlanTypeChanged(state.id, it.toState()) },
        typeSelectionEnabled = state.typeSelectionEnabled,
        deleteButtonEnabled = state.deleteButtonEnabled,
        onDeleteConfirmed = { actionHandler.delete(state.type, state.id) },
        primaryButtonEnabled = state.saveButtonEnabled,
        primaryButtonLabel = "Save",
        onPrimaryButtonTapped = { actionHandler.save(state.id) },
        secondaryButtonLabel = "Cancel",
        onSecondaryButtonTapped = { actionHandler.cancelEdit(state.id) },
    ) {
        when (state) {
            is ManualStartEndAddPlanState -> {
                val itemState = rememberStartEndAddPlanListItemState(
                    startState = rememberAddPlanRowState(
                        key = state.startState.searchResults,
                        selectedTime = state.startState.time,
                    ), endState = rememberAddPlanRowState(
                        key = state.endState.searchResults,
                        selectedTime = state.endState.time,
                    )
                )
                when (state) {
                    is AddFlightItemState -> {
                        LaunchedEffect(itemState.startState.selectedTime) {
                            itemState.startState.selectedTime?.let {
                                actionHandler.setDepartureTime(state.id, it)
                            }
                        }
                        LaunchedEffect(itemState.startState.selectedSearchResultIndex) {
                            if (itemState.startState.selectedSearchResultIndex != -1) {
                                actionHandler.airportFromSearchResultTapped(
                                    state.id,
                                    itemState.startState.selectedSearchResultIndex,
                                )
                            }
                        }
                        LaunchedEffect(itemState.endState.selectedTime) {
                            itemState.endState.selectedTime?.let {
                                actionHandler.setArrivalTime(state.id, it)
                            }
                        }
                        LaunchedEffect(itemState.endState.selectedSearchResultIndex) {
                            if (itemState.endState.selectedSearchResultIndex != -1) {
                                actionHandler.airportToSearchResultTapped(
                                    state.id,
                                    itemState.endState.selectedSearchResultIndex,
                                )
                            }
                        }
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
                        )
                    }

                    is ManualAddLodgingItemState -> {
                        LaunchedEffect(itemState.startState.selectedTime) {
                            itemState.startState.selectedTime?.let {
                                actionHandler.setCheckInTime(state.id, it)
                            }
                        }
                        LaunchedEffect(itemState.startState.selectedSearchResultIndex) {
                            if (itemState.startState.selectedSearchResultIndex != -1) {
                                actionHandler.locationSearchResultTapped(
                                    state.id,
                                    itemState.startState.selectedSearchResultIndex,
                                )
                            }
                        }
                        LaunchedEffect(itemState.endState.selectedTime) {
                            itemState.endState.selectedTime?.let {
                                actionHandler.setCheckOutTime(state.id, it)
                            }
                        }
                        AddLodgingListItem(
                            startEndAddPlanState = itemState,
                            uiState = state,
                            onLodgingTextChanged = {
                                actionHandler.locationTextChanged(
                                    state.id, it
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
                    onCheckInDateSelected = { actionHandler.setCheckInTime(state.id, it) },
                    onCheckOutDateSelected = { actionHandler.setCheckOutTime(state.id, it) },
                    onLocationSearchTextChanged = {
                        actionHandler.locationTextChanged(
                            state.id,
                            it
                        )
                    },
                    onLocationSearchResultSelected = {
                        actionHandler.locationSearchResultTapped(
                            state.id,
                            it
                        )
                    },
                    onSwitchToManualButtonTapped = {
                        actionHandler.onSwitchToManualButtonTapped(state.id)
                    })
            }
        }
    }
}

fun AddPlanType.toState() = when (this) {
    AddPlanType.Flight -> AddPlanItemState.Type.Flight
    AddPlanType.Lodging -> AddPlanItemState.Type.Lodging
}

val AddPlanItemState.uiType
    get() = when (this) {
        is AddFlightItemState -> AddPlanType.Flight
        is ManualAddLodgingItemState, is LodgingSearchItemState -> AddPlanType.Lodging
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
                    ),
                    endState = ManualAddPlanState(
                        Time("2025-10-18T06:00 -0300"),
                        Time.now(),
                        dateSelectionEnabled = true,
                        locationText = "John F. Kennedy",
                        searchResults = emptyList(),
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
    override fun airportFromSearchTextChanged(itemId: String, content: CharSequence) = Unit
    override fun airportToSearchTextChanged(itemId: String, content: CharSequence) = Unit
    override fun airportFromSearchResultTapped(itemId: String, index: Int) = Unit
    override fun airportToSearchResultTapped(itemId: String, index: Int) = Unit
    override fun locationTextChanged(itemId: String, content: CharSequence) = Unit
    override fun locationSearchResultTapped(itemId: String, index: Int) = Unit
    override fun onSwitchToManualButtonTapped(itemId: String) = Unit
    override fun setCheckInTime(itemId: String, time: Time) = Unit
    override fun setCheckOutTime(itemId: String, time: Time) = Unit
    override fun setDepartureTime(itemId: String, time: Time) = Unit
    override fun setArrivalTime(itemId: String, time: Time) = Unit
}
