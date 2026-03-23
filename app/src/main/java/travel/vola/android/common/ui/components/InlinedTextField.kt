package travel.vola.android.common.ui.components

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.ui.theme.AppTheme

@Composable
fun InlinedTextField(
    initialValue: String = "",
    onDone: (String) -> Unit,
    colors: ButtonColors? = null,
    label: @Composable (() -> Unit),
) {
    InlinedTextField(initialValue, InlinedTextFieldStyle.NORMAL, onDone, colors, label)
}

@Composable
fun OutlinedInlinedTextField(
    initialValue: String = "",
    onDone: (String) -> Unit,
    colors: ButtonColors? = null,
    label: @Composable (() -> Unit),
) {
    InlinedTextField(initialValue, InlinedTextFieldStyle.OUTLINED, onDone, colors, label)
}

private enum class InlinedTextFieldStyle {
    NORMAL,
    OUTLINED,
}

@Composable
private fun InlinedTextField(
    initialValue: String = "",
    style: InlinedTextFieldStyle,
    onDone: (String) -> Unit,
    colors: ButtonColors? = null,
    label: @Composable () -> Unit,
) {
    var showTextField by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    if (showTextField) {
        var textFieldContent by remember { mutableStateOf(initialValue) }
        fun done() {
            onDone(textFieldContent)
            showTextField = false
        }
        TextField(
            value = textFieldContent,
            onValueChange = { textFieldContent = it },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Companion.Transparent,
                focusedContainerColor = Color.Companion.Transparent,
                unfocusedIndicatorColor = Color.Companion.Transparent,
            ),
            modifier = Modifier
                .widthIn(min = 120.dp)
                .focusRequester(focusRequester),
            placeholder = label,
            keyboardActions = KeyboardActions(onDone = { done() }),
            trailingIcon = {
                if (textFieldContent.isNotBlank()) {
                    IconButton(onClick = { done() }) {
                        Icon(Icons.Default.Done, contentDescription = "confirm")
                    }
                }
            },
        )
        DisposableEffect(focusRequester) {
            focusRequester.requestFocus()
            onDispose {
                textFieldContent = ""
            }
        }
    } else {
        when (style) {
            InlinedTextFieldStyle.NORMAL -> {
                TextButton(
                    onClick = { showTextField = true },
                    colors = colors ?: ButtonDefaults.textButtonColors(),
                ) { label() }
            }

            InlinedTextFieldStyle.OUTLINED -> {
                OutlinedButton(
                    onClick = { showTextField = true },
                    colors = colors ?: ButtonDefaults.outlinedButtonColors(),
                ) { label() }
            }
        }
    }
}

@Composable
@Preview
fun InlinedTextFieldPreview() {
    AppTheme {
        val items = remember { mutableStateListOf<String>() }
        FlowRow {
            items.forEach {
                FilterChip(selected = false, onClick = {}, label = {
                    Text(it)
                })
            }
            OutlinedInlinedTextField(
                onDone = { items.add(it) },
                label = { Text("Add More") },
            )
        }
    }
}
