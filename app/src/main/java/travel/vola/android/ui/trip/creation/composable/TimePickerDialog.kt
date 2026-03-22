package travel.vola.android.ui.trip.creation.composable

import travel.vola.android.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.extensions.timeString
import travel.vola.android.extensions.update
import travel.vola.android.ui.theme.AppTheme
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismissRequest: () -> Unit,
    initialTime: ZonedDateTime = ZonedDateTime.now(),
    minimumSelectableTime: ZonedDateTime? = null,
) {
    val state = rememberTimePickerState(initialTime.hour, initialTime.minute)
    val minDateTime = minimumSelectableTime ?: initialTime.update(
        hour = 0,
        minute = 0,
    )
    val selectedDateTime = initialTime.update(
        hour = state.hour,
        minute = state.minute,
    )
    val timeConfirmEnabled = selectedDateTime >= minDateTime
    ConfirmationDialog(
        onConfirm = {
            onTimeSelected(state.hour, state.minute)
        },
        onDismiss = onDismissRequest,
        confirmButtonEnabled = timeConfirmEnabled,
    ) {
        Column {
            TimePicker(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                state = state,
            )
            if (!timeConfirmEnabled) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.error_time_after_min, minDateTime.timeString),
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun TimePickerDialogPreview() {
    AppTheme {
        TimePickerDialog(
            onTimeSelected = { _, _ -> },
            onDismissRequest = {},
            initialTime = ZonedDateTime.now().update(hour = 9, minute = 57),
            minimumSelectableTime = ZonedDateTime.now().update(hour = 9, minute = 57),
        )
    }
}