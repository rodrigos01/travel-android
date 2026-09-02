package travel.vola.android.ui.trip.creation.usecase

import travel.vola.android.model.data.Airport
import travel.vola.android.model.data.FlexibleDayCategory
import travel.vola.android.model.data.Place

/**
 * Holds the single pending item's non-displayable domain entities - things a type-specific
 * UseCase needs to build the final [travel.vola.android.model.data.TripEntity] on save but that
 * don't belong in the UI-facing [travel.vola.android.ui.trip.state.AddPlanItemState]. Shared
 * across all type-specific UseCases; only one [Entry] is ever set at a time, matching the
 * single-pending-item invariant enforced by AddPlanUseCase.
 */
class PendingDataStore {

    sealed interface Entry {
        data class Flight(
            val entityId: String?,
            val airportFrom: Airport?,
            val airportTo: Airport?,
        ) : Entry

        data class Lodging(
            val entityId: String?,
            val selectedPlace: Place?,
            val city: Place?,
        ) : Entry

        data class TimedPlace(
            val entityId: String?,
            val place: Place?,
            val city: Place?,
        ) : Entry

        data class Restaurant(
            val entityId: String?,
            val place: Place?,
            val city: Place?,
        ) : Entry

        data class FlexibleSection(
            val city: Place,
            val categories: List<FlexibleDayCategory>,
        ) : Entry

        data class LodgingSearch(
            val city: Place?,
            val searchResults: List<Place> = emptyList(),
        ) : Entry
    }

    var current: Entry? = null
        private set

    fun set(entry: Entry) {
        current = entry
    }

    fun update(updater: (Entry) -> Entry) {
        current = current?.let(updater) ?: error("No pending entry in store")
    }

    fun clear() {
        current = null
    }
}

inline fun <reified T : PendingDataStore.Entry> PendingDataStore.currentAs(): T? = current as? T

inline fun <reified T : PendingDataStore.Entry> PendingDataStore.requireCurrent(): T =
    current as? T ?: error("No pending ${T::class.simpleName} entry in store")
