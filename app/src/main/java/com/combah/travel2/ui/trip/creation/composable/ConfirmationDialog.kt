package com.combah.travel2.ui.trip.creation.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    buttonEnabled: Boolean,
    errorMessage: String? = null,
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
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    content()
                }
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                TextButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.End),
                    enabled = buttonEnabled,
                    onClick = onConfirm,
                ) {
                    Text("Confirm")
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
    buttonEnabled = false,
    errorMessage = "Invalid Time",
    content = { Text("Dialog") },
)
