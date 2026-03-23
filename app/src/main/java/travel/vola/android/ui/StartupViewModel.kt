package travel.vola.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import travel.vola.android.model.data.ServerStatus
import travel.vola.android.model.repository.StartupRepository

class StartupViewModel(repository: StartupRepository) : ViewModel() {
    data class UiState(val isLoaded: Boolean = false, val status: ServerStatus? = null)

    val uiState = flow {
        val status = repository.checkStatus()
        emit(UiState(isLoaded = true, status = status))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    class Factory : ViewModelProvider.Factory by viewModelFactory(builder = {
        initializer {
            StartupViewModel(StartupRepository())
        }
    })
}
