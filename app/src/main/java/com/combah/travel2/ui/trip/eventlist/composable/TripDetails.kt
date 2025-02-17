package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.repository.AddFlightRepository
import com.combah.travel2.model.repository.AddLodgingRepository
import com.combah.travel2.model.repository.mock.MockTripRepository
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.AddPlanType
import com.combah.travel2.ui.trip.creation.composable.ConfirmationDialog
import com.combah.travel2.ui.trip.viewmodel.AddFlightUseCase
import com.combah.travel2.ui.trip.viewmodel.AddLodgingUseCase
import com.combah.travel2.ui.trip.viewmodel.AddPlanUseCase
import com.combah.travel2.ui.trip.viewmodel.AddPlanUseCase.AddPlanItem
import com.combah.travel2.ui.trip.viewmodel.TripItem
import com.combah.travel2.ui.trip.viewmodel.TripItem.DateRangeItem
import com.combah.travel2.ui.trip.viewmodel.TripItem.EmptyDateItem
import com.combah.travel2.ui.trip.viewmodel.TripItem.FlightArrivalItem
import com.combah.travel2.ui.trip.viewmodel.TripItem.FlightDepartureItem
import com.combah.travel2.ui.trip.viewmodel.TripItem.HotelCheckInItem
import com.combah.travel2.ui.trip.viewmodel.TripItem.HotelCheckOutItem
import com.combah.travel2.ui.trip.viewmodel.TripItem.MonthItem
import com.combah.travel2.ui.trip.viewmodel.TripItem.PlaceItem
import com.combah.travel2.ui.trip.viewmodel.TripViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TripDetails(
    viewModel: TripViewModel,
    navController: NavController,
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var isInEditMode by remember {
        mutableStateOf(false)
    }
    var enteredName by remember(state.title) {
        mutableStateOf(state.title)
    }
    var showToolbarOverflowMenu by remember {
        mutableStateOf(false)
    }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    if (showDeleteConfirmation) {
        ConfirmationDialog(
            onConfirm = {
                showDeleteConfirmation = false
                viewModel.deleteTrip()
            },
            onDismiss = { showDeleteConfirmation = false },
            confirmButtonLabel = "Delete",
            confirmButtonColors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            dismissButtonLabel = "Cancel"
        ) {
            Text("Delete ${state.title}?")
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(title = {
                if (isInEditMode) {
                    TextField(value = enteredName, onValueChange = { enteredName = it })
                } else {
                    Text(text = state.title)
                }
            }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = ""
                    )
                }
            }, actions = {
                if (isInEditMode) {
                    IconButton(onClick = { isInEditMode = false }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "")
                    }
                    IconButton(onClick = {
                        isInEditMode = false
                        viewModel.tripNameChanged(enteredName)
                    }) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = "")
                    }
                } else {
                    IconButton(onClick = { isInEditMode = true }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "")
                    }
                    Box {
                        IconButton(onClick = { showToolbarOverflowMenu = true }) {
                            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "")
                        }
                        DropdownMenu(
                            expanded = showToolbarOverflowMenu,
                            onDismissRequest = { showToolbarOverflowMenu = false },
                            properties = PopupProperties(focusable = false)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete Trip") },
                                onClick = {
                                    showToolbarOverflowMenu = false
                                    showDeleteConfirmation = true
                                },
                                colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.onSecondaryContainer),
                            )
                        }
                    }
                }
            })
        },
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            items(
                state.items,
                key = { (it as? TripItem.Identifiable)?.id ?: it.hashCode() }) { event ->
                Box(
                    modifier = Modifier.animateItem(placementSpec = spring(visibilityThreshold = IntOffset.VisibilityThreshold))
                ) {
                    TripDetailItem(event, viewModel, scope)
                }
            }
        }
    }
}

