package travel.vola.android.model.repository

import com.vola.android.model.data.Airport
import com.vola.android.model.network.ApiResponse
import com.vola.android.model.network.request
import com.vola.android.model.network.toAppDataModel

class AddFlightRepository : AutoCompleteRepository<Airport> {
    override suspend fun autocomplete(query: String): List<Airport> {
        return request<ApiResponse.AirportAutoComplete>("places/autocomplete") {
            url {
                parameters.append("query", query)
                parameters.append("types", "airport")
            }
        }?.data?.map { it.toAppDataModel() } ?: emptyList()
    }
}
