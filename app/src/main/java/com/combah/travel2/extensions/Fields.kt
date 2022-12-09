package com.combah.travel2.extensions

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun moveFocus(direction: FocusDirection): KeyboardActions {
    val focusManager = LocalFocusManager.current
    return KeyboardActions(onNext = { focusManager.moveFocus(direction)})
}