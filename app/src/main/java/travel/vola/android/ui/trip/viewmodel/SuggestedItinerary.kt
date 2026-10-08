package travel.vola.android.ui.trip.viewmodel

import travel.vola.android.model.data.Itinerary
import travel.vola.android.model.data.ItineraryDay
import travel.vola.android.model.data.ItineraryEvent
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.TimedPlace
import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * The itinerary with the plans being suggested laid over it. The backend knows nothing of them
 * (they only become part of the trip if the user confirms one), so they go where they fall: on
 * a day with events, among them by time; on empty days, in place of them, leaving whatever empty
 * days are left either side.
 */
fun Itinerary.withSuggestions(suggestions: SuggestionsUseCase.DailyItineraryState?): Itinerary {
    if (suggestions == null) {
        return this
    }
    val cities = legs.mapNotNull { it.place }.associateBy { it.id }
    val byDate = suggestions.asEvents(cities).groupBy { it.first }.mapValues { (_, events) -> events.map { it.second } }
    // A date shared by two legs (a travel day) takes its suggestions in the first.
    val pending = byDate.toMutableMap()
    return copy(
        legs = legs.map { leg ->
            leg.copy(days = leg.days.flatMap { it.withSuggestions(pending) })
        },
    )
}

private fun ItineraryDay.withSuggestions(pending: MutableMap<LocalDate, List<ItineraryEvent>>): List<ItineraryDay> {
    val gap = events.singleOrNull() as? ItineraryEvent.EmptyDays
    return if (gap != null) {
        gap.fillIn(pending)
    } else {
        val suggested = pending.remove(date) ?: return listOf(this)
        listOf(copy(events = suggested.fold(events) { events, event -> events.insertedByTime(event) }))
    }
}

/** The empty days, with a day of suggestions wherever there are some. */
private fun ItineraryEvent.EmptyDays.fillIn(pending: MutableMap<LocalDate, List<ItineraryEvent>>): List<ItineraryDay> {
    val days = mutableListOf<ItineraryDay>()
    var emptyFrom: LocalDate? = null

    fun closeEmptyDays(until: LocalDate) {
        val from = emptyFrom ?: return
        val id = if (from == start && until == end) id else "$id:$from:$until"
        days += ItineraryDay(from, listOf(ItineraryEvent.EmptyDays(id, from, until)))
        emptyFrom = null
    }

    generateSequence(start) { it.plusDays(1) }.takeWhile { it <= end }.forEach { date ->
        val suggested = pending.remove(date)
        if (suggested == null) {
            emptyFrom = emptyFrom ?: date
        } else {
            closeEmptyDays(until = date.minusDays(1))
            days += ItineraryDay(date, suggested)
        }
    }
    closeEmptyDays(until = end)
    return days
}

private fun List<ItineraryEvent>.insertedByTime(event: ItineraryEvent): List<ItineraryEvent> {
    val moment = event.moment ?: return this + event
    val position = indexOfFirst { it.moment?.isAfter(moment) == true }.takeIf { it != -1 } ?: size
    return take(position) + event + drop(position)
}

private val ItineraryEvent.moment: ZonedDateTime?
    get() = when (this) {
        is ItineraryEvent.OfEntity -> timestamp
        is ItineraryEvent.Placeholder -> placeholder.timestamp
        is ItineraryEvent.EmptyDays -> null
    }

// ---- Suggestions as events ----

/** Each suggestion with the date it is for. Ones for somewhere the trip doesn't go are left out. */
private fun SuggestionsUseCase.DailyItineraryState.asEvents(cities: Map<String, Place>): List<Pair<LocalDate, ItineraryEvent>> {
    val places = days.flatMap { day ->
        day.timedPlaces.mapNotNull { suggestion ->
            cities[suggestion.cityId]?.let { suggestion.asTimedPlace(it, day.date) }
        }
    }.filter { it.endDateTime?.toLocalDate()?.let { end -> end == it.startDateTime.toLocalDate() } ?: true }
    val sections = days.flatMap { day ->
        day.sections.mapNotNull { section -> cities[section.city.id]?.let { section.copy(city = it) } }
    }
    return places.map { it.startDateTime.toLocalDate() to ItineraryEvent.TimedPlaceVisit(it.id, it) } +
        sections.map { it.date.toLocalDate() to ItineraryEvent.FlexibleSection(it.id, it) } +
        placeHolders.map { it.timestamp.toLocalDate() to ItineraryEvent.Placeholder(it) }
}

private fun SuggestionsUseCase.TimedPlaceSuggestion.asTimedPlace(city: Place, day: ZonedDateTime): TimedPlace {
    val zone = city.timeZone.toZoneId()
    return TimedPlace(
        id = id,
        startDateTime = startTime?.withZoneSameLocal(zone) ?: day,
        hasStartTime = startTime != null,
        endDateTime = endTime?.withZoneSameLocal(zone),
        hasEndTime = endTime != null,
        city = city,
        place = Place(
            id = name,
            name = name,
            coverImage = coverImage,
            latitude = 0.0,
            longitude = 0.0,
            address = reason,
            externalId = id,
            timeZone = city.timeZone,
            source = "",
        ),
    )
}
