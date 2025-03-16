package com.combah.travel2.ui.trip.creation.composable

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.extensions.Time
import com.combah.travel2.extensions.now
import com.combah.travel2.extensions.update
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import kotlinx.coroutines.launch
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerButton(
    minimumSelectableTime: Time?,
    onDateSelected: (Time) -> Unit,
    modifier: Modifier = Modifier,
    selectedTime: Time? = null,
    content: @Composable () -> Unit,
) {
    val minTimeInDeviceTimeZone =
        minimumSelectableTime?.update(timeZone = TimeZone.getTimeZone("UTC"))
    val selectedTimeInDeviceTimeZone = selectedTime?.update(timeZone = TimeZone.getTimeZone("UTC"))
    var showDatePicker by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Initial)
                    val press = PressInteraction.Press(down.position)
                    coroutineScope.launch { interactionSource.emit(press) }
                    waitForUpOrCancellation(pass = PointerEventPass.Initial)?.let {
                        coroutineScope.launch {
                            interactionSource.emit(
                                PressInteraction.Release(
                                    press
                                )
                            )
                        }
                        showDatePicker = true
                    } ?: coroutineScope.launch {
                        interactionSource.emit(
                            PressInteraction.Cancel(
                                press
                            )
                        )
                    }
                }
            }
            .indication(interactionSource, LocalIndication.current),
    ) {
        content()
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedTimeInDeviceTimeZone?.timeInMillis,
            initialDisplayedMonthMillis = selectedTimeInDeviceTimeZone?.timeInMillis
                ?: minTimeInDeviceTimeZone?.timeInMillis ?: Time.now().timeInMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return minTimeInDeviceTimeZone == null || utcTimeMillis >= minTimeInDeviceTimeZone.timeInMillis
                }
            }
        )
        ConfirmationDialog(
            onConfirm = {
                datePickerState.selectedTime?.let {
                    onDateSelected(
                        it
                    )
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            confirmButtonEnabled = true,
        ) {
            DatePicker(
                state = datePickerState,
            )
        }
    }
}

@Composable
@Preview
fun DatePickerButtonPreview() {
    AppTheme {
        Surface {
            DatePickerButton(
                selectedTime = Time("2025-11-28T00:00 -0300"),
                minimumSelectableTime = null,
                onDateSelected = {},
            ) {
                TextButton(onClick = {}) { Text("Pick Date") }
            }
        }
    }
}
