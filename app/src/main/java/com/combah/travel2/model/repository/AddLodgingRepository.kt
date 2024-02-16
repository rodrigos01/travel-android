package com.combah.travel2.model.repository

import com.combah.travel2.model.data.Lodging

class AddLodgingRepository {

    suspend fun autocomplete(query: String): List<Lodging> {
        return emptyList()
    }
}