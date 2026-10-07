package travel.vola.android.ui.trip.viewmodel

import travel.vola.android.extensions.dayOfMonthString
import travel.vola.android.extensions.dayOfWeekString
import travel.vola.android.extensions.timeString
import travel.vola.android.model.data.EntityEventType
import travel.vola.android.model.data.EntityRef
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Flight
import travel.vola.android.model.data.ItineraryEvent
import travel.vola.android.model.data.ItineraryLeg
import travel.vola.android.model.data.LegType
import travel.vola.android.model.data.Lodging
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.RestaurantReservation
import travel.vola.android.model.data.SuggestionPlaceholder
import travel.vola.android.model.data.TimedPlace
import travel.vola.android.model.data.Trip
import travel.vola.android.ui.trip.state.TripItemState
import travel.vola.android.ui.trip.state.TripItemState.EventItemState.BackgroundStyle
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Turns the itinerary the backend built into the rows the trip screen shows.
 *
 * What is on the trip, and in what order, is decided by the backend (see `buildItinerary` in
 * travel-node). This only decides how it looks: the place header at the start of a leg, month
 * headers, which rows show their date, and how rows in a day are bordered. It also lays the
 * suggestions the user is looking at on top, which the backend knows nothing about.
 */
class ItineraryMapper(
    private val createFlexibleSectionState: (
        section: FlexibleDaySection,
        showDate: Boolean,
        backgroundStyle: BackgroundStyle,
    ) -> TripItemState.FlexibleDaySectionState,
) {

    /**
     * A trip with no itinerary yet (one the backend hasn't processed) has no rows; a trip with
     * an itinerary but nothing in it has the prompt to add a first plan.
     */
    fun map(
        trip: Trip,
        flexibleSectionItems: List<TripItemState.FlexibleDaySectionState> = emptyList(),
        suggestions: SuggestionsUseCase.DailyItineraryState? = null,
    ): List<TripItemState> {
        val itinerary = trip.itinerary ?: return emptyList()
        val legs = itinerary.legs.map { it.asMapped() }
            .withSuggestions(suggestions.asSlots(itinerary.legs))
        val entities = Entities(trip, flexibleSectionItems)

        val items = Flattener(entities).flatten(legs)
        return items.ifEmpty {
            listOf(TripItemState.InitialAddPlanItemState(UUID.randomUUID().toString(), ZonedDateTime.now()))
        }
    }

    // ---- The itinerary plus whatever is laid over it ----

    private data class MappedLeg(val leg: ItineraryLeg, val days: List<MappedDay>)

    private data class MappedDay(val date: LocalDate, val slots: List<Slot>)

    private sealed interface Slot {

        data class OfEntity(val event: ItineraryEvent.OfEntity) : Slot

        data class SuggestedPlace(val place: TimedPlace) : Slot

        data class SuggestedSection(val section: FlexibleDaySection) : Slot

        data class Placeholder(val placeholder: SuggestionPlaceholder) : Slot

        /** One empty day when [start] is [end], a range of them otherwise. */
        data class Gap(val id: String, val start: LocalDate, val end: LocalDate) : Slot
    }

    private val Slot.timestamp: ZonedDateTime?
        get() = when (this) {
            is Slot.OfEntity -> event.timestamp
            is Slot.SuggestedPlace -> place.startDateTime
            is Slot.SuggestedSection -> section.date
            is Slot.Placeholder -> placeholder.timestamp
            is Slot.Gap -> null
        }

    private fun ItineraryLeg.asMapped() = MappedLeg(
        leg = this,
        days = days.map { day ->
            MappedDay(
                date = day.date,
                slots = day.events.map { it.asSlot() },
            )
        },
    )

    private fun ItineraryEvent.asSlot(): Slot = when (this) {
        is ItineraryEvent.OfEntity -> Slot.OfEntity(this)
        is ItineraryEvent.EmptyDay -> Slot.Gap(id, date, date)
        is ItineraryEvent.EmptyDateRange -> Slot.Gap(id, start, end)
    }

    // ---- Suggestions ----

    private fun SuggestionsUseCase.DailyItineraryState?.asSlots(legs: List<ItineraryLeg>): List<Slot> {
        if (this == null) {
            return emptyList()
        }
        // Suggestions are for the places the trip goes to.
        val cities = legs.filter { it.type != LegType.TRANSIT }.mapNotNull { it.place }.associateBy { it.id }
        val places = days.flatMap { day ->
            day.timedPlaces.mapNotNull { suggestion ->
                cities[suggestion.cityId]?.let { Slot.SuggestedPlace(suggestion.asTimedPlace(it, day.date)) }
            }
        }
        val sections = days.flatMap { day ->
            day.sections.mapNotNull { section ->
                cities[section.city.id]?.let { Slot.SuggestedSection(section.copy(city = it)) }
            }
        }
        return places + sections + placeHolders.map { Slot.Placeholder(it) }
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

    /**
     * Puts each suggestion on its day: among that day's events by time, or, on a day with
     * nothing planned, in place of it. The empty days either side stay as they were, which can
     * turn a range into a shorter range, a single empty day, or nothing.
     */
    private fun List<MappedLeg>.withSuggestions(suggestions: List<Slot>): List<MappedLeg> =
        suggestions.fold(this) { legs, suggestion ->
            val timestamp = suggestion.timestamp ?: return@fold legs
            val date = timestamp.toLocalDate()
            val cityId = (suggestion as? Slot.Placeholder)?.placeholder?.city?.id
                ?: (suggestion as? Slot.SuggestedPlace)?.place?.city?.id
                ?: (suggestion as? Slot.SuggestedSection)?.section?.city?.id
            val candidates = legs.indices.filter { date in legs[it].coverage() }
            val target = candidates.firstOrNull { legs[it].leg.place?.id == cityId }
                ?: candidates.firstOrNull()
                ?: return@fold legs
            legs.toMutableList().apply { this[target] = legs[target].with(suggestion, date, timestamp) }
        }

    /** From the leg's first day to the last day it has something, or something empty, on. */
    private fun MappedLeg.coverage(): ClosedRange<LocalDate> {
        val last = days.maxOfOrNull { day ->
            day.slots.maxOfOrNull { (it as? Slot.Gap)?.end ?: day.date } ?: day.date
        } ?: leg.endDate
        return leg.startDate..maxOf(leg.endDate, last)
    }

    private fun MappedLeg.with(suggestion: Slot, date: LocalDate, timestamp: ZonedDateTime): MappedLeg {
        val containing = days.indexOfFirst { day ->
            day.date == date || day.slots.any { it is Slot.Gap && date in it.start..it.end }
        }
        if (containing == -1) {
            val position = days.indexOfFirst { it.date > date }.let { if (it == -1) days.size else it }
            return copy(days = days.toMutableList().apply { add(position, MappedDay(date, listOf(suggestion))) })
        }
        val day = days[containing]
        val gap = day.slots.singleOrNull() as? Slot.Gap
        val replacement = if (gap != null) {
            gap.without(date, leg.id) + MappedDay(date, listOf(suggestion))
        } else {
            val position = day.slots.indexOfFirst { slot -> slot.timestamp?.let { it.isAfter(timestamp) } == true }
                .let { if (it == -1) day.slots.size else it }
            listOf(day.copy(slots = day.slots.toMutableList().apply { add(position, suggestion) }))
        }
        return copy(days = days.take(containing) + replacement.sortedBy { it.date } + days.drop(containing + 1))
    }

    /** What is left of this gap around [date], as days. */
    private fun Slot.Gap.without(date: LocalDate, legId: String): List<MappedDay> = buildList {
        if (start < date) {
            add(MappedDay(start, listOf(piece(legId, start, date.minusDays(1)))))
        }
        if (date < end) {
            add(MappedDay(date.plusDays(1), listOf(piece(legId, date.plusDays(1), end))))
        }
    }

    private fun Slot.Gap.piece(legId: String, start: LocalDate, end: LocalDate) = Slot.Gap(
        id = if (start == this.start && end == this.end) {
            id
        } else if (start == end) {
            "$legId:$start"
        } else {
            "$legId:$start:$end"
        },
        start = start,
        end = end,
    )

    // ---- Entities, by the references the itinerary holds ----

    private class Entities(trip: Trip, val flexibleSectionItems: List<TripItemState.FlexibleDaySectionState>) {
        val flights: Map<String, Flight> = trip.flights.associateBy { it.id }
        val lodgings: Map<String, Lodging> = trip.lodgings.associateBy { it.id }
        val places: Map<String, TimedPlace> = trip.places.associateBy { it.id }
        val restaurants: Map<String, RestaurantReservation> = trip.restaurants.associateBy { it.id }
        val sections: Map<String, FlexibleDaySection> = trip.flexibleSections.associateBy { it.id }
    }

    // ---- Rows ----

    /** An event row, once its position in the day is known. */
    private fun interface Row {
        fun build(showDate: Boolean, backgroundStyle: BackgroundStyle): TripItemState.EventItemState
    }

    private inner class Flattener(private val entities: Entities) {

        private val items = mutableListOf<TripItemState>()
        private val monthsSeen = mutableSetOf<YearMonth>()
        private var previousTimestamp: ZonedDateTime? = null

        fun flatten(legs: List<MappedLeg>): List<TripItemState> {
            legs.forEach { add(it) }
            return items
        }

        private fun add(mapped: MappedLeg) {
            val leg = mapped.leg
            val sectionId = leg.place?.id ?: ""
            // Rows whose entity is gone (it was deleted a moment ago) are left out.
            val days = mapped.days.map { day ->
                day to day.slots.mapNotNull { slot -> slot.asRow(sectionId) }
            }

            if (leg.type != LegType.TRANSIT) {
                val headerTimestamp = leg.headerTimestamp(days.firstNotNullOfOrNull { (_, rows) -> rows.firstOrNull()?.first })
                items += leg.header(headerTimestamp, sectionId)
                addMonthIfNew(leg.startDate, headerTimestamp, sectionId)
                previousTimestamp = headerTimestamp
            }

            days.forEach { (day, rows) ->
                val gap = day.slots.singleOrNull() as? Slot.Gap
                if (gap != null) {
                    items += gap.asItem(sectionId)
                    return@forEach
                }
                rows.forEachIndexed { index, (timestamp, slot, row) ->
                    addMonthIfNew(day.date, timestamp, sectionId)
                    val first = index == 0
                    val last = index == rows.lastIndex
                    val backgroundStyle = when {
                        first && last -> BackgroundStyle.SINGLE
                        first -> BackgroundStyle.TOP
                        last -> BackgroundStyle.BOTTOM
                        else -> BackgroundStyle.MIDDLE
                    }
                    // The flight home lands without a date of its own.
                    val isReturn = leg.type == LegType.TRANSIT && slot.isFlightArrival
                    items += row.build(showDate = first && !isReturn, backgroundStyle = backgroundStyle)
                    previousTimestamp = timestamp
                }
            }
        }

        private fun addMonthIfNew(date: LocalDate, timestamp: ZonedDateTime, sectionId: String) {
            if (monthsSeen.add(YearMonth.from(date))) {
                items += TripItemState.MonthItemState(
                    timestamp = timestamp,
                    month = date.monthString,
                    year = date.year.toString(),
                    sectionId = sectionId,
                )
            }
        }

        // ---- Place headers ----

        private fun ItineraryLeg.headerTimestamp(firstRow: ZonedDateTime?): ZonedDateTime =
            entityRef?.let { entities.places[it.id]?.startDateTime }
                ?: firstRow
                ?: startDate.atStartOfDay(place?.timeZone?.toZoneId() ?: ZoneId.systemDefault())

        private fun ItineraryLeg.header(timestamp: ZonedDateTime, sectionId: String) = TripItemState.PlaceItemState(
            id = entityRef?.id ?: id,
            timestamp = timestamp,
            sectionId = sectionId,
            placeName = title,
            imageUrl = thumbnailUrl ?: place?.coverImage ?: "",
            dateStart = startDate.dayAndMonth,
            dateEnd = endDate.dayAndMonth,
            entityRef = entityRef,
        )

        // ---- Empty days ----

        private fun Slot.Gap.asItem(sectionId: String): TripItemState {
            // Where a plan added here would start: the day after the last thing, at the same time.
            val timestamp = previousTimestamp?.let {
                it.plusDays(ChronoUnit.DAYS.between(it.toLocalDate(), start))
            } ?: start.atStartOfDay(ZoneId.systemDefault())
            return if (start == end) {
                TripItemState.EmptyDateItemState(
                    id = id,
                    timestamp = timestamp,
                    sectionId = sectionId,
                    dayOfMonth = start.dayOfMonth.toString(),
                    dayOfWeek = start.weekdayString,
                    isGeneratingPlans = false,
                )
            } else {
                TripItemState.DateRangeItemState(
                    id = id,
                    timestamp = timestamp,
                    sectionId = sectionId,
                    dayOfMonthStart = start.dayOfMonth.toString(),
                    dayOfWeekStart = start.weekdayString,
                    dayOfMonthEnd = end.dayOfMonth.toString(),
                    dayOfWeekEnd = end.weekdayString,
                    isGeneratingPlans = false,
                )
            }
        }

        // ---- Event rows ----

        private val Slot.isFlightArrival: Boolean
            get() = this is Slot.OfEntity && event.type == EntityEventType.FLIGHT_ARRIVAL

        private fun Slot.asRow(sectionId: String): Triple<ZonedDateTime, Slot, Row>? {
            val timestamp = timestamp ?: return null
            val row: Row = when (this) {
                is Slot.Gap -> return null
                is Slot.OfEntity -> entityRow(event, sectionId) ?: return null
                is Slot.SuggestedPlace -> timedPlaceRow(place, id = place.id, sectionId = sectionId, entityRef = null)
                is Slot.SuggestedSection -> flexibleSectionRow(section, sectionId, entityRef = null)
                is Slot.Placeholder -> Row { showDate, backgroundStyle ->
                    TripItemState.SuggestionPlaceholderItemState(
                        placeholder.timestamp,
                        showDate = showDate,
                        backgroundStyle = backgroundStyle,
                        sectionId = sectionId,
                        dayOfMonth = placeholder.timestamp.dayOfMonthString,
                        dayOfWeek = placeholder.timestamp.dayOfWeekString,
                    )
                }
            }
            return Triple(timestamp, this, row)
        }

        private fun entityRow(event: ItineraryEvent.OfEntity, sectionId: String): Row? = when (event.type) {
            EntityEventType.FLIGHT_DEPARTURE, EntityEventType.FLIGHT_ARRIVAL -> {
                val segment = entities.flights[event.entityRef.id]?.segments?.getOrNull(event.entityRef.segmentIndex ?: 0)
                segment?.let { segment ->
                    Row { showDate, backgroundStyle ->
                        if (event.type == EntityEventType.FLIGHT_DEPARTURE) {
                            TripItemState.FlightDepartureItemState(
                                id = event.id,
                                timestamp = event.timestamp,
                                showDate = showDate,
                                dayOfMonth = event.timestamp.dayOfMonthString,
                                dayOfWeek = event.timestamp.dayOfWeekString,
                                time = event.timestamp.timeString,
                                destination = segment.airportTo.city.name,
                                airport = segment.airportFrom.name,
                                backgroundStyle = backgroundStyle,
                                sectionId = sectionId,
                                entityRef = event.entityRef,
                            )
                        } else {
                            TripItemState.FlightArrivalItemState(
                                id = event.id,
                                timestamp = event.timestamp,
                                showDate = showDate,
                                dayOfMonth = event.timestamp.dayOfMonthString,
                                dayOfWeek = event.timestamp.dayOfWeekString,
                                time = event.timestamp.timeString,
                                airport = segment.airportTo.name,
                                backgroundStyle = backgroundStyle,
                                sectionId = sectionId,
                                entityRef = event.entityRef,
                            )
                        }
                    }
                }
            }

            EntityEventType.LODGING_CHECK_IN -> entities.lodgings[event.entityRef.id]?.let { lodging ->
                Row { showDate, backgroundStyle ->
                    TripItemState.HotelCheckInItemState(
                        id = event.id,
                        timestamp = event.timestamp,
                        showDate = showDate,
                        dayOfMonth = event.timestamp.dayOfMonthString,
                        dayOfWeek = event.timestamp.dayOfWeekString,
                        time = event.timestamp.timeString,
                        hotelName = lodging.name ?: "",
                        hotelAddress = lodging.address,
                        backgroundStyle = backgroundStyle,
                        sectionId = sectionId,
                        entityRef = event.entityRef,
                    )
                }
            }

            EntityEventType.LODGING_CHECK_OUT -> entities.lodgings[event.entityRef.id]?.let { lodging ->
                Row { showDate, backgroundStyle ->
                    TripItemState.HotelCheckOutItemState(
                        id = event.id,
                        timestamp = event.timestamp,
                        showDate = showDate,
                        dayOfMonth = event.timestamp.dayOfMonthString,
                        dayOfWeek = event.timestamp.dayOfWeekString,
                        time = event.timestamp.timeString,
                        hotelName = lodging.name ?: lodging.address,
                        backgroundStyle = backgroundStyle,
                        sectionId = sectionId,
                        entityRef = event.entityRef,
                    )
                }
            }

            EntityEventType.TIMED_PLACE -> entities.places[event.entityRef.id]?.let {
                timedPlaceRow(it, event.id, sectionId, event.entityRef)
            }

            EntityEventType.RESTAURANT -> entities.restaurants[event.entityRef.id]?.let { restaurant ->
                Row { showDate, backgroundStyle ->
                    TripItemState.RestaurantReservationItemState(
                        id = event.id,
                        timestamp = event.timestamp,
                        showDate = showDate,
                        dayOfMonth = event.timestamp.dayOfMonthString,
                        dayOfWeek = event.timestamp.dayOfWeekString,
                        time = event.timestamp.timeString,
                        restaurantName = restaurant.place.name,
                        restaurantAddress = restaurant.place.address,
                        backgroundStyle = backgroundStyle,
                        sectionId = sectionId,
                        entityRef = event.entityRef,
                    )
                }
            }

            EntityEventType.FLEXIBLE_SECTION -> entities.sections[event.entityRef.id]?.let {
                flexibleSectionRow(it, sectionId, event.entityRef)
            }
        }

        private fun timedPlaceRow(place: TimedPlace, id: String, sectionId: String, entityRef: EntityRef?) =
            Row { showDate, backgroundStyle ->
                TripItemState.TimedPlaceItemState(
                    id = id,
                    timestamp = place.startDateTime,
                    showDate = showDate,
                    dayOfMonth = place.startDateTime.dayOfMonthString,
                    dayOfWeek = place.startDateTime.dayOfWeekString,
                    time = place.startDateTime.timeString,
                    showTime = place.hasStartTime,
                    placeName = place.place.name,
                    cityName = place.city.name,
                    imageUrl = place.place.coverImage ?: "",
                    backgroundStyle = backgroundStyle,
                    sectionId = sectionId,
                    entityRef = entityRef,
                )
            }

        private fun flexibleSectionRow(section: FlexibleDaySection, sectionId: String, entityRef: EntityRef?) =
            Row { showDate, backgroundStyle ->
                (
                    entities.flexibleSectionItems.firstOrNull { it.id == section.id }?.copy(
                        showDate = showDate,
                        backgroundStyle = backgroundStyle,
                    )
                        // Sections the use case doesn't know about came from the backend or a suggestion.
                        ?: createFlexibleSectionState(section, showDate, backgroundStyle)
                    ).copy(sectionId = sectionId, entityRef = entityRef)
            }
    }

    private companion object {
        private val DAY_AND_MONTH = DateTimeFormatter.ofPattern("MMM d")
        private val DAY_OF_WEEK = DateTimeFormatter.ofPattern("E")

        val LocalDate.dayAndMonth: String get() = format(DAY_AND_MONTH)
        val LocalDate.weekdayString: String get() = format(DAY_OF_WEEK)
        val LocalDate.monthString: String get() = DateFormatSymbols.getInstance().months[monthValue - 1]
    }
}
