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
import com.combah.travel2.ui.theme.AppTheme

@Composable
fun ConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonEnabled: Boolean = true,
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
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                ) {
                    content()
                }
                Row(modifier = Modifier.align(Alignment.End).padding(end = 8.dp, bottom = 8.dp)) {
                    TextButton(
                        onClick = onDismiss,
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
fun ConfirmationDialogPreview() = AppTheme {
    ConfirmationDialog(
        onConfirm = {},
        onDismiss = {},
        confirmButtonEnabled = true,
    ) { Text("This is a Dialog") }
}

