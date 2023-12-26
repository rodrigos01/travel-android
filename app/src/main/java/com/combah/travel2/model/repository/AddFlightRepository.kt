package com.combah.travel2.model.repository

import com.combah.travel2.model.data.Airport

class AddFlightRepository {

    suspend fun autocomplete(query: String): List<Airport> = emptyList()
}