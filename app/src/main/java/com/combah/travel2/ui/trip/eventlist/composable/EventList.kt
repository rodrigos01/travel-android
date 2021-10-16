package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.combah.travel2.ui.data.*
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun EventList(
    events: List<TripEvent>,
    firstEvents: Set<TripEvent>,
    addTransportationClickListener: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    ModalBottomSheetLayout(sheetContent = {
        AddPlan(
            addTransportationClickListener = addTransportationClickListener,
            onClickClose = {
                scope.launch {
                    bottomSheetState.hide()
                }
            })
    }, sheetState = bottomSheetState) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    scope.launch {
                        bottomSheetState.show()
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "add event")
                }
            },
        ) {
            LazyColumn {
                items(events) { event ->
                    val isFirst = firstEvents.contains(event)
                    when (event) {
                        is MonthEvent -> MonthEventListItem(event = event)
                        is PlaceEvent -> PlaceEventListItem(event = event)
                        is FlightEvent -> FlightEventListItem(event = event, firstInDate = isFirst)
                        is ArrivalEvent -> ArrivalEventListItem(
                            event = event,
                            firstInDate = isFirst
                        )
                        is CheckinEvent -> CheckinListItem(event = event, firstInDate = isFirst)
                        is CheckoutEvent -> CheckoutListItem(event = event, firstInDate = isFirst)
                    }
                }
            }
        }
    }
}