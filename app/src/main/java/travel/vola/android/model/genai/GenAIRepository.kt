package travel.vola.android.model.genai

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class GenAIRepository private constructor(
    private val placesClient: PlacesClient,
) {

    constructor(applicationContext: Context) : this(Places.createClient(applicationContext))

    enum class FunctionNames(val value: String) {
        INITIAL_PARAMETERS("genInitialParameters"),
        INITIAL_PARAMETERS_FOLLOW_UP("genInitialParametersFollowUp"),
        HIGH_LEVEL_ITINERARY_OPTIONS("genHighLevelItineraryOptions"),
    }

    private val chatModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-2.5-flash",
                systemInstruction = content {
                    text("You are an AI Travel Assistant running on the background of a Travel Planning application. Help the user plan a trip, initially by planning a high-level travel itinerary focused only on destination and dates, then later by planning fine-grained day-by-day itineraries")
                },
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
                                description = "Creates the high-level itinerary options for the trip creation assistant"
                            ),
                        )
                    )
                )
            ).startChat()
    }

    suspend fun genInitialParametersOptions(basicInformation: GenAIData.BasicInformation): GenAIData.InitialParametersOptions? {
        val prompt = Prompts.INITIAL_PARAMETERS
        val promptQuery =
            prompt.prompt + "\n Basic Information: \n" + Json.encodeToString(basicInformation)

        val result = chatModel.sendMessage(promptQuery)
        return result.getFunctionCallParams(FunctionNames.INITIAL_PARAMETERS, "parameters")
    }

    suspend fun genInitialParametersFollowUpQuestions(
        parameters: GenAIData.InitialParametersOptions
    ): GenAIData.FollowUpQuestionsOutput? {
        val prompt = Prompts.INITIAL_PARAMETERS_FOLLOW_UP
        val promptQuery =
            prompt.prompt +
                    "\n Parameters: \n" + Json.encodeToString(parameters)
        val result = chatModel.sendMessage(promptQuery)
        return result.getFunctionCallParams(FunctionNames.INITIAL_PARAMETERS_FOLLOW_UP, "questions")
    }

    suspend fun genHighLevelItineraryOptions(followUpQuestions: List<GenAIData.FollowUpQuestion>): GenAIData.HighLevelItineraryOptions? {
        val prompt = Prompts.HIGH_LEVEL_ITINERARY_OPTIONS
        val promptQuery = prompt.prompt +
                "\n Follow-up Questions: \n" +
                followUpQuestions.joinToString("\n") { "Q: ${it.question}, A: ${it.answers.first()}" }
        val result = chatModel.sendMessage(promptQuery)
        return result.getFunctionCallParams(FunctionNames.HIGH_LEVEL_ITINERARY_OPTIONS, "result")
    }

    private suspend inline fun <reified T> GenerateContentResponse.getFunctionCallParams(
        functionName: FunctionNames,
        argName: String
    ): T? {
        val functionCall =
            functionCalls.find { it.name == functionName.value }

        val json = functionCall?.args[argName] ?: return null
        val jsonString = json.toString()
        val params = Json.decodeFromString<T>(jsonString)
        chatModel.sendMessage(content("function") {
            part(
                FunctionResponsePart(
                    functionName.value, JsonObject(
                        mapOf("questions" to json)
                    )
                )
            )
        })
        return params
    }
}