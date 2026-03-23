package travel.vola.android.ui.trip.creation.assistant.viewmodel

import travel.vola.android.common.ui.components.SearchResult
import travel.vola.android.model.data.Place
import java.time.ZonedDateTime

sealed interface UiState {
    data object Generating : UiState

    data object Error : UiState

    val skipEnabled: Boolean
        get() = false

    data class BasicInformation(
        val destinations: List<String> = emptyList(),
        val destinationSearchResults: List<SearchResult> = emptyList(),
        override val skipEnabled: Boolean = true,
        val startDate: ZonedDateTime? = null,
        val endDate: ZonedDateTime? = null,
        val fixedDates: Boolean = false,
        val groupType: TravelGroupType = TravelGroupType.SOLO,
        val travelersChangeEnabled: Boolean = false,
        val travelers: Int? = 1,
        val nextButtonEnabled: Boolean = false,
    ) : UiState

    enum class TravelGroupType {
        SOLO,
        COUPLE,
        FAMILY,
        FRIENDS,
        COWORKERS,
    }

    enum class CTAType {
        NEXT,
        UPDATE,
    }

    data class InitialParameters(
        val optionGroups: List<OptionGroup>,
        val anythingElse: String = "",
        override val skipEnabled: Boolean = true,
        val ctaEnabled: Boolean = false,
        val ctaType: CTAType = CTAType.NEXT,
    ) : UiState

    enum class OptionGroupType {
        OCCASIONS,
        INTERESTS,
        VIBE,
        FOCUS,
        DURATION,
        MUST_HAVE,
    }

    data class OptionGroup(val type: OptionGroupType, val options: List<Option>)

    data class InitialParametersFollowUp(
        val questions: List<FollowUpQuestion>,
        override val skipEnabled: Boolean = true,
        val ctaEnabled: Boolean = false,
        val ctaType: CTAType = CTAType.NEXT,
    ) : UiState

    data class FollowUpQuestion(
        val choices: List<String>,
        val question: String,
        val answers: List<Option>,
    )

    data class HighLevelItineraryOptions(
        val itineraries: List<Itinerary>,
        override val skipEnabled: Boolean = true,
        val selected: Itinerary? = null,
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
