package travel.vola.android.ui.trip.viewmodel

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import travel.vola.android.model.data.Place
import java.time.ZonedDateTime

class GenAiUseCase {

    @Serializable
    private data class GenAISuggestion(
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val searchQuery: String,
    )

    @Serializable
    private data class GenAiSuggestionResponse(
        val suggestions: List<GenAISuggestion>,
        val predictedChanges: List<String>,
    )

    private val suggestionsSchema = Schema.obj(
        mapOf(
            "suggestions" to Schema.array(
                Schema.obj(
                    mapOf(
                        "name" to Schema.string(),
                        "address" to Schema.string(),
                        "latitude" to Schema.float(),
                        "longitude" to Schema.float(),
                        "searchQuery" to Schema.string("query to search for the place in google maps"),
                    )
                )
            ),
            "predictedChanges" to Schema.array(
                Schema.string("A change the user would possibly make to the list of places")
            )
        )
    )

    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(modelName = "gemini-2.5-pro", generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = suggestionsSchema
            })
    }

    suspend fun getSuggestions(
        city: Place,
        date: ZonedDateTime,
        existingPlaces: List<Place>
    ): SuggestionsResult? {
        val prompt =
            "provide a list of 5 places to visit in ${city.name} on ${date.toLocalDate()}, " +
                    "considering that the user already has the following places on their " +
                    "itinerary ${existingPlaces.joinToString()}. Alongside the list, " +
                    "provide 3 possible changes you predict the user might want to make to the list. " +
                    "Keep those suggestions brief, 3 words maximum so they can fit on a button"
        val jsonString = model.generateContent(prompt).text ?: return null

        val response = Json.decodeFromString<GenAiSuggestionResponse>(jsonString)
        // TODO: get actual places from response
        return SuggestionsResult(emptyList(), predictedChanges = emptyList())
    }
}

data class SuggestionsResult(val places: List<Place>, val predictedChanges: List<String>)