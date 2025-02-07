package com.combah.travel2.model.repository

import com.combah.travel2.model.data.SimplePlace
import com.combah.travel2.model.network.ApiResponse
import com.combah.travel2.model.network.request
import com.combah.travel2.model.network.toAppDataModel

class AddLodgingRepository : AutoCompleteRepository<SimplePlace> {

    override suspend fun autocomplete(query: String): List<SimplePlace> {
        return request<ApiResponse.PlaceAutoComplete>("/places/autocomplete") {
            url {
                parameters.append("query", query)
                parameters.append("types", "lodging")
            }
        }?.data?.map { it.toAppDataModel() } ?: emptyList()
    }
}
