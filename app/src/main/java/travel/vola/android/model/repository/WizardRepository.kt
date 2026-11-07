package travel.vola.android.model.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import travel.vola.android.model.data.WizardAnswer
import travel.vola.android.model.data.WizardBasicInformation
import travel.vola.android.model.data.WizardCity
import travel.vola.android.model.data.WizardItineraries
import travel.vola.android.model.data.WizardItinerary
import travel.vola.android.model.data.WizardOptions
import travel.vola.android.model.data.WizardParameters
import travel.vola.android.model.data.WizardQuestion
import travel.vola.android.model.data.WizardStep
import travel.vola.android.model.network.KtorWizardApi
import travel.vola.android.model.network.WizardApi
import travel.vola.android.model.network.WizardApiData
import travel.vola.android.model.network.toAppDataModel
import java.time.LocalDate

/**
 * The trip-creation wizard's AI steps. The backend keeps the conversation: every step returns an
 * interaction id, and the next step sends it back instead of anything the model said before.
 * Calls throw (WizardException for the backend's refusals) when a step can't be produced.
 */
class WizardRepository(
    private val api: WizardApi = KtorWizardApi(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val logError: (String, Throwable) -> Unit = { message, error -> Log.w(TAG, message, error) },
) {

    suspend fun options(info: WizardBasicInformation): WizardStep<WizardOptions> {
        val response = api.options(
            WizardApiData.OptionsRequest(
                WizardApiData.BasicInformation(
                    groupType = info.groupType.name,
                    travelers = info.travelers,
                    destinations = info.destinations,
                    startDate = info.startDate.toString(),
                    endDate = info.endDate.toString(),
                    fixedDates = info.fixedDates,
                ),
            ),
        )
        return WizardStep(
            response.interactionId,
            with(response.options) { WizardOptions(occasions, interests, vibe, focus, mustHave, duration) },
        )
    }

    suspend fun followUp(
        interactionId: String,
        parameters: WizardParameters,
    ): WizardStep<List<WizardQuestion>> {
        val response = api.followUp(
            WizardApiData.FollowUpRequest(
                interactionId,
                with(parameters) {
                    WizardApiData.Parameters(occasions, interests, vibe, focus, mustHave, duration, anythingElse)
                },
            ),
        )
        return WizardStep(
            response.interactionId,
            response.questions.map { WizardQuestion(it.parameterSelections, it.question, it.answers) },
        )
    }

    suspend fun itineraries(
        interactionId: String,
        answers: List<WizardAnswer>,
    ): WizardStep<WizardItineraries> {
        val response = api.itineraries(
            WizardApiData.ItinerariesRequest(
                interactionId,
                answers.map { WizardApiData.Answer(it.question, it.answer) },
            ),
        )
        return WizardStep(
            response.interactionId,
            WizardItineraries(response.planningLogic, response.itineraries.map { it.toAppData() }),
        )
    }

    suspend fun refine(
        interactionId: String,
        feedback: String,
        itinerary: WizardItinerary,
    ): WizardStep<WizardItinerary> {
        val response = api.refine(
            WizardApiData.RefineRequest(interactionId, feedback, itinerary.toApiData()),
        )
        return WizardStep(response.interactionId, response.itinerary.toAppData())
    }

    /**
     * Lets the backend delete these conversations (and what they continued) once the wizard is
     * over. Nobody waits on it and a failure only means they expire on their own, so it doesn't
     * depend on the screen that asked.
     */
    fun end(interactionIds: Collection<String>) {
        interactionIds.forEach { id ->
            scope.launch {
                try {
                    api.end(id)
                } catch (e: Exception) {
                    logError("Could not end wizard interaction $id", e)
                }
            }
        }
    }

    private fun WizardApiData.Itinerary.toAppData() = WizardItinerary(
        name = name,
        description = description,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
        cities = cities.map {
            WizardCity(
                name = it.name,
                searchQuery = it.searchQuery,
                startDate = LocalDate.parse(it.startDate),
                endDate = LocalDate.parse(it.endDate),
                place = it.place?.toAppDataModel(),
            )
        },
        predictedChanges = predictedChanges,
    )

    private fun WizardItinerary.toApiData() = WizardApiData.Itinerary(
        name = name,
        description = description,
        startDate = startDate.toString(),
        endDate = endDate.toString(),
        cities = cities.map {
            WizardApiData.City(
                name = it.name,
                searchQuery = it.searchQuery,
                startDate = it.startDate.toString(),
                endDate = it.endDate.toString(),
            )
        },
        predictedChanges = predictedChanges,
    )

    private companion object {
        const val TAG = "WizardRepository"
    }
}
