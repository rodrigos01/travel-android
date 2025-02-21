package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.extensions.now
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.AddPlanType
import com.combah.travel2.ui.trip.creation.usecase.AddPlanItemActionHandler
import com.combah.travel2.ui.trip.creation.usecase.BaseAddPlanItemActionHandler
import com.combah.travel2.ui.trip.state.AddFlightItemState
import com.combah.travel2.ui.trip.state.AddLodgingItemState
import com.combah.travel2.ui.trip.state.AddPlanItemState
import com.combah.travel2.ui.trip.state.ManualAddPlanState
import com.combah.travel2.ui.trip.state.ManualStartEndAddPlanState
import com.combah.travel2.ui.trip.state.type
import kotlinx.coroutines.launch

@Composable
fun AddPlanListItem(
    state: AddPlanItemState,
    addPlanActionHandler: BaseAddPlanItemActionHandler,
    actionHandler: AddPlanItemActionHandler,
) {
    val scope = rememberCoroutineScope()
    AddPlanListItem(
        state,
        onTypeSelected = {
            addPlanActionHandler.addPlanTypeChanged(state.id, it)
        },
        onAirportFromTextChanged = {
            scope.launch {
                actionHandler.airportFromSearchTextChanged(state.id, it)
            }
        },
        onAirportToTextChanged = {
            scope.launch {
                actionHandler.airportToSearchTextChanged(state.id, it)
            }
        },
        onLodgingTextChanged = {
            scope.launch {
                actionHandler.lodgingTextChanged(
                    state.id, it
                )
            }
        },
        onSaveButtonTapped = {
            addPlanActionHandler.save(state.id)
        },
        onCancelButtonTapped = {
            addPlanActionHandler.cancelEdit(state.id)
        },
        onDeleteButtonTapped = {
            addPlanActionHandler.delete(
                state.type,
                state.id
            )
        })
}

@Composable
private fun AddPlanListItem(
    state: AddPlanItemState,
    onTypeSelected: (AddPlanItemState.Type) -> Unit,
    onAirportFromTextChanged: (CharSequence) -> Unit,
    onAirportToTextChanged: (CharSequence) -> Unit,
    onLodgingTextChanged: (CharSequence) -> Unit,
    onSaveButtonTapped: () -> Unit,
    onCancelButtonTapped: () -> Unit,
    onDeleteButtonTapped: () -> Unit = {}
) {
    AddPlanScaffold(
        state.type.toAddPlanType(),
        onTypeSelected = { onTypeSelected(it.toState()) },
        typeSelectionEnabled = state.typeSelectionEnabled,
        deleteButtonEnabled = state.deleteButtonEnabled,
        onDeleteConfirmed = onDeleteButtonTapped,
        primaryButtonEnabled = state.saveButtonEnabled,
        primaryButtonLabel = "Save",
        onPrimaryButtonTapped = onSaveButtonTapped,
        secondaryButtonLabel = "Cancel",
        onSecondaryButtonTapped = onCancelButtonTapped,
    ) {
        when (state) {
            is ManualStartEndAddPlanState -> {
                val startEndAddPlanListItemState = rememberStartEndAddPlanListItemState(
                    startState = rememberAddPlanRowState(
                        selectedTime = state.startState.time,
                        minTime = state.startState.minTime,
                        searchResults = state.startState.searchResults,
                    ), endState = rememberAddPlanRowState(
                        selectedTime = state.endState.time,
                        minTime = state.endState.minTime,
                        searchResults = state.endState.searchResults,
                    )
                )
                when (state) {
                    is AddFlightItemState -> AddFlightListItem(
                        startEndAddPlanState = startEndAddPlanListItemState,
                        uiState = state,
                        onAirportFromTextChanged = onAirportFromTextChanged,
                        onAirportToTextChanged = onAirportToTextChanged,
                    )

                    is AddLodgingItemState -> AddLodgingListItem(
                        startEndAddPlanState = startEndAddPlanListItemState,
                        uiState = state,
                        onLodgingTextChanged = onLodgingTextChanged,
                    )
                }
            }
        }
    }
}

fun AddPlanType.toState() = when (this) {
    AddPlanType.Flight -> AddPlanItemState.Type.Flight
    AddPlanType.Lodging -> AddPlanItemState.Type.Lodging
}

fun AddPlanItemState.Type.toAddPlanType() = when (this) {
    AddPlanItemState.Type.Flight -> AddPlanType.Flight
    AddPlanItemState.Type.Lodging -> AddPlanType.Lodging
}

@Preview
@Composable
fun AddPlanListItemPreview() {
    AppTheme {
        Surface {
            AddPlanListItem(
                state = AddFlightItemState(
                    id = "",
                    timestamp = Time.now(),
                    startState = ManualAddPlanState(
                        Time.now(),
                        Time.now(),
                        dateSelectionEnabled = false,
                        locationText = null,
                        searchResults = emptyList(),
                    ),
                    endState = ManualAddPlanState(
                        Time.now(),
                        Time.now(),
                        dateSelectionEnabled = true,
                        locationText = null,
                        searchResults = emptyList(),
                    ),
                    typeSelectionEnabled = true,
                    saveButtonEnabled = true,
                    deleteButtonEnabled = true,
                ),
                onAirportFromTextChanged = {},
                onAirportToTextChanged = {},
                onLodgingTextChanged = {},
                onTypeSelected = {},
                onCancelButtonTapped = {},
                onDeleteButtonTapped = {},
                onSaveButtonTapped = {},
            )
        }
    }
}