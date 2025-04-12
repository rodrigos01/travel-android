package travel.vola.android.model.repository

import io.ktor.http.appendPathSegments
import travel.vola.android.extensions.asISO8601DateString
import travel.vola.android.model.data.LodgingSearchResult
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.SimplePlace
import travel.vola.android.model.data.Time
import travel.vola.android.model.network.ApiData
import travel.vola.android.model.network.ApiResponse
import travel.vola.android.model.network.request
import travel.vola.android.model.network.toAppDataModel
import java.util.Currency
import java.util.Locale

class LodgingSearchRepository {

    suspend fun autocomplete(query: String, autocompleteKey: String): List<SimplePlace> {
        return request<ApiResponse.PlaceAutoComplete>("/places/autocomplete") {
            url {
                parameters.append("query", query)
                parameters.append("types", "lodging")
                parameters.append("sessionId", autocompleteKey)
            }
        }?.data?.map { it.toAppDataModel() } ?: emptyList()
    }

    suspend fun placeCity(placeId: String, autocompleteKey: String): Place? {
        return request<ApiData.Place>("/places/cities") {
            url {
                parameters.append("placeId", placeId)
                parameters.append("autocompleteSessionId", autocompleteKey)
            }
        }?.toAppDataModel()
    }

    suspend fun autocompleteCity(query: String): List<Place> {
        return request<ApiResponse.CityAutoComplete>("/lodging/autocomplete") {
            url {
                parameters.append("query", query)
            }
        }?.data?.map { it.toAppDataModel() } ?: emptyList()
    }

    suspend fun search(
        locationId: String, checkIn: Time, checkOut: Time
    ): List<LodgingSearchResult> {
        return request<ApiResponse.LodgingSearch>("/lodging/searchV2") {
            url {
                parameters.append("cityId", locationId)
                parameters.append("checkin", checkIn.asISO8601DateString())
                parameters.append("checkout", checkOut.asISO8601DateString())
                parameters.append("adults", "2")
                parameters.append("children", "0")
                parameters.append(
                    "currency", Currency.getInstance(Locale.getDefault()).currencyCode
                )
            }
        }?.hotels?.map { it.toAppDataModel() } ?: emptyList()
    }

    suspend fun details(
        lodgingId: String,
        checkIn: Time,
        checkOut: Time,
        latitude: Double? = null,
        longitude: Double? = null,
    ): ApiData.LodgingDetails? {
        return request("/lodging/") {
            url {
                appendPathSegments(lodgingId)
                parameters.append("checkin", checkIn.asISO8601DateString())
                parameters.append("checkout", checkOut.asISO8601DateString())
                parameters.append("adults", "1")
                parameters.append("children", "0")
                parameters.append("lat", latitude.toString())
                parameters.append("lon", longitude.toString())
                parameters.append(
                    "currency", Currency.getInstance(Locale.getDefault()).currencyCode
                )
            }
        }
    }
}
