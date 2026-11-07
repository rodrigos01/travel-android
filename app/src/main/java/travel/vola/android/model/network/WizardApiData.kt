package travel.vola.android.model.network

import kotlinx.serialization.Serializable

// Wire format of travel-node's /ai/wizard endpoints (api/ai/). Dates are
// YYYY-MM-DD; names are snake_case on the wire (see KtorClient).
interface WizardApiData {

    @Serializable
    data class BasicInformation(
        val groupType: String,
        val travelers: Int,
        val destinations: List<String>,
        val startDate: String,
        val endDate: String,
        val fixedDates: Boolean,
    )

    @Serializable
    data class OptionsRequest(val basicInformation: BasicInformation)

    @Serializable
    data class Options(
        val occasions: List<String>,
        val interests: List<String>,
        val vibe: List<String>,
        val focus: List<String>,
        val mustHave: List<String>,
        val duration: List<String>,
    )

    @Serializable
    data class OptionsResponse(val interactionId: String, val options: Options)

    @Serializable
    data class Parameters(
        val occasions: List<String>,
        val interests: List<String>,
        val vibe: List<String>,
        val focus: List<String>,
        val mustHave: List<String>,
        val duration: List<String>,
        val anythingElse: String,
    )

    @Serializable
    data class FollowUpRequest(val interactionId: String, val parameters: Parameters)

    @Serializable
    data class Question(
        val parameterSelections: List<String>,
        val question: String,
        val answers: List<String>,
    )

    @Serializable
    data class FollowUpResponse(val interactionId: String, val questions: List<Question>)

    @Serializable
    data class Answer(val question: String, val answer: String)

    @Serializable
    data class ItinerariesRequest(val interactionId: String, val answers: List<Answer>)

    @Serializable
    data class City(
        val name: String,
        val searchQuery: String,
        val startDate: String,
        val endDate: String,
        // Only in responses: the place the backend resolved the city to.
        val place: ApiData.Place? = null,
    )

    @Serializable
    data class Itinerary(
        val name: String,
        val description: String,
        val startDate: String,
        val endDate: String,
        val cities: List<City>,
        val predictedChanges: List<String>,
    )

    @Serializable
    data class ItinerariesResponse(
        val interactionId: String,
        val planningLogic: String,
        val itineraries: List<Itinerary>,
    )

    @Serializable
    data class RefineRequest(
        val interactionId: String,
        val feedback: String,
        val itinerary: Itinerary,
    )

    @Serializable
    data class RefineResponse(val interactionId: String, val itinerary: Itinerary)
}
