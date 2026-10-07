package travel.vola.android.ui.trip.viewmodel

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.model.data.EntityRef
import travel.vola.android.model.data.EntityType
import travel.vola.android.model.data.Itinerary
import travel.vola.android.model.data.LegType
import travel.vola.android.model.data.SuggestionPlaceholder
import travel.vola.android.ui.trip.state.TripItemState.DateRangeItemState
import travel.vola.android.ui.trip.state.TripItemState.EmptyDateItemState
import travel.vola.android.ui.trip.state.TripItemState.EventItemState
import travel.vola.android.ui.trip.state.TripItemState.FlexibleDaySectionState
import travel.vola.android.ui.trip.state.TripItemState.FlightArrivalItemState
import travel.vola.android.ui.trip.state.TripItemState.FlightDepartureItemState
import travel.vola.android.ui.trip.state.TripItemState.HotelCheckInItemState
import travel.vola.android.ui.trip.state.TripItemState.HotelCheckOutItemState
import travel.vola.android.ui.trip.state.TripItemState.MonthItemState
import travel.vola.android.ui.trip.state.TripItemState.PlaceItemState
import travel.vola.android.ui.trip.state.TripItemState.RestaurantReservationItemState
import travel.vola.android.ui.trip.state.TripItemState.TimedPlaceItemState

class ItineraryMapperTest {

    private val newYork = place("New York")
    private val porto = place("Porto", coverImage = "https://example.com/porto.jpg")
    private val nice = place("Nice")

    private val outbound = flight("out", newYork, "2024-05-10T22:05:00-04:00", porto, "2024-05-11T10:00:00+01:00")
    private val home = flight("home", porto, "2024-05-14T17:05:00+01:00", newYork, "2024-05-14T20:05:00-04:00")
    private val portoHotel = lodging("hotel", porto, "2024-05-11T13:00:00+01:00", "2024-05-14T11:00:00+01:00")

    private val mapper = ItineraryMapper { section, showDate, backgroundStyle ->
        FlexibleDaySectionState(
            id = section.id,
            timestamp = section.date,
            dayOfMonth = null,
            dayOfWeek = null,
            showDate = showDate,
            backgroundStyle = backgroundStyle,
            name = section.name,
            subtitle = "",
            categories = emptyList(),
            searchResults = emptyList(),
            isGenerated = true,
        )
    }

    /** NYC -> Porto for four nights -> NYC, as the backend lays it out. */
    private fun roundTrip() = trip {
        transit(newYork, "2024-05-10") { day("2024-05-10") { departure(outbound) } }
        leg(porto, "2024-05-11", "2024-05-14") {
            day("2024-05-11") {
                arrival(outbound)
                checkIn(portoHotel)
            }
            emptyRange("2024-05-12", "2024-05-13")
            day("2024-05-14") {
                checkOut(portoHotel)
                departure(home)
            }
        }
        transit(newYork, "2024-05-14") { day("2024-05-14") { arrival(home) } }
    }

    // ---- What there is ----

    @Test
    fun `a trip the backend hasn't processed yet has no rows`() {
        val unprocessed = trip { }.copy(itinerary = null)

        assertThat(mapper.map(unprocessed)).isEmpty()
    }

    @Test
    fun `a trip with an empty itinerary asks for a first plan`() {
        assertThat(mapper.map(trip { }).labels()).containsExactly("initial")
    }

    @Test
    fun `a round trip is laid out leg by leg`() {
        assertThat(mapper.map(roundTrip()).labels()).containsExactly(
            "month May 2024",
            "departure New York Airport +date SINGLE",
            "place Porto May 11-May 14",
            "arrival Porto Airport +date TOP",
            "check-in Hotel Porto BOTTOM",
            "range 12-13",
            "check-out Hotel Porto +date TOP",
            "departure Porto Airport BOTTOM",
            "arrival New York Airport SINGLE",
        )
    }

    // ---- Place headers ----

