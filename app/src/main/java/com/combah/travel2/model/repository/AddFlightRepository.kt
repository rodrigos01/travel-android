package com.combah.travel2.model.repository

import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.network.ApiResponse
import com.combah.travel2.model.network.request
import com.combah.travel2.model.network.toAppDataModel

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
