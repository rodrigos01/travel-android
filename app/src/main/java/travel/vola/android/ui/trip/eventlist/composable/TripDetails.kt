package travel.vola.android.ui.trip.eventlist.composable

import android.graphics.Bitmap
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import coil.request.SuccessResult
import com.google.android.gms.maps.model.LatLng
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.ktx.animateColorScheme
import com.materialkolor.rememberDynamicColorScheme
import travel.vola.android.common.ui.components.MapScaffold
import travel.vola.android.common.ui.components.rememberMapScaffoldState
import travel.vola.android.common.ui.preview.TabletPreview
import travel.vola.android.common.ui.state.MarkerType
import travel.vola.android.extensions.Time
import travel.vola.android.extensions.viewModel
import travel.vola.android.model.data.Identifiable
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.composable.AddPlanType
import travel.vola.android.ui.trip.creation.composable.ConfirmationDialog
import travel.vola.android.ui.trip.creation.composable.TripDetailsToolbar
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemActionHandler
import travel.vola.android.ui.trip.state.AddPlanItemState
import travel.vola.android.ui.trip.state.TripItemState
import travel.vola.android.ui.trip.state.TripItemState.DateRangeItemState
import travel.vola.android.ui.trip.state.TripItemState.EmptyDateItemState
import travel.vola.android.ui.trip.state.TripItemState.EventItemState.BackgroundStyle
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
    tripId: String,
    navController: NavController,
) {
    val viewModel: TripViewModel = viewModel(factory = TripViewModel.Factory(tripId))
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    TripDetails(
        state,
        viewModel,
        onBackPressed = { navController.popBackStack() },
        onDeleteConfirmed = viewModel::deleteTrip,
        onTripNameChanged = viewModel::tripNameChanged,
        onAddButonTapped = viewModel::addButtonTapped,
        onEmptyAddRowTapped = viewModel::emptyDateRowTapped,
        onItemTapped = viewModel::itemTapped,
        onAddPlanTypeSelected = { viewModel.onAddPlanTypeSelected(it?.toState()) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripDetails(
    state: TripViewModel.ViewState,
    addPlanItemActionHandler: AddPlanItemActionHandler = NoOpActionHandler,
    onBackPressed: () -> Unit = {},
    onDeleteConfirmed: () -> Unit = {},
    onTripNameChanged: (String) -> Unit = {},
    onAddButonTapped: (String) -> Unit = {},
    onEmptyAddRowTapped: (String) -> Unit = {},
    onItemTapped: (String) -> Unit = {},
    onAddPlanTypeSelected: (AddPlanType?) -> Unit = {},
) {
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
                listScrollState.layoutInfo.visibleItemsInfo.takeIf { it.isNotEmpty() }
                    ?.let { visibleItems ->
                        visibleItems.getOrNull(visibleItems.lastIndex / 2 + 1)?.index
                    } ?: listScrollState.firstVisibleItemIndex
            }
        }
    }
    val focusedPlace by produceState<TripViewModel.PlaceState?>(null, currentPlaceIndex) {
        value =
            state.places.sortedBy { it.listIndex }.lastOrNull { it.listIndex <= currentPlaceIndex }
    }
    val allMarkers = state.places.flatMap { it.markers }
    val boundingMarkers = focusedPlace?.markers ?: emptyList()
    val mapScaffoldState = rememberMapScaffoldState()


    val isDarkTheme = isSystemInDarkTheme()
    val colorSchemeBitmaps = remember { mutableStateMapOf<String, Bitmap?>() }
    val seedColor by remember(focusedPlace, colorSchemeBitmaps) {
        derivedStateOf {
            colorSchemeBitmaps[focusedPlace?.place?.id]?.let { Palette.from(it) }?.generate()
                ?.dominantSwatch?.rgb
        }
    }
    val colorScheme = animateColorScheme(seedColor?.let {
        rememberDynamicColorScheme(
            seedColor = Color(it),
            isDark = isDarkTheme,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
            style = PaletteStyle.Expressive,
        )
    } ?: MaterialTheme.colorScheme)
    MaterialTheme(
        colorScheme = colorScheme,
    ) {
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
                            onDeleteConfirmed()
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
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = ""
                        )
                    }
                }, actions = {
                    if (isInEditMode) {
                        IconButton(onClick = { isInEditMode = false }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "")
                        }
                        IconButton(onClick = {
                            isInEditMode = false
                            onTripNameChanged(enteredName)
                        }) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = "")
                        }
                    } else {
                        IconButton(onClick = { isInEditMode = true }) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = "")
                        }
                        if (mapScaffoldState.showMap) {
                            IconButton(onClick = { mapScaffoldState.showMap = false }) {
                                Icon(imageVector = Icons.AutoMirrored.Default.List, contentDescription = "")
                            }
                        } else {
                            IconButton(onClick = { mapScaffoldState.showMap = true }) {
                                Icon(imageVector = Icons.Default.Map, contentDescription = "")
                            }

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
            Box(modifier = Modifier.fillMaxSize()) {
                List(
                    state,
                    listScrollState,
                    paddingValues,
                    addPlanItemActionHandler,
                    onAddButonTapped,
                    onEmptyAddRowTapped,
                    onItemTapped,
                    onPlaceImageLoaded = { placeId, result ->
                        colorSchemeBitmaps[placeId] =
                            result.drawable.toBitmapOrNull()
                                ?.copy(Bitmap.Config.ARGB_8888, true)
                    },
                )
                if (state.addPlanItemState != null) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent,
                        onClick = { onAddPlanTypeSelected(null) },
                    ) {}
                }
                TripDetailsToolbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    addPlanState = state.addPlanItemState,
                    onTypeSelected = onAddPlanTypeSelected,
                )
            }
        }
    }
}

