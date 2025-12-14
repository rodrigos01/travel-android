package travel.vola.android.ui.trip.creation.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import travel.vola.android.common.ui.components.SearchResult
import travel.vola.android.di.factoryDependencies
import travel.vola.android.extensions.Time
import travel.vola.android.extensions.viewModelFactory
import travel.vola.android.model.genai.GenAIData
import travel.vola.android.model.genai.GenAIRepository
import travel.vola.android.model.repository.GeographyAutoCompleteRepository
import java.time.ZonedDateTime

class TripCreationAssistantViewModel(
    private val repository: GenAIRepository,
    private val destinationAutoCompleteRepository: GeographyAutoCompleteRepository,
) : ViewModel() {
    sealed interface UiState {
        data object Generating : UiState

        data object Error : UiState

        data class BasicInformation(
            val destinations: List<String> = emptyList(),
            val destinationSearchResults: List<SearchResult> = emptyList(),
            val startDate: ZonedDateTime? = null,
            val endDate: ZonedDateTime? = null,
            val fixedDates: Boolean = false,
            val groupType: TravelGroupType = TravelGroupType.SOLO,
            val travelers: Int = 1,
        ) : UiState

        enum class TravelGroupType {
            SOLO,
            COUPLE,
            FAMILY,
            FRIENDS,
            COWORKERS
        }

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

        data class HighLevelItineraryOptions(
            val itineraries: List<Itinerary>,
            val predictedChanges: List<Option>,
        ) : UiState

        data class Itinerary(
            val name: String,
            val description: String,
            val startDate: ZonedDateTime,
            val endDate: ZonedDateTime,
            val cities: List<ItineraryCity>,
        )

        data class ItineraryCity(
            val name: String,
            val startDate: ZonedDateTime,
            val endDate: ZonedDateTime,
        )

        data class Option(val option: String, val isSelected: Boolean = false)
    }

    private sealed interface Stage {
        data object BasicInformation : Stage
        data object InitialParameters : Stage
        data class InitialParametersFollowUp(val state: UiState.InitialParameters) : Stage
        data class HighLevelItineraryOptions(val state: UiState.InitialParametersFollowUp) : Stage
    }

    private val stage = MutableStateFlow<Stage>(Stage.BasicInformation)

    private val generatedState = stage.map {
        when (it) {
            is Stage.BasicInformation -> UiState.BasicInformation()
            is Stage.InitialParameters -> generateInitialParametersState()
            is Stage.InitialParametersFollowUp -> getInitialParametersFollowUpState(it.state)
            is Stage.HighLevelItineraryOptions -> getHighLevelItineraryOptionsState(it.state)
        } ?: UiState.Error
    }
    private val internalState = MutableStateFlow<UiState>(UiState.BasicInformation())

    val uiState = merge(generatedState, internalState).stateIn(
        viewModelScope,
        started = SharingStarted.Lazily,
        internalState.value
    )

    private val basicInformation = GenAIData.BasicInformation(
        destination = "France, Switzerland, Italy",
        dates = "July",
        duration = null,
        groupType = GenAIData.GroupType.COUPLE,
        travelers = 2
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

    private suspend fun getInitialParametersFollowUpState(state: UiState.InitialParameters): UiState.InitialParametersFollowUp? {
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

    private suspend fun getHighLevelItineraryOptionsState(state: UiState.InitialParametersFollowUp): UiState.HighLevelItineraryOptions? {
        val answeredQuestions = state.questions.map { question ->
            GenAIData.FollowUpQuestion(
                parameter = "",
                parameterSelection = "",
                question = question.question,
                answers = question.answers.filter { it.isSelected }.map { it.option },
            )
        }
        val result = repository.genHighLevelItineraryOptions(answeredQuestions) ?: return null
        return UiState.HighLevelItineraryOptions(
            itineraries = result.itineraries.map { itinerary ->
                UiState.Itinerary(
                    name = itinerary.name,
                    description = itinerary.description,
                    startDate = Time(itinerary.startDate),
                    endDate = Time(itinerary.endDate),
                    cities = itinerary.cities.map {
                        UiState.ItineraryCity(
                            name = it.name,
                            startDate = Time(it.startDate),
                            endDate = Time(it.endDate),
                        )
                    },
                )
            },
            predictedChanges = result.predictedChanges.map { UiState.Option(it) },
        )
    }

    fun onDestinationSearchTextChanged(query: CharSequence) {
        val state = uiState.value as? UiState.BasicInformation ?: return
        viewModelScope.launch {
            val results = destinationAutoCompleteRepository.autocomplete(query.toString())
            if (results.isNotEmpty()) {
                internalState.value = state.copy(
                    destinationSearchResults = results.map { SearchResult(it.name, it.address) }
                )
            }
        }
    }

    fun onDestinationSearchResultSelected(index: Int) {
        viewModelScope.launch {
            val state = uiState.value as? UiState.BasicInformation ?: return@launch
            val selected = state.destinationSearchResults[index]
            val destinationName = selected.title + selected.subtitle.takeIf { it.isNotBlank() }
                ?.let { ", $it" }.orEmpty()
            internalState.value = state.copy(
                destinations = state.destinations + destinationName,
                destinationSearchResults = emptyList()
            )
        }
    }

    fun onDestinationClearTapped(index: Int) {
        val state = uiState.value as? UiState.BasicInformation ?: return
        internalState.value = state.copy(
            destinations = state.destinations - state.destinations[index]
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
        val state = uiState.value as? UiState.InitialParameters ?: return
        internalState.value = UiState.Generating
        stage.value = Stage.InitialParametersFollowUp(state)
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
        val state = uiState.value as? UiState.InitialParametersFollowUp ?: return
        internalState.value = UiState.Generating
        stage.value = Stage.HighLevelItineraryOptions(state)
    }

    private fun List<UiState.OptionGroup>.selectedValues(type: UiState.OptionGroupType): List<String> =
        find { it.type == type }?.options?.filter { it.isSelected }?.map { it.option }
            ?: emptyList()

    class Factory : ViewModelProvider.Factory by viewModelFactory(initializer = {
        TripCreationAssistantViewModel(
            factoryDependencies.genAIRepository,
            GeographyAutoCompleteRepository(),
        )
    })
}