package travel.vola.android.model.genai

import kotlinx.serialization.Serializable

object GenAIData {

    enum class GroupType {
        SOLO,
        FAMILY,
        FRIENDS,
        COWORKERS,
        COUPLE
    }

    @Serializable
    data class BasicInformation(
        val destination: String,
        val dates: String,
        val duration: String?,
        val groupType: GroupType,
        val travelers: Int,
    )

    @Serializable
    data class InitialParametersOptions(
        val occasions: List<String>,
        val interests: List<String>,
        val vibe: List<String>,
        val focus: List<String>,
        val mustHave: List<String>,
        val duration: List<String>,
    )

    @Serializable
    data class FollowUpQuestionsOutput(
        val questions: List<FollowUpQuestion>
    )

    @Serializable
    data class FollowUpQuestion(
        val parameter: String,
        val parameterSelection: String,
        val question: String,
        val answers: List<String>
    )

    @Serializable
    data class HighLevelItineraryOptions(
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
        val name: String,
        val startDate: DateResult,
        val endDate: DateResult,
    )

    @Serializable
    data class DateResult(
        val day: Int,
        val month: Int,
        val year: Int,
    )
}