package com.combah.travel2.ui.trip.creation.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonEnabled: Boolean,
    confirmButtonLabel: String = "Confirm",
    confirmButtonColors: ButtonColors = ButtonDefaults.textButtonColors(),
    dismissButtonLabel: String = "Cancel",
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .padding(all = 16.dp)
                ) {
                    content()
                }
                Row(modifier = Modifier.align(Alignment.End).padding(end = 8.dp, bottom = 8.dp)) {
                    TextButton(
                        onClick = onConfirm,
                    ) {
                        Text(dismissButtonLabel)
                    }
                    TextButton(
                        enabled = confirmButtonEnabled,
                        colors = confirmButtonColors,
                        onClick = onConfirm,
                    ) {
                        Text(confirmButtonLabel)
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun ConfirmationDialogPreview() = ConfirmationDialog(
    onConfirm = {},
    onDismiss = {},
    confirmButtonEnabled = true,
    content = { Text("Dialog") },
)
