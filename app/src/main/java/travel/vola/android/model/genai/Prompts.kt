package travel.vola.android.model.genai

import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.StringFormat

enum class Prompts(val prompt: String, val outputSchema: Schema) {
    DAILY_ITINERARY(
        prompt = "Based on the trip itinerary below, generate a reference guide for the dates requested. This reference guide should be organized by the days of the trip and for each day, have a diverse list of suggested places for the user to visit based on their preferences and other plans they already have for that date. The places should have categories so that they can be easily filtered in the UI and each day should have between 5 and 10 suggested places, with each category having at least 2 suggestions. Travel days should be divided in sections for each city the user will be on that date. Make sure the suggestions don’t include places the user already has in their itinerary.",
        outputSchema = Schema.obj(
            mapOf(
                "numDays" to Schema.integer(),
                "days" to Schema.array(
                    Schema.obj(
                        mapOf(
                            "date" to Schema.string(
                                "date of the day in the format YYYY-MM-DD",
                                format = StringFormat.Custom("date"),
                            ),
                            "sections" to Schema.array(
                                description = "Different sections of the day for when the user will be in different cities in the same day.",
                                items = Schema.obj(
                                    mapOf(
                                        "cityId" to Schema.string("id of the city as provided in the original itinerary"),
                                        "name" to Schema.string("A Short title describing the selection of places in this section"),
                                        "places" to Schema.array(placeSchema),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        ),
    ),
}

private val placeSchema = Schema.obj(
    mapOf(
        "name" to Schema.string("name of the place"),
        "category" to Schema.string("A fun category name for the place so it can be grouped with other suggestions"),
        "note" to Schema.string("A short, 5-10 word note indicating why the user should visit this place"),
        "searchQuery" to Schema.string("query to search for the place in google maps"),
        "startTime" to Schema.string(
            "time the user needs to be at this place, including the date",
            nullable = true,
            format = StringFormat.Custom("date-time"),
        ),
        "endTime" to Schema.string(
            "time the user needs to leave this place, including the date",
            nullable = true,
            format = StringFormat.Custom("date-time"),
        ),
    ),
)
