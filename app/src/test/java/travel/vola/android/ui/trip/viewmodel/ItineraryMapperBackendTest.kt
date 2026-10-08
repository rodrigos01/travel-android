package travel.vola.android.ui.trip.viewmodel

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import travel.vola.android.model.data.Identifiable
import travel.vola.android.test.BackendTrips
import travel.vola.android.ui.trip.state.TripItemState.FlexibleDaySectionState

/** The whole way from a document the backend wrote to the rows on the screen. */
class ItineraryMapperBackendTest {

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
        )
    }

    private fun rows(name: String) = mapper.map(BackendTrips.trip(name).itinerary)

    @Test
    fun `a trip across three cities`() {
        assertThat(rows("europe").labels()).containsExactly(
            "month May 2024",
            "departure New York Airport +date SINGLE",
            "place Porto May 11-May 21",
            "arrival Porto Airport +date TOP",
            "check-in Hotel Porto BOTTOM",
            "place-visit Ribeira +date TOP",
            "restaurant Cervejaria BOTTOM",
            "range 13-20",
            "check-out Hotel Porto +date TOP",
            "departure Porto Airport BOTTOM",
            "place Nice May 21-Jun 2",
            "arrival Nice Airport +date TOP",
            "check-in Hotel Nice BOTTOM",
            "range 22-24",
            "place-visit Monaco +date SINGLE",
            "range 26-29",
            "restaurant Socca +date SINGLE",
            "range 31-1",
            "month June 2024",
            "check-out Hotel Nice +date SINGLE",
            "range 3-11",
            "place Sorrento Jun 12-Jun 14",
            "check-in Hotel Sorrento +date SINGLE",
            "empty 13",
            "check-out Hotel Sorrento +date TOP",
            "departure Sorrento Airport BOTTOM",
            "arrival New York Airport SINGLE",
        )
    }

    @Test
    fun `a lunch in the next town is part of the day in Paris`() {
        val labels = rows("excursions").labels()

        assertThat(labels.filter { it.startsWith("place ") }).containsExactly(
            "place Paris Jun 3-Jun 9",
            "place Rome Jun 12-Jun 12",
        )
        assertThat(labels).containsSubsequence(
            "place-visit Moet et Chandon +date TOP",
            "restaurant Epernay bistro BOTTOM",
        )
    }

    @Test
    fun `the rest of a stay carries on under the leg in progress`() {
        val labels = rows("interleavedStay").labels()

        assertThat(labels.filter { it.startsWith("place ") }).containsExactly(
            "place Paris Jun 3-Jun 3",
            "place Reims Jun 5-Jun 6",
        )
        // The Paris hotel's check-out comes after the Paris stay has ended, so it is shown where it falls.
        assertThat(labels.last()).startsWith("check-out Hotel Paris")
    }

    @Test
    fun `no two rows share an id`() {
        listOf("europe", "excursions", "interleavedStay").forEach { name ->
            val ids = rows(name).mapNotNull { (it as? Identifiable)?.id }
            assertThat(ids).doesNotHaveDuplicates()
        }
    }
}
