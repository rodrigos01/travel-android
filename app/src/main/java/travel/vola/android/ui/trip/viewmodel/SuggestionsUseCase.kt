package travel.vola.android.ui.trip.viewmodel

import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.model.data.GroupType
import travel.vola.android.model.data.Trip
import travel.vola.android.model.genai.GenAIData
import travel.vola.android.model.genai.GenAIRepository
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class SuggestionsUseCase(
    private val repository: GenAIRepository,
) {

    data class DailyItineraryState(
        val days: List<SuggestedDay>,
        val predictedChanges: List<String>,
    )

    data class SuggestedDay(
        val date: ZonedDateTime,
        val timedPlaces: List<TimedPlaceSuggestion>,
        val sections: List<DaySectionSuggestion>,
    )

    data class DaySectionSuggestion(
        val type: String,
        val name: String,
        val suggestions: List<TimedPlaceSuggestion>,
    )

    data class TimedPlaceSuggestion(
        val id: String,
        val name: String,
        val cityId: String,
        val coverImage: String,
        val reason: String,
        val startTime: ZonedDateTime?,
        val endTime: ZonedDateTime?,
    )


    suspend fun getSuggestions(trip: Trip): DailyItineraryState? {
        val preferences = trip.preferences ?: return null
        val result = repository.genDailyItinerary(
            basicInformation = GenAIData.BasicInformation(
                destination = "",
                dates = "",
                duration = "",
                groupType = when (preferences.basicInformation.groupType) {
                    GroupType.SOLO -> GenAIData.GroupType.SOLO
                    GroupType.FAMILY -> GenAIData.GroupType.FAMILY
                    GroupType.FRIENDS -> GenAIData.GroupType.FRIENDS
                    GroupType.COWORKERS -> GenAIData.GroupType.COWORKERS
                    GroupType.COUPLE -> GenAIData.GroupType.COUPLE
                },
                travelers = preferences.basicInformation.travelers
            ),
            parameters = GenAIData.InitialParametersOptions(
                occasions = preferences.initialParameters.occasions,
                interests = preferences.initialParameters.interests,
                vibe = preferences.initialParameters.vibe,
                focus = preferences.initialParameters.focus,
                mustHave = preferences.initialParameters.mustHave,
                duration = preferences.initialParameters.duration,
                anythingElse = preferences.initialParameters.anythingElse,
            ),
            followUpQuestions = preferences.questionsAnswers.map {
                GenAIData.FollowUpQuestion(
                    parameterSelections = emptyList(),
                    question = it.question,
                    answers = listOf(it.answer)
                )
            },
            itineraryType = GenAIData.ItineraryType.DETAILED,
            itinerary = GenAIData.Itinerary(
                name = trip.name.orEmpty(),
                startDate = trip.places.first().startDateTime.let {
                    GenAIData.DateResult(
                        it.dayOfMonth,
                        it.monthValue,
                        it.year
                    )
                },
                endDate = (trip.places.last().endDateTime ?: ZonedDateTime.now()).let {
                    GenAIData.DateResult(
                        it.dayOfMonth,
                        it.monthValue,
                        it.year
                    )
                },
                description = "",
                cities = trip.places.map { timedPlace ->
                    GenAIData.ItineraryCity(
                        timedPlace.place.id,
                        timedPlace.place.name,
                        searchQuery = "",
                        startDate = timedPlace.startDateTime.let {
                            GenAIData.DateResult(
                                it.dayOfMonth,
                                it.monthValue,
                                it.year
                            )
                        },
                        endDate = timedPlace.endDateTime?.let {
                            GenAIData.DateResult(
                                it.dayOfMonth,
                                it.monthValue,
                                it.year
                            )
                        } ?: return null,
                    )
                },
                predictedChanges = emptyList(),
            ),
        )
        return result?.let { result ->
            DailyItineraryState(
                days = result.days.map { day ->
                    SuggestedDay(
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(
                            day.date
                        )
                            ?.let {
                                ZonedDateTime.ofInstant(
                                    it.toInstant(),
                                    ZoneId.systemDefault()
                                )
                            } ?: ZonedDateTime.now(),
                        timedPlaces = day.timedPlaces.map { place ->
                            TimedPlaceSuggestion(
                                id = place.id,
                                cityId = place.cityId,
                                name = place.name,
                                coverImage = "",
                                reason = place.reason,
                                startTime = place.startTime?.let { zonedDateTime(it) },
                                endTime = place.endTime?.let { zonedDateTime(it) },
                            )
                        },
                        sections = day.sections.map { section ->
                            DaySectionSuggestion(
                                type = section.type,
                                name = section.name,
                                suggestions = section.suggestions.places.map { place ->
                                    TimedPlaceSuggestion(
                                        id = place.id,
                                        cityId = place.cityId,
                                        name = place.name,
                                        coverImage = "",
                                        reason = place.reason,
                                        startTime = place.startTime?.let { zonedDateTime(it) },
                                        endTime = place.endTime?.let { zonedDateTime(it) },
                                    )
                                }
                            )
                        },
                    )
                },
                predictedChanges = emptyList(),
            )
        }
    }
}