    @Test
    fun `a place leg gets a header with its dates, picture and place, and a transit leg doesn't`() {
        val items = mapper.map(roundTrip())

        val headers = items.filterIsInstance<PlaceItemState>()
        assertThat(headers).hasSize(1)
        val header = headers.single()
        assertThat(header.placeName).isEqualTo("Porto")
        assertThat(header.dateStart).isEqualTo("May 11")
        assertThat(header.dateEnd).isEqualTo("May 14")
        assertThat(header.sectionId).isEqualTo("Porto")
        assertThat(header.id).isEqualTo("Porto_2024-05-11")
        assertThat(header.entityRef).isNull()
        assertThat(header.timestamp).isEqualTo(zonedDateTime("2024-05-11T10:00:00+01:00"))
    }

    @Test
    fun `the picture comes from the leg, falling back to the place's`() {
        val withThumbnail = trip { leg(porto, "2024-05-11", "2024-05-12", thumbnailUrl = "https://example.com/t.jpg") { } }
        val withoutThumbnail = trip { leg(porto, "2024-05-11", "2024-05-12") { } }

        assertThat(mapper.map(withThumbnail).filterIsInstance<PlaceItemState>().single().imageUrl)
            .isEqualTo("https://example.com/t.jpg")
        assertThat(mapper.map(withoutThumbnail).filterIsInstance<PlaceItemState>().single().imageUrl)
            .isEqualTo("https://example.com/porto.jpg")
    }

    @Test
    fun `a header with no events is still shown`() {
        val stay = timedPlace("stay", porto, porto, "2024-05-11T00:00:00+01:00", "2024-05-14T00:00:00+01:00")
        val items = mapper.map(trip { leg(porto, "2024-05-11", "2024-05-14", startedBy = stay) { } })

        assertThat(items.labels()).containsExactly("place Porto May 11-May 14", "month May 2024")
    }

    @Test
    fun `a leg a timed place started is that place's header, and edits it`() {
        val stay = timedPlace("stay", porto, porto, "2024-05-11T00:00:00+01:00", "2024-05-14T00:00:00+01:00")
        val header = mapper.map(trip { leg(porto, "2024-05-11", "2024-05-14", startedBy = stay) { } })
            .filterIsInstance<PlaceItemState>().single()

        assertThat(header.id).isEqualTo("stay")
        assertThat(header.entityRef).isEqualTo(EntityRef(EntityType.PLACE, "stay"))
        assertThat(header.timestamp).isEqualTo(zonedDateTime("2024-05-11T00:00:00+01:00"))
    }

    @Test
    fun `a leg of a kind the app doesn't know is shown like a place`() {
        val items = mapper.map(trip { leg(porto, "2024-05-11", "2024-05-12", type = LegType.UNKNOWN) { } })

        assertThat(items.filterIsInstance<PlaceItemState>()).hasSize(1)
    }

    // ---- Months ----

    @Test
    fun `there is a month header the first time each month appears`() {
        val july = place("Lisbon")
        val trip = trip {
            leg(porto, "2024-05-30", "2024-06-02") {
                day("2024-05-30") { restaurant(restaurant("r1", place("Cervejaria"), porto, "2024-05-30T20:00:00+01:00")) }
                emptyRange("2024-05-31", "2024-06-01")
                day("2024-06-02") { restaurant(restaurant("r2", place("Taberna"), porto, "2024-06-02T20:00:00+01:00")) }
            }
            leg(july, "2024-07-01", "2024-07-01") {
                day("2024-07-01") { restaurant(restaurant("r3", place("Time Out"), july, "2024-07-01T13:00:00+01:00")) }
            }
        }

        assertThat(mapper.map(trip).filterIsInstance<MonthItemState>().map { it.month to it.year })
            .containsExactly("May" to "2024", "June" to "2024", "July" to "2024")
    }

    @Test
    fun `a month header sits after the place header and before the first row`() {
        val trip = trip {
            leg(porto, "2024-05-11", "2024-05-11") { day("2024-05-11") { checkIn(portoHotel) } }
        }

        assertThat(mapper.map(trip).labels()).containsExactly(
            "place Porto May 11-May 11",
            "month May 2024",
            "check-in Hotel Porto +date SINGLE",
        )
    }

