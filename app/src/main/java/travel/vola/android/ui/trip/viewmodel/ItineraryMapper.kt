package travel.vola.android.ui.trip.viewmodel

import travel.vola.android.extensions.dayOfMonthString
import travel.vola.android.extensions.dayOfWeekString
import travel.vola.android.extensions.timeString
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Itinerary
import travel.vola.android.model.data.ItineraryEvent
import travel.vola.android.model.data.ItineraryLeg
import travel.vola.android.model.data.LegType
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
 * The rows of the trip screen for an itinerary: a header for each leg that has one, month
 * headers, and a row for each event, with its date label and border worked out from where it
 * falls in its day. What is in the itinerary, and in what order, was decided by the backend.
 */
class ItineraryMapper(
    private val createFlexibleSectionState: (
        section: FlexibleDaySection,
        showDate: Boolean,
        backgroundStyle: BackgroundStyle,
    ) -> TripItemState.FlexibleDaySectionState,
) {

    /**
     * A trip with no itinerary yet (one the backend hasn't processed) has no rows; one with an
     * itinerary and nothing in it asks for a first plan.
     */
    fun map(
        itinerary: Itinerary?,
        flexibleSectionItems: List<TripItemState.FlexibleDaySectionState> = emptyList(),
        suggestions: SuggestionsUseCase.DailyItineraryState? = null,
    ): List<TripItemState> {
        itinerary ?: return emptyList()
        val items = Rows(flexibleSectionItems).apply { itinerary.withSuggestions(suggestions).legs.forEach(::add) }.items
        return items.ifEmpty {
            listOf(TripItemState.InitialAddPlanItemState(UUID.randomUUID().toString(), ZonedDateTime.now()))
        }
    }

    private inner class Rows(private val flexibleSectionItems: List<TripItemState.FlexibleDaySectionState>) {

        val items = mutableListOf<TripItemState>()
        private val monthsSeen = mutableSetOf<YearMonth>()
        private var previous: ZonedDateTime? = null

        fun add(leg: ItineraryLeg) {
            val sectionId = leg.place?.id ?: ""
            if (leg.type == LegType.PLACE) {
                val timestamp = leg.startedBy?.startDateTime ?: leg.firstTimestamp ?: leg.startDate.atStartOfDay(leg.zone)
                items += leg.header(timestamp, sectionId)
                addMonth(leg.startDate, timestamp, sectionId)
            }
            leg.days.forEach { day ->
                val events = day.events
                events.singleOrNull().let { it as? ItineraryEvent.EmptyDays }?.let { empty ->
                    items += empty.toItem(sectionId)
                    return@forEach
                }
                events.forEachIndexed { index, event ->
                    val timestamp = event.moment
                    if (index == 0) addMonth(day.date, timestamp, sectionId)
                    val isReturn = leg.type == LegType.TRANSIT && event is ItineraryEvent.FlightArrival
                    items += event.toItem(
                        showDate = index == 0 && !isReturn,
                        backgroundStyle = backgroundStyle(index, events.size),
                        sectionId = sectionId,
                    )
                    previous = timestamp
                }
            }
        }

        private fun addMonth(date: LocalDate, timestamp: ZonedDateTime, sectionId: String) {
            if (monthsSeen.add(YearMonth.from(date))) {
                items += TripItemState.MonthItemState(
                    timestamp = timestamp,
                    month = DateFormatSymbols.getInstance().months[date.monthValue - 1],
                    year = date.year.toString(),
                    sectionId = sectionId,
                )
            }
        }

        private fun backgroundStyle(index: Int, count: Int) = when {
            count == 1 -> BackgroundStyle.SINGLE
            index == 0 -> BackgroundStyle.TOP
            index == count - 1 -> BackgroundStyle.BOTTOM
            else -> BackgroundStyle.MIDDLE
        }

        // ---- Headers ----

        private val ItineraryLeg.zone get() = place?.timeZone?.toZoneId() ?: ZoneId.systemDefault()

        private val ItineraryLeg.firstTimestamp
            get() = days.asSequence().flatMap { it.events }.filterIsInstance<ItineraryEvent.OfEntity>()
                .firstOrNull()?.timestamp

        private fun ItineraryLeg.header(timestamp: ZonedDateTime, sectionId: String) = TripItemState.PlaceItemState(
            id = startedBy?.id ?: id,
            timestamp = timestamp,
            sectionId = sectionId,
            placeName = title,
            imageUrl = thumbnailUrl ?: place?.coverImage ?: "",
            dateStart = startDate.format(DAY_AND_MONTH),
            dateEnd = endDate.format(DAY_AND_MONTH),
        )

        // ---- Empty days ----

        private fun ItineraryEvent.EmptyDays.toItem(sectionId: String): TripItemState {
            // Where a plan added here would start: the day after the last thing, at the same time.
            val timestamp = previous?.let { it.plusDays(ChronoUnit.DAYS.between(it.toLocalDate(), start)) }
                ?: start.atStartOfDay(ZoneId.systemDefault())
            return if (start == end) {
                TripItemState.EmptyDateItemState(
                    id = id,
                    timestamp = timestamp,
                    sectionId = sectionId,
                    dayOfMonth = start.dayOfMonth.toString(),
                    dayOfWeek = start.format(DAY_OF_WEEK),
                    isGeneratingPlans = false,
                )
            } else {
                TripItemState.DateRangeItemState(
                    id = id,
                    timestamp = timestamp,
                    sectionId = sectionId,
                    dayOfMonthStart = start.dayOfMonth.toString(),
                    dayOfWeekStart = start.format(DAY_OF_WEEK),
                    dayOfMonthEnd = end.dayOfMonth.toString(),
                    dayOfWeekEnd = end.format(DAY_OF_WEEK),
                    isGeneratingPlans = false,
                )
            }
        }

        // ---- Events ----

        private val ItineraryEvent.moment: ZonedDateTime
            get() = when (this) {
                is ItineraryEvent.OfEntity -> timestamp
                is ItineraryEvent.Placeholder -> placeholder.timestamp
                is ItineraryEvent.EmptyDays -> error("Empty days have no time of day")
            }

        private fun ItineraryEvent.toItem(
            showDate: Boolean,
            backgroundStyle: BackgroundStyle,
            sectionId: String,
        ): TripItemState.EventItemState {
            val time = moment
            return when (this) {
                is ItineraryEvent.FlightDeparture -> TripItemState.FlightDepartureItemState(
                    id = id,
                    timestamp = time,
                    showDate = showDate,
                    dayOfMonth = time.dayOfMonthString,
                    dayOfWeek = time.dayOfWeekString,
                    time = time.timeString,
                    destination = segment.airportTo.city.name,
                    airport = segment.airportFrom.name,
                    backgroundStyle = backgroundStyle,
                    sectionId = sectionId,
                )

                is ItineraryEvent.FlightArrival -> TripItemState.FlightArrivalItemState(
                    id = id,
                    timestamp = time,
                    showDate = showDate,
                    dayOfMonth = time.dayOfMonthString,
                    dayOfWeek = time.dayOfWeekString,
                    time = time.timeString,
                    airport = segment.airportTo.name,
                    backgroundStyle = backgroundStyle,
                    sectionId = sectionId,
                )

                is ItineraryEvent.LodgingCheckIn -> TripItemState.HotelCheckInItemState(
                    id = id,
                    timestamp = time,
                    showDate = showDate,
                    dayOfMonth = time.dayOfMonthString,
                    dayOfWeek = time.dayOfWeekString,
                    time = time.timeString,
                    hotelName = lodging.name ?: "",
                    hotelAddress = lodging.address,
                    backgroundStyle = backgroundStyle,
                    sectionId = sectionId,
                )

                is ItineraryEvent.LodgingCheckOut -> TripItemState.HotelCheckOutItemState(
                    id = id,
                    timestamp = time,
                    showDate = showDate,
                    dayOfMonth = time.dayOfMonthString,
                    dayOfWeek = time.dayOfWeekString,
                    time = time.timeString,
                    hotelName = lodging.name ?: lodging.address,
                    backgroundStyle = backgroundStyle,
                    sectionId = sectionId,
                )

                is ItineraryEvent.TimedPlaceVisit -> TripItemState.TimedPlaceItemState(
                    id = id,
                    timestamp = time,
                    showDate = showDate,
                    dayOfMonth = time.dayOfMonthString,
                    dayOfWeek = time.dayOfWeekString,
                    time = time.timeString,
                    showTime = place.hasStartTime,
                    placeName = place.place.name,
                    cityName = place.city.name,
                    imageUrl = place.place.coverImage ?: "",
                    backgroundStyle = backgroundStyle,
                    sectionId = sectionId,
                )

                is ItineraryEvent.Restaurant -> TripItemState.RestaurantReservationItemState(
                    id = id,
                    timestamp = time,
                    showDate = showDate,
                    dayOfMonth = time.dayOfMonthString,
                    dayOfWeek = time.dayOfWeekString,
                    time = time.timeString,
                    restaurantName = reservation.place.name,
                    restaurantAddress = reservation.place.address,
                    backgroundStyle = backgroundStyle,
                    sectionId = sectionId,
                )

                // The use case keeps the state of sections being edited or searched; one it has
                // never seen came from the backend or a suggestion.
                is ItineraryEvent.FlexibleSection -> (
                    flexibleSectionItems.firstOrNull { it.id == section.id }
                        ?.copy(showDate = showDate, backgroundStyle = backgroundStyle)
                        ?: createFlexibleSectionState(section, showDate, backgroundStyle)
                    ).copy(sectionId = sectionId)

                is ItineraryEvent.Placeholder -> TripItemState.SuggestionPlaceholderItemState(
                    time,
                    showDate = showDate,
                    backgroundStyle = backgroundStyle,
                    sectionId = sectionId,
                    dayOfMonth = time.dayOfMonthString,
                    dayOfWeek = time.dayOfWeekString,
                )

                is ItineraryEvent.EmptyDays -> error("Empty days are not an event row")
            }
        }
    }

    private companion object {
        val DAY_AND_MONTH: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")
        val DAY_OF_WEEK: DateTimeFormatter = DateTimeFormatter.ofPattern("E")
    }
}
