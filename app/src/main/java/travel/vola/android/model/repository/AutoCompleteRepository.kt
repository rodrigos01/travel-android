package travel.vola.android.model.repository

interface AutoCompleteRepository<T, E> {
    suspend fun autocomplete(query: String, autocompleteKey: String = ""): List<T>

    suspend fun details(id: String, autocompleteKey: String = ""): E?
}
