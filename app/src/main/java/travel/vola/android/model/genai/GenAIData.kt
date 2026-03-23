package travel.vola.android.model.genai

import kotlinx.serialization.Serializable

object GenAIData {

    enum class GroupType {
        SOLO,
        FAMILY,
        FRIENDS,
        COWORKERS,
        COUPLE,
    }

    enum class ItineraryType(val value: String) {
        DETAILED("detailed"),
        OPEN_ENDED("open-ended"),
    }

    @Serializable
    data class BasicInformation(
        val destination: String,
        val dates: String,
        val duration: String?,
        val groupType: GroupType,
        val travelers: Int?,
    )

    @Serializable
    data class InitialParametersOptions(
        val occasions: List<String>,
        val interests: List<String>,
        val vibe: List<String>,
        val focus: List<String>,
        val mustHave: List<String>,
        val duration: List<String>,
        val anythingElse: String,
    )

    @Serializable
    data class FollowUpQuestionsOutput(
        val numQuestions: Int,
        val questions: List<FollowUpQuestion>,
    )

    @Serializable
    data class FollowUpQuestion(
        val parameterSelections: List<String>,
        val question: String,
        val answers: List<String>,
    )

    @Serializable
    data class HighLevelItineraryOptions(
        val planningLogic: String,
        val itineraries: List<Itinerary>,
    )

    @Serializable
    data class Itinerary(
        val name: String,
        val description: String,
        val startDate: DateResult,
        val endDate: DateResult,
        val cities: List<ItineraryCity>,
        val predictedChanges: List<String>,
    )

    @Serializable
    data class ItineraryCity(
        val id: String,
        val name: String,
        val startDate: DateResult,
        val endDate: DateResult,
        val searchQuery: String,
    )

    @Serializable
    data class DateResult(
        val day: Int,
        val month: Int,
        val year: Int,
    )

    @Serializable
    data class DailyItinerary(
        val numDays: Int,
        val days: List<ItineraryDay>,
    )

    @Serializable
    data class DailyItineraryChange(
        val cityId: String,
        val changes: List<String>,
    )

    @Serializable
    data class ItineraryDay(
        val date: String,
        val sections: List<Section>,
    )

    @Serializable
    data class TimedPlace(
        val name: String,
        val note: String,
        val category: String,
        val searchQuery: String,
        val startTime: String?,
        val endTime: String?,
    )

    @Serializable
    data class Section(
        val name: String,
        val cityId: String,
        val places: List<TimedPlace>,
    )

    @Serializable
    data class SectionCategory(
        val categoryName: String,
        val places: List<TimedPlace>,
    )
}
