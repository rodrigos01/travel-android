package travel.vola.android.model.repository

import io.ktor.http.appendPathSegments
import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.AirportSearchResult
import travel.vola.android.model.network.ApiData
import travel.vola.android.model.network.ApiResponse
import travel.vola.android.model.network.request
import travel.vola.android.model.network.toAppDataModel

class AddFlightRepository : AutoCompleteRepository<AirportSearchResult> {
    override suspend fun autocomplete(query: String): List<AirportSearchResult> {
        return request<ApiResponse.AirportAutoComplete>("flights/airport/autocomplete") {
            url {
                parameters.append("query", query)
            }
        }?.data?.map { it.toAppDataModel() } ?: emptyList()
    }

    suspend fun airportDetails(iata: String): Airport {
        return request<ApiData.Airport>("flights/airport") {
            url {
                appendPathSegments(iata)
            }
        }?.toAppDataModel() ?: error("Airport not found")
    }
}
