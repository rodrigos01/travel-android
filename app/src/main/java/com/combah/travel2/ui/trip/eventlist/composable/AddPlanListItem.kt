package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.extensions.Time
import com.combah.travel2.extensions.now
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.AddPlanType
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemActionHandler
import com.combah.travel2.ui.trip.state.AddFlightItemState
import com.combah.travel2.ui.trip.state.AddLodgingItemState
import com.combah.travel2.ui.trip.state.AddPlanItemState
import com.combah.travel2.ui.trip.state.LodgingSearchItemState
import com.combah.travel2.ui.trip.state.ManualAddPlanState
import com.combah.travel2.ui.trip.state.ManualStartEndAddPlanState
import com.combah.travel2.ui.trip.state.type

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
                        selectedTime = state.startState.time,
                    ), endState = rememberAddPlanRowState(
                        selectedTime = state.endState.time,
                    )
                )
                when (state) {
                    is AddFlightItemState -> {
                        LaunchedEffect(
                            itemState.startState.selectedTime,
                            itemState.startState.selectedSearchResultIndex,
                            itemState.endState.selectedTime,
                            itemState.endState.selectedSearchResultIndex,
                        ) {
                            itemState.startState.selectedTime?.let {
                                actionHandler.setDepartureTime(state.id, it)
                            }
                            if (itemState.startState.selectedSearchResultIndex != -1) {
                                actionHandler.airportFromSearchResultTapped(
                                    state.id,
                                    itemState.startState.selectedSearchResultIndex,
                                )
                            }
                            itemState.endState.selectedTime?.let {
                                actionHandler.setArrivalTime(state.id, it)
                            }
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

                    is AddLodgingItemState -> {
                        LaunchedEffect(
                            itemState.startState.selectedTime,
                            itemState.startState.selectedSearchResultIndex,
                            itemState.endState.selectedTime,
                        ) {
                            itemState.startState.selectedTime?.let {
                                actionHandler.setCheckInTime(state.id, it)
                            }
                            if (itemState.startState.selectedSearchResultIndex != -1) {
                                actionHandler.locationSearchResultTapped(
                                    state.id,
                                    itemState.startState.selectedSearchResultIndex,
                                )
                            }
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
        is AddLodgingItemState, is LodgingSearchItemState -> AddPlanType.Lodging
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
