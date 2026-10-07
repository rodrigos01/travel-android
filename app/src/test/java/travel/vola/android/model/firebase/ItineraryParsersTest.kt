package travel.vola.android.model.firebase

import com.google.firebase.firestore.util.CustomClassMapper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.longOrNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.model.data.EntityEventType
import travel.vola.android.model.data.EntityRef
import travel.vola.android.model.data.EntityType
import travel.vola.android.model.data.ItineraryEvent
import travel.vola.android.model.data.LegType
import java.time.LocalDate

class ItineraryParsersTest {

    private val place = FirebaseData.Place(
        id = "id-porto",
        name = "Porto",
        externalId = "ext-porto",
        timeZone = "Europe/Lisbon",
    )

    private fun event(
        id: String,
        type: String,
        ref: FirebaseData.EntityRef? = FirebaseData.EntityRef("lodging", "hotel"),
        timestamp: String? = "2024-05-11T14:00:00+01:00",
        endDate: String? = null,
    ) = FirebaseData.ItineraryEvent(id, type, ref, timestamp, endDate)

    private fun itinerary(vararg events: FirebaseData.ItineraryEvent, legType: String = "place") =
        FirebaseData.Itinerary(
            version = 1,
            legs = listOf(
                FirebaseData.ItineraryLeg(
                    id = "ext-porto_2024-05-11",
                    type = legType,
                    title = "Porto",
                    startDate = "2024-05-11",
                    endDate = "2024-05-21",
                    thumbnailUrl = "https://example.com/porto.jpg",
                    place = place,
                    days = listOf(FirebaseData.ItineraryDay("2024-05-11", events.toList())),
                ),
            ),
        )

    private fun trip(itinerary: FirebaseData.Itinerary?) = FirebaseData.Trip(id = "t", itinerary = itinerary)

    @Test
    fun `a trip without an itinerary has none`() {
        assertThat(trip(null).toAppDataModel().itinerary).isNull()
    }

    @Test
    fun `a leg keeps its header information and the place keyed by its external id`() {
        val leg = trip(itinerary(event("e", "lodgingCheckIn"))).toAppDataModel().itinerary!!.legs.single()

        assertThat(leg.id).isEqualTo("ext-porto_2024-05-11")
        assertThat(leg.type).isEqualTo(LegType.PLACE)
        assertThat(leg.title).isEqualTo("Porto")
        assertThat(leg.startDate).isEqualTo(LocalDate.of(2024, 5, 11))
        assertThat(leg.endDate).isEqualTo(LocalDate.of(2024, 5, 21))
        assertThat(leg.thumbnailUrl).isEqualTo("https://example.com/porto.jpg")
        assertThat(leg.place!!.id).isEqualTo("ext-porto")
        assertThat(leg.entityRef).isNull()
    }

    @Test
    fun `leg types are mapped and an unknown one is kept as unknown`() {
        fun typeOf(raw: String) =
            trip(itinerary(legType = raw)).toAppDataModel().itinerary!!.legs.single().type

        assertThat(typeOf("place")).isEqualTo(LegType.PLACE)
        assertThat(typeOf("transit")).isEqualTo(LegType.TRANSIT)
        assertThat(typeOf("cruise")).isEqualTo(LegType.UNKNOWN)
    }

    @Test
    fun `every entity event type is mapped with its reference and timestamp`() {
        val expected = mapOf(
            "flightDeparture" to EntityEventType.FLIGHT_DEPARTURE,
            "flightArrival" to EntityEventType.FLIGHT_ARRIVAL,
            "lodgingCheckIn" to EntityEventType.LODGING_CHECK_IN,
            "lodgingCheckOut" to EntityEventType.LODGING_CHECK_OUT,
            "timedPlace" to EntityEventType.TIMED_PLACE,
            "restaurant" to EntityEventType.RESTAURANT,
            "flexibleSection" to EntityEventType.FLEXIBLE_SECTION,
        )
        val events = expected.keys.map { event("id-$it", it) }

        val parsed = trip(itinerary(*events.toTypedArray())).toAppDataModel()
            .itinerary!!.legs.single().days.single().events

        assertThat(parsed).hasSize(expected.size)
        parsed.forEach { event ->
            event as ItineraryEvent.OfEntity
            assertThat(event.type).isEqualTo(expected.getValue(event.id.removePrefix("id-")))
            assertThat(event.timestamp).isEqualTo(zonedDateTime("2024-05-11T14:00:00+01:00"))
            assertThat(event.entityRef).isEqualTo(EntityRef(EntityType.LODGING, "hotel"))
        }
    }

    @Test
    fun `a flight event keeps its segment index`() {
        val flight = event("f:1:arrival", "flightArrival", FirebaseData.EntityRef("flight", "f", 1))
        val parsed = trip(itinerary(flight)).toAppDataModel().itinerary!!.legs.single().days.single().events.single()

        assertThat((parsed as ItineraryEvent.OfEntity).entityRef)
            .isEqualTo(EntityRef(EntityType.FLIGHT, "f", segmentIndex = 1))
    }

