package com.combah.travel2.ui.trip.creation.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.combah.travel2.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerButton(
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val timePickerState = rememberTimePickerState()
    var showTimePicker: Boolean by remember { mutableStateOf(false) }
    Surface(
        onClick = { showTimePicker = true },
        modifier = modifier,
        content = content,
    )
    if (showTimePicker) {
        ConfirmationDialog(
            onConfirm = {
                onTimeSelected(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
            buttonEnabled = true,
        ) {
            TimePicker(
                modifier = Modifier.padding(top = 16.dp), state = timePickerState,
            )
        }
    }
}

@Composable
fun TimePickerTextButton(
    text: String,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier,
) {
    TimePickerButton(
        onTimeSelected,
        modifier = modifier,
    ) {
        Row {
            Image(
                painter = painterResource(id = R.drawable.ic_time_16),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 4.dp)
            )
            Text(
                text = text,
                style = TextStyle(color = MaterialTheme.colorScheme.tertiary)
            )
        }
    }
}
