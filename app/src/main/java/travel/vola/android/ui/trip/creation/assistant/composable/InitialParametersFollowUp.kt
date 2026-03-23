package travel.vola.android.ui.trip.creation.assistant.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.R
import travel.vola.android.common.ui.components.InlinedTextField
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.assistant.viewmodel.UiState

@Composable
fun InitialParametersFollowUp(
    state: UiState.InitialParametersFollowUp,
    onOptionTapped: (Int, UiState.FollowUpQuestion) -> Unit,
    onCustomAnswerAdded: (String, UiState.FollowUpQuestion) -> Unit,
    onNextTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        state.questions.forEachIndexed { index, question ->
            if (index > 0) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            FlowRow {
                question.choices.forEachIndexed { index, option ->
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = {
                            Text(option)
                        },
                    )
                }
            }
            Text(
                question.question,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            question.answers.forEachIndexed { optionIndex, option ->
                ElevatedCard(
                    onClick = {
                        onOptionTapped(
                            optionIndex,
                            question,
                        )
                    },
                    colors = if (option.isSelected) {
                        CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    } else {
                        CardDefaults.elevatedCardColors()
                    },
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    Text(option.option, modifier = Modifier.padding(8.dp))
                }
            }
            InlinedTextField(
                initialValue = "",
                onDone = { onCustomAnswerAdded(it, question) },
                label = { Text(stringResource(R.string.option_something_else)) },
            )
        }
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
fun InitialParametersFollowUpPreview() {
    AppTheme {
        Surface {
            InitialParametersFollowUp(
                state = UiState.InitialParametersFollowUp(
                    questions = listOf(
                        UiState.FollowUpQuestion(
                            choices = listOf("workation"),
                            "What time of day would you prefer to work on weekdays?",
                            listOfOptions(
                                "I prefer to have my work in the Morning, when I'm the most productive",
                                "Aternoons are my favoriote time for working",
                                "No need to dedicate time for work, I'll just wing it LOL",
                                selected = 0,
                            ),
                        ),
                        UiState.FollowUpQuestion(
                            choices = listOf("workation", "nightlife"),
                            "What time of day would you prefer to work on weekdays?",
                            listOfOptions(
                                "I prefer to have my work in the Morning, when I'm the most productive",
                                "Aternoons are my favoriote time for working",
                                "No need to dedicate time for work, I'll just wing it LOL",
                                selected = 0,
                            ),
                        ),
                    ),
                ),
                onOptionTapped = { _, _ -> },
                onCustomAnswerAdded = { _, _ -> },
                onNextTapped = {},
            )
        }
    }
}