    @Test
    fun `empty days and ranges take their dates from the day they are on and their end date`() {
        val day = FirebaseData.ItineraryDay(
            date = "2024-05-12",
            events = listOf(
                event("one", "emptyDay", ref = null, timestamp = null),
                event("range", "emptyDateRange", ref = null, timestamp = null, endDate = "2024-05-15"),
            ),
        )
        val source = itinerary().let { it.copy(legs = listOf(it.legs.single().copy(days = listOf(day)))) }

        val events = trip(source).toAppDataModel().itinerary!!.legs.single().days.single().events

        assertThat(events).containsExactly(
            ItineraryEvent.EmptyDay("one", LocalDate.of(2024, 5, 12)),
            ItineraryEvent.EmptyDateRange("range", LocalDate.of(2024, 5, 12), LocalDate.of(2024, 5, 15)),
        )
    }

    @Test
    fun `an event type this version doesn't know is dropped and the rest of the day is kept`() {
        val parsed = trip(itinerary(event("keep", "restaurant"), event("new", "somethingNew")))
            .toAppDataModel().itinerary!!.legs.single().days.single().events

        assertThat(parsed.map { it.id }).containsExactly("keep")
    }

    @Test
    fun `an itinerary that can't be read doesn't take the trip down with it`() {
        val broken = itinerary(event("e", "restaurant", timestamp = "not a time"))
        val result = trip(broken).toAppDataModel()

        assertThat(result.id).isEqualTo("t")
        assertThat(result.itinerary).isNull()
    }

    @Test
    fun `an entity event without a reference makes the itinerary unreadable`() {
        assertThat(trip(itinerary(event("e", "restaurant", ref = null))).toAppDataModel().itinerary).isNull()
    }

    // The documents below are what the backend's buildItinerary writes for the
    // trips in travel-node's api/trips/itinerary/corpus.js (regenerate them
    // there if the builder's output changes). Reading them with Firestore's own
    // object mapper - the one tripConverter uses - checks the storage shape and
    // these classes agree, which nothing else here can.

    @Test
    fun `the backend's output for a multi-leg trip reads through the Firestore mapper`() {
        val itinerary = readFixture("europe").toAppDataModel()

        assertThat(itinerary.version).isEqualTo(1)
        assertThat(itinerary.legs.map { it.type to it.title }).containsExactly(
            LegType.TRANSIT to "New York",
            LegType.PLACE to "Porto",
            LegType.PLACE to "Nice",
            LegType.PLACE to "Sorrento",
            LegType.TRANSIT to "New York",
        )
        val porto = itinerary.legs[1]
        assertThat(porto.startDate).isEqualTo(LocalDate.of(2024, 5, 11))
        assertThat(porto.endDate).isEqualTo(LocalDate.of(2024, 5, 21))
        val arrival = porto.days.first().events.first() as ItineraryEvent.OfEntity
        assertThat(arrival.type).isEqualTo(EntityEventType.FLIGHT_ARRIVAL)
        assertThat(arrival.entityRef).isEqualTo(EntityRef(EntityType.FLIGHT, "jfk-opo", segmentIndex = 0))
        assertThat(itinerary.legs.flatMap { leg -> leg.days.flatMap { it.events } })
            .anyMatch { it is ItineraryEvent.EmptyDateRange }
    }

    @Test
    fun `day trips stay in the leg they happened in`() {
        val itinerary = readFixture("excursions").toAppDataModel()

        val paris = itinerary.legs.first { it.title == "Paris" }
        val ids = paris.days.flatMap { day -> day.events.map { it.id } }
        assertThat(ids).contains("moet", "epernay-lunch")
        assertThat(itinerary.legs.map { it.title }).doesNotContain("Epernay")
    }

    @Test
    fun `a leg started by a timed place references it`() {
        val itinerary = readFixture("interleavedStay").toAppDataModel()

        assertThat(itinerary.legs.first().entityRef).isEqualTo(EntityRef(EntityType.PLACE, "paris-stay"))
    }

    private fun readFixture(name: String): FirebaseData.Itinerary {
        val text = checkNotNull(javaClass.getResource("/itinerary/$name.json")) { "missing fixture $name" }.readText()

        @Suppress("UNCHECKED_CAST")
        val map = Json.parseToJsonElement(text).toPlain() as Map<String, Any?>
        return CustomClassMapper.convertToCustomClass(map, FirebaseData.Itinerary::class.java, null)
    }

    // What Firestore hands the mapper: Long for whole numbers, Double otherwise.
    private fun JsonElement.toPlain(): Any? = when (this) {
        is JsonNull -> null
        is JsonObject -> mapValues { it.value.toPlain() }
        is JsonArray -> map { it.toPlain() }
        is JsonPrimitive -> when {
            isString -> content
            else -> booleanOrNull ?: longOrNull ?: content.toDouble()
        }
    }
}
