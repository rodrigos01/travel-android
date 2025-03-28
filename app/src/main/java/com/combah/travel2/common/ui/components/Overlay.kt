package com.combah.travel2.common.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.combah.travel2.common.ui.preview.PreviewLightDarkSystemUI
import com.combah.travel2.ui.theme.AppTheme

@Composable
fun Overlay(content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.7F),
        content = content,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
@PreviewLightDarkSystemUI
fun OverlayPreview() {
    AppTheme {
        Overlay {
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.padding(16.dp)) {
                TextButton(
                    onClick = {}
                ) {
                    Text("Button")
                }
                Surface(
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth().aspectRatio(9 / 16F)
                ) {}
            }
        }
    }
}

