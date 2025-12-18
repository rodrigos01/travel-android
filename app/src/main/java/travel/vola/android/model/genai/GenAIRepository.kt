package travel.vola.android.model.genai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.FunctionCallingConfig
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.ToolConfig
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.typeOf

class GenAIRepository internal constructor(
    private val logger: Logger,
    modelFactory: () -> GenerativeModel
) {

    interface Logger {
        fun error(tag: String, message: String, throwable: Throwable? = null): Int
        fun debug(tag: String, message: String): Int
    }

    enum class FunctionNames(val value: String) {
        INITIAL_PARAMETERS("genInitialParameters"),
        INITIAL_PARAMETERS_FOLLOW_UP("genInitialParametersFollowUp"),
        HIGH_LEVEL_ITINERARY_OPTIONS("genHighLevelItineraryOptions"),
        REFINE_ITINERARY("refineItinerary"),
        DAILY_ITINERARY("genDailyItinerary"),
    }

    constructor() : this(object : Logger {
        override fun error(
            tag: String,
            message: String,
            throwable: Throwable?
        ) = Log.e(tag, message, throwable)

        override fun debug(tag: String, message: String): Int = Log.d(tag, message)
    }, modelFactory = {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                generationConfig = generationConfig {
                    maxOutputTokens = 65536 // Use max output tokens to avoid truncation
                },
                modelName = "gemini-3-flash-preview",
                systemInstruction = content {
                    text("You are an AI Travel Assistant running on the background of a Travel Planning application. Help the user plan a trip, initially by planning a high-level travel itinerary focused only on destination and dates, then later by planning fine-grained day-by-day itineraries")
                },
                toolConfig = ToolConfig(FunctionCallingConfig.any()),
                tools = listOf(
                    Tool.functionDeclarations(
                        listOf(
                            FunctionDeclaration(
                                name = FunctionNames.INITIAL_PARAMETERS.value,
                                parameters = mapOf("parameters" to Prompts.INITIAL_PARAMETERS.outputSchema),
                                description = "Creates the initial set of parameter options for the trip creation assistant"
                            ),
                            FunctionDeclaration(
                                name = FunctionNames.INITIAL_PARAMETERS_FOLLOW_UP.value,
                                parameters = mapOf("questions" to Prompts.INITIAL_PARAMETERS_FOLLOW_UP.outputSchema),
                                description = "Creates the follow-up questions for the trip creation assistant"
                            ),
                            FunctionDeclaration(
                                name = FunctionNames.HIGH_LEVEL_ITINERARY_OPTIONS.value,
                                parameters = mapOf("result" to Prompts.HIGH_LEVEL_ITINERARY_OPTIONS.outputSchema),
                                description = "Creates the high-level travel itinerary options for the trip creation assistant"
                            ),
                            FunctionDeclaration(
                                name = FunctionNames.REFINE_ITINERARY.value,
                                parameters = mapOf("result" to Prompts.REFINE_ITINERARY.outputSchema),
                                description = "Creates the refined itinerary based on the user's feedback for the trip creation assistant",
                            ),
                            FunctionDeclaration(
                                name = FunctionNames.DAILY_ITINERARY.value,
                                parameters = mapOf("result" to Prompts.DAILY_ITINERARY.outputSchema),
                                description = "Creates the daily itineraries for the trip creation assistant",
                            ),
                        )
                    )
                )
            )
    })

    private val model by lazy { modelFactory() }

    private val chatModel by lazy { model.startChat() }

    suspend fun genInitialParametersOptions(basicInformation: GenAIData.BasicInformation): GenAIData.InitialParametersOptions? {
        val prompt = Prompts.INITIAL_PARAMETERS
        val promptQuery =
            prompt.prompt + "\n Basic Information: \n" + Json.encodeToString(basicInformation)

        return sendMessage<GenAIData.InitialParametersOptions>(
            promptQuery,
            FunctionNames.INITIAL_PARAMETERS,
            "parameters"
        )
    }

    suspend fun genInitialParametersFollowUpQuestions(
        parameters: GenAIData.InitialParametersOptions
    ): GenAIData.FollowUpQuestionsOutput? {
        val prompt = Prompts.INITIAL_PARAMETERS_FOLLOW_UP
        val promptQuery = prompt.prompt + "\n Parameters: \n" + Json.encodeToString(parameters)
        return sendMessage(
            promptQuery,
            FunctionNames.INITIAL_PARAMETERS_FOLLOW_UP,
            "questions",
        )
    }

    suspend fun genHighLevelItineraryOptions(followUpQuestions: List<GenAIData.FollowUpQuestion>): GenAIData.HighLevelItineraryOptions? {
        val prompt = Prompts.HIGH_LEVEL_ITINERARY_OPTIONS
        val promptQuery =
            prompt.prompt + "\n Follow-up Questions: \n" + followUpQuestions.joinToString("\n") { "Q: ${it.question}, A: ${it.answers.first()}" }
        return sendMessage<GenAIData.HighLevelItineraryOptions>(
            promptQuery,
            FunctionNames.HIGH_LEVEL_ITINERARY_OPTIONS,
            argName = "result",
        )
    }

    suspend fun genRefinedItinerary(
        refinement: String,
        itinerary: GenAIData.Itinerary
    ): GenAIData.Itinerary? {
        val prompt = Prompts.REFINE_ITINERARY
        val promptQuery =
            prompt.prompt + "\n Feedback: " + refinement +
                    "\n Selected Itinerary:\n" + Json.encodeToString(itinerary)
        return sendMessage<GenAIData.Itinerary>(
            promptQuery,
            FunctionNames.REFINE_ITINERARY,
            argName = "result",
        )
    }

    private val dailyItineraryModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                generationConfig = generationConfig {
                    maxOutputTokens = 65536 // Use max output tokens to avoid truncation
                    responseMimeType = "application/json"
                    responseSchema = Prompts.DAILY_ITINERARY.outputSchema
                },
                modelName = "gemini-3-flash-preview",
            )
    }

    suspend fun genDailyItinerary(
        basicInformation: GenAIData.BasicInformation,
        parameters: GenAIData.InitialParametersOptions,
        followUpQuestions: List<GenAIData.FollowUpQuestion>,
        itinerary: GenAIData.Itinerary,
        itineraryType: GenAIData.ItineraryType,
    ): GenAIData.DailyItinerary? {
        val prompt = Prompts.DAILY_ITINERARY
        val promptQuery =
            prompt.prompt + "\n Basic Information: \n" + Json.encodeToString(basicInformation) +
                    "\n Parameters: \n" + Json.encodeToString(parameters) +
                    "\n Follow-up Questions: \n" + followUpQuestions.joinToString("\n") { "Q: ${it.question}, A: ${it.answers.first()}" } +
                    "\n Selected Itinerary:\n" + Json.encodeToString(itinerary) +
                    "\n Itinerary Type: " + itineraryType.value
        val result = dailyItineraryModel.generateContent(promptQuery)
        return result.text?.let { Json.decodeFromString<GenAIData.DailyItinerary>(it) }
    }

    private fun GenerateContentResponse.getJsonArgs(
        functionName: FunctionNames, argName: String
    ): JsonElement? {
        val functionCall = functionCalls.find { it.name == functionName.value }

        val args = functionCall?.args[argName]
        if (args == null) {
            logger.debug("GenAIRepository", "No args found for function $functionName")
            logger.debug("GenAIRepository", this.toString())
        }
        return args
    }

    private suspend inline fun <reified T> GenerateContentResponse.getFunctionCallParams(
        functionName: FunctionNames, argName: String
    ): T {
        val json = getJsonArgs(functionName, argName)
        if (json == null) {
            if (typeOf<T>().isMarkedNullable) {
                return null as T
            } else {
                throw IllegalStateException("No args found for function $functionName")
            }
        }
        val jsonString = json.toString()
        val params = Json.decodeFromString<T>(jsonString)
        chatModel.sendMessage(content("function") {
            part(
                FunctionResponsePart(
                    functionName.value, JsonObject(
                        mapOf(argName to json)
                    )
                )
            )
        })
        return params
    }

    private suspend inline fun <reified T> sendMessage(
        prompt: String, functionName: FunctionNames, argName: String,
    ): T? {
        var currentPrompt = prompt
        var attempts = 0
        while (attempts < 3) {
            try {
                val response = chatModel.sendMessage(currentPrompt)
                return response.getFunctionCallParams<T>(functionName, argName)
            } catch (t: Throwable) {
                logger.error("GenAIRepository", "Error sending message", t)
                currentPrompt =
                    "Your previous response triggered the following error:\n${t.message}\n\nplease, regenerate the response"
                attempts++
            }
        }
        return null
    }
}