package travel.vola.android.ui.trip.creation.assistant.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.R
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.assistant.viewmodel.UiState

@Composable
fun InitialParameters(
    state: UiState.InitialParameters,
    onOptionTapped: (Int, UiState.OptionGroupType) -> Unit,
    onOptionAdded: (UiState.OptionGroupType, String) -> Unit,
    onAnythingElseTextChanged: (String) -> Unit,
    onNextTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
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
                        onClick = { onOptionTapped(index, group.type) },
                        label = {
                            Text(option.option)
                        },
                    )
                }
            }
        }
        Text("Anything Else?", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = state.anythingElse,
            placeholder = {
                Text(stringResource(R.string.prompt_additional_info))
            },
            onValueChange = onAnythingElseTextChanged,
            modifier = Modifier
                .padding(top = 4.dp)
                .height(96.dp),
        )
        Button(
            onClick = onNextTapped,
            enabled = state.ctaEnabled,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(
                when (state.ctaType) {
                    UiState.CTAType.NEXT -> "Next"
                    UiState.CTAType.UPDATE -> "Update Trip"
                },
            )
        }
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
                            listOfOptions("Workation", "Vacation", "Business Trip", "Family Trip"),
                        ),
                        UiState.OptionGroup(
                            UiState.OptionGroupType.INTERESTS,
                            listOfOptions("Hiking", "Shopping", "Sightseeing"),
                        ),
                    ),
                ),
                onOptionTapped = { _, _ -> },
                onOptionAdded = { _, _ -> },
                onAnythingElseTextChanged = {},
                onNextTapped = {},
            )
        }
    }
}
