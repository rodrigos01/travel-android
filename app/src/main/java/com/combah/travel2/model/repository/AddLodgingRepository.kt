package com.combah.travel2.model.repository

import com.combah.travel2.model.data.SimplePlace
import com.combah.travel2.model.network.ApiResponse
import com.combah.travel2.model.network.request
import com.combah.travel2.model.network.toAppDataModel

class AddLodgingRepository {

    enum class ResultType(val serverValue: String) {
        Lodging("lodging"),
        City("locality")
    }

    suspend fun autocomplete(query: String, type: ResultType): List<SimplePlace> {
        return request<ApiResponse.PlaceAutoComplete>("/places/autocomplete") {
            url {
                parameters.append("query", query)
                parameters.append("types", type.serverValue)
            }
        }?.data?.map { it.toAppDataModel() } ?: emptyList()
    }
}
