package travel.vola.android.model.network

import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.http.encodeURLPathPart
import io.ktor.http.isSuccess

class WizardException(message: String) : Exception(message)

/** The wizard's AI endpoints on travel-node, one call per step. */
interface WizardApi {
    suspend fun options(request: WizardApiData.OptionsRequest): WizardApiData.OptionsResponse
    suspend fun followUp(request: WizardApiData.FollowUpRequest): WizardApiData.FollowUpResponse
    suspend fun itineraries(request: WizardApiData.ItinerariesRequest): WizardApiData.ItinerariesResponse
    suspend fun refine(request: WizardApiData.RefineRequest): WizardApiData.RefineResponse

    /** Deletes the conversation this interaction ends, and the ones it continued. */
    suspend fun end(interactionId: String)
}

class KtorWizardApi : WizardApi {

    override suspend fun options(request: WizardApiData.OptionsRequest): WizardApiData.OptionsResponse =
        call("/ai/wizard/options", request)

    override suspend fun followUp(request: WizardApiData.FollowUpRequest): WizardApiData.FollowUpResponse =
        call("/ai/wizard/follow-up", request)

    override suspend fun itineraries(request: WizardApiData.ItinerariesRequest): WizardApiData.ItinerariesResponse =
        call("/ai/wizard/itineraries", request)

    override suspend fun refine(request: WizardApiData.RefineRequest): WizardApiData.RefineResponse =
        call("/ai/wizard/refine", request)

    override suspend fun end(interactionId: String) {
        val path = "/ai/wizard/${interactionId.encodeURLPathPart()}"
        val response = delete(path)
        if (!response.status.isSuccess()) {
            throw WizardException("DELETE $path failed with status ${response.status.value}: ${response.bodyAsText()}")
        }
    }

    private suspend inline fun <reified Request, reified Response> call(path: String, request: Request): Response {
        val response = post(path, request)
        if (!response.status.isSuccess()) {
            throw WizardException("POST $path failed with status ${response.status.value}: ${response.bodyAsText()}")
        }
        return response.body()
    }
}
