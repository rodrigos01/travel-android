package travel.vola.android.ui.trip.creation.assistant.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import travel.vola.android.common.ui.components.SearchBox
import travel.vola.android.common.ui.components.SearchResult
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.assistant.viewmodel.TripCreationAssistantViewModel.UiState
import travel.vola.android.ui.trip.creation.composable.DatePickerButton
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BasicInformationForm(
    state: UiState.BasicInformation,
    onLocationSearchTextChanged: (CharSequence) -> Unit,
    onLocationSearchResultSelected: (Int) -> Unit,
    onDestinationClearTapped: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text("Destinations", style = MaterialTheme.typography.titleMedium)
        var showSearchDialog by remember { mutableStateOf(false) }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            state.destinations.forEachIndexed { index, destination ->
                InputChip(
                    selected = false,
                    onClick = {},
                    label = {
                        Text(destination)
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "",
                            modifier = Modifier
                                .clickable(onClick = { onDestinationClearTapped(index) })
                                .size(16.dp),
                        )
                    }
                )
            }
            TextButton(onClick = { showSearchDialog = true }) {
                Text("Add")
            }
        }
        AnimatedVisibility(visible = showSearchDialog) {
            SearchBoxDialog(
                onDismiss = { showSearchDialog = false },
                searchResults = state.destinationSearchResults,
                onLocationSearchTextChanged = onLocationSearchTextChanged,
                onLocationSearchResultSelected = onLocationSearchResultSelected,
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
private fun SearchBoxDialog(
    onDismiss: () -> Unit,
    searchResults: List<SearchResult>,
    onLocationSearchTextChanged: (CharSequence) -> Unit,
    onLocationSearchResultSelected: (Int) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        var query by remember { mutableStateOf("") }

        val focusRequester = remember { FocusRequester() }
        SearchBox(
            query = query,
            placeHolder = { Text("Enter location") },
            searchResults = searchResults,
            onQueryChange = {
                query = it
                onLocationSearchTextChanged(it)
            },
            onResultTapped = {
                onLocationSearchResultSelected(it)
                onDismiss()
            },
            focusRequester = focusRequester,
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
@Preview
fun BasicInformationFormPreview() {
    AppTheme {
        Surface {
            BasicInformationForm(
                state = UiState.BasicInformation(
                    destinations = listOf("Paris", "London"),
                    destinationSearchResults = emptyList(),
                    startDate = ZonedDateTime.now(),
                    endDate = ZonedDateTime.now().plusDays(10),
                    fixedDates = false,
                    groupType = UiState.TravelGroupType.COUPLE,
                    travelers = 2,
                ),
                onDestinationClearTapped = {},
                onLocationSearchTextChanged = {},
                onLocationSearchResultSelected = {},
            )
        }
    }
}