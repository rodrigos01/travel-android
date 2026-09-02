package travel.vola.android.ui.trip.creation.usecase

import travel.vola.android.ui.trip.state.AddPlanItemState

interface AddPlanItemActionHandler : AddFlexibleSectionPersistedActionHandler {
    fun onUpdated(state: AddPlanItemState)
    fun onSwitchToManualButtonTapped()
    fun onFindLodgingButtonTapped()
    fun onGenerateSectionTapped()
    fun save()
    fun cancelEdit()
    fun delete(type: AddPlanItemState.Type, itemId: String)
}

/**
 * Inline editing of an already-*saved* [travel.vola.android.model.data.FlexibleDaySection], wired
 * from [travel.vola.android.ui.trip.eventlist.composable.TripDetailItem] against a persisted
 * section's own id. Unrelated to the single pending-item reducer flow above.
 */
interface AddFlexibleSectionPersistedActionHandler {
    fun onFlexibleCategoryAdded(itemId: String, category: String)
    fun onFlexibleItemSearchTextChanged(itemId: String, content: CharSequence)
    fun onFlexibleItemSearchResultSelected(itemId: String, resultId: String, categoryIndex: Int)
    fun onFlexibleItemNoteAdded(itemId: String, index: Int, categoryIndex: Int, note: String)
}
