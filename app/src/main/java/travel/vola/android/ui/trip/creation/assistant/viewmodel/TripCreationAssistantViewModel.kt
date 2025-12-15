package travel.vola.android.ui.trip.creation.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import travel.vola.android.common.ui.components.SearchResult
import travel.vola.android.di.factoryDependencies
import travel.vola.android.extensions.dateString
import travel.vola.android.extensions.viewModelFactory
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.genai.GenAIData
import travel.vola.android.model.genai.GenAIRepository
import travel.vola.android.model.repository.GeographyAutoCompleteRepository
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.ui.trip.eventlist.composable.TripDetailsDestination
import java.time.DateTimeException
import java.time.ZonedDateTime
import java.util.TimeZone

class TripCreationAssistantViewModel(
    private val navController: NavController,
    private val repository: GenAIRepository,
    private val destinationAutoCompleteRepository: GeographyAutoCompleteRepository,
    private val tripRepository: TripRepository,
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
            val travelersChangeEnabled: Boolean = false,
            val travelers: Int = 1,
            val nextButtonEnabled: Boolean = false,
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
        ) : UiState

        data class Itinerary(
            val name: String,
            val description: String,
            val startDate: ZonedDateTime,
            val endDate: ZonedDateTime,
            val cities: List<ItineraryCity>,
            val predictedChanges: List<Option>,
        )

        data class ItineraryCity(
            val name: String,
            val startDate: ZonedDateTime,
            val endDate: ZonedDateTime,
            internal val place: Place?,
        )

        data class Option(val option: String, val isSelected: Boolean = false)
    }

    private sealed interface Stage {

        data object Retry : Stage
        data object BasicInformation : Stage
        data class InitialParameters(val state: UiState.BasicInformation) : Stage
        data class InitialParametersFollowUp(val state: UiState.InitialParameters) : Stage
        data class HighLevelItineraryOptions(val state: UiState) : Stage
    }

    private val stage = MutableStateFlow<Stage>(Stage.BasicInformation)

    private val generatedState = stage.map {
        when (it) {
            is Stage.BasicInformation -> UiState.BasicInformation()
            is Stage.InitialParameters -> generateInitialParametersState(it.state)
            is Stage.InitialParametersFollowUp -> getInitialParametersFollowUpState(it.state)
            is Stage.HighLevelItineraryOptions -> getHighLevelItineraryOptionsState(it.state)
            is Stage.Retry -> UiState.Generating
        } ?: UiState.Error
    }
    private val internalState = MutableStateFlow<UiState>(UiState.BasicInformation())

    val uiState = merge(generatedState, internalState).stateIn(
        viewModelScope,
        started = SharingStarted.Lazily,
        internalState.value
    )

    private suspend fun generateInitialParametersState(basicInformation: UiState.BasicInformation): UiState.InitialParameters? {

        val startDateString = basicInformation.startDate?.dateString
        val endDateString = basicInformation.endDate?.dateString
        val info = GenAIData.BasicInformation(
            destination = basicInformation.destinations.joinToString(";"),
            dates = if (basicInformation.fixedDates) {
                "from $startDateString to $endDateString"
            } else {
                "between $startDateString and $endDateString"
            },
            groupType = when (basicInformation.groupType) {
                UiState.TravelGroupType.SOLO -> GenAIData.GroupType.SOLO
                UiState.TravelGroupType.COUPLE -> GenAIData.GroupType.COUPLE
                UiState.TravelGroupType.FAMILY -> GenAIData.GroupType.FAMILY
                UiState.TravelGroupType.FRIENDS -> GenAIData.GroupType.FRIENDS
                UiState.TravelGroupType.COWORKERS -> GenAIData.GroupType.COWORKERS
            },
            travelers = basicInformation.travelers,
            duration = null,
        )
        val options =
            repository.genInitialParametersOptions(info) ?: return null
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

    private suspend fun getInitialParametersFollowUpState(state: UiState.InitialParameters): UiState? {
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
        if (followUpQuestions.questions.isNotEmpty()) {
            return UiState.InitialParametersFollowUp(
                questions = followUpQuestions.questions.map { question ->
                    UiState.FollowUpQuestion(
                        question = question.question,
                        answers = question.answers.map { UiState.Option(it) },
                    )
                }
            )
        } else {
            stage.value = Stage.HighLevelItineraryOptions(state)
            return UiState.Generating
        }
    }

    private suspend fun getHighLevelItineraryOptionsState(state: UiState): UiState.HighLevelItineraryOptions? {
        val answeredQuestions = if (state is UiState.InitialParametersFollowUp) {
            state.questions.map { question ->
                GenAIData.FollowUpQuestion(
                    parameterSelections = emptyList(),
                    question = question.question,
                    answers = question.answers.filter { it.isSelected }.map { it.option },
                )
            }
        } else {
            emptyList()
        }
        val result = repository.genHighLevelItineraryOptions(answeredQuestions) ?: return null
        return UiState.HighLevelItineraryOptions(
            itineraries = result.itineraries.map { itinerary ->
                UiState.Itinerary(
                    name = itinerary.name,
                    description = itinerary.description,
                    startDate = itinerary.startDate.parseAsDate(),
                    endDate = itinerary.endDate.parseAsDate(),
                    cities = itinerary.cities.map {
                        val searchResult =
                            destinationAutoCompleteRepository.autocomplete(it.searchQuery)
                                .firstOrNull()
                        val place = searchResult?.id?.let { id ->
                            destinationAutoCompleteRepository.details(id)
                        }
                        UiState.ItineraryCity(
                            name = it.name,
                            startDate = it.startDate.parseAsDate(),
                            endDate = it.endDate.parseAsDate(),
                            place = place?.place,
                        )
                    },
                    predictedChanges = itinerary.predictedChanges.map { UiState.Option(it) },
                )
            },
        )
    }

    private fun GenAIData.DateResult.parseAsDate(): ZonedDateTime {
        try {
            return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, TimeZone.getDefault().toZoneId())
        } catch (_: DateTimeException) {
            // The LLM might hallucinate 2/29 on a non-Leap year.
            return ZonedDateTime.of(
                year,
                month,
                day - 1,
                0,
                0,
                0,
                0,
                TimeZone.getDefault().toZoneId()
            )
        }
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
            updateBasicState(
                state.copy(
                    destinations = state.destinations + destinationName,
                    destinationSearchResults = emptyList()
                )
            )
        }
    }

    fun onDestinationClearTapped(index: Int) {
        val state = uiState.value as? UiState.BasicInformation ?: return
        updateBasicState(
            state.copy(
                destinations = state.destinations - state.destinations[index]
            )
        )
    }

    fun onFixedDatesSet(fixedDates: Boolean) {
        val state = uiState.value as? UiState.BasicInformation ?: return
        updateBasicState(state.copy(fixedDates = fixedDates))
    }

    fun onStartDateSet(date: ZonedDateTime) {
        val state = uiState.value as? UiState.BasicInformation ?: return
        updateBasicState(state.copy(startDate = date))
    }

    fun onEndDateSet(date: ZonedDateTime) {
        val state = uiState.value as? UiState.BasicInformation ?: return
        updateBasicState(state.copy(endDate = date))
    }

    fun onGroupTypeSet(groupType: UiState.TravelGroupType) {
        val state = uiState.value as? UiState.BasicInformation ?: return
        val newTravelerCount = when (groupType) {
            UiState.TravelGroupType.SOLO -> 1
            UiState.TravelGroupType.COUPLE -> 2
            else -> state.travelers
        }
        val travelersChangeEnabled =
            groupType != UiState.TravelGroupType.SOLO && groupType != UiState.TravelGroupType.COUPLE
        updateBasicState(
            state.copy(
                groupType = groupType,
                travelersChangeEnabled = travelersChangeEnabled,
                travelers = newTravelerCount
            )
        )
    }

    fun onTravelersSet(travelers: Int) {
        val state = uiState.value as? UiState.BasicInformation ?: return
        updateBasicState(state.copy(travelers = travelers))
    }

    private fun updateBasicState(state: UiState.BasicInformation) {
        val nextButtonEnabled =
            state.destinations.isNotEmpty() && state.startDate != null && state.endDate != null && state.travelers > 0
        internalState.value = state.copy(nextButtonEnabled = nextButtonEnabled)
    }

    fun onBasicInformationNextTapped() {
        val state = uiState.value as? UiState.BasicInformation ?: return
        internalState.value = UiState.Generating
        stage.value = Stage.InitialParameters(state)
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

    fun onInitialParametersOptionAdded(optionGroupType: UiState.OptionGroupType, option: String) {
        val state = uiState.value as? UiState.InitialParameters ?: return
        val newOptionGroups = state.optionGroups.map { group ->
            if (group.type == optionGroupType) {
                group.copy(options = group.options + UiState.Option(option, isSelected = true))
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

    fun onRetryTapped() {
        // Resend the current state to retry
        val currentStage = stage.value
        stage.value = Stage.Retry
        stage.value = currentStage
    }

    fun onSkipTapped() {
        viewModelScope.launch {
            val tripId = tripRepository.addTrip()
            navController.navigate(TripDetailsDestination.getRoute(tripId))
        }
    }

    fun onCreateTripTapped(itinerary: UiState.Itinerary) {
        viewModelScope.launch {
            val tripId = tripRepository.addTrip(
                name = itinerary.name,
                places = itinerary.cities.mapNotNull {
                    it.place?.let { place ->
                        TimedPlace(
                            id = it.name,
                            startDateTime = it.startDate,
                            hasStartTime = true,
                            endDateTime = it.endDate,
                            hasEndTime = true,
                            place = place,
                            city = place,
                        )
                    }
                }
            )
            navController.navigate(TripDetailsDestination.getRoute(tripId))
        }
    }

    private fun List<UiState.OptionGroup>.selectedValues(type: UiState.OptionGroupType): List<String> =
        find { it.type == type }?.options?.filter { it.isSelected }?.map { it.option }
            ?: emptyList()

    class Factory : ViewModelProvider.Factory by viewModelFactory(initializer = {
        TripCreationAssistantViewModel(
            factoryDependencies.navController,
            factoryDependencies.genAIRepository,
            GeographyAutoCompleteRepository(),
            factoryDependencies.tripRepository,
        )
    })
}