    @Test
    fun `a month that is already shown isn't shown again by the next leg`() {
        assertThat(mapper.map(roundTrip()).filterIsInstance<MonthItemState>()).hasSize(1)
    }

    // ---- Dates and borders ----

    @Test
    fun `only the first row of a day shows its date`() {
        val dinner = restaurant("dinner", place("Cervejaria"), porto, "2024-05-12T20:00:00+01:00")
        val lunch = restaurant("lunch", place("Taberna"), porto, "2024-05-12T13:00:00+01:00")
        val visit = timedPlace("visit", place("Ribeira"), porto, "2024-05-12T10:00:00+01:00")
        val trip = trip {
            leg(porto, "2024-05-12", "2024-05-12") {
                day("2024-05-12") {
                    timedPlace(visit)
                    restaurant(lunch)
                    restaurant(dinner)
                }
            }
        }

        val rows = mapper.map(trip).filterIsInstance<EventItemState>()

        assertThat(rows.map { it.showDate }).containsExactly(true, false, false)
        assertThat(rows.map { it.backgroundStyle }).containsExactly(
            EventItemState.BackgroundStyle.TOP,
            EventItemState.BackgroundStyle.MIDDLE,
            EventItemState.BackgroundStyle.BOTTOM,
        )
    }

    @Test
    fun `a lone row is single`() {
        val rows = mapper.map(roundTrip()).filterIsInstance<EventItemState>()

        assertThat(rows.first().backgroundStyle).isEqualTo(EventItemState.BackgroundStyle.SINGLE)
    }

    @Test
    fun `a day is not continued across a place change on the same date`() {
        // Checking out of one city and flying to the next: each is a section of its own.
        val lisbon = place("Lisbon")
        val flight = flight("hop", porto, "2024-05-14T17:00:00+01:00", lisbon, "2024-05-14T18:00:00+01:00")
        val trip = trip {
            leg(porto, "2024-05-14", "2024-05-14") {
                day("2024-05-14") {
                    checkOut(portoHotel)
                    departure(flight)
                }
            }
            leg(lisbon, "2024-05-14", "2024-05-14") { day("2024-05-14") { arrival(flight) } }
        }

        val rows = mapper.map(trip).filterIsInstance<EventItemState>()

        assertThat(rows.map { it.showDate }).containsExactly(true, false, true)
        assertThat(rows.last().backgroundStyle).isEqualTo(EventItemState.BackgroundStyle.SINGLE)
    }

    @Test
    fun `the flight home doesn't show a date of its own`() {
        val arrival = mapper.map(roundTrip()).filterIsInstance<FlightArrivalItemState>().last()

        assertThat(arrival.airport).isEqualTo("New York Airport")
        assertThat(arrival.showDate).isFalse()
    }

    @Test
    fun `a row says which section it is in by the leg's place`() {
        val rows = mapper.map(roundTrip()).filterIsInstance<EventItemState>()

        assertThat(rows.map { it.sectionId }).containsExactly(
            "New York",
            "Porto",
            "Porto",
            "Porto",
            "Porto",
            "New York",
        )
    }

    // ---- Rows ----

    @Test
    fun `rows have the details of their entity, a stable id and a reference to edit it by`() {
        val items = mapper.map(roundTrip())

        val departure = items.filterIsInstance<FlightDepartureItemState>().first()
        assertThat(departure.id).isEqualTo("out:0:departure")
        assertThat(departure.destination).isEqualTo("Porto")
        assertThat(departure.airport).isEqualTo("New York Airport")
        assertThat(departure.dayOfMonth).isEqualTo("10")
        assertThat(departure.entityRef).isEqualTo(EntityRef(EntityType.FLIGHT, "out", segmentIndex = 0))

        val checkIn = items.filterIsInstance<HotelCheckInItemState>().single()
        assertThat(checkIn.id).isEqualTo("hotel:checkIn")
        assertThat(checkIn.hotelName).isEqualTo("Hotel Porto")
        assertThat(checkIn.entityRef).isEqualTo(EntityRef(EntityType.LODGING, "hotel"))

        val checkOut = items.filterIsInstance<HotelCheckOutItemState>().single()
        assertThat(checkOut.id).isEqualTo("hotel:checkOut")
        assertThat(checkOut.entityRef).isEqualTo(EntityRef(EntityType.LODGING, "hotel"))
    }

