package travel.vola.android.model.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import travel.vola.android.model.data.PlaceDetailsResult
import travel.vola.android.model.data.SimplePlace

class GeographyAutoCompleteRepository(
    private val regionsAutoCompleteRepository: PlaceAutoCompleteRepository = PlaceAutoCompleteRepository(
        types = listOf("(regions)"), resolveCity = false,
    ),
    private val areasAutoCompleteRepository: PlaceAutoCompleteRepository = PlaceAutoCompleteRepository(
        types = listOf("continent", "colloquial_area"), resolveCity = false,
    )
) : AutoCompleteRepository<SimplePlace, PlaceDetailsResult> {
    override suspend fun autocomplete(
        query: String,
        autocompleteKey: String
    ): List<SimplePlace> {
        return coroutineScope {
            awaitAll(
                async { regionsAutoCompleteRepository.autocomplete(query, autocompleteKey) },
                async { areasAutoCompleteRepository.autocomplete(query, autocompleteKey) }
            ).flatten()
        }
    }

    override suspend fun details(
        id: String,
        autocompleteKey: String
    ): PlaceDetailsResult? {
        return areasAutoCompleteRepository.details(id, autocompleteKey)
    }
}