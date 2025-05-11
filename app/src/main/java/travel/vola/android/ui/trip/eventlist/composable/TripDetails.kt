package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.LatLng
import travel.vola.android.common.ui.components.MapScaffold
import travel.vola.android.common.ui.components.rememberMapScaffoldState
import travel.vola.android.common.ui.preview.TabletPreview
import travel.vola.android.common.ui.state.MarkerType
import travel.vola.android.model.PlaceRepository
import travel.vola.android.model.data.Identifiable
import travel.vola.android.model.repository.mock.MockTripRepository
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.composable.ConfirmationDialog
import travel.vola.android.ui.trip.state.AddPlanItemState
import travel.vola.android.ui.trip.state.TripItemState
import travel.vola.android.ui.trip.state.TripItemState.DateRangeItemState
import travel.vola.android.ui.trip.state.TripItemState.EmptyDateItemState
import travel.vola.android.ui.trip.state.TripItemState.FlightArrivalItemState
import travel.vola.android.ui.trip.state.TripItemState.FlightDepartureItemState
import travel.vola.android.ui.trip.state.TripItemState.HotelCheckInItemState
import travel.vola.android.ui.trip.state.TripItemState.HotelCheckOutItemState
import travel.vola.android.ui.trip.state.TripItemState.MonthItemState
import travel.vola.android.ui.trip.state.TripItemState.PlaceItemState
import travel.vola.android.ui.trip.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetails(
    viewModel: TripViewModel,
    navController: NavController,
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val listScrollState = rememberLazyListState()
    val currentPlaceIndex by remember {
        derivedStateOf(policy = object : SnapshotMutationPolicy<Int> {
            override fun equivalent(a: Int, b: Int): Boolean {
                val itemA = state.items.getOrNull(a)
                val itemB = state.items.getOrNull(b)
                return itemA != null && (itemA !is PlaceItemState || itemA == itemB)
            }
        }) {
            if (!listScrollState.canScrollBackward) {
                -1
            } else if (!listScrollState.canScrollForward) {
                state.places.maxOfOrNull { it.listIndex } ?: -1
            } else {
                listScrollState.firstVisibleItemIndex
            }
        }
    }
    val focusedPlace by produceState<TripViewModel.PlaceState?>(null, currentPlaceIndex) {
        value = state.places.firstOrNull { it.listIndex == currentPlaceIndex }
    }
    val allMarkers = state.places.flatMap { it.markers }
    val boundingMarkers = focusedPlace?.markers ?: emptyList()
    val mapScaffoldState = rememberMapScaffoldState()

    MapScaffold(
        allMarkers.filter { it.type != MarkerType.City },
        boundingMarkers.map { LatLng(it.position.first, it.position.second) },
        state = mapScaffoldState,
        topBar = {
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
    ) { paddingValues ->
        List(state, listScrollState, viewModel, paddingValues)
    }
}

@Composable
fun List(
    state: TripViewModel.ViewState,
    scrollState: LazyListState,
    viewModel: TripViewModel,
    contentPadding: PaddingValues,
) {
    LazyColumn(contentPadding = contentPadding, state = scrollState) {
        items(state.items, key = { (it as? Identifiable)?.id ?: it.hashCode() }) { event ->
            Box(
                modifier = Modifier.animateItem(placementSpec = spring(visibilityThreshold = IntOffset.VisibilityThreshold))
            ) {
                TripDetailItem(event, viewModel)
            }
        }
    }
}

@Composable
private fun TripDetailItem(
    event: TripItemState, viewModel: TripViewModel
) {
    when (event) {
        is MonthItemState -> MonthEventListItem(event.month, event.year)
        is DateRangeItemState -> DateRangeListItem(
            dayOfMonthStart = event.dayOfMonthStart,
            dayOfWeekStart = event.dayOfWeekStart,
            dayOfMonthEnd = event.dayOfMonthEnd,
            dayOfWeekEnd = event.dayOfWeekEnd,
            showBottomDivider = event.showBottomDivider,
            onAddButtonClick = { viewModel.addButtonTapped(event.id) },
        )

        is EmptyDateItemState -> EmptyDateListItem(
            dayOfMonth = event.dayOfMonth,
            dayOfWeek = event.dayOfWeek,
            showBottomDivider = event.showBottomDivider,
            onTap = { viewModel.emptyDateRowTapped(event.id) },
        )

        is PlaceItemState -> PlaceEventListItem(event.imageUrl,
            event.placeName,
            event.dateStart,
            event.dateEnd,
            modifier = Modifier.clickable { viewModel.itemTapped(event.id) })

        is TripItemState.EventItemState -> Surface(
            onClick = { viewModel.itemTapped(event.id) },
        ) {
            when (event) {
                is FlightDepartureItemState -> FlightEventListItem(
                    event.showDate,
                    event.dayOfMonth,
                    event.dayOfWeek,
                    event.time,
                    event.destination,
                    event.airport,
                )

                is FlightArrivalItemState -> ArrivalEventListItem(
                    event.showDate,
                    event.dayOfMonth,
                    event.dayOfWeek,
                    event.time,
                    event.airport,
                )

                is HotelCheckInItemState -> CheckinListItem(
                    event.showDate,
                    event.dayOfMonth,
                    event.dayOfWeek,
                    event.time,
                    event.hotelName,
                )

                is HotelCheckOutItemState -> CheckoutListItem(
                    event.showDate,
                    event.dayOfMonth,
                    event.dayOfWeek,
                    event.time,
                    event.hotelName,
                )

                is TripItemState.TimedPlaceItemState -> TimedPlaceListItem(event)
            }
        }

        is TripItemState.InitialAddPlanItemState -> EmptyAddPlanListItem(
            showDivider = false,
            onAddButtonClick = { viewModel.addButtonTapped(event.id) })

        is TripItemState.EmptyAddPlanItemState -> EmptyAddPlanListItem(showDivider = event.showDivider,
            onAddButtonClick = { viewModel.addButtonTapped(event.id) })

        is AddPlanItemState -> AddPlanListItem(
            event,
            actionHandler = viewModel,
        )
    }
}

@Composable
@Preview
@TabletPreview
fun TripDetailsPreview() {
    AppTheme(dynamicColor = false) {
        val navController = rememberNavController()
        TripDetails(
            TripViewModel(
                MockTripRepository(),
                PlaceRepository(),
                "minhaTrip",
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