    @Test
    fun `a timed place and a restaurant are rows with their own ids`() {
        val visit = timedPlace("visit", place("Ribeira"), porto, "2024-05-12T10:00:00+01:00", hasStartTime = false)
        val dinner = restaurant("dinner", place("Cervejaria"), porto, "2024-05-12T20:00:00+01:00")
        val items = mapper.map(
            trip {
                leg(porto, "2024-05-12", "2024-05-12") {
                    day("2024-05-12") {
                        timedPlace(visit)
                        restaurant(dinner)
                    }
                }
            },
        )

        val place = items.filterIsInstance<TimedPlaceItemState>().single()
        assertThat(place.id).isEqualTo("visit")
        assertThat(place.placeName).isEqualTo("Ribeira")
        assertThat(place.cityName).isEqualTo("Porto")
        assertThat(place.showTime).isFalse()
        assertThat(place.entityRef).isEqualTo(EntityRef(EntityType.PLACE, "visit"))
        val reservation = items.filterIsInstance<RestaurantReservationItemState>().single()
        assertThat(reservation.id).isEqualTo("dinner")
        assertThat(reservation.restaurantName).isEqualTo("Cervejaria")
        assertThat(reservation.entityRef).isEqualTo(EntityRef(EntityType.RESTAURANT, "dinner"))
    }

    @Test
    fun `a flight with several segments has a row for the segment the event names`() {
        val connection = flight("multi", porto, "2024-05-12T08:00:00+01:00", nice, "2024-05-12T10:00:00+02:00")
        val trip = trip {
            leg(porto, "2024-05-12", "2024-05-12") { day("2024-05-12") { departure(connection, segment = 0) } }
        }

        val departure = mapper.map(trip).filterIsInstance<FlightDepartureItemState>().single()
        assertThat(departure.destination).isEqualTo("Nice")
    }

    @Test
    fun `a row whose entity is no longer on the trip is left out`() {
        val trip = roundTrip().let { it.copy(lodgings = emptyList()) }

        val labels = mapper.map(trip).labels()

        assertThat(labels).noneMatch { it.startsWith("check-") }
    }

    @Test
    fun `a flexible section uses the use case's state, or makes one`() {
        val section = flexibleSection("day-out", porto, "2024-05-12T00:00:00+01:00")
        val other = flexibleSection("other", porto, "2024-05-13T00:00:00+01:00")
        val trip = trip {
            leg(porto, "2024-05-12", "2024-05-13") {
                day("2024-05-12") { flexibleSection(section) }
                day("2024-05-13") { flexibleSection(other) }
            }
        }
        val known = FlexibleDaySectionState(
            id = "day-out",
            timestamp = section.date,
            dayOfMonth = "12",
            dayOfWeek = "Sun",
            showDate = false,
            name = "My day out",
            subtitle = "Ribeira",
            categories = emptyList(),
            searchResults = emptyList(),
        )

        val rows = mapper.map(trip, flexibleSectionItems = listOf(known)).filterIsInstance<FlexibleDaySectionState>()

        assertThat(rows.map { it.name }).containsExactly("My day out", "Day in Porto")
        assertThat(rows.map { it.showDate }).containsExactly(true, true)
        assertThat(rows.map { it.isGenerated }).containsExactly(false, true)
        assertThat(rows.first().entityRef).isEqualTo(EntityRef(EntityType.FLEXIBLE_SECTION, "day-out"))
    }

    // ---- Empty days ----

