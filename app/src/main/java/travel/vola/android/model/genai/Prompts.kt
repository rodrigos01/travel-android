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
                            "parameterSelection" to Schema.string("selection in the parameter that generated the question"),
                            "question" to Schema.string("question to ask the user"),
                            "answers" to Schema.array(
                                Schema.string("possible answers to the question")
                            )
                        )
                    )
                )
            )
        )
    ),
    HIGH_LEVEL_ITINERARY_OPTIONS(
        prompt = "Generate 3 high-level travel itinerary options based on the user's basic information, the parameters provided and the answers provided to the questions below. These itineraries should be basic skeletons with just cities visited and how long to stay in each. If the destination is not specific (i.e. a region, a country or a continent), suggest itineraries that include multiple cities to match their parameters. If the duration is not specific (a range of days) the itineraries should include suggested start and end dates that best match the destinations and parameters. The itineraries should consider their interests, focus and must-have experiences for their dates. If the itineraries include multiple cities, it should consider travel between the destinations for their order. At the end, suggest 3 short-phrase predicted potential changes the users might want to make to the generated itineraries, focused solely on the cities and periods in each of them.",
        outputSchema = Schema.obj(
            mapOf(
                "itineraries" to Schema.array(
                    Schema.obj(
                        mapOf(
                            "name" to Schema.string("name of the itinerary"),
                            "description" to Schema.string("A single-sentence description for this itinerary that includes why it fits the user choices"),
                            "startDate" to Schema.string("ISO-8601 formatted date representing the first day of the itinerary"),
                            "endDate" to Schema.string("ISO-8601 formatted date representing the last day of the itinerary"),
                            "cities" to Schema.array(
                                Schema.obj(
                                    mapOf(
                                        "name" to Schema.string("name of the city"),
                                        "startDate" to Schema.string("ISO-8601 formatted date representing the first day in this city"),
                                        "endDate" to Schema.string("ISO-8601 formatted date representing the last day in  this city"),
                                    )
                                )
                            )
                        )
                    )
                ),
                "predictedChanges" to Schema.array(
                    Schema.string(),
                    description = "3 short-phrase predicted potential changes the users might want to make to the generated itineraries, focused solely on the cities and periods in each of them"
                ),
            )
        )
    ),
}