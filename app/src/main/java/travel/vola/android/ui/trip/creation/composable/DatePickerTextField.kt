package travel.vola.android.ui.trip.creation.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import travel.vola.android.extensions.format
import java.text.DateFormat
import java.util.Date
import java.util.TimeZone

@Composable
fun DatePickerTextField(
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier,
    date: Date? = null,
    minDate: Date? = null,
    label: String? = null,
) {
    val openDialog = remember {
        mutableStateOf(false)
    }
    val dateTimePickerState = rememberDateTimePickerState(minDate, date)
    when {
        openDialog.value -> DateTimePickerDialog(
            state = dateTimePickerState,
            onDismiss = { openDialog.value = false },
            onDateSelected = { timestamp ->
                openDialog.value = false
                onDateSelected(Date(timestamp))
            },
        )
    }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = date?.format(
                style = DateFormat.SHORT,
                includeTime = true,
                targetTimeZone = TimeZone.getTimeZone("UTC"),
            ) ?: "",
            onValueChange = {},
            label = { Text(text = label ?: "") },
            readOnly = true,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0f)
                .clickable(onClick = { openDialog.value = true }),
        )
    }
}

@Composable
@Preview
fun DatePickerTextFieldPreview() = DatePickerTextField(onDateSelected = {})
