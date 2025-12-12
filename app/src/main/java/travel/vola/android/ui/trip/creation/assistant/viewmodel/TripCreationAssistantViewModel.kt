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

class TripCreationAssistantViewModel(private val repository: GenAIRepository) : ViewModel() {
    sealed interface UiState {
        data object Generating : UiState
        data class InitialParameters(
            val optionGroups: List<OptionGroup>,
        ) : UiState

        enum class OptionGroupType {
            OCCASIONS,
            INTERESTS,
            VIBE,
            FOCUS,
            DURATION,
            MUST_HAVE
        }

        data class OptionGroup(val type: OptionGroupType, val options: List<Option>)

        data class Option(val option: String, val isSelected: Boolean = false)
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
                    optionGroups = listOf(
                        UiState.OptionGroup(
                            UiState.OptionGroupType.OCCASIONS,
                            options.occasions.map { UiState.Option(it) }
                        ),
                        UiState.OptionGroup(
                            UiState.OptionGroupType.INTERESTS,
                            options.interests.map { UiState.Option(it) }
                        ),
                        UiState.OptionGroup(
                            UiState.OptionGroupType.VIBE,
                            options.vibe.map { UiState.Option(it) }
                        ),
                        UiState.OptionGroup(
                            UiState.OptionGroupType.FOCUS,
                            options.focus.map { UiState.Option(it) }
                        ),
                        UiState.OptionGroup(
                            UiState.OptionGroupType.DURATION,
                            options.duration.map { UiState.Option(it) }
                        ),
                        UiState.OptionGroup(
                            UiState.OptionGroupType.MUST_HAVE,
                            options.mustHave.map { UiState.Option(it) }
                        ),
                    ),
                )
            }
        }
    }

    fun onInitialParameterOptionTapped(index: Int, optionGroupType: UiState.OptionGroupType) {
        val state = uiState.value as? UiState.InitialParameters ?: return
        _uiState.value = state.copy(
            optionGroups = state.optionGroups.map { group ->
                if (group.type == optionGroupType) {
                    group.copy(options = group.options.mapIndexed { optionIndex, option ->
                        option.copy(
                            isSelected = if (optionIndex == index) !option.isSelected else option.isSelected
                        )
                    })
                } else {
                    group
                }
            }
        )
    }

    class Factory : ViewModelProvider.Factory by viewModelFactory(initializer = {
        TripCreationAssistantViewModel(factoryDependencies.genAIRepository)
    })
}