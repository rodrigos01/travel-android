package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.combah.travel2.model.data.Time
import com.combah.travel2.ui.theme.AppTheme
import com.combah.travel2.ui.trip.creation.composable.AddPlanType
import com.combah.travel2.ui.trip.creation.composable.AutoCompleteTextField
import com.combah.travel2.ui.trip.creation.composable.DatePickerButton
import com.combah.travel2.ui.trip.creation.composable.TimePickerButton
import com.combah.travel2.ui.trip.creation.composable.TimePickerTextButton
import com.combah.travel2.ui.trip.creation.composable.TypeSelectorButton
import com.combah.travel2.ui.trip.creation.composable.rememberAutoCompleteTextFieldState
import java.util.TimeZone

@Composable
fun AddLodgingListItem(
    onTypeSelected: (AddPlanType) -> Unit,
    checkInTime: String? = null,
    onCheckInTimeChanged: (hour: Int, minute: Int) -> Unit,
    lodgingLabel: String? = null,
    onLodgingTextChanged: (CharSequence) -> Unit,
    lodgingSearchResults: List<String> = emptyList(),
    lodgingSearchResultTapped: (Int) -> Unit,
    checkOutDayOfMonth: String,
    checkOutDayOfWeek: String,
    minCheckoutDate: Time,
    onCheckOutDateChanged: (Time) -> Unit,
    checkOutTime: String? = null,
    onCheckOutTimeChanged: (hour: Int, minute: Int) -> Unit,
    onSaveButtonTapped: () -> Unit,
    onCancelButtonTapped: () -> Unit,
) {
    ConstraintLayout(
        Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
    ) {
        val (
            checkInLabel,
            checkInTimeSelector,
            typeSelector,
            lodgingText,
            checkOutLabel,
            checkOutDaySelector,
            checkOutTimeText,
            cancelButton,
            saveButton,
        ) = createRefs()
        Text(text = "Check-In",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.constrainAs(checkInLabel) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(parent.start, margin = 16.dp)
            })
        TimePickerTextButton(
            text = checkInTime ?: "Choose check-in Time",
            onCheckInTimeChanged,
            modifier = Modifier
                .semantics { role = Role.Button }
                .constrainAs(checkInTimeSelector) {
                    top.linkTo(checkInLabel.top)
                    bottom.linkTo(checkInLabel.bottom)
                    start.linkTo(lodgingText.start)
                },
        )
        TypeSelectorButton(
            initialType = AddPlanType.Lodging,
            onOptionSelected = onTypeSelected,
            modifier = Modifier
                .constrainAs(typeSelector) {
                    top.linkTo(lodgingText.top, margin = 8.dp)
                    bottom.linkTo(lodgingText.bottom)
                    start.linkTo(parent.start, margin = 16.dp)
                    height = Dimension.fillToConstraints
                }
                .width(96.dp))
        AutoCompleteTextField(
            state = rememberAutoCompleteTextFieldState(
                lodgingLabel, lodgingSearchResults,
            ),
            label = "Lodging",
            placeHolder = "Hotel name or address",
            onLodgingTextChanged,
            lodgingSearchResultTapped,
            modifier = Modifier
                .constrainAs(lodgingText) {
                    start.linkTo(typeSelector.end, margin = 8.dp)
                    top.linkTo(checkInTimeSelector.bottom)
                    end.linkTo(parent.end, margin = 16.dp)
                    width = Dimension.fillToConstraints
                }
                .wrapContentSize(Alignment.TopStart)
        )
        Text(text = "Check-out",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.constrainAs(checkOutLabel) {
                top.linkTo(typeSelector.bottom, margin = 8.dp)
                start.linkTo(parent.start, margin = 16.dp)
            })
        DatePickerButton(
            minCheckoutDate,
            checkOutDayOfMonth,
            checkOutDayOfWeek,
            onCheckOutDateChanged,
            modifier = Modifier
                .constrainAs(checkOutDaySelector) {
                    top.linkTo(checkOutLabel.bottom, margin = 8.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                }
                .width(96.dp)
                .wrapContentHeight(),
        )
        val timePickerButtonState = remember {
            mutableStateOf(false)
        }
        val focusManager = LocalFocusManager.current
        TimePickerButton(
            onTimeSelected = { hour, minute ->
                onCheckOutTimeChanged(hour, minute)
                focusManager.clearFocus()
            },
            showTimePickerState = timePickerButtonState,
            modifier = Modifier.constrainAs(checkOutTimeText) {
                start.linkTo(checkOutDaySelector.end, margin = 8.dp)
                top.linkTo(checkOutDaySelector.top)
                bottom.linkTo(checkOutDaySelector.bottom)
                end.linkTo(parent.end, margin = 16.dp)
                width = Dimension.fillToConstraints
            }
        ) {
            OutlinedTextField(
                value = checkOutTime.orEmpty(),
                label = { Text("Check-out time") },
                placeholder = { Text("Check-out time") },
                onValueChange = {},
                modifier = Modifier.onFocusChanged {
                    if (it.hasFocus) {
                        timePickerButtonState.value = true
                    }
                }
            )
        }
        OutlinedButton(
            onClick = { onCancelButtonTapped() },
            modifier = Modifier.constrainAs(cancelButton) {
                top.linkTo(saveButton.top)
                bottom.linkTo(saveButton.bottom)
                end.linkTo(saveButton.start, margin = 8.dp)
            }
        ) {
            Text("Cancel")
        }
        Button(
            onClick = { onSaveButtonTapped() },
            modifier = Modifier.constrainAs(saveButton) {
                top.linkTo(checkOutDaySelector.bottom, margin = 8.dp)
                bottom.linkTo(parent.bottom, margin = 16.dp)
                end.linkTo(parent.end, margin = 16.dp)
            }
        ) {
            Text("Save")
        }
    }
}

@Composable
@Preview
fun AddLodgingListItemPreview() {
    AppTheme {
        AddLodgingListItem(
            onTypeSelected = {},
            onCheckInTimeChanged = { _, _ -> },
            onLodgingTextChanged = {},
            lodgingSearchResultTapped = {},
            checkOutDayOfMonth = "15",
            checkOutDayOfWeek = "Wed",
            onCheckOutDateChanged = {},
            checkOutTime = null,
            minCheckoutDate = Time(0L, TimeZone.getDefault()),
            onCheckOutTimeChanged = { _, _ -> },
            onSaveButtonTapped = {},
            onCancelButtonTapped = {},
        )
    }
}
