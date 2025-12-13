package travel.vola.android.ui.trip.creation.assistant.composable

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.assistant.viewmodel.TripCreationAssistantViewModel.UiState
import travel.vola.android.ui.trip.creation.composable.DatePickerButton
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BasicInformationForm(state: UiState.BasicInformation, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("Destinations", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            state.destinations.forEach { destination ->
                InputChip(selected = false, onClick = {}, label = { Text(destination) })
            }
            TextField(
                value = "",
                onValueChange = {},
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.widthIn(min = 120.dp),
                label = { Text("Enter Destination") },
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = state.fixedDates, onCheckedChange = {})
            Text("Fixed Dates")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Start Date")
            DatePickerButton(label = "Pick Date", selectedTime = state.startDate) { }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("End Date")
            DatePickerButton(label = "Pick Date", selectedTime = state.startDate) { }
        }
        Text("Group Type", style = MaterialTheme.typography.titleMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            UiState.TravelGroupType.entries.forEachIndexed { index, option ->
                ToggleButton(
                    checked = state.groupType == option,
                    onCheckedChange = {},
                    shapes = when (index) {
                        0 -> {
                            ButtonGroupDefaults.connectedLeadingButtonShapes()
                        }

                        UiState.TravelGroupType.entries.lastIndex -> {
                            ButtonGroupDefaults.connectedTrailingButtonShapes()
                        }

                        else -> {
                            ButtonGroupDefaults.connectedMiddleButtonShapes()
                        }
                    },
                ) {
                    Text(option.label)
                }
            }
        }
        OutlinedTextField(
            state.travelers.toString(),
            onValueChange = {},
            label = { Text("Travelers") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.widthIn(max = 120.dp)
        )
        Button(
            onClick = {},
            enabled = false,
            modifier = Modifier.align(Alignment.End)
        ) { Text("Next") }
    }
}

private val UiState.TravelGroupType.label
    get() = when (this) {
        UiState.TravelGroupType.SOLO -> "Solo"
        UiState.TravelGroupType.FAMILY -> "Family"
        UiState.TravelGroupType.FRIENDS -> "Friends"
        UiState.TravelGroupType.COWORKERS -> "Coworkers"
        UiState.TravelGroupType.COUPLE -> "Couple"
    }

@Composable
@Preview
fun BasicInformationFormPreview() {
    AppTheme {
        Surface {
            BasicInformationForm(
                state = UiState.BasicInformation(
                    destinations = listOf("Paris", "London"),
                    startDate = ZonedDateTime.now(),
                    endDate = ZonedDateTime.now().plusDays(10),
                    fixedDates = false,
                    groupType = UiState.TravelGroupType.COUPLE,
                    travelers = 2,
                )
            )
        }
    }
}