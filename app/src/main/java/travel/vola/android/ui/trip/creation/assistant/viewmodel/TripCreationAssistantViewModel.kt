package travel.vola.android.ui.trip.creation.assistant.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import travel.vola.android.common.ui.components.SearchResult
import travel.vola.android.di.factoryDependencies
import travel.vola.android.extensions.viewModelFactory
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.model.data.AnsweredQuestion
import travel.vola.android.model.data.BasicInformation
import travel.vola.android.model.data.GroupType
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.TripParameters
import travel.vola.android.model.data.TripPreferences
import travel.vola.android.model.data.WizardAnswer
import travel.vola.android.model.data.WizardBasicInformation
import travel.vola.android.model.data.WizardCity
import travel.vola.android.model.data.WizardItinerary
import travel.vola.android.model.data.WizardParameters
import travel.vola.android.model.data.WizardStep
import travel.vola.android.model.repository.GeographyAutoCompleteRepository
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.model.repository.WizardRepository
import travel.vola.android.ui.home.HomeScreenDestination
import travel.vola.android.ui.trip.creation.assistant.composable.TripCreationAssistantDestination
import travel.vola.android.ui.trip.eventlist.composable.TripDetailsDestination
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class TripCreationAssistantViewModel(
    private val navController: NavController,
    private val repository: WizardRepository,
    private val destinationAutoCompleteRepository: GeographyAutoCompleteRepository,
    private val tripRepository: TripRepository,
    private val tripId: String?,
    destinations: List<String>,
    startDate: String?,
    endDate: String?,
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
            basicInformation = UiState.BasicInformation(
                destinations = destinations,
                startDate = startDate?.let { zonedDateTime(it) },
                endDate = endDate?.let { zonedDateTime(it) },
                fixedDates = startDate != null && endDate != null,
                nextButtonEnabled = destinations.isNotEmpty() && startDate != null && endDate != null,
                skipEnabled = tripId == null,
            ),
            initialParameters = null,
            initialParametersFollowUp = null,
            highLevelItineraryOptions = null,
        ),
    )

    // The backend keeps the conversation with the model; each step continues from the previous
    // step's interaction. Going back and forward again continues from the same one (the backend
    // allows branching), so a step's id is only replaced by a newer answer to the same step.
    private var optionsInteractionId: String? = null
    private var followUpInteractionId: String? = null
    private var itinerariesInteractionId: String? = null

    // The conversations still open at the backend: the latest interaction of each branch. They
    // are ended with the wizard (the backend deletes what each one continued).
    private val openInteractions = mutableSetOf<String>()

    private fun <T> WizardStep<T>.opened(continuedFrom: String?): WizardStep<T> {
        continuedFrom?.let(openInteractions::remove)
        openInteractions += interactionId
        return this
    }

    private suspend fun <T> attempt(step: String, block: suspend () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Wizard step $step failed", e)
        null
    }

    private val generatedState = combine(step, compositeState) { step, state ->
        when (step) {
            is Step.BasicInformation -> state.basicInformation
            is Step.InitialParameters -> state.initialParameters ?: generateInitialParametersState(
                state.basicInformation,
            )

            is Step.InitialParametersFollowUp ->
                state.initialParametersFollowUp
                    ?: state.initialParameters?.let { getInitialParametersFollowUpState(it) }

            is Step.HighLevelItineraryOptions ->
                state.highLevelItineraryOptions
                    ?: state.initialParametersFollowUp?.let { getHighLevelItineraryOptionsState(it) }

            is Step.ItineraryRefinement -> state.highLevelItineraryOptions?.let {
                getItineraryRefinementState(
                    step.option,
                    it,
                )
            }

            is Step.Retry -> UiState.Generating
        } ?: UiState.Error
    }.onEach { internalState.value = it }
    private val internalState =
        MutableStateFlow<UiState>(UiState.BasicInformation(skipEnabled = tripId == null))

    val uiState = merge(generatedState, internalState).stateIn(
        viewModelScope,
        started = SharingStarted.Lazily,
        internalState.value,
    )

    private suspend fun generateInitialParametersState(
        basicInformation: UiState.BasicInformation,
    ): UiState.InitialParameters? {
        val existingParameters = tripId?.let {
            tripRepository.findTripById(it).firstOrNull()
        }?.preferences?.initialParameters
        val info = WizardBasicInformation(
            destinations = basicInformation.destinations,
            startDate = basicInformation.startDate?.toLocalDate() ?: return null,
            endDate = basicInformation.endDate?.toLocalDate() ?: return null,
            fixedDates = basicInformation.fixedDates,
            groupType = basicInformation.groupType.asGroupType(),
            travelers = basicInformation.travelers ?: 1,
        )
        val result = attempt("options") { repository.options(info).opened(continuedFrom = null) }
            ?: return null
        optionsInteractionId = result.interactionId
        val options = result.value
        return UiState.InitialParameters(
            optionGroups = listOf(
                options.occasions.asOptionGroup(
                    UiState.OptionGroupType.OCCASIONS,
                    existingParameters?.occasions,
                ),
                options.interests.asOptionGroup(
                    UiState.OptionGroupType.INTERESTS,
                    existingParameters?.interests,
                ),
                options.vibe.asOptionGroup(
                    UiState.OptionGroupType.VIBE,
                    existingParameters?.vibe,
                ),
                options.focus.asOptionGroup(
                    UiState.OptionGroupType.FOCUS,
                    existingParameters?.focus,
                ),
                options.duration.asOptionGroup(
                    UiState.OptionGroupType.DURATION,
                    existingParameters?.duration,
                ),
                options.mustHave.asOptionGroup(
                    UiState.OptionGroupType.MUST_HAVE,
                    existingParameters?.mustHave,
                ),
            ),
            ctaType = if (tripId == null) UiState.CTAType.NEXT else UiState.CTAType.UPDATE,
            skipEnabled = tripId == null,
        )
    }

    private fun List<String>.asOptionGroup(
        type: UiState.OptionGroupType,
        selectedOptions: List<String>?,
    ): UiState.OptionGroup {
        val selected = selectedOptions.orEmpty()
        return UiState.OptionGroup(
            type,
            selected.map {
                UiState.Option(
                    it,
                    isSelected = true,
                )
            } + (this - selected).map { UiState.Option(it) },
        )
    }

    private suspend fun getInitialParametersFollowUpState(state: UiState.InitialParameters): UiState? {
        val parameters = WizardParameters(
            occasions = state.optionGroups.selectedValues(UiState.OptionGroupType.OCCASIONS),
            interests = state.optionGroups.selectedValues(UiState.OptionGroupType.INTERESTS),
            vibe = state.optionGroups.selectedValues(UiState.OptionGroupType.VIBE),
            focus = state.optionGroups.selectedValues(UiState.OptionGroupType.FOCUS),
            duration = state.optionGroups.selectedValues(UiState.OptionGroupType.DURATION),
            mustHave = state.optionGroups.selectedValues(UiState.OptionGroupType.MUST_HAVE),
            anythingElse = state.anythingElse,
        )

        val continuedFrom = optionsInteractionId ?: return null
        val result = attempt("follow-up") {
            repository.followUp(continuedFrom, parameters).opened(continuedFrom)
        } ?: return null
        followUpInteractionId = result.interactionId
        val followUpQuestions = result.value
        return if (followUpQuestions.isNotEmpty()) {
            UiState.InitialParametersFollowUp(
                questions = followUpQuestions.map { question ->
                    UiState.FollowUpQuestion(
                        choices = question.parameterSelections,
                        question = question.question,
                        answers = question.answers.map { UiState.Option(it) },
                    )
                },
                ctaType = if (tripId == null) UiState.CTAType.NEXT else UiState.CTAType.UPDATE,
                skipEnabled = tripId == null,
            )
        } else if (tripId != null) {
            updateTrip()
            UiState.Generating
        } else {
            getHighLevelItineraryOptionsState(state)
        }
    }

    private suspend fun getHighLevelItineraryOptionsState(state: UiState): UiState.HighLevelItineraryOptions? {
        val answers = (state as? UiState.InitialParametersFollowUp)?.questions.orEmpty()
            .mapNotNull { question ->
                question.answers.firstOrNull { it.isSelected }
                    ?.let { WizardAnswer(question.question, it.option) }
            }
        val continuedFrom = followUpInteractionId ?: return null
        val result = attempt("itineraries") {
            repository.itineraries(continuedFrom, answers).opened(continuedFrom)
        } ?: return null
        itinerariesInteractionId = result.interactionId
        return UiState.HighLevelItineraryOptions(
            itineraries = result.value.itineraries.map { it.asUiItinerary() },
            skipEnabled = tripId == null,
        )
    }

    private fun WizardItinerary.asUiItinerary() = UiState.Itinerary(
        name = name,
        description = description,
        startDate = startDate.asStartOfDay(),
        endDate = endDate.asStartOfDay(),
        cities = cities.map {
            UiState.ItineraryCity(
                name = it.name,
                startDate = it.startDate.asStartOfDay(),
                endDate = it.endDate.asStartOfDay(),
                place = it.place,
            )
        },
        predictedChanges = predictedChanges.map { UiState.Option(it) },
    )

    private suspend fun getItineraryRefinementState(
        refinement: String,
        state: UiState.HighLevelItineraryOptions,
    ): UiState.HighLevelItineraryOptions {
        val selected = state.selected ?: return state
        val continuedFrom = itinerariesInteractionId ?: return state
        val result = attempt("refinement") {
            repository.refine(continuedFrom, refinement, selected.asWizardItinerary())
                .opened(continuedFrom)
        } ?: return state
        itinerariesInteractionId = result.interactionId
        val refinedItinerary = result.value.asUiItinerary()
        return state.copy(
            selected = refinedItinerary,
            itineraries = state.itineraries.map {
                if (it == selected) {
                    refinedItinerary
                } else {
                    it
                }
            },
        )
    }

    private fun UiState.Itinerary.asWizardItinerary() = WizardItinerary(
        name = name,
        description = description,
        startDate = startDate.toLocalDate(),
        endDate = endDate.toLocalDate(),
        cities = cities.map {
            WizardCity(
                name = it.name,
                searchQuery = it.place?.let { place -> "${place.name}, ${place.address}" } ?: it.name,
                startDate = it.startDate.toLocalDate(),
                endDate = it.endDate.toLocalDate(),
                place = it.place,
            )
        },
        predictedChanges = predictedChanges.map { it.option },
    )

    private fun LocalDate.asStartOfDay(): ZonedDateTime = atStartOfDay(ZoneId.systemDefault())

    private fun UiState.TravelGroupType.asGroupType() = when (this) {
        UiState.TravelGroupType.SOLO -> GroupType.SOLO
        UiState.TravelGroupType.COUPLE -> GroupType.COUPLE
        UiState.TravelGroupType.FAMILY -> GroupType.FAMILY
        UiState.TravelGroupType.FRIENDS -> GroupType.FRIENDS
        UiState.TravelGroupType.COWORKERS -> GroupType.COWORKERS
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
                                it.address,
                            )
                        },
                    ),
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
                    destinationSearchResults = emptyList(),
                ),
            )
        }
    }

    fun onDestinationClearTapped(index: Int) {
        val state = uiState.value as? UiState.BasicInformation ?: return
        updateBasicState(
            state.copy(
                destinations = state.destinations - state.destinations[index],
            ),
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
                startDate = date,
                endDate = state.endDate ?: date.plusDays(7),
            ),
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
                travelers = newTravelerCount,
            ),
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
                group.copy(
                    options = group.options.mapIndexed { optionIndex, option ->
                        option.copy(
                            isSelected = if (optionIndex == index) !option.isSelected else option.isSelected,
                        )
                    },
                )
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
            initialParameters = state.copy(ctaEnabled = nextButtonEnabled)
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
                currentQuestion.copy(
                    answers = currentQuestion.answers.mapIndexed { optionIndex, option ->
                        option.copy(isSelected = optionIndex == index)
                    },
                )
            } else {
                currentQuestion
            }
        }
        compositeState.update {
            initialParametersFollowUp = state.copy(
                questions = newQuestions,
                ctaEnabled = newQuestions.all { question ->
                    question.answers.any { it.isSelected }
                },
            )
        }
    }

    fun onFollowUpQuestionCustomAnswerAdded(answer: String, question: UiState.FollowUpQuestion) {
        val state = uiState.value as? UiState.InitialParametersFollowUp ?: return
        val newQuestions = state.questions.map { currentQuestion ->
            if (currentQuestion == question) {
                currentQuestion.copy(
                    answers = currentQuestion.answers.map { it.copy(isSelected = false) } + UiState.Option(
                        answer,
                        isSelected = true,
                    ),
                )
            } else {
                currentQuestion
            }
        }
        compositeState.update {
            initialParametersFollowUp = state.copy(
                questions = newQuestions,
                ctaEnabled = newQuestions.all { question ->
                    question.answers.any { it.isSelected }
                },
            )
        }
    }

    fun onFollowUpQuestionsNextTapped() {
        if (tripId != null) {
            updateTrip()
        } else {
            val state = uiState.value as? UiState.InitialParametersFollowUp ?: return
            internalState.value = UiState.Generating
            step.value = Step.HighLevelItineraryOptions(state)
        }
    }

    fun onRetryTapped() {
        // Resend the current state to retry
        val currentStage = step.value
        step.value = Step.Retry
        step.value = currentStage
    }

    fun onSkipTapped() {
        viewModelScope.launch {
            val id = tripId ?: tripRepository.addTrip()
            navController.navigate(TripDetailsDestination.getRoute(id))
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

    fun updateTrip() {
        val id = tripId ?: return
        val state = compositeState.value
        viewModelScope.launch {
            tripRepository.updateTripPreferences(id, createTripPreferences(state))
            setResult(TripCreationAssistantDestination.FinishedStatus.COMPLETED)
            navController.popBackStack()
        }
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
                preferences = createTripPreferences(state),
            )
            navController.navigate(
                TripDetailsDestination.getRoute(tripId),
            ) {
                popUpTo(HomeScreenDestination.ROUTE)
            }
        }
    }

    private fun createTripPreferences(state: CompositeState): TripPreferences = TripPreferences(
        basicInformation = BasicInformation(
            groupType = state.basicInformation.groupType.asGroupType(),
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
                    answer,
                )
            }
        } ?: emptyList(),
    )

    fun List<UiState.OptionGroup>?.valuesByType(type: UiState.OptionGroupType) =
        this?.first { it.type == type }?.options?.filter { it.isSelected }
            ?.map { it.option } ?: emptyList()

    fun onNavigateBack() {
        val currentStage = step.value
        if (currentStage == Step.BasicInformation) {
            setResult(TripCreationAssistantDestination.FinishedStatus.CANCELLED)
            navController.popBackStack()
            return
        }
        when (currentStage) {
            is Step.InitialParameters -> Step.BasicInformation to currentStage.state
            is Step.InitialParametersFollowUp -> Step.InitialParameters(
                UiState.BasicInformation(
                    skipEnabled = tripId == null,
                ),
            ) to currentStage.state

            is Step.HighLevelItineraryOptions -> Step.InitialParametersFollowUp(
                UiState.InitialParameters(
                    emptyList(),
                    skipEnabled = tripId == null,
                ),
            ) to currentStage.state

            else -> null
        }?.let { (newStage, newState) ->
            internalState.value = newState
            step.value = newStage
        }
    }

    private fun setResult(result: TripCreationAssistantDestination.FinishedStatus) {
        navController.previousBackStackEntry?.savedStateHandle?.set(
            TripCreationAssistantDestination.RESULT_KEY_FINISHED_STATUS,
            result,
        )
    }

    private fun List<UiState.OptionGroup>.selectedValues(type: UiState.OptionGroupType): List<String> =
        find { it.type == type }?.options?.filter { it.isSelected }?.map { it.option }
            ?: emptyList()

    override fun onCleared() {
        repository.end(openInteractions.toList())
    }

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

    private companion object {
        const val TAG = "TripCreationAssistant"
    }

    class Factory(
        private val tripId: String?,
        private val destinations: List<String>,
        private val startDate: String?,
        private val endDate: String?,
    ) : ViewModelProvider.Factory by viewModelFactory(initializer = {
        TripCreationAssistantViewModel(
            factoryDependencies.navController,
            factoryDependencies.wizardRepository,
            GeographyAutoCompleteRepository(),
            factoryDependencies.tripRepository,
            tripId,
            destinations,
            startDate,
            endDate,
        )
    })
}