@Composable
private fun TripDetailItem(
    event: TripItem, viewModel: TripViewModel, scope: CoroutineScope
) {
    when (event) {
        is MonthItem -> MonthEventListItem(event.month, event.year)
        is DateRangeItem -> DateRangeListItem(
            dayOfMonthStart = event.dayOfMonthStart,
            dayOfWeekStart = event.dayOfWeekStart,
            dayOfMonthEnd = event.dayOfMonthEnd,
            dayOfWeekEnd = event.dayOfWeekEnd,
            onAddButtonClick = { viewModel.addButtonTapped(event.id) },
        )

        is EmptyDateItem -> EmptyDateListItem(
            dayOfMonth = event.dayOfMonth,
            dayOfWeek = event.dayOfWeek,
            onTap = { viewModel.emptyDateRowTapped(event.id) },
        )

        is PlaceItem -> PlaceEventListItem(
            event.imageUrl, event.placeName, event.dateStart, event.dateEnd
        )

        is TripItem.EventItem -> Surface(
            onClick = { viewModel.itemTapped(event.id) },
        ) {
            when (event) {
                is FlightDepartureItem -> FlightEventListItem(
                    event.showDate,
                    event.dayOfMonth,
                    event.dayOfWeek,
                    event.time,
                    event.destination,
                    event.airport,
                )

                is FlightArrivalItem -> ArrivalEventListItem(
                    event.showDate,
                    event.dayOfMonth,
                    event.dayOfWeek,
                    event.time,
                    event.airport,
                )

                is HotelCheckInItem -> CheckinListItem(
                    event.showDate,
                    event.dayOfMonth,
                    event.dayOfWeek,
                    event.time,
                    event.hotelName,
                )

                is HotelCheckOutItem -> CheckoutListItem(
                    event.showDate,
                    event.dayOfMonth,
                    event.dayOfWeek,
                    event.time,
                    event.hotelName,
                )
            }
        }

        is TripItem.InitialAddPlanItem -> EmptyAddPlanListItem(
            showDivider = false,
            onAddButtonClick = { viewModel.addButtonTapped(event.id) })

        is TripItem.EmptyAddPlanItem -> EmptyAddPlanListItem(
            showDivider = event.showDivider,
            onAddButtonClick = { viewModel.addButtonTapped(event.id) })

        is AddFlightUseCase.AddFlightItem -> AddFlightListItem(
            onTypeSelected = {
                viewModel.typeSelected(
                    event.id, it
                )
            },
            isEditing = event.isEditing,
            minDepartureTime = event.minDepartureTime,
            minArrivalTime = event.minArrivalTime,
            departureDateSelectionEnabled = event.startDateSelectionEnabled,
            departureDayOfMonth = event.departureDayOfMonth,
            departureDayOfWeek = event.departureDayOfWeek,
            onDepartureDateChanged = { viewModel.setDepartureDate(event.id, it) },
            departureTime = event.departureTime,
            onDepartureTimeChanged = { hour, minute ->
                viewModel.setDepartureTime(
                    event.id, hour, minute
                )
            },
            airportFromName = event.airportFromName,
            onAirportFromTextChanged = {
                scope.launch {
                    viewModel.airportFromSearchTextChanged(event.id, it)
                }
            },
            airportFromSearchResults = event.airportFromSearchResults,
            airportFromSearchResultTapped = {
                viewModel.airportFromSearchResultTapped(
                    event.id, it
                )
            },
            arrivalTime = event.arrivalTime,
            arrivalDayOfMonth = event.arrivalDayOfMonth,
            arrivalDayOfWeek = event.arrivalDayOfWeek,
            onArrivalTimeChanged = { hour, minute ->
                viewModel.setArrivalTime(
                    event.id, hour, minute
                )
            },
            onArrivalDateChanged = { viewModel.setArrivalDate(event.id, it) },
            airportToName = event.airportToName,
            onAirportToTextChanged = {
                scope.launch {
                    viewModel.airportToSearchTextChanged(event.id, it)
                }
            },
            airportToSearchResults = event.airportToSearchResults,
            airportToSearchResultTapped = {
                viewModel.airportToSearchResultTapped(
                    event.id, it
                )
            },
            saveButtonEnabled = event.saveButtonEnabled,
            onSaveButtonTapped = {
                viewModel.save(event.id)
            },
            onCancelButtonTapped = {
                viewModel.cancelEdit(event.id)
            },
            onDeleteButtonTapped = { viewModel.delete(AddPlanItem.Type.Flight, event.id) }
        )

        is AddLodgingUseCase.AddLodgingItem -> AddLodgingListItem(
            onTypeSelected = { viewModel.typeSelected(event.id, it) },
            isEditing = event.isEditing,
            minCheckInTime = event.minCheckInTime,
            checkInDateSelectionEnabled = event.startDateSelectionEnabled,
            checkInDayOfMonth = event.checkInDayOfMonth,
            checkInDayOfWeek = event.checkInDayOfWeek,
            onCheckInDateChanged = { viewModel.setCheckInDate(event.id, it) },
            checkInTime = event.checkInTime,
            onCheckInTimeChanged = { hour, minute ->
                viewModel.setCheckInTime(
                    event.id, hour, minute
                )
            },
            lodgingLabel = event.name,
            onLodgingTextChanged = {
                scope.launch {
                    viewModel.lodgingTextChanged(
                        event.id, it
                    )
                }
            },
            lodgingSearchResults = event.lodgingSearchResults,
            lodgingSearchResultTapped = {
                viewModel.lodgingSearchResultTapped(
                    event.id, it
                )
            },
            checkOutDayOfMonth = event.checkOutDayOfMonth,
            checkOutDayOfWeek = event.checkOutDayOfWeek,
            onCheckOutDateChanged = { viewModel.setCheckOutDate(event.id, it) },
            checkOutTime = event.checkOutTime,
            minCheckOutTime = event.minCheckOutTime,
            onCheckOutTimeChanged = { hour, minute ->
                viewModel.setCheckoutTime(
                    event.id, hour, minute
                )
            },
            saveButtonEnabled = event.saveButtonEnabled,
            onSaveButtonTapped = { viewModel.save(event.id) },
            onCancelButtonTapped = { viewModel.cancelEdit(event.id) },
            onDeleteConfirmed = { viewModel.delete(AddPlanItem.Type.Lodging, event.id) },
        )
    }
}

private fun TripViewModel.typeSelected(itemId: String, newType: AddPlanType) = when (newType) {
    AddPlanType.Flight -> addPlanTypeChanged(itemId, AddPlanItem.Type.Flight)
    AddPlanType.Lodging -> addPlanTypeChanged(itemId, AddPlanItem.Type.Lodging)
}

@Composable
@Preview
fun TripDetailsPreview() {
    AppTheme(dynamicColor = false) {
        val navController = rememberNavController()
        TripDetails(
            TripViewModel(
                MockTripRepository(),
                "minhaTrip",
                AddPlanUseCase(
                    AddFlightUseCase(AddFlightRepository(), TimeFormatter()),
                    AddLodgingUseCase(AddLodgingRepository(), TimeFormatter()),
                ),
                TimeFormatter(),
                navController,
            ),
            navController,
        )
    }
}

object TripDetailsDestination {
    const val ARG_TRIP_ID = "tripId"
    private const val ROUTE_NAME = "trip_details"
    const val ROUTE = "$ROUTE_NAME/{$ARG_TRIP_ID}"

    fun getRoute(tripId: String) = "$ROUTE_NAME/$tripId"
}
