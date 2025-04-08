package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import travel.vola.android.common.coroutines.MutexScope
import travel.vola.android.model.data.Time
import travel.vola.android.model.repository.AutoCompleteRepository
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.creation.usecase.PendingData
import travel.vola.android.ui.trip.state.ManualAddPlanState

class ManualAddPlanUseCase<T, S>(
    private val coroutineScope: CoroutineScope,
    private val repository: AutoCompleteRepository<S>,
    private val itemStore: AddPlanItemStore<PendingData.PendingAddPlanData<T?, S>, ManualAddPlanState> = AddPlanItemStore(),
) {
    fun items(
        createState: (PendingData.PendingAddPlanData<T?, S>, AddPlanUseCase.StateParams) -> ManualAddPlanState
    ) = itemStore.items(createState)

    fun addItem(itemId: String, time: Time, params: AddPlanUseCase.StateParams) {
        itemStore.addItem(itemId, PendingData.PendingAddPlanData(time, null, emptyList()), params)
    }

    fun setTime(itemId: String, time: Time) {
        itemStore.update(itemId) {
            it.copy(time = time)
        }
    }

    private val autoCompleteScope = MutexScope(coroutineScope.coroutineContext)
    fun textChanged(
        itemId: String, content: CharSequence
    ) {
        if (content.length < 3) {
            return
        }
        autoCompleteScope.launch {
            val results = repository.autocomplete(content.toString())
            itemStore.update(itemId) {
                it.copy(searchResults = results)
            }
        }
    }

    fun searchResultTapped(itemId: String, index: Int, createData: suspend (S) -> T) {
        val selected = itemStore.getData(itemId)?.searchResults?.getOrNull(index) ?: return
        itemStore.update(itemId) { data ->
            data.copy(data = null, searchResults = emptyList())
        }
        coroutineScope.launch {
            val newData = createData(selected)
            itemStore.update(itemId) { data ->
                data.copy(data = newData, searchResults = emptyList())
            }
        }
    }

    fun remove(itemId: String) {
        itemStore.remove(itemId)
    }
}
