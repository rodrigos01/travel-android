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
import java.util.TimeZone

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
    ): SuggestionsResult? {
        val prompt =
            "provide a list of 5 places to visit in ${city.name} on ${date.toLocalDate()}, " +
                    "considering that the user already has the following places on their " +
                    "itinerary ${existingPlaces.joinToString()}. Alongside the list, " +
                    "provide 3 possible changes you predict the user might want to make to the list. " +
                    "Keep those suggestions brief, 3 words maximum so they can fit on a button"
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
        return SuggestionsResult(places.mapNotNull {
            Place(
                id = it?.id ?: "",
                name = it?.displayName ?: "",
                latitude = it?.location?.latitude ?: 0.0,
                longitude = it?.location?.longitude ?: 0.0,
                coverImage = null,
                address = "",
                externalId = it?.id ?: "",
                timeZone = TimeZone.getTimeZone(date.zone.id),
                source = "Google"
            )
        }, response.predictedChanges)
    }
}

data class SuggestionsResult(val places: List<Place>, val predictedChanges: List<String>)