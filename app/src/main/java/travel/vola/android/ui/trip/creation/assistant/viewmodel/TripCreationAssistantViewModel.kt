package travel.vola.android.ui.trip.creation.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import travel.vola.android.di.factoryDependencies
import travel.vola.android.extensions.viewModelFactory
import travel.vola.android.model.genai.GenAIData
import travel.vola.android.model.genai.GenAIRepository

class TripCreationAssistantViewModel(private val repository: GenAIRepository) : ViewModel() {
    sealed interface UiState {
        data object Generating : UiState
        data class InitialParameters(
            val optionGroups: List<OptionGroup>,
            val nextButtonEnabled: Boolean = false,
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

        data class InitialParametersFollowUp(
            val questions: List<FollowUpQuestion>,
            val nextButtonEnabled: Boolean = false,
        ) : UiState

        data class FollowUpQuestion(val question: String, val answers: List<Option>)

        data class Option(val option: String, val isSelected: Boolean = false)
    }

    private enum class Stage {
        INITIAL_PARAMETERS,
        INITIAL_PARAMETERS_FOLLOW_UP
    }

    private val stage = MutableStateFlow(Stage.INITIAL_PARAMETERS)

    private val generatedState = stage.map {
        when (it) {
            Stage.INITIAL_PARAMETERS -> generateInitialParametersState()
            Stage.INITIAL_PARAMETERS_FOLLOW_UP -> getInitialParametersFollowUpState()
        }
    }
    private val internalState = MutableStateFlow<UiState>(UiState.Generating)

    val uiState = merge(generatedState, internalState).stateIn(
        viewModelScope,
        started = SharingStarted.Lazily,
        internalState.value
    )

    private val basicInformation = GenAIData.BasicInformation(
        destination = "Scandinavia",
        dates = "February",
        duration = null,
        groupType = GenAIData.GroupType.SOLO,
        travelers = 1
    )

    private suspend fun generateInitialParametersState(): UiState.InitialParameters? {

        val options =
            repository.genInitialParametersOptions(basicInformation) ?: return null
        return UiState.InitialParameters(
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

    private suspend fun getInitialParametersFollowUpState(): UiState.InitialParametersFollowUp? {
        val state = uiState.value as? UiState.InitialParameters ?: return null
        val parameters = GenAIData.InitialParametersOptions(
            occasions = state.optionGroups.selectedValues(UiState.OptionGroupType.OCCASIONS),
            interests = state.optionGroups.selectedValues(UiState.OptionGroupType.INTERESTS),
            vibe = state.optionGroups.selectedValues(UiState.OptionGroupType.VIBE),
            focus = state.optionGroups.selectedValues(UiState.OptionGroupType.FOCUS),
            duration = state.optionGroups.selectedValues(UiState.OptionGroupType.DURATION),
            mustHave = state.optionGroups.selectedValues(UiState.OptionGroupType.MUST_HAVE),
        )

        val followUpQuestions =
            repository.genInitialParametersFollowUpQuestions(parameters) ?: return null
        return UiState.InitialParametersFollowUp(
            questions = followUpQuestions.questions.map { question ->
                UiState.FollowUpQuestion(
                    question = question.question,
                    answers = question.answers.map { UiState.Option(it) },
                )
            }
        )
    }

    fun onInitialParameterOptionTapped(index: Int, optionGroupType: UiState.OptionGroupType) {
        val state = uiState.value as? UiState.InitialParameters ?: return
        val newOptionGroups = state.optionGroups.map { group ->
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
        internalState.value = state.copy(
            optionGroups = newOptionGroups,
            nextButtonEnabled = newOptionGroups.all { group ->
                group.options.any { it.isSelected }
            }
        )
    }

    fun onInitialParametersNextTapped() {
        internalState.value = UiState.Generating
        stage.value = Stage.INITIAL_PARAMETERS_FOLLOW_UP
    }

    fun onFollowUpQuestionOptionTapped(index: Int, question: UiState.FollowUpQuestion) {
        val state = uiState.value as? UiState.InitialParametersFollowUp ?: return
        val newQuestions = state.questions.map { currentQuestion ->
            if (currentQuestion == question) {
                currentQuestion.copy(answers = currentQuestion.answers.mapIndexed { optionIndex, option ->
                    option.copy(isSelected = optionIndex == index)
                })
            } else {
                currentQuestion
            }
        }
        internalState.value = state.copy(
            questions = newQuestions,
            nextButtonEnabled = newQuestions.all { question ->
                question.answers.any { it.isSelected }
            }
        )
    }

    fun onFollowUpQuestionsNextTapped() {

    }

    private fun List<UiState.OptionGroup>.selectedValues(type: UiState.OptionGroupType): List<String> =
        find { it.type == type }?.options?.filter { it.isSelected }?.map { it.option }
            ?: emptyList()

    class Factory : ViewModelProvider.Factory by viewModelFactory(initializer = {
        TripCreationAssistantViewModel(factoryDependencies.genAIRepository)
    })
}