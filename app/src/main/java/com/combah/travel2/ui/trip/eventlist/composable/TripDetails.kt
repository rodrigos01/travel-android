package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.combah.travel2.extensions.TimeFormatter
import com.combah.travel2.model.repository.AddFlightRepository
import com.combah.travel2.model.repository.mock.MockTripRepository
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.viewmodel.AddFlightUseCase
import com.combah.travel2.ui.trip.viewmodel.AddLodgingUseCase
import com.combah.travel2.ui.trip.viewmodel.AddPlanUseCase
import com.combah.travel2.ui.trip.viewmodel.TripViewModel
import com.combah.travel2.ui.trip.viewmodel.TripViewModel.TripItem.DateRangeItem
import com.combah.travel2.ui.trip.viewmodel.TripViewModel.TripItem.FlightArrivalItem
import com.combah.travel2.ui.trip.viewmodel.TripViewModel.TripItem.FlightDepartureItem
import com.combah.travel2.ui.trip.viewmodel.TripViewModel.TripItem.HotelCheckInItem
import com.combah.travel2.ui.trip.viewmodel.TripViewModel.TripItem.HotelCheckOutItem
import com.combah.travel2.ui.trip.viewmodel.TripViewModel.TripItem.MonthItem
import com.combah.travel2.ui.trip.viewmodel.TripViewModel.TripItem.PlaceItem

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
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
    ) {
        LazyColumn(contentPadding = it) {
            items(state.items) { event ->
                when (event) {
                    is MonthItem -> MonthEventListItem(event.month, event.year)
                    is DateRangeItem -> DateRangeListItem(
                        dayOfMonthStart = event.dayOfMonthStart,
                        dayOfWeekStart = event.dayOfWeekStart,
                        dayOfMonthEnd = event.dayOfMonthEnd,
                        dayOfWeekEnd = event.dayOfWeekEnd,
                    )

                    is PlaceItem -> PlaceEventListItem(
                        event.imageUrl,
                        event.placeName,
                        event.dateStart,
                        event.dateEnd
                    )

                    is FlightDepartureItem -> FlightEventListItem(
                        event.showDate,
                        event.dayOfMonth,
                        event.dayOfWeek,
                        event.time,
                        event.destination,
                        event.airport,
                        event.showDivider,
                    )

                    is FlightArrivalItem -> ArrivalEventListItem(
                        event.showDate,
                        event.dayOfMonth,
                        event.dayOfWeek,
                        event.time,
                        event.airport,
                        event.showDivider,
                    )

                    is HotelCheckInItem -> CheckinListItem(
                        event.showDate,
                        event.dayOfMonth,
                        event.dayOfWeek,
                        event.time,
                        event.hotelName,
                        event.showDivider,
                    )

                    is HotelCheckOutItem -> CheckoutListItem(
                        event.showDate,
                        event.dayOfMonth,
                        event.dayOfWeek,
                        event.time,
                        event.hotelName,
                        event.showDivider,
                    )

                    is AddFlightUseCase.AddFlightItem -> AddFlightListItem()
                    is AddLodgingUseCase.AddLodgingItem -> AddLodgingListItem()
                }
            }
        }
    }
}

@Composable
@Preview
fun TripDetailsPreview() {
    AppTheme(dynamicColor = false) {
        TripDetails(
            TripViewModel(
                MockTripRepository(),
                "minhaTrip",
                AddPlanUseCase(
                    AddFlightUseCase(AddFlightRepository(), TimeFormatter()),
                    AddLodgingUseCase(TimeFormatter())
                ),
                TimeFormatter(),
            ), rememberNavController()
        )
    }
}

object TripDetailsDestination {
    const val ARG_TRIP_ID = "tripId"
    private const val ROUTE_NAME = "trip_details"
    const val ROUTE = "$ROUTE_NAME/{$ARG_TRIP_ID}"

    fun getRoute(tripId: String) = "$ROUTE_NAME/$tripId"
}