@Composable
fun List(
    state: TripViewModel.ViewState,
    scrollState: LazyListState,
    contentPadding: PaddingValues,
    addPlanItemActionHandler: AddPlanItemActionHandler,
    onAddButonTapped: (String) -> Unit,
    onEmptyAddRowTapped: (String) -> Unit,
    onItemTapped: (String) -> Unit,
    onPlaceImageLoaded: (String, SuccessResult) -> Unit,
) {
    LazyColumn(contentPadding = contentPadding, state = scrollState) {
        items(
            state.items,
            key = { (it as? Identifiable)?.id ?: it.hashCode() }) { event ->
            Box(
                modifier = Modifier
                    .animateItem(placementSpec = spring(visibilityThreshold = IntOffset.VisibilityThreshold))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TripDetailItem(
                    event,
                    addPlanItemActionHandler,
                    onAddButonTapped,
                    onEmptyAddRowTapped,
                    onItemTapped,
                    onImageLoaded = {
                        val sectionId =
                            (event as? TripItemState.SectionItemState)?.sectionId
                        if (sectionId != null) {
                            onPlaceImageLoaded(sectionId, it)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun TripDetailItem(
    event: TripItemState,
    addPlanItemActionHandler: AddPlanItemActionHandler,
    onAddButonTapped: (String) -> Unit,
    onEmptyAddRowTapped: (String) -> Unit,
    onItemTapped: (String) -> Unit,
    onImageLoaded: (SuccessResult) -> Unit,
) {
    when (event) {
        is MonthItemState -> MonthEventListItem(event.month, event.year)
        is DateRangeItemState -> DateRangeListItem(
            dayOfMonthStart = event.dayOfMonthStart,
            dayOfWeekStart = event.dayOfWeekStart,
            dayOfMonthEnd = event.dayOfMonthEnd,
            dayOfWeekEnd = event.dayOfWeekEnd,
            onAddButtonClick = { onAddButonTapped(event.id) },
        )

        is EmptyDateItemState -> EmptyDateListItem(
            dayOfMonth = event.dayOfMonth,
            dayOfWeek = event.dayOfWeek,
            onTap = { onEmptyAddRowTapped(event.id) },
        )

        is PlaceItemState -> PlaceEventListItem(
            event.imageUrl,
            event.placeName,
            event.dateStart,
            event.dateEnd,
            onImageLoaded,
            modifier = Modifier.clickable { onItemTapped(event.id) })

        is TripItemState.EventItemState -> Surface(
            onClick = { onItemTapped(event.id) },
        ) {
            when (event) {
                is FlightDepartureItemState -> FlightEventListItem(
                    event.showDate,
                    event.dayOfMonth,
                    event.dayOfWeek,
                    event.time,
                    event.destination,
                    event.airport,
                    event.backgroundStyle.asEvenListItemPosition(),
                )

                is FlightArrivalItemState -> ArrivalEventListItem(
                    event.showDate,
                    event.dayOfMonth,
                    event.dayOfWeek,
                    event.time,
                    event.airport,
                    event.backgroundStyle.asEvenListItemPosition(),
                )

                is HotelCheckInItemState -> CheckinListItem(
                    event.showDate,
                    event.dayOfMonth,
                    event.dayOfWeek,
                    event.time,
                    event.hotelName,
                    event.backgroundStyle.asEvenListItemPosition(),
                )

                is HotelCheckOutItemState -> CheckoutListItem(
                    event.showDate,
                    event.dayOfMonth,
                    event.dayOfWeek,
                    event.time,
                    event.hotelName,
                    event.backgroundStyle.asEvenListItemPosition(),
                )

                is TripItemState.TimedPlaceItemState -> TimedPlaceListItem(event)
                is TripItemState.RestaurantReservationItemState -> RestaurantListItem(event)
            }
        }

        is TripItemState.InitialAddPlanItemState -> EmptyAddPlanListItem(
            onAddButtonClick = { onAddButonTapped(event.id) })

        is AddPlanItemState -> AddPlanListItem(
            event,
            actionHandler = addPlanItemActionHandler,
        )
    }
}

@Composable
@Preview
@TabletPreview
fun TripDetailsPreview() {
    val state = TripViewModel.ViewState(
        title = "My Trip",
        items = listOf(
            MonthItemState(
                timestamp = Time("2025-10-17T18:25 +0200"),
                month = "October",
                year = "2025",
            ),
            FlightDepartureItemState(
                id = "1",
                showDate = true,
                timestamp = Time("2025-10-17T22:25 -0400"),
                dayOfMonth = "17",
                dayOfWeek = "Fri",
                time = "18:25",
                destination = "Paris",
                airport = "John F. Kennedy",
                backgroundStyle = BackgroundStyle.SINGLE,
            ),
            PlaceItemState(
                id = "2",
                timestamp = Time("2025-10-18T18:25 +0200"),
                imageUrl = "",
                placeName = "Paris",
                dateStart = "18 Oct",
                dateEnd = "20 Oct",
            ),
            FlightArrivalItemState(
                id = "3",
                showDate = true,
                timestamp = Time("2025-10-18T10:05 +0200"),
                dayOfMonth = "18",
                dayOfWeek = "Sat",
                time = "10:05",
                airport = "Charles de Gaule",
                backgroundStyle = BackgroundStyle.TOP,
            ),
            HotelCheckInItemState(
                id = "4",
                showDate = false,
                timestamp = Time("2025-10-18T15:00 +0200"),
                dayOfMonth = "18",
                dayOfWeek = "Sat",
                time = "15:00",
                hotelName = "Hotel Novotel Paris Les Halles",
                hotelAddress = "Rue de fleury, 143",
                backgroundStyle = BackgroundStyle.MIDDLE,
            ),
            TripItemState.RestaurantReservationItemState(
                id = "5",
                showDate = false,
                timestamp = Time("2025-10-18T19:00 +0200"),
                dayOfWeek = "Sat",
                dayOfMonth = "18",
                time = "19:00",
                restaurantName = "Au pied de cochon",
                restaurantAddress = "Rue do cochon, 82",
                backgroundStyle = BackgroundStyle.BOTTOM,
            ),
            EmptyDateItemState(
                id = "6",
                timestamp = Time("2025-10-19T19:00 +0200"),
                dayOfMonth = "19",
                dayOfWeek = "Fri",
            ),
            HotelCheckOutItemState(
                id = "7",
                showDate = true,
                timestamp = Time("2025-10-22T11:00 +0200"),
                dayOfMonth = "22",
                dayOfWeek = "Sun",
                time = "11:00",
                hotelName = "Hotel Novotel Paris Les Halles",
                backgroundStyle = BackgroundStyle.TOP,
            ),
            FlightDepartureItemState(
                id = "8",
                showDate = false,
                timestamp = Time("2025-10-22T16:25 +0200"),
                dayOfMonth = "22",
                dayOfWeek = "Sun",
                time = "16:25",
                destination = "Rome",
                airport = "Charles de Gaule",
                backgroundStyle = BackgroundStyle.BOTTOM,
            ),
            PlaceItemState(
                id = "9",
                timestamp = Time("2025-10-22T16:25 +0200"),
                imageUrl = "",
                placeName = "Rome",
                dateStart = "22 Oct",
                dateEnd = "25 Oct",
            ),
            FlightArrivalItemState(
                id = "3",
                showDate = true,
                timestamp = Time("2025-10-22T19:05 +0200"),
                dayOfMonth = "22",
                dayOfWeek = "Sun",
                time = "19:05",
                airport = "Fiumicino Airport",
                backgroundStyle = BackgroundStyle.TOP,
            ),
            HotelCheckInItemState(
                id = "4",
                showDate = false,
                timestamp = Time("2025-10-22T15:00 +0200"),
                dayOfMonth = "22",
                dayOfWeek = "Sun",
                time = "15:00",
                hotelName = "Hotel Continental Roma",
                hotelAddress = "Rua di Roma",
                backgroundStyle = BackgroundStyle.BOTTOM,
            ),
        ),
        places = emptyList(),
    )
    AppTheme {
        TripDetails(
            state,
        )
    }
}

object TripDetailsDestination {
    const val ARG_TRIP_ID = "tripId"
    private const val ROUTE_NAME = "trip_details"
    const val ROUTE = "$ROUTE_NAME/{$ARG_TRIP_ID}"

    fun getRoute(tripId: String) = "$ROUTE_NAME/$tripId"
}
