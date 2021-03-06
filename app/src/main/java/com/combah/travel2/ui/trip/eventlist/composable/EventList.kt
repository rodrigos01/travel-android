package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.data.Hotel
import com.combah.travel2.model.data.Place
import com.combah.travel2.ui.data.*
import java.util.*

@Composable
fun EventList(events: List<TripEvent>, firstEvents: Set<TripEvent>) {
    LazyColumn {
        items(events) { event ->
            val isFirst = firstEvents.contains(event)
            when (event) {
                is MonthEvent -> MonthEventListItem(event = event)
                is PlaceEvent -> PlaceEventListItem(event = event)
                is FlightEvent -> FlightEventListItem(event = event, firstInDate = isFirst)
                is ArrivalEvent -> ArrivalEventListItem(event = event, firstInDate = isFirst)
                is CheckinEvent -> CheckinListItem(event = event, firstInDate = isFirst)
                is CheckoutEvent -> CheckoutListItem(event = event, firstInDate = isFirst)
            }
        }
    }
}