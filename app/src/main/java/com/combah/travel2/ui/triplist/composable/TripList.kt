package com.combah.travel2.ui.triplist.composable

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import com.combah.travel2.model.data.Trip

@ExperimentalMaterialApi
@Composable
fun TripList(trips: List<Trip>, onItemClick: (Trip) -> Unit) {
    LazyColumn {
        items(trips) { trip ->
            TripListItem(
                name = trip.name,
                coverImageUrl = trip.coverImage,
                onClick = { onItemClick(trip) })
        }
    }
}