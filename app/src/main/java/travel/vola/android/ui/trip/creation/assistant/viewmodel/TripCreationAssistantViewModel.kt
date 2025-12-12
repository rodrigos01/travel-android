package travel.vola.android.ui.trip.creation.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import travel.vola.android.di.factoryDependencies
import travel.vola.android.extensions.viewModelFactory
import travel.vola.android.model.genai.GenAIData
import travel.vola.android.model.genai.GenAIRepository

class TripCreationAssistantViewModel(private val repository: GenAIRepository): ViewModel() {
    sealed interface UiState {
        data object Generating: UiState
        data class InitialParameters(
            val occasions: List<String>,
            val interests: List<String>,
            val vibe: List<String>,
            val focus: List<String>,
            val mustHave: List<String>,
            val duration: List<String>,
        ): UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Generating)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val basicInformation = GenAIData.BasicInformation(
                destination = "Scandinavia",
                dates = "February",
                duration = null,
                groupType = GenAIData.GroupType.SOLO,
                travelers = 1
            )
            val options = repository.genInitialParametersOptions(basicInformation)
            if (options != null) {
                _uiState.value = UiState.InitialParameters(
                    occasions = options.occasions,
                    interests = options.interests,
                    vibe = options.vibe,
                    focus = options.focus,
                    duration = options.duration,
                    mustHave = options.mustHave,
                )
            }
        }
    }

    class Factory : ViewModelProvider.Factory by viewModelFactory(initializer = {
        TripCreationAssistantViewModel(factoryDependencies.genAIRepository)
    })
}