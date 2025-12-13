package travel.vola.android.model.genai

import com.google.firebase.ai.type.Schema

enum class Prompts(val prompt: String, val outputSchema: Schema) {
    INITIAL_PARAMETERS(
        "provide sets of options, based on the basic information provided by the user, to allow for the generation of their initial itinerary. The options should be compatible and commonly used with the basic information provided. The options should be associated with the basic information provided",
        Schema.obj(
            mapOf(
                "occasions" to Schema.array(
                    Schema.string("occasions travelers commonly travel for. Include a generic option like “vacation” for when there's no special occasion"),
                    description = "3-5 occasions travelers commonly travel for. Include a generic option like “vacation” for when there's no special occasion"
                ),
                "interests" to Schema.array(
                    Schema.string(),
                    description = "10-15 short-phrased broad interests (1-3 words)",
                ),
                "vibe" to Schema.array(
                    Schema.string(),
                    description = "5-8 short (1-3 words) atmospheres travelers commonly look for"
                ),
                "focus" to Schema.array(
                    Schema.string(),
                    description = "3-5 \"themes\" travelers would often plan their trips around"
                ),
                "mustHave" to Schema.array(
                    Schema.string(),
                    description = "3-5 possible experiences travelers often travel for. Those will be considered non-negotiable in their itinerary and the main reason the user is traveling"
                ),
                "duration" to Schema.array(
                    Schema.string(),
                    description = "(if not provided in the basic information): 3 time range options that are optimal for this trip.",
                    nullable = true,
                ),
            )
        )
    ),
    INITIAL_PARAMETERS_FOLLOW_UP(
        prompt = "The user has selected the below parameters from the options you provided. generate a maximum of 3 follow-up clarifying questions for any choices they might have made that conflict with each other or with their basic trip information or that require further clarification. Each question should be accompanied with 2-3 possible answers for the user to choose. Keep the answers as brief, single sentences. The answers should be definitive and not require further clarification. If the basic information and parameters are enough for generating an itinerary, it is acceptable to return no questions.",
        outputSchema = Schema.obj(
            mapOf(
                "questions" to Schema.array(
                    Schema.obj(
                        mapOf(
                            "parameter" to Schema.string("parameter of the selection that generated the question"),
                            "answer" to Schema.string("selection in the parameter that generated the question"),
                            "question" to Schema.string("question to ask the user"),
                            "answers" to Schema.array(
                                Schema.string("possible answers to the question")
                            )
                        )
                    )
                )
            )
        )
    )
}