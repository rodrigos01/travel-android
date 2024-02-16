package com.combah.travel2.model.repository

import com.combah.travel2.model.data.Lodging

class AddLodgingRepository : AutoCompleteRepository<Lodging> {

    override suspend fun autocomplete(query: String): List<Lodging> {
        return emptyList()
    }
}
