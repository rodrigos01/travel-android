package com.combah.travel2.extensions

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

@Composable
inline fun <reified VM : ViewModel> viewModel(noinline initializer: () -> VM): VM =
    viewModel(
        factory = viewModelFactory {
            initializer {
                initializer()
            }
        }
    )