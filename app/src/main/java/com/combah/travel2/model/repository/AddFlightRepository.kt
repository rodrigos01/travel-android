package com.combah.travel2.model.repository

import com.combah.travel2.model.data.Airport
import com.combah.travel2.model.network.ApiResponse
import com.combah.travel2.model.network.httpClient
import com.combah.travel2.model.network.toAppDataModel
import io.ktor.client.call.body
import io.ktor.client.request.get

class AddFlightRepository {
    suspend fun autocomplete(query: String): List<Airport> {
        val response: ApiResponse.AirportAutoComplete =
            httpClient().get("http://10.0.2.2:5000/places/autocomplete") {
                url {
                    parameters.append("query", query)
                    parameters.append("types", "airport")
                }
            }.body()
        return response.data.map { it.toAppDataModel() }
    }
}