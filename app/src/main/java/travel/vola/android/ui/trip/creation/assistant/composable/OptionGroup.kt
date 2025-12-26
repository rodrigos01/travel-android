package travel.vola.android.ui.trip.creation.assistant.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import travel.vola.android.common.ui.components.InlinedTextField
import travel.vola.android.ui.trip.creation.assistant.viewmodel.UiState


@Composable
fun OptionGroup(
    title: String,
    onOptionAdded: (String) -> Unit = {},
    options: @Composable FlowRowScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            content = {
                options()
                InlinedTextField(
                    onDone = { textFieldContent ->
                        if (textFieldContent.isNotBlank()) {
                            onOptionAdded(textFieldContent)
                        }
                    },
                    label = { Text("Add More") }
                )
            },
        )
    }
}

fun listOfOptions(vararg options: String, selected: Int = -1): List<UiState.Option> =
    options.mapIndexed { index, option -> UiState.Option(option, isSelected = index == selected) }