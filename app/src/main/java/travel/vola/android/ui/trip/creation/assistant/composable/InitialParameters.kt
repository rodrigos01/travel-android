package travel.vola.android.ui.trip.creation.assistant.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.assistant.viewmodel.TripCreationAssistantViewModel.UiState

@Composable
fun InitialParameters(
    state: UiState.InitialParameters,
    onInitialParameterOptionTapped: (Int, UiState.OptionGroupType) -> Unit,
    onInitialParametersNextTapped: () -> Unit,
    onOptionAdded: (UiState.OptionGroupType, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        state.optionGroups.forEach { group ->
            OptionGroup(
                title = when (group.type) {
                    UiState.OptionGroupType.OCCASIONS -> "Occasions"
                    UiState.OptionGroupType.INTERESTS -> "Interests"
                    UiState.OptionGroupType.VIBE -> "Vibe"
                    UiState.OptionGroupType.FOCUS -> "Focus"
                    UiState.OptionGroupType.DURATION -> "Duration"
                    UiState.OptionGroupType.MUST_HAVE -> "Must Have"
                },
                onOptionAdded = { onOptionAdded(group.type, it) },
            ) {
                group.options.forEachIndexed { index, option ->
                    FilterChip(
                        selected = option.isSelected,
                        onClick = { onInitialParameterOptionTapped(index, group.type) },
                        label = {
                            Text(option.option)
                        })
                }
            }
        }
        Text("Anything Else?", style = MaterialTheme.typography.titleLarge)
        Button(
            onClick = onInitialParametersNextTapped,
            enabled = state.nextButtonEnabled,
            modifier = Modifier.align(Alignment.End)
        ) { Text("Next") }
    }
}

@Composable
@Preview
fun InitialParametersPreview() {
    AppTheme {
        Surface {
            InitialParameters(
                state = UiState.InitialParameters(
                    optionGroups = listOf(
                        UiState.OptionGroup(
                            UiState.OptionGroupType.OCCASIONS,
                            listOfOptions("Workation", "Vacation", "Business Trip", "Family Trip")
                        ),
                        UiState.OptionGroup(
                            UiState.OptionGroupType.INTERESTS,
                            listOfOptions("Hiking", "Shopping", "Sightseeing")
                        )
                    )
                ),
                onInitialParameterOptionTapped = { _, _ -> },
                onInitialParametersNextTapped = {},
                onOptionAdded = { _, _ -> },
            )
        }
    }
}