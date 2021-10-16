package com.combah.travel2.ui.triplist.composable

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.combah.travel2.model.data.Trip
import com.combah.travel2.ui.triplist.TripListViewModel

@Composable
fun TripList(viewModel: TripListViewModel, onItemClick: (Trip) -> Unit) {
    val trips by viewModel.trips.observeAsState(emptyList())
    LazyColumn {
        items(trips) { trip ->
            TripListItem(
                name = trip.name,
                coverImageUrl = trip.coverImage,
                onClick = { onItemClick(trip) })
        }
    }
}