package travel.vola.android.model.repository

import io.ktor.http.appendPathSegments
import travel.vola.android.model.data.PlaceDetailsResult
import travel.vola.android.model.data.SimplePlace
import travel.vola.android.model.network.ApiResponse
import travel.vola.android.model.network.request
import travel.vola.android.model.network.toAppDataModel

class PlaceAutoCompleteRepository(private val types: List<String>) :
    AutoCompleteRepository<SimplePlace, PlaceDetailsResult> {
    override suspend fun autocomplete(query: String, autocompleteKey: String): List<SimplePlace> {
        return request<ApiResponse.PlaceAutoComplete>("/places/autocomplete") {
            url {
                parameters.append("query", query)
                parameters.append("types", types.joinToString(","))
                parameters.append("sessionId", autocompleteKey)
            }
        }?.results?.map { it.toAppDataModel() } ?: emptyList()
    }

    override suspend fun details(id: String, autocompleteKey: String): PlaceDetailsResult? {
        return request<ApiResponse.PlaceDetails>("/places/") {
            url {
                appendPathSegments(id)
                parameters.append("autocompleteSessionId", autocompleteKey)
                parameters.append("resolveCity", "true")
            }
        }?.toAppDataModel()
    }
}