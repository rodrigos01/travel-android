package com.combah.travel2.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment

fun Fragment.setContent(content: @Composable () -> Unit): ComposeView =
    ComposeView(requireContext()).apply {
        setContent(content)
    }