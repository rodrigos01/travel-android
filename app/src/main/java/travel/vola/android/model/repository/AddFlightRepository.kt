package travel.vola.android.model.repository

import io.ktor.http.appendPathSegments
import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.AirportSearchResult
import travel.vola.android.model.network.ApiData
import travel.vola.android.model.network.ApiResponse
import travel.vola.android.model.network.request
import travel.vola.android.model.network.toAppDataModel

class AddFlightRepository : AutoCompleteRepository<AirportSearchResult, Airport> {
    override suspend fun autocomplete(
        query: String,
        autocompleteKey: String,
        locationBias: Pair<Double, Double>?
    ): List<AirportSearchResult> {
        return request<ApiResponse.AirportAutoComplete>("flights/airport/autocomplete") {
            url {
                parameters.append("query", query)
            }
        }?.data?.map { it.toAppDataModel() } ?: emptyList()
    }

    override suspend fun details(id: String, autocompleteKey: String): Airport {
        return request<ApiData.Airport>("flights/airport") {
            url {
                appendPathSegments(id)
            }
        }?.toAppDataModel() ?: error("Airport not found")
    }
}
