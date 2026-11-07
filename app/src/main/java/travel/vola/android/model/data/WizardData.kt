package travel.vola.android.model.data

import java.time.LocalDate

// What the trip-creation wizard exchanges with the backend's AI steps
// (WizardRepository). The conversation itself lives server-side: each step
// hands back an interaction id that the next step continues from.

class WizardStep<T>(val interactionId: String, val value: T)

data class WizardBasicInformation(
    val destinations: List<String>,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val fixedDates: Boolean,
    val groupType: GroupType,
    val travelers: Int,
)

data class WizardOptions(
    val occasions: List<String>,
    val interests: List<String>,
    val vibe: List<String>,
    val focus: List<String>,
    val mustHave: List<String>,
    val duration: List<String>,
)

data class WizardParameters(
    val occasions: List<String>,
    val interests: List<String>,
    val vibe: List<String>,
    val focus: List<String>,
    val mustHave: List<String>,
    val duration: List<String>,
    val anythingElse: String,
)

data class WizardQuestion(
    val parameterSelections: List<String>,
    val question: String,
    val answers: List<String>,
)

data class WizardAnswer(val question: String, val answer: String)

data class WizardCity(
    val name: String,
    val searchQuery: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    // The place the backend found for the city, if it found one.
    val place: Place?,
)

data class WizardItinerary(
    val name: String,
    val description: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val cities: List<WizardCity>,
    val predictedChanges: List<String>,
)

data class WizardItineraries(
    val planningLogic: String,
    val itineraries: List<WizardItinerary>,
)
