package com.combah.travel2.model.repository

import com.combah.travel2.extensions.asISO8601DateString
import com.combah.travel2.model.data.LodgingSearchResult
import com.combah.travel2.model.data.Place
import com.combah.travel2.model.data.SimplePlace
import com.combah.travel2.model.data.Time
import com.combah.travel2.model.network.ApiResponse
import com.combah.travel2.model.network.request
import com.combah.travel2.model.network.toAppDataModel
import java.util.Currency
import java.util.Locale

class AddLodgingRepository {

    suspend fun autocomplete(query: String): List<SimplePlace> {
        return request<ApiResponse.PlaceAutoComplete>("/places/autocomplete") {
            url {
                parameters.append("query", query)
                parameters.append("types", "lodging")
            }
        }?.data?.map { it.toAppDataModel() } ?: emptyList()
    }

    suspend fun autocompleteCity(query: String): List<Place> {
        return request<ApiResponse.CityAutoComplete>("/lodging/autocomplete") {
            url {
                parameters.append("query", query)
            }
        }?.data?.map { it.toAppDataModel() } ?: emptyList()
    }

    suspend fun search(
        locationId: String,
        checkIn: Time,
        checkOut: Time
    ): List<LodgingSearchResult> {
        return request<ApiResponse.LodgingSearch>("/lodging/search") {
            url {
                parameters.append("cityId", locationId)
                parameters.append("checkIn", checkIn.asISO8601DateString())
                parameters.append("checkOut", checkOut.asISO8601DateString())
                parameters.append("adults", "1")
                parameters.append("children", "0")
                parameters.append(
                    "currency",
                    Currency.getInstance(Locale.getDefault()).currencyCode
                )
            }
        }?.hotels?.map { it.toAppDataModel() } ?: emptyList()
    }
}
