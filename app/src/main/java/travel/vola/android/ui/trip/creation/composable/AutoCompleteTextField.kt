package travel.vola.android.ui.trip.creation.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.PopupProperties
import travel.vola.android.ui.theme.AppTheme

data class AutoCompleteTextFieldState<T>(
    val text: String?,
    val suggestions: List<T>,
)

@Composable
fun <T> rememberAutoCompleteTextFieldState(text: String?, suggestions: List<T>) =
    remember(text, suggestions) { AutoCompleteTextFieldState(text, suggestions) }

@Composable
fun <T> AutoCompleteTextField(
    state: AutoCompleteTextFieldState<T>,
    label: String?,
    placeHolder: String?,
    onTextChanged: (String) -> Unit,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: (T) -> String
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = modifier
    ) {
        var input: String? by remember {
            mutableStateOf(null)
        }
        var selection: T? by remember {
            mutableStateOf(null)
        }
        val text = remember(state.text, input, selection) {
            input ?: selection?.let { itemContent(it) } ?: state.text.orEmpty()
        }
        OutlinedTextField(
            value = text,
            label = {
                label?.let { Text(it) }
            },
            placeholder = {
                placeHolder?.let { Text(it) }
            },
            onValueChange = {
                input = it
                onTextChanged(it)
            },
        )
        var showSuggestions by remember(state.suggestions) {
            mutableStateOf(state.suggestions.isNotEmpty())
        }
        DropdownMenu(
            expanded = showSuggestions,
            onDismissRequest = { showSuggestions = false },
            properties = PopupProperties(focusable = false)
        ) {
            state.suggestions.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(itemContent(option)) },
                    onClick = {
                        showSuggestions = false
                        input = null
                        selection = option
                        onOptionSelected(index)
                        focusManager.clearFocus()
                    },
                    colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.onSecondaryContainer),
                )
            }
        }
    }
}

@Composable
@Preview
fun AutoCompleteTextFieldPreview() {
    AppTheme {
        Surface {
            AutoCompleteTextField(
                state = rememberAutoCompleteTextFieldState(null, List(4) { "Item$it" }),
                label = "label",
                placeHolder = null,
                onTextChanged = {},
                onOptionSelected = {},
                itemContent = { it }
            )
        }
    }
}
