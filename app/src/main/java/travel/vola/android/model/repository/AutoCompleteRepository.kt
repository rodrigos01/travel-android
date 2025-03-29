package travel.vola.android.model.repository

interface AutoCompleteRepository<T> {
    suspend fun autocomplete(query: String): List<T>
}
