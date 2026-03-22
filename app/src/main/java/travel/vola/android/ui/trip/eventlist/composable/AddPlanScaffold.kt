package travel.vola.android.ui.trip.eventlist.composable

import travel.vola.android.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.composable.AddPlanType
import travel.vola.android.ui.trip.creation.composable.ConfirmationDialog
import travel.vola.android.ui.trip.creation.composable.TypeSelectorButton

@Composable
fun AddPlanScaffold(
    type: AddPlanType,
    onTypeSelected: (AddPlanType) -> Unit,
    typeSelectionEnabled: Boolean,
    deleteButtonEnabled: Boolean,
    onDeleteConfirmed: () -> Unit,
    primaryButtonEnabled: Boolean,
    primaryButtonLabel: String,
    onPrimaryButtonTapped: () -> Unit,
    secondaryButtonLabel: String,
    onSecondaryButtonTapped: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit),
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    if (showDeleteConfirmation) {
        ConfirmationDialog(
            onConfirm = onDeleteConfirmed,
            onDismiss = { showDeleteConfirmation = false },
            confirmButtonLabel = stringResource(R.string.action_delete),
            confirmButtonColors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            dismissButtonLabel = stringResource(R.string.action_cancel)
        ) {
            Text(stringResource(R.string.dialog_delete_plan_confirmation, type.label))
        }
    }
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row {
            TypeSelectorButton(
                initialType = type,
                onOptionSelected = onTypeSelected,
                enabled = typeSelectionEnabled,
            )
            if (deleteButtonEnabled) {
                Spacer(modifier = Modifier.weight(1F))
                TextButton(onClick = { showDeleteConfirmation = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Text(stringResource(R.string.action_delete))
                }
            }
        }
        content()
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(top = 8.dp, end = 16.dp)
                .align(Alignment.End)
        ) {
            OutlinedButton(onClick = onSecondaryButtonTapped) {
                Text(secondaryButtonLabel)
            }
            Button(onClick = onPrimaryButtonTapped, enabled = primaryButtonEnabled) {
                Text(primaryButtonLabel)
            }
        }
    }
}

@Composable
@Preview
fun AddPlanScaffoldPreview() {
    AppTheme {
        AddPlanScaffold(
            type = AddPlanType.Lodging,
            onTypeSelected = {},
            typeSelectionEnabled = true,
            deleteButtonEnabled = true,
            onDeleteConfirmed = {},
            primaryButtonEnabled = true,
            primaryButtonLabel = stringResource(R.string.action_save),
            onPrimaryButtonTapped = {},
            secondaryButtonLabel = stringResource(R.string.action_cancel),
            onSecondaryButtonTapped = {},
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text("Add Plan Content 1")
            Text("Add Plan Content 2")
        }
    }
}