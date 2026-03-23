package travel.vola.android.model.repository

import io.ktor.http.appendPathSegments
import travel.vola.android.model.data.PlaceDetailsResult
import travel.vola.android.model.data.SimplePlace
import travel.vola.android.model.network.ApiResponse
import travel.vola.android.model.network.request
import travel.vola.android.model.network.toAppDataModel

class PlaceAutoCompleteRepository(
    private val types: List<String>,
    private val resolveCity: Boolean = true,
) :
    AutoCompleteRepository<SimplePlace, PlaceDetailsResult> {
    override suspend fun autocomplete(
        query: String,
        autocompleteKey: String,
        locationBias: Pair<Double, Double>?,
    ): List<SimplePlace> {
        return request<ApiResponse.PlaceAutoComplete>("/places/autocomplete") {
            url {
                parameters.append("query", query)
                parameters.append("types", types.joinToString(","))
                parameters.append("sessionId", autocompleteKey)
                if (locationBias != null) {
                    val (latitude, longitude) = locationBias
                    parameters.append("latitude", latitude.toString())
                    parameters.append("longitude", longitude.toString())
                }
            }
        }?.results?.map { it.toAppDataModel() } ?: emptyList()
    }

    override suspend fun details(id: String, autocompleteKey: String): PlaceDetailsResult? {
        return request<ApiResponse.PlaceDetails>("/places/") {
            url {
                appendPathSegments(id)
                parameters.append("autocompleteSessionId", autocompleteKey)
                parameters.append("resolveCity", if (resolveCity) "true" else "false")
            }
        }?.toAppDataModel()
    }
}
