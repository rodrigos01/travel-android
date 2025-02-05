package com.combah.travel2.ui.trip.creation.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combah.travel2.R
import com.combah.travel2.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerButton(
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    showTimePickerState: MutableState<Boolean> = remember { mutableStateOf(false) },
    timePickerDialogState: TimePickerDialogState = rememberTimePickerDialogState(),
    content: @Composable () -> Unit,
) {
    var showTimePicker: Boolean by showTimePickerState
    Surface(
        onClick = { showTimePicker = true },
        modifier = modifier,
        content = content,
    )
    if (showTimePicker) {
        TimePickerDialog(
            state = timePickerDialogState,
            onConfirm = {
                onTimeSelected(
                    timePickerDialogState.selectedHour,
                    timePickerDialogState.selectedMinutes
                )
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

@Composable
fun TimePickerTextButton(
    text: String,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    timePickerDialogState: TimePickerDialogState = rememberTimePickerDialogState(),
    showTimePickerState: MutableState<Boolean> = remember { mutableStateOf(false) },
) {
    TimePickerButton(
        onTimeSelected,
        modifier = modifier,
        timePickerDialogState = timePickerDialogState,
        showTimePickerState = showTimePickerState,
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

@Composable
@Preview
fun TimePickerButtonPreview() {
    AppTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            TimePickerTextButton(
                text = "pick a time",
                onTimeSelected = { _, _ -> },
                showTimePickerState = remember { mutableStateOf(true) }
            )
        }
    }
}
