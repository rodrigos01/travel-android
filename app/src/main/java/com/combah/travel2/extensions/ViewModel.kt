package com.combah.travel2.extensions

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

inline fun <reified VM : ViewModel> ComponentActivity.viewModel(noinline initializer: () -> VM): Lazy<VM> =
    viewModels {
        viewModelFactory {
            initializer {
                initializer()
            }
        }
    }