    @Test
    fun `several empty days are a range and one is a single empty day`() {
        val trip = trip {
            leg(porto, "2024-05-11", "2024-05-20") {
                day("2024-05-11") { checkIn(portoHotel) }
                emptyRange("2024-05-12", "2024-05-18")
                day("2024-05-19") { restaurant(restaurant("r", place("Cervejaria"), porto, "2024-05-19T20:00:00+01:00")) }
                emptyDay("2024-05-20")
            }
        }

        val items = mapper.map(trip)

        val range = items.filterIsInstance<DateRangeItemState>().single()
        assertThat(range.id).isEqualTo("empty_2024-05-12_2024-05-18")
        assertThat(range.dayOfMonthStart).isEqualTo("12")
        assertThat(range.dayOfMonthEnd).isEqualTo("18")
        assertThat(range.sectionId).isEqualTo("Porto")
        val single = items.filterIsInstance<EmptyDateItemState>().single()
        assertThat(single.id).isEqualTo("empty_2024-05-20")
        assertThat(single.dayOfMonth).isEqualTo("20")
    }

    @Test
    fun `an empty row is dated the day it starts, at the time of the row before it`() {
        val items = mapper.map(roundTrip())

        val range = items.filterIsInstance<DateRangeItemState>().single()
        // The last row before it was the 13:00 check-in on the 11th.
        assertThat(range.timestamp).isEqualTo(zonedDateTime("2024-05-12T13:00:00+01:00"))
    }

    // ---- Suggestions ----

    private val emptyStay = trip {
        leg(porto, "2024-05-11", "2024-05-19") {
            day("2024-05-11") { checkIn(portoHotel) }
            emptyRange("2024-05-12", "2024-05-18")
            day("2024-05-19") { checkOut(portoHotel) }
        }
    }

    private fun placeholderOn(date: String) = SuggestionPlaceholder(zonedDateTime("${date}T00:00:00+01:00"), porto)

    private fun suggestions(
        placeHolders: List<SuggestionPlaceholder> = emptyList(),
        days: List<SuggestionsUseCase.SuggestedDay> = emptyList(),
    ) = SuggestionsUseCase.DailyItineraryState(days, emptyList(), placeHolders)

    @Test
    fun `a suggestion in the middle of a range splits it`() {
        val items = mapper.map(emptyStay, suggestions = suggestions(listOf(placeholderOn("2024-05-15"))))

        assertThat(items.labels()).containsSubsequence(
            "range 12-14",
            "placeholder +date SINGLE",
            "range 16-18",
        )
    }

    @Test
    fun `a suggestion at the edge of a range shortens it`() {
        val atStart = mapper.map(emptyStay, suggestions = suggestions(listOf(placeholderOn("2024-05-12"))))
        val atEnd = mapper.map(emptyStay, suggestions = suggestions(listOf(placeholderOn("2024-05-18"))))

        assertThat(atStart.labels()).containsSubsequence("placeholder +date SINGLE", "range 13-18")
        assertThat(atEnd.labels()).containsSubsequence("range 12-17", "placeholder +date SINGLE")
    }

    @Test
    fun `a range cut down to one day becomes an empty day, and to none disappears`() {
        val twoDays = trip {
            leg(porto, "2024-05-11", "2024-05-14") {
                day("2024-05-11") { checkIn(portoHotel) }
                emptyRange("2024-05-12", "2024-05-13")
                day("2024-05-14") { checkOut(portoHotel) }
            }
        }
        val oneDay = trip {
            leg(porto, "2024-05-11", "2024-05-13") {
                day("2024-05-11") { checkIn(portoHotel) }
                emptyDay("2024-05-12")
                day("2024-05-13") { checkOut(portoHotel) }
            }
        }

        val cutDown = mapper.map(twoDays, suggestions = suggestions(listOf(placeholderOn("2024-05-12"))))
        val gone = mapper.map(oneDay, suggestions = suggestions(listOf(placeholderOn("2024-05-12"))))

        assertThat(cutDown.labels()).containsSubsequence("placeholder +date SINGLE", "empty 13")
        assertThat(gone.labels()).noneMatch { it.startsWith("range") || it.startsWith("empty") }
        assertThat(gone.labels()).contains("placeholder +date SINGLE")
    }

