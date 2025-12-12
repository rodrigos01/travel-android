package travel.vola.android.model.genai

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.serialization.json.Json

class GenAIRepository private constructor(
    private val placesClient: PlacesClient,
) {

    constructor(applicationContext: Context) : this(Places.createClient(applicationContext))

    private val models = Prompts.entries.associateWith { lazy { createModel(it.outputSchema) } }

    suspend fun genInitialParametersOptions(basicInformation: GenAIData.BasicInformation): GenAIData.InitialParametersOptions? {
        val prompt = Prompts.INITIAL_PARAMETERS
        val model = models[prompt]?.value ?: return null

        val promptQuery =
            prompt.prompt + "\n User Information: \n" + Json.encodeToString(basicInformation)

        val jsonString = model.generateContent(promptQuery).text ?: return null
        return Json.decodeFromString(jsonString)
    }

    private fun createModel(schema: Schema): GenerativeModel {
        return Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(modelName = "gemini-2.5-flash", generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = schema
            })
    }
}