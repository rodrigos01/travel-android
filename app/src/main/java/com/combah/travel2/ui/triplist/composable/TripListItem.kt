package com.combah.travel2.ui.triplist.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter

@Composable
fun TripListItem(name: String?, coverImageUrl: String?, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Image(
            painter = rememberImagePainter(data = coverImageUrl),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.77f),
            contentDescription = "Place Description",
            contentScale = ContentScale.Crop
        )
        Text(
            text = name ?: "",
            style = MaterialTheme.typography.h5,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Start)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
@Preview
fun EventListItemPreview() {
    MaterialTheme {
        TripListItem(
            name = "Trip to Barcelona, Paris, Grindewald and Zurich",
            coverImageUrl = "",
            {})
    }
}