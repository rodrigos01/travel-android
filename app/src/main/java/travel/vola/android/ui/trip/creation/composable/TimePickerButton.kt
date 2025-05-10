package travel.vola.android.ui.trip.creation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import travel.vola.android.common.ui.components.Clock
import travel.vola.android.ui.theme.AppTheme

@Composable
fun TimePickerHost(
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    showTimePickerState: MutableState<Boolean> = remember { mutableStateOf(false) },
    timePickerDialogState: TimePickerDialogState = rememberTimePickerDialogState(),
    content: @Composable () -> Unit,
) {
    var showTimePicker: Boolean by showTimePickerState
    content()
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
    contentColor: Color = ButtonDefaults.textButtonColors().contentColor,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    iconSize: Dp = 24.dp,
    timePickerDialogState: TimePickerDialogState = rememberTimePickerDialogState(),
    showTimePickerState: MutableState<Boolean> = remember { mutableStateOf(false) },
) {
    var showTimePicker: Boolean by showTimePickerState
    TimePickerHost(
        onTimeSelected,
        timePickerDialogState = timePickerDialogState,
        showTimePickerState = showTimePickerState,
    ) {
        TextButton(
            onClick = { showTimePicker = true },
            colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
            modifier = modifier,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Clock,
                    contentDescription = null,
                    modifier = Modifier
                        .size(iconSize),
                )
                Text(
                    text = text,
                    style = textStyle,
                )
            }
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
                showTimePickerState = remember { mutableStateOf(false) }
            )
        }
    }
}
