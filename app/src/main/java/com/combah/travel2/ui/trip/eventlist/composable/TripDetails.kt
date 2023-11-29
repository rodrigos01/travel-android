package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.combah.travel2.model.repository.mock.MockTripRepository
import com.combah.travel2.ui.data.*
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.TripViewModel
import com.combah.travel2.ui.trip.creation.composable.TransportationSetupDestination
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetails(
    viewModel: TripViewModel,
    navController: NavController,
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val bottomSheetState =
        rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showBottomSheet = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "add event")
            }
        },
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
    ) {
        LazyColumn(contentPadding = it) {
            items(state.items) { event ->
//                val isFirst = state.firstEvents?.contains(event) ?: true
//                when (event) {
//                    is TripViewModel.TripItem.MonthItem -> MonthEventListItem(event = event)
//                    is EmptyDateRangeEvent -> DateRangeListItem(
//                        dayOfMonthStart = event.dateStart.dayOfMonthString(),
//                        dayOfWeekStart = event.dateStart.dayOfWeekString(),
//                        dayOfMonthEnd = event.dateEnd.dayOfMonthString(),
//                        dayOfWeekEnd = event.dateEnd.dayOfWeekString(),
//                    )
//                    is PlaceEvent -> PlaceEventListItem(event = event)
//                    is FlightEvent -> FlightEventListItem(event = event, firstInDate = isFirst)
//                    is ArrivalEvent -> ArrivalEventListItem(
//                        event = event,
//                        firstInDate = isFirst
//                    )
//                    is CheckinEvent -> CheckinListItem(event = event, firstInDate = isFirst)
//                    is CheckoutEvent -> CheckoutListItem(event = event, firstInDate = isFirst)
//                }
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState
            ) {
                AddPlan(
                    addTransportationClickListener = {
                        navController.navigate(
                            TransportationSetupDestination.KEY
                        )
                    },
                    onClickClose = {
                        scope.launch {
                            bottomSheetState.hide()
                        }.invokeOnCompletion {
                            showBottomSheet = false
                        }
                    })
            }
        }
    }
}

@Composable
@Preview
fun TripDetailsPreview() {
    AppTheme(dynamicColor = false) {
        TripDetails(TripViewModel(MockTripRepository(), "minhaTrip"), rememberNavController())
    }
}

object TripDetailsDestination {
    const val ARG_TRIP_ID = "tripId"
    private const val ROUTE_NAME = "trip_details"
    const val ROUTE = "$ROUTE_NAME/{$ARG_TRIP_ID}"

    fun getRoute(tripId: String) = "$ROUTE_NAME/$tripId"
}