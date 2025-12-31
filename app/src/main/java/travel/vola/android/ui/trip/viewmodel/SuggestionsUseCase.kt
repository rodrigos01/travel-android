package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import travel.vola.android.extensions.dateString
import travel.vola.android.extensions.getDestinations
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.model.data.FlexibleDayCategory
import travel.vola.android.model.data.FlexibleDayItem
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.GroupType
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.Trip
import travel.vola.android.model.genai.GenAIData
import travel.vola.android.model.genai.GenAIRepository
import java.time.ZonedDateTime
import java.util.TimeZone
import java.util.UUID

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
        val sections: List<FlexibleDaySection>,
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

    private val _state = MutableStateFlow(DailyItineraryState(emptyList(), emptyList()))
    val state = _state.asStateFlow()


    suspend fun getSuggestions(trip: Trip, dates: List<ZonedDateTime>) {
        val preferences = trip.preferences ?: return
        val destinations = trip.getDestinations()
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
            itineraryType = GenAIData.ItineraryType.OPEN_ENDED,
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
                cities = destinations.map { destination ->
                    GenAIData.ItineraryCity(
                        destination.place.id,
                        destination.place.name,
                        searchQuery = "",
                        startDate = destination.startDateTime.let {
                            GenAIData.DateResult(
                                it.dayOfMonth,
                                it.monthValue,
                                it.year
                            )
                        },
                        endDate = destination.endDateTime?.let {
                            GenAIData.DateResult(
                                it.dayOfMonth,
                                it.monthValue,
                                it.year
                            )
                        } ?: return,
                    )
                },
                predictedChanges = emptyList(),
            ),
            dates = dates,
        )
        val existingDates = state.value.days.map { it.date.dateString("yyyy-MM-dd") }
        val existingDays = result?.days?.filter { existingDates.contains(it.date) } ?: emptyList()
        val newDays = result?.days?.filterNot { existingDates.contains(it.date) }?.map { day ->
            val date = zonedDateTime(day.date, pattern = "yyyy-MM-dd")
            SuggestedDay(
                date = date,
                timedPlaces = emptyList(),
                sections = day.sections.map { it.toAppData(date) },
            )
        } ?: emptyList()
        _state.value = state.value.copy(
            days = newDays + state.value.days.map { currentDay ->
                val newDay =
                    existingDays.firstOrNull { it.date == currentDay.date.dateString("yyyy-MM-dd") }
                currentDay.copy(
                    sections = currentDay.sections + (newDay?.sections?.map { section ->
                        section.toAppData(currentDay.date)
                    } ?: emptyList())
                )
            }
        )
    }

    private fun GenAIData.Section.toAppData(
        date: ZonedDateTime,
    ): FlexibleDaySection = FlexibleDaySection(
        id = UUID.randomUUID().toString(),
        name = name,
        date = date,
        city = Place(
            id = cityId,
            name = "",
            coverImage = "",
            latitude = 0.0,
            longitude = 0.0,
            address = "",
            externalId = cityId,
            timeZone = TimeZone.getDefault(),
            source = "",
        ),
        categories = places.groupBy { it.category }
            .map { (categoryName, places) ->
                FlexibleDayCategory(
                    name = categoryName,
                    items = places.map { place ->
                        val placeId = UUID.randomUUID().toString()
                        FlexibleDayItem(
                            id = placeId,
                            place = Place(
                                id = placeId,
                                name = place.name,
                                coverImage = "",
                                latitude = 0.0,
                                longitude = 0.0,
                                address = "",
                                externalId = placeId,
                                timeZone = TimeZone.getDefault(),
                                source = "Gemini",
                            ),
                            note = place.note
                        )
                    }
                )
            },
    )
}