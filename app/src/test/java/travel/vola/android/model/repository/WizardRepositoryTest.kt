package travel.vola.android.model.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import travel.vola.android.model.data.GroupType
import travel.vola.android.model.data.WizardAnswer
import travel.vola.android.model.data.WizardBasicInformation
import travel.vola.android.model.data.WizardCity
import travel.vola.android.model.data.WizardItinerary
import travel.vola.android.model.data.WizardParameters
import travel.vola.android.model.network.WizardApi
import travel.vola.android.model.network.WizardApiData
import java.time.LocalDate

class WizardRepositoryTest {

    private class FakeApi : WizardApi {
        val calls = mutableListOf<Any>()
        val ended = mutableListOf<String>()
        var failEnd: String? = null

        override suspend fun options(request: WizardApiData.OptionsRequest): WizardApiData.OptionsResponse {
            calls += request
            return WizardApiData.OptionsResponse(
                "int-1",
                WizardApiData.Options(listOf("Vacation"), listOf("Food"), listOf("Relaxed"), listOf("Culture"), listOf("Wine"), listOf("1 week")),
            )
        }

        override suspend fun followUp(request: WizardApiData.FollowUpRequest): WizardApiData.FollowUpResponse {
            calls += request
            return WizardApiData.FollowUpResponse(
                "int-2",
                listOf(WizardApiData.Question(listOf("Food"), "Street food?", listOf("Yes", "No"))),
            )
        }

        override suspend fun itineraries(request: WizardApiData.ItinerariesRequest): WizardApiData.ItinerariesResponse {
            calls += request
            return WizardApiData.ItinerariesResponse("int-3", "Logic", listOf(apiItinerary("A")))
        }

        override suspend fun refine(request: WizardApiData.RefineRequest): WizardApiData.RefineResponse {
            calls += request
            return WizardApiData.RefineResponse("int-4", apiItinerary("Refined"))
        }

        override suspend fun end(interactionId: String) {
            if (interactionId == failEnd) error("boom")
            ended += interactionId
        }

        private fun apiItinerary(name: String) = WizardApiData.Itinerary(
            name,
            "d",
            "2025-06-01",
            "2025-06-08",
            listOf(WizardApiData.City("Lisbon", "Lisbon, Portugal", "2025-06-01", "2025-06-08", place = null)),
            listOf("a", "b", "c"),
        )
    }

    private val api = FakeApi()
    private val errors = mutableListOf<String>()

    private fun TestScope.repository() = WizardRepository(
        api,
        scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        logError = { message, _ -> errors += message },
    )

    private val itinerary = WizardItinerary(
        "Portugal",
        "d",
        LocalDate.of(2025, 6, 1),
        LocalDate.of(2025, 6, 8),
        listOf(WizardCity("Lisbon", "Lisbon, Portugal", LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 8), place = null)),
        listOf("a", "b", "c"),
    )

    @Test
    fun `options sends ISO dates and the group type name`() = runTest {
        val step = repository().options(
            WizardBasicInformation(listOf("Portugal"), LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 8), true, GroupType.COUPLE, 2),
        )
        val request = api.calls.single() as WizardApiData.OptionsRequest
        assertThat(request.basicInformation).isEqualTo(
            WizardApiData.BasicInformation("COUPLE", 2, listOf("Portugal"), "2025-06-01", "2025-06-08", true),
        )
        assertThat(step.interactionId).isEqualTo("int-1")
        assertThat(step.value.mustHave).containsExactly("Wine")
    }

    @Test
    fun `follow up continues the given interaction`() = runTest {
        val step = repository().followUp(
            "int-1",
            WizardParameters(listOf("Vacation"), emptyList(), emptyList(), listOf("Culture"), emptyList(), emptyList(), "none"),
        )
        val request = api.calls.single() as WizardApiData.FollowUpRequest
        assertThat(request.interactionId).isEqualTo("int-1")
        assertThat(request.parameters.anythingElse).isEqualTo("none")
        assertThat(step.interactionId).isEqualTo("int-2")
        assertThat(step.value.single().answers).containsExactly("Yes", "No")
    }

    @Test
    fun `itineraries parse their dates`() = runTest {
        val step = repository().itineraries("int-2", listOf(WizardAnswer("Street food?", "Yes")))
        val request = api.calls.single() as WizardApiData.ItinerariesRequest
        assertThat(request.answers).containsExactly(WizardApiData.Answer("Street food?", "Yes"))
        val city = step.value.itineraries.single().cities.single()
        assertThat(city.startDate).isEqualTo(LocalDate.of(2025, 6, 1))
        assertThat(city.place).isNull()
        assertThat(step.value.planningLogic).isEqualTo("Logic")
    }

    @Test
    fun `refine sends the itinerary without places and returns the revised one`() = runTest {
        val step = repository().refine("int-3", "More Porto", itinerary)
        val request = api.calls.single() as WizardApiData.RefineRequest
        assertThat(request.feedback).isEqualTo("More Porto")
        assertThat(request.itinerary.cities.single().searchQuery).isEqualTo("Lisbon, Portugal")
        assertThat(request.itinerary.startDate).isEqualTo("2025-06-01")
        assertThat(request.itinerary.cities.single().place).isNull()
        assertThat(step.interactionId).isEqualTo("int-4")
        assertThat(step.value.name).isEqualTo("Refined")
    }

    @Test
    fun `ending the wizard ends every open interaction and survives failures`() = runTest {
        api.failEnd = "b"
        repository().apply { end(listOf("a", "b", "c")) }
        advanceUntilIdle()
        assertThat(api.ended).containsExactlyInAnyOrder("a", "c")
        assertThat(errors).hasSize(1)
    }
}
