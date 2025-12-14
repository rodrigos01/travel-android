package travel.vola.android.ui.trip.creation.assistant.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import travel.vola.android.ui.trip.creation.assistant.viewmodel.TripCreationAssistantViewModel.UiState


@Composable
fun OptionGroup(
    title: String,
    onOptionAdded: (String) -> Unit = {},
    options: @Composable FlowRowScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)

        var textFieldContent by remember { mutableStateOf("") }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            content = {
                options()
                TextField(
                    value = textFieldContent,
                    onValueChange = { textFieldContent = it },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier.widthIn(min = 120.dp),
                    label = { Text("Add More") },
                    keyboardActions = KeyboardActions(onDone = {
                        if (textFieldContent.isNotBlank()) {
                            onOptionAdded(textFieldContent)
                            textFieldContent = ""
                        }
                    })
                )
            },
        )
    }
}

fun listOfOptions(vararg options: String, selected: Int = -1): List<UiState.Option> =
    options.mapIndexed { index, option -> UiState.Option(option, isSelected = index == selected) }