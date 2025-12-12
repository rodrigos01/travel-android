package travel.vola.android.model.genai

import com.google.firebase.ai.type.Schema

enum class Prompts(val prompt: String, val outputSchema: Schema) {
    INITIAL_PARAMETERS(
        "provide sets options, based on the information provided by the user, to allow for the generation of high-level travel itineraries focused only on destination and dates. The options should be associated with the destination, time period (if provided) and duration (if provided)." +
                "The options should follow these requirements:" +
                "Occasions: between 3 and 4 options," +
                "Interests: between 10 and 15 options," +
                "Vibe: between 5 and 8 options," +
                "Focus: between 3 and 5 options," +
                "Must-Have: between 3 and 5 options," +
                "Duration: 3 options if the user has not provided a fixed duration, empty otherwise," +
                "Must-Have: between 3 and 5 options.",
        Schema.obj(
            mapOf(
                "occasions" to Schema.array(
                    Schema.string("occasions travelers commonly travel for. Include a generic option like “vacation” for when there's no special occasion")
                ),
                "interests" to Schema.array(
                    Schema.string("occasions travelers commonly travel for. Include a generic option like “vacation” for when there's no special occasion")
                ),
                "vibe" to Schema.array(
                    Schema.string("short (one or two words) atmospheres travelers commonly look for")
                ),
                "focus" to Schema.array(
                    Schema.string("\"themes\" travelers would often plan their trips around")
                ),
                "mustHave" to Schema.array(
                    Schema.string("possible experiences travelers often travel for")
                ),
                "duration" to Schema.array(
                    Schema.string("time range options that are optimal for this trip")
                ),
            )
        )
    )
}