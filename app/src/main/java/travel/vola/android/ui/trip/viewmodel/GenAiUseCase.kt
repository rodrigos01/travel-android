package travel.vola.android.ui.trip.viewmodel

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import travel.vola.android.model.data.Place
import java.time.ZonedDateTime

class GenAiUseCase {
    private val suggestionsSchema = Schema.obj(
        mapOf(
            "suggestions" to Schema.array(
                Schema.obj(
                    mapOf(
                        "name" to Schema.string(),
                        "address" to Schema.string(),
                        "latitude" to Schema.float(),
                        "longitude" to Schema.float(),
                        "placeId" to Schema.string("id of the place in the Google Maps API"),
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

    suspend fun getSuggestions(city: Place, date: ZonedDateTime): String? {
        val prompt = "provide a list of 5 places to visit in ${city.name} on ${date.toLocalDate()}"

        return model.generateContent(prompt).text
    }
}