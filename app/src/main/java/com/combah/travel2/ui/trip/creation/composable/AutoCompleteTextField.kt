package com.combah.travel2.ui.trip.creation.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.window.PopupProperties

class AutoCompleteTextFieldState(
    val initialValue: String?,
    val suggestions: List<String>,
)

@Composable
fun rememberAutoCompleteTextFieldState(initialValue: String?, suggestions: List<String>) =
    remember(initialValue, suggestions) { AutoCompleteTextFieldState(initialValue, suggestions) }

@Composable
fun AutoCompleteTextField(
    state: AutoCompleteTextFieldState,
    onTextChanged: (String) -> Unit,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = modifier
    ) {
        var input: String? by remember {
            mutableStateOf(null)
        }
        var selection: String? by remember {
            mutableStateOf(null)
        }
        val airportFromText = remember(state.initialValue, input, selection) {
            input ?: selection ?: state.initialValue.orEmpty()
        }
        OutlinedTextField(
            value = airportFromText,
            label = {
                Text("from")
            },
            placeholder = {
                Text("Enter City or Airport")
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
                    text = { Text(option) },
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
