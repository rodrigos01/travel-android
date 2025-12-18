package travel.vola.android.ui.trip.creation.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import travel.vola.android.common.ui.components.SearchResult
import travel.vola.android.di.factoryDependencies
import travel.vola.android.extensions.dateString
import travel.vola.android.extensions.viewModelFactory
import travel.vola.android.model.data.AnsweredQuestion
import travel.vola.android.model.data.BasicInformation
import travel.vola.android.model.data.GroupType
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.TripParameters
import travel.vola.android.model.data.TripPreferences
import travel.vola.android.model.genai.GenAIData
import travel.vola.android.model.genai.GenAIRepository
import travel.vola.android.model.repository.GeographyAutoCompleteRepository
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.ui.home.HomeScreenDestination
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

    private sealed interface Step {

        data object Retry : Step
        data object BasicInformation : Step
        data class InitialParameters(val state: UiState.BasicInformation) : Step
        data class InitialParametersFollowUp(val state: UiState.InitialParameters) : Step
        data class HighLevelItineraryOptions(val state: UiState) : Step

        data class ItineraryRefinement(
            val option: String,
            val state: UiState.HighLevelItineraryOptions,
        ) : Step
    }

    data class CompositeState(
        val basicInformation: UiState.BasicInformation,
        val initialParameters: UiState.InitialParameters?,
        val initialParametersFollowUp: UiState.InitialParametersFollowUp?,
        val highLevelItineraryOptions: UiState.HighLevelItineraryOptions?,
    )

    private val step = MutableStateFlow<Step>(Step.BasicInformation)
    private val compositeState = MutableStateFlow<CompositeState>(
        CompositeState(
            basicInformation = UiState.BasicInformation(),
            initialParameters = null,
            initialParametersFollowUp = null,
            highLevelItineraryOptions = null,
        )
    )

    private val generatedState = combine(step, compositeState) { step, state ->
        when (step) {
            is Step.BasicInformation -> state.basicInformation
            is Step.InitialParameters -> state.initialParameters ?: generateInitialParametersState(
                state.basicInformation
            )

            is Step.InitialParametersFollowUp -> state.initialParametersFollowUp
                ?: state.initialParameters?.let { getInitialParametersFollowUpState(it) }

            is Step.HighLevelItineraryOptions -> state.highLevelItineraryOptions
                ?: state.initialParametersFollowUp?.let { getHighLevelItineraryOptionsState(it) }

            is Step.ItineraryRefinement -> state.highLevelItineraryOptions?.let {
                getItineraryRefinementState(
                    step.option,
                    it
                )
            }

            is Step.Retry -> UiState.Generating
        } ?: UiState.Error
    }.onEach { internalState.value = it }
    private val internalState = MutableStateFlow<UiState>(UiState.BasicInformation())

    val uiState = merge(generatedState, internalState).stateIn(
        viewModelScope, started = SharingStarted.Lazily, internalState.value
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
        val options = repository.genInitialParametersOptions(info) ?: return null
        return UiState.InitialParameters(
            optionGroups = listOf(
                UiState.OptionGroup(
                    UiState.OptionGroupType.OCCASIONS,
                    options.occasions.map { UiState.Option(it) }),
                UiState.OptionGroup(
                    UiState.OptionGroupType.INTERESTS,
                    options.interests.map { UiState.Option(it) }),
                UiState.OptionGroup(
                    UiState.OptionGroupType.VIBE, options.vibe.map { UiState.Option(it) }),
                UiState.OptionGroup(
                    UiState.OptionGroupType.FOCUS, options.focus.map { UiState.Option(it) }),
                UiState.OptionGroup(
                    UiState.OptionGroupType.DURATION, options.duration.map { UiState.Option(it) }),
                UiState.OptionGroup(
                    UiState.OptionGroupType.MUST_HAVE, options.mustHave.map { UiState.Option(it) }),
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
            anythingElse = state.anythingElse,
        )

        val followUpQuestions =
            repository.genInitialParametersFollowUpQuestions(parameters) ?: return null
        if (followUpQuestions.questions.isNotEmpty()) {
            return UiState.InitialParametersFollowUp(
                questions = followUpQuestions.questions.map { question ->
                    UiState.FollowUpQuestion(
                        choices = question.parameterSelections,
                        question = question.question,
                        answers = question.answers.map { UiState.Option(it) },
                    )
                })
        } else {
            step.value = Step.HighLevelItineraryOptions(state)
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

    private suspend fun getItineraryRefinementState(
        refinement: String,
        state: UiState.HighLevelItineraryOptions
    ): UiState.HighLevelItineraryOptions {
        val selected = state.selected ?: return state
        val refinementResult = repository.genRefinedItinerary(
            refinement,
            itinerary = GenAIData.Itinerary(
                name = selected.name,
                description = selected.description,
                cities = selected.cities.map {
                    GenAIData.ItineraryCity(
                        name = it.name,
                        startDate = it.startDate.asDateResult(),
                        endDate = it.endDate.asDateResult(),
                        searchQuery = "${it.place?.name}, ${it.place?.address}",
                    )
                },
                startDate = selected.startDate.asDateResult(),
                endDate = selected.endDate.asDateResult(),
                predictedChanges = selected.predictedChanges.map { it.option },
            ),
        ) ?: return state
        val refinedItinerary = UiState.Itinerary(
            name = refinementResult.name,
            description = refinementResult.description,
            cities = refinementResult.cities.map {
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
            startDate = refinementResult.startDate.parseAsDate(),
            endDate = refinementResult.endDate.parseAsDate(),
            predictedChanges = refinementResult.predictedChanges.map { UiState.Option(it) },
        )
        return state.copy(
            selected = refinedItinerary,
            itineraries = state.itineraries.map {
                if (it == selected) {
                    refinedItinerary
                } else {
                    it
                }
            }
        )
    }

    private fun ZonedDateTime.asDateResult() = GenAIData.DateResult(
        year = year, month = monthValue, day = dayOfMonth
    )

    private fun GenAIData.DateResult.parseAsDate(): ZonedDateTime {
        try {
            return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, TimeZone.getDefault().toZoneId())
        } catch (_: DateTimeException) {
            // The LLM might hallucinate 2/29 on a non-Leap year.
            return ZonedDateTime.of(
                year, month, day - 1, 0, 0, 0, 0, TimeZone.getDefault().toZoneId()
            )
        }
    }

    fun onDestinationSearchTextChanged(query: CharSequence) {
        val state = uiState.value as? UiState.BasicInformation ?: return
        viewModelScope.launch {
            val results = destinationAutoCompleteRepository.autocomplete(query.toString())
            if (results.isNotEmpty()) {
                updateBasicState(
                    state.copy(
                        destinationSearchResults = results.map {
                            SearchResult(
                                it.name,
                                it.address
                            )
                        })
                )
            }
        }
    }

    fun onDestinationSearchResultSelected(index: Int) {
        viewModelScope.launch {
            val state = uiState.value as? UiState.BasicInformation ?: return@launch
            val selected = state.destinationSearchResults[index]
            val destinationName =
                selected.title + selected.subtitle.takeIf { it.isNotBlank() }?.let { ", $it" }
                    .orEmpty()
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
        updateBasicState(
            state.copy(
                startDate = date, endDate = state.endDate ?: date.plusDays(7)
            )
        )
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

    fun onTravelersSet(travelers: Int?) {
        val state = uiState.value as? UiState.BasicInformation ?: return
        updateBasicState(state.copy(travelers = travelers))
    }

    private fun updateBasicState(state: UiState.BasicInformation) {
        val nextButtonEnabled =
            state.destinations.isNotEmpty() && state.startDate != null && state.endDate != null && state.endDate > state.startDate && state.travelers != null && state.travelers > 0
        compositeState.update {
            basicInformation = state.copy(nextButtonEnabled = nextButtonEnabled)
        }
    }

    fun onBasicInformationNextTapped() {
        val state = uiState.value as? UiState.BasicInformation ?: return
        internalState.value = UiState.Generating
        step.value = Step.InitialParameters(state)
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
        updateInitialParametersState(state.copy(optionGroups = newOptionGroups))
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
        updateInitialParametersState(state.copy(optionGroups = newOptionGroups))
    }

    fun onInitialParametersAnythingElseUpdated(value: String) {
        val state = uiState.value as? UiState.InitialParameters ?: return
        updateInitialParametersState(state.copy(anythingElse = value))
    }

    private fun updateInitialParametersState(state: UiState.InitialParameters) {
        val nextButtonEnabled = state.optionGroups.all { group ->
            group.options.any { it.isSelected }
        }
        compositeState.update {
            initialParameters = state.copy(nextButtonEnabled = nextButtonEnabled)
        }
    }

    fun onInitialParametersNextTapped() {
        val state = uiState.value as? UiState.InitialParameters ?: return
        internalState.value = UiState.Generating
        step.value = Step.InitialParametersFollowUp(state)
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
        compositeState.update {
            initialParametersFollowUp = state.copy(
                questions = newQuestions, nextButtonEnabled = newQuestions.all { question ->
                    question.answers.any { it.isSelected }
                })
        }
    }

    fun onFollowUpQuestionCustomAnswerAdded(answer: String, question: UiState.FollowUpQuestion) {
        val state = uiState.value as? UiState.InitialParametersFollowUp ?: return
        val newQuestions = state.questions.map { currentQuestion ->
            if (currentQuestion == question) {
                currentQuestion.copy(answers = currentQuestion.answers.map { it.copy(isSelected = false) } + UiState.Option(
                    answer, isSelected = true
                ))
            } else {
                currentQuestion
            }
        }
        compositeState.update {
            initialParametersFollowUp = state.copy(
                questions = newQuestions, nextButtonEnabled = newQuestions.all { question ->
                    question.answers.any { it.isSelected }
                })
        }
    }

    fun onFollowUpQuestionsNextTapped() {
        val state = uiState.value as? UiState.InitialParametersFollowUp ?: return
        internalState.value = UiState.Generating
        step.value = Step.HighLevelItineraryOptions(state)
    }

    fun onRetryTapped() {
        // Resend the current state to retry
        val currentStage = step.value
        step.value = Step.Retry
        step.value = currentStage
    }

    fun onSkipTapped() {
        viewModelScope.launch {
            val tripId = tripRepository.addTrip()
            navController.navigate(TripDetailsDestination.getRoute(tripId))
        }
    }

    fun onItinerarySelected(itinerary: UiState.Itinerary?) {
        val state = uiState.value as? UiState.HighLevelItineraryOptions ?: return
        compositeState.update {
            highLevelItineraryOptions = state.copy(selected = itinerary)
        }
    }

    fun onConfirmationOptionSelected(option: String) {
        val state = uiState.value as? UiState.HighLevelItineraryOptions ?: return
        internalState.value = UiState.Generating
        step.value = Step.ItineraryRefinement(option, state)
    }

    fun onCreateTripTapped(itinerary: UiState.Itinerary) {
        val state = compositeState.value
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
                },
                preferences = TripPreferences(
                    basicInformation = BasicInformation(
                        groupType = when (state.basicInformation.groupType) {
                            UiState.TravelGroupType.SOLO -> GroupType.SOLO
                            UiState.TravelGroupType.COUPLE -> GroupType.COUPLE
                            UiState.TravelGroupType.FAMILY -> GroupType.FAMILY
                            UiState.TravelGroupType.FRIENDS -> GroupType.FRIENDS
                            UiState.TravelGroupType.COWORKERS -> GroupType.COWORKERS
                        },
                        travelers = state.basicInformation.travelers ?: 1,
                    ),
                    initialParameters = TripParameters(
                        occasions = state.initialParameters?.optionGroups.valuesByType(UiState.OptionGroupType.OCCASIONS),
                        interests = state.initialParameters?.optionGroups.valuesByType(UiState.OptionGroupType.INTERESTS),
                        vibe = state.initialParameters?.optionGroups.valuesByType(UiState.OptionGroupType.VIBE),
                        focus = state.initialParameters?.optionGroups.valuesByType(UiState.OptionGroupType.FOCUS),
                        duration = state.initialParameters?.optionGroups.valuesByType(UiState.OptionGroupType.DURATION),
                        mustHave = state.initialParameters?.optionGroups.valuesByType(UiState.OptionGroupType.MUST_HAVE),
                        anythingElse = state.initialParameters?.anythingElse ?: "",
                    ),
                    questionsAnswers = state.initialParametersFollowUp?.questions?.mapNotNull { question ->
                        question.answers.firstOrNull { it.isSelected }?.option?.let { answer ->
                            AnsweredQuestion(
                                question.question,
                                answer
                            )
                        }
                    } ?: emptyList(),
                ),
            )
            navController.navigate(
                TripDetailsDestination.getRoute(tripId)
            ) {
                popUpTo(HomeScreenDestination.ROUTE)
            }
        }
    }

    fun List<UiState.OptionGroup>?.valuesByType(type: UiState.OptionGroupType) =
        this?.first { it.type == type }?.options?.filter { it.isSelected }
            ?.map { it.option } ?: emptyList()

    fun onNavigateBack() {
        val currentStage = step.value
        if (currentStage == Step.BasicInformation) {
            navController.popBackStack()
            return
        }
        when (currentStage) {
            is Step.InitialParameters -> Step.BasicInformation to currentStage.state
            is Step.InitialParametersFollowUp -> Step.InitialParameters(UiState.BasicInformation()) to currentStage.state
            is Step.HighLevelItineraryOptions -> Step.InitialParametersFollowUp(
                UiState.InitialParameters(
                    emptyList()
                )
            ) to currentStage.state

            else -> null
        }?.let { (newStage, newState) ->
            internalState.value = newState
            step.value = newStage
        }
    }

    private fun List<UiState.OptionGroup>.selectedValues(type: UiState.OptionGroupType): List<String> =
        find { it.type == type }?.options?.filter { it.isSelected }?.map { it.option }
            ?: emptyList()

    private fun MutableStateFlow<CompositeState>.update(operation: MutableCompositeState.() -> Unit) {
        this.value = MutableCompositeState(
            this.value.basicInformation,
            this.value.initialParameters,
            this.value.initialParametersFollowUp,
            this.value.highLevelItineraryOptions,
        ).apply(operation).let {
            CompositeState(
                it.basicInformation,
                it.initialParameters,
                it.initialParametersFollowUp,
                it.highLevelItineraryOptions,
            )
        }
    }

    private data class MutableCompositeState(
        var basicInformation: UiState.BasicInformation,
        var initialParameters: UiState.InitialParameters?,
        var initialParametersFollowUp: UiState.InitialParametersFollowUp?,
        var highLevelItineraryOptions: UiState.HighLevelItineraryOptions?,
    )

    class Factory : ViewModelProvider.Factory by viewModelFactory(initializer = {
        TripCreationAssistantViewModel(
            factoryDependencies.navController,
            factoryDependencies.genAIRepository,
            GeographyAutoCompleteRepository(),
            factoryDependencies.tripRepository,
        )
    })
}