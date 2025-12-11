package travel.vola.android.ui.trip.viewmodel

import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import travel.vola.android.model.data.Place
import travel.vola.android.ui.applicationContext
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
    private data class GenAiSuggestionResponse(val suggestions: List<GenAISuggestion>)

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

    private val placesClient by lazy {
        Places.createClient(applicationContext)
    }

    private val placeFields = listOf(
        com.google.android.libraries.places.api.model.Place.Field.ID,
        com.google.android.libraries.places.api.model.Place.Field.DISPLAY_NAME,
        com.google.android.libraries.places.api.model.Place.Field.PHOTO_METADATAS,
        com.google.android.libraries.places.api.model.Place.Field.LOCATION,
    )

    suspend fun getSuggestions(
        city: Place,
        date: ZonedDateTime,
        existingPlaces: List<Place>
    ): String? {
        val prompt =
            "provide a list of 5 places to visit in ${city.name} on ${date.toLocalDate()}, " +
                    "considering that the user already has the following places on their " +
                    "itinerary ${existingPlaces.joinToString()}"
        val jsonString = model.generateContent(prompt).text ?: return null

        val response = Json.decodeFromString<GenAiSuggestionResponse>(jsonString)
        val places = response.suggestions.map {
            coroutineScope {
                async {
                    val request =
                        SearchByTextRequest.builder(it.searchQuery, placeFields)
                            .setMaxResultCount(1)
                            .build()
                    placesClient.searchByText(request).await().places.firstOrNull()
                }
            }
        }.awaitAll()
        return places.mapNotNull { it?.displayName }.joinToString()
    }
}