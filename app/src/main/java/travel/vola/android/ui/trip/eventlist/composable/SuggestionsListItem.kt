package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.ui.trip.state.TripItemState

@Composable
fun SuggestionsListItem(event: TripItemState.SuggestionsItemState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 16.dp)
    ) {
        Text(event.text)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            event.predictedChanges.forEach {
                Button(onClick = {}, modifier = Modifier.weight(1f)) {
                    Text(it, )
                }
            }
        }
    }
}

@Preview
@Composable
fun SuggestionsListItemPreview() {
    SuggestionsListItem(
        TripItemState.SuggestionsItemState(
            text = "Place 1, Place 2, Place 3",
            predictedChanges = listOf("More Museums", "Less Free Time", "More Restaurants")
        )
    )
}