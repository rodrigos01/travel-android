package travel.vola.android.model.firebase

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.model.data.ItineraryEvent
import travel.vola.android.model.data.LegType
import travel.vola.android.model.data.Trip
import travel.vola.android.model.firebase.FirebaseData.EntityTypes
import travel.vola.android.model.firebase.FirebaseData.EventTypes
import travel.vola.android.model.firebase.FirebaseData.LegTypes
import travel.vola.android.test.BackendTrips
import java.time.LocalDate

/**
 * The trips are documents as the backend writes them (see [BackendTrips]), read with Firestore's
 * own object mapper - the one tripConverter uses. That checks the storage shape and the classes
 * here agree, which nothing else can.
 */
class ItineraryParsersTest {

    private fun europe() = BackendTrips.trip("europe").itinerary!!

    // ---- The backend's own output ----

    @Test
    fun `a trip without an itinerary has none`() {
        val document = BackendTrips.document("europe").copy(itinerary = null)

        assertThat(document.toAppDataModel().itinerary).isNull()
    }

    @Test
    fun `legs keep their header information`() {
        val legs = europe().legs

        assertThat(legs.map { it.type to it.title }).containsExactly(
            LegType.TRANSIT to "New York",
            LegType.PLACE to "Porto",
            LegType.PLACE to "Nice",
            LegType.PLACE to "Sorrento",
            LegType.TRANSIT to "New York",
        )
        val porto = legs[1]
        assertThat(porto.id).isEqualTo("ext-porto_2024-05-11")
        assertThat(porto.startDate).isEqualTo(LocalDate.of(2024, 5, 11))
        assertThat(porto.endDate).isEqualTo(LocalDate.of(2024, 5, 21))
        assertThat(porto.place!!.id).isEqualTo("ext-porto")
        assertThat(porto.startedBy).isNull()
    }

    @Test
    fun `events are the entities they refer to`() {
        val trip = BackendTrips.trip("europe")
        val porto = trip.itinerary!!.legs[1]

        val arrival = porto.days.first().events.first() as ItineraryEvent.FlightArrival
        assertThat(arrival.id).isEqualTo("jfk-opo:0:arrival")
        assertThat(arrival.flight).isSameAs(trip.flights.first { it.id == "jfk-opo" })
        assertThat(arrival.segment).isSameAs(arrival.flight.segments.first())
        assertThat(arrival.timestamp).isEqualTo(zonedDateTime("2024-05-11T10:00:00+01:00"))

        val checkIn = porto.days.first().events[1] as ItineraryEvent.LodgingCheckIn
        assertThat(checkIn.lodging).isSameAs(trip.lodgings.first { it.id == "porto-hotel" })

        val events = trip.itinerary!!.events.toList()
        assertThat(events.filterIsInstance<ItineraryEvent.TimedPlaceVisit>().map { it.place.id })
            .containsExactly("ribeira", "monaco")
        assertThat(events.filterIsInstance<ItineraryEvent.Restaurant>().map { it.reservation.id })
            .containsExactly("porto-dinner", "nice-lunch")
    }

    @Test
    fun `one empty day and several are both empty days`() {
        val empty = europe().events.filterIsInstance<ItineraryEvent.EmptyDays>().toList()

        assertThat(empty).contains(
            // Sorrento on the 13th, between check-in and check-out.
            ItineraryEvent.EmptyDays("ext-sorrento_2024-06-12:2024-06-13", LocalDate.of(2024, 6, 13), LocalDate.of(2024, 6, 13)),
            ItineraryEvent.EmptyDays("ext-porto_2024-05-11:2024-05-13:2024-05-20", LocalDate.of(2024, 5, 13), LocalDate.of(2024, 5, 20)),
        )
    }

    @Test
    fun `day trips stay in the leg they happened in`() {
        val itinerary = BackendTrips.trip("excursions").itinerary!!

        val paris = itinerary.legs.first { it.title == "Paris" }
        assertThat(paris.days.flatMap { day -> day.events.map { it.id } }).contains("moet", "epernay-lunch")
        assertThat(itinerary.legs.map { it.title }).doesNotContain("Epernay")
    }

    @Test
    fun `a leg a timed place started has it`() {
        val itinerary = BackendTrips.trip("interleavedStay").itinerary!!

        assertThat(itinerary.legs.first().startedBy!!.id).isEqualTo("paris-stay")
    }

    // ---- What the app can't make sense of ----

    /** Europe's trip with an itinerary of one place leg holding [events] on the 11th. */
    private fun tripWith(
        legType: String = LegTypes.PLACE,
        date: String = "2024-05-11",
        vararg events: FirebaseData.ItineraryEvent,
    ): Trip {
        val leg = FirebaseData.ItineraryLeg(
            id = "leg",
            type = legType,
            title = "Porto",
            startDate = "2024-05-11",
            endDate = "2024-05-11",
            days = listOf(FirebaseData.ItineraryDay(date, events.toList())),
        )
        return BackendTrips.document("europe").copy(itinerary = FirebaseData.Itinerary(1, listOf(leg))).toAppDataModel()
    }

    private fun event(
        id: String,
        type: String,
        entityType: String? = null,
        entityId: String = "",
        segmentIndex: Int? = null,
    ) = FirebaseData.ItineraryEvent(
        id = id,
        type = type,
        entityRef = entityType?.let { FirebaseData.EntityRef(it, entityId, segmentIndex) },
        timestamp = "2024-05-11T10:00:00+01:00",
    )

    private fun Trip.eventIds() = itinerary!!.events.map { it.id }.toList()

    private val checkIn = event("in", EventTypes.LODGING_CHECK_IN, EntityTypes.LODGING, "porto-hotel")

    @Test
    fun `a leg of a type this version doesn't know is left out`() {
        val trip = tripWith(legType = "cruise", events = arrayOf(checkIn))

        assertThat(trip.itinerary!!.legs).isEmpty()
    }

    @Test
    fun `an event of a type this version doesn't know is left out and the rest of the day is kept`() {
        val trip = tripWith(events = arrayOf(checkIn, event("new", "somethingNew")))

        assertThat(trip.eventIds()).containsExactly("in")
    }

    @Test
    fun `an event whose entity is no longer on the trip is left out`() {
        val gone = event("gone", EventTypes.LODGING_CHECK_OUT, EntityTypes.LODGING, "no-such-hotel")

        assertThat(tripWith(events = arrayOf(checkIn, gone)).eventIds()).containsExactly("in")
    }

    @Test
    fun `an event whose reference is the wrong kind of entity is left out`() {
        val wrong = event("wrong", EventTypes.LODGING_CHECK_OUT, EntityTypes.RESTAURANT, "porto-hotel")

        assertThat(tripWith(events = arrayOf(checkIn, wrong)).eventIds()).containsExactly("in")
    }

    @Test
    fun `a flight event for a segment the flight doesn't have is left out`() {
        val missing = event("missing", EventTypes.FLIGHT_ARRIVAL, EntityTypes.FLIGHT, "jfk-opo", segmentIndex = 3)

        assertThat(tripWith(events = arrayOf(checkIn, missing)).eventIds()).containsExactly("in")
    }

    @Test
    fun `an itinerary that can't be read doesn't take the trip down with it`() {
        val trip = tripWith(date = "not a date", events = arrayOf(checkIn))

        assertThat(trip.id).isEqualTo("europe")
        assertThat(trip.flights).isNotEmpty()
        assertThat(trip.itinerary).isNull()
    }
}
