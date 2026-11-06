package travel.vola.android.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import travel.vola.android.ui.trip.viewmodel.flight
import travel.vola.android.ui.trip.viewmodel.place
import travel.vola.android.ui.trip.viewmodel.trip
import java.time.LocalDate

class TripTest {

    private val newYork = place("New York")
    private val porto = place("Porto")
    private val lisbon = place("Lisbon")

    @Test
    fun `destinations are the places the trip stays in, in order`() {
        val outbound = flight("out", newYork, "2024-05-10T22:05:00-04:00", porto, "2024-05-11T10:00:00+01:00")
        val trip = trip {
            transit(newYork, "2024-05-10") { day("2024-05-10") { departure(outbound) } }
            leg(porto, "2024-05-11", "2024-05-14") { day("2024-05-11") { arrival(outbound) } }
            leg(lisbon, "2024-05-14", "2024-05-20") { }
        }

        val destinations = trip.getDestinations()

        assertThat(destinations.map { it.place.name }).containsExactly("Porto", "Lisbon")
        assertThat(destinations.map { it.startDateTime.toLocalDate() })
            .containsExactly(LocalDate.of(2024, 5, 11), LocalDate.of(2024, 5, 14))
        assertThat(destinations.map { it.endDateTime!!.toLocalDate() })
            .containsExactly(LocalDate.of(2024, 5, 14), LocalDate.of(2024, 5, 20))
    }

    @Test
    fun `a trip without an itinerary has no destinations`() {
        assertThat(trip { }.copy(itinerary = null).getDestinations()).isEmpty()
    }
}
