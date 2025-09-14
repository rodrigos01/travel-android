package travel.vola.android.extensions

import androidx.compose.runtime.Composable
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.plus
import androidx.lifecycle.viewmodel.viewModelFactory
import travel.vola.android.di.LocalViewModelCreationExtras

@Composable
inline fun <reified VM : ViewModel> viewModel(
    factory: ViewModelProvider.Factory,
    additionalExtras: CreationExtras = CreationExtras.Empty
): VM {
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    val creationExtras = if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
        viewModelStoreOwner.defaultViewModelCreationExtras
    } else {
        CreationExtras.Empty
    } + LocalViewModelCreationExtras.current + additionalExtras
    return viewModel(
        key = creationExtras.toString(),
        factory = factory,
        extras = creationExtras,
    )
}

inline fun <reified VM : ViewModel> viewModelFactory(noinline initializer: CreationExtras.() -> VM): ViewModelProvider.Factory =
    viewModelFactory {
        initializer(initializer)
    }