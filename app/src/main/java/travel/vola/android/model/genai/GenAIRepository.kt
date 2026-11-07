package travel.vola.android.model.genai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import kotlinx.serialization.json.Json
import travel.vola.android.extensions.dateString
import java.time.ZonedDateTime

class GenAIRepository internal constructor(
    private val logger: Logger,
    private val dailyItineraryModelFactory: () -> GenerativeModel,
) {

    interface Logger {
        fun error(tag: String, message: String, throwable: Throwable? = null): Int
        fun debug(tag: String, message: String): Int
    }

    constructor() : this(
        object : Logger {
            override fun error(
                tag: String,
                message: String,
                throwable: Throwable?,
            ) = Log.e(tag, message, throwable)

            override fun debug(tag: String, message: String): Int = Log.d(tag, message)
        },
        dailyItineraryModelFactory = {
            Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel(
                    generationConfig = generationConfig {
                        maxOutputTokens = 65536 // Use max output tokens to avoid truncation
                        responseMimeType = "application/json"
                        responseSchema = Prompts.DAILY_ITINERARY.outputSchema
                    },
                    modelName = "gemini-3-flash-preview",
                )
        },
    )

    private val dailyItineraryModel by lazy { dailyItineraryModelFactory() }

    suspend fun genDailyItinerary(
        basicInformation: GenAIData.BasicInformation,
        parameters: GenAIData.InitialParametersOptions,
        followUpQuestions: List<GenAIData.FollowUpQuestion>,
        itinerary: GenAIData.Itinerary,
        lodgings: List<GenAIData.TimedPlace>,
        existingPlaces: List<GenAIData.TimedPlace>,
        itineraryType: GenAIData.ItineraryType,
        dates: List<ZonedDateTime>,
    ): GenAIData.DailyItinerary? {
        val prompt = Prompts.DAILY_ITINERARY
        val promptQuery =
            prompt.prompt +
                "\n Requested dates: " + dates.joinToString(", ") { it.dateString } +
                "\n Basic Information: \n" + Json.encodeToString(basicInformation) +
                "\n Parameters: \n" + Json.encodeToString(parameters) +
                "\n Follow-up Questions: \n" + followUpQuestions.joinToString("\n") { "Q: ${it.question}, A: ${it.answers.first()}" } +
                "\n Selected Itinerary:\n" + Json.encodeToString(itinerary) +
                "\n lodgings: " + lodgings.joinToString("\n - ") { "${it.startTime} - ${it.endTime}: ${it.name}" } +
                "\n Places already in itinerary: " + existingPlaces.joinToString("\n - ") { "${it.startTime}: ${it.name}" }
        return withMeasuredLatency("genDailyItinerary") {
            val result = dailyItineraryModel.generateContent(promptQuery)
            result.text?.let { Json.decodeFromString<GenAIData.DailyItinerary>(it) }
        }
    }

    private suspend fun <T> withMeasuredLatency(
        processIdentifier: String,
        block: suspend () -> T,
    ): T {
        logger.debug("GenAIRepository", "starting $processIdentifier")
        val startTime = System.currentTimeMillis()
        val result = block()
        val latency = System.currentTimeMillis() - startTime
        logger.debug("GenAIRepository", "finished $processIdentifier in ${latency}ms")
        return result
    }
}
