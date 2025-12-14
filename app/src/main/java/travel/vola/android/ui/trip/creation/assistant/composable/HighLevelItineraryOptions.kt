package travel.vola.android.ui.trip.creation.assistant.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.extensions.dateString
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.assistant.viewmodel.TripCreationAssistantViewModel.UiState
import java.time.ZonedDateTime

@Composable
fun HighLevelItineraryOptions(
    state: UiState.HighLevelItineraryOptions,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf<UiState.Itinerary?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        AnimatedContent(selected) { selectedItinerary ->
            if (selectedItinerary != null) {
                ItineraryCard(selectedItinerary, onTap = {})
                selectedItinerary.predictedChanges.forEach { option ->
                    ElevatedCard(
                        onClick = {},
                    ) {
                        Text(option.option, modifier = Modifier.padding(8.dp))
                    }
                }
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = { selected = null }) {
                        Text("Return to list")
                    }
                    Button(onClick = {}) {
                        Text("Create Trip")
                    }
                }
            } else {
                Text("Select the itinerary that more closely matches your preferences. You can make changes to it after selecting it.")
                state.itineraries.forEach { itinerary ->
                    ItineraryCard(itinerary, { selected = itinerary })
                }
            }
        }
    }
}

@Composable
fun ItineraryCard(itinerary: UiState.Itinerary, onTap: () -> Unit) {
    Card(
        onClick = onTap,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                itinerary.name,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "${itinerary.startDate.dateString} - ${itinerary.endDate.dateString}",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                itinerary.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Column(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(8.dp)
                    .fillMaxWidth(),
            ) {
                itinerary.cities.forEach { city ->
                    Text(
                        city.name,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        "${city.startDate.dateString} - ${city.endDate.dateString}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun HighLevelItineraryOptionsPreview() {
    AppTheme {
        Surface {
            HighLevelItineraryOptions(
                state = UiState.HighLevelItineraryOptions(
                    itineraries = listOf(
                        UiState.Itinerary(
                            name = "Scandi Design",
                            description = "Explore design in scandinavia",
                            startDate = ZonedDateTime.now(),
                            endDate = ZonedDateTime.now().plusDays(10),
                            cities = listOf(
                                UiState.ItineraryCity(
                                    name = "Copenhagen",
                                    startDate = ZonedDateTime.now(),
                                    endDate = ZonedDateTime.now().plusDays(3),
                                ),
                                UiState.ItineraryCity(
                                    name = "Stockholm",
                                    startDate = ZonedDateTime.now().plusDays(3),
                                    endDate = ZonedDateTime.now().plusDays(6),
                                ),
                                UiState.ItineraryCity(
                                    name = "Tromso",
                                    startDate = ZonedDateTime.now().plusDays(6),
                                    endDate = ZonedDateTime.now().plusDays(10),
                                ),
                            ),

                            predictedChanges = listOfOptions(
                                "Gimme more Lights!",
                                "More coffee cities"
                            ),
                        )
                    ),
                )
            )
        }
    }
}