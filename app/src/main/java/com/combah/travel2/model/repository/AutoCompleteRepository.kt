package com.combah.travel2.model.repository

interface AutoCompleteRepository<T> {
    suspend fun autocomplete(query: String): List<T>
}