    @Test
    fun `several suggestions in one range each take their day`() {
        val items = mapper.map(
            emptyStay,
            suggestions = suggestions(listOf(placeholderOn("2024-05-13"), placeholderOn("2024-05-15"))),
        )

        assertThat(items.labels().filter { it.startsWith("range") || it.startsWith("empty") || it.startsWith("placeholder") })
            .containsExactly(
                "empty 12",
                "placeholder +date SINGLE",
                "empty 14",
                "placeholder +date SINGLE",
                "range 16-18",
            )
    }

    @Test
    fun `the empty rows still start where a plan added there should`() {
        val items = mapper.map(emptyStay, suggestions = suggestions(listOf(placeholderOn("2024-05-15"))))

        val after = items.filterIsInstance<DateRangeItemState>().last()
        assertThat(after.dayOfMonthStart).isEqualTo("16")
        assertThat(after.timestamp.toLocalDate().dayOfMonth).isEqualTo(16)
    }

    @Test
    fun `a suggestion goes among the day's events by time`() {
        val trip = trip {
            leg(porto, "2024-05-11", "2024-05-11") {
                day("2024-05-11") {
                    arrival(outbound)
                    checkIn(portoHotel)
                }
            }
        }
        val visit = SuggestionsUseCase.TimedPlaceSuggestion(
            id = "suggested",
            name = "Ribeira",
            cityId = "Porto",
            coverImage = "",
            reason = "Riverside",
            startTime = zonedDateTime("2024-05-11T11:00:00+01:00"),
            endTime = null,
        )
        val day = SuggestionsUseCase.SuggestedDay(zonedDateTime("2024-05-11T00:00:00+01:00"), listOf(visit), emptyList())

        val items = mapper.map(trip, suggestions = suggestions(days = listOf(day)))

        assertThat(items.labels()).containsSubsequence(
            "arrival Porto Airport +date TOP",
            "place-visit Ribeira MIDDLE",
            "check-in Hotel Porto BOTTOM",
        )
        assertThat(items.filterIsInstance<TimedPlaceItemState>().single().entityRef).isNull()
    }

    @Test
    fun `a suggested section on an empty day takes its place`() {
        val section = flexibleSection("suggested-day", porto, "2024-05-15T00:00:00+01:00")
        val day = SuggestionsUseCase.SuggestedDay(section.date, emptyList(), listOf(section))

        val items = mapper.map(emptyStay, suggestions = suggestions(days = listOf(day)))

        assertThat(items.labels()).containsSubsequence("range 12-14", "section Day in Porto +date SINGLE", "range 16-18")
        assertThat(items.filterIsInstance<FlexibleDaySectionState>().single().entityRef).isNull()
    }

    @Test
    fun `a suggestion for somewhere the trip doesn't go is left out`() {
        val section = flexibleSection("elsewhere", place("Rome"), "2024-05-15T00:00:00+01:00")
        val day = SuggestionsUseCase.SuggestedDay(section.date, emptyList(), listOf(section))

        val items = mapper.map(emptyStay, suggestions = suggestions(days = listOf(day)))

        assertThat(items.labels()).doesNotContain("section Day in Rome +date SINGLE")
        assertThat(items.labels()).contains("range 12-18")
    }

    @Test
    fun `a suggestion for a date outside the trip is left out`() {
        val items = mapper.map(emptyStay, suggestions = suggestions(listOf(placeholderOn("2024-06-30"))))

        assertThat(items.labels()).doesNotContain("placeholder +date SINGLE")
    }

    @Test
    fun `suggestions don't change the itinerary they are laid over`() {
        val itinerary: Itinerary = emptyStay.itinerary!!

        mapper.map(emptyStay, suggestions = suggestions(listOf(placeholderOn("2024-05-15"))))

        assertThat(emptyStay.itinerary).isEqualTo(itinerary)
    }
}
