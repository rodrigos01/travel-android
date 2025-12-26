package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import travel.vola.android.extensions.MapFlow
import travel.vola.android.model.data.FlexibleDayCategory
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.ui.trip.creation.usecase.AddFlexibleSectionItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.creation.usecase.PendingData
import travel.vola.android.ui.trip.state.AddFlexibleSectionItemState
import java.time.ZonedDateTime

class AddFlexibleSectionUseCase(
    private val tripId: String,
    private val repository: TripRepository,
    private val coroutineScope: CoroutineScope,
    private val itemStore: AddPlanItemStore<PendingData.PendingFlexibleSection, AddFlexibleSectionItemState> = AddPlanItemStore(),
) : AddPlanUseCase.AddItemUseCase<FlexibleDaySection, AddFlexibleSectionItemState>,
    AddPlanUseCase.EntityFactory<FlexibleDaySection, AddFlexibleSectionItemState>,
    AddFlexibleSectionItemActionHandler {
    override val items: MapFlow<String, AddFlexibleSectionItemState> = itemStore.items(::createItem)

    val trip = repository.findTripById(tripId)
        .stateIn(coroutineScope, SharingStarted.Eagerly, initialValue = null)

    override fun addItem(
        id: String,
        time: ZonedDateTime,
        params: AddPlanUseCase.StateParams
    ) {
        itemStore.addItem(
            PendingData.PendingFlexibleSection(
                id = id,
                startDateTime = time,
                hasStartTime = false,
                sectionName = "",
            ),
            params,
        )
    }

    override fun addItem(
        id: String,
        entity: FlexibleDaySection,
        params: AddPlanUseCase.StateParams
    ) {
        itemStore.addItem(
            PendingData.PendingFlexibleSection(
                id = id,
                startDateTime = entity.date,
                hasStartTime = false,
                sectionName = entity.name,
            ),
            params,
        )
    }

    override fun removeItem(item: AddFlexibleSectionItemState) {
        itemStore.remove(item)
    }

    private fun createItem(
        data: PendingData.PendingFlexibleSection,
        stateParams: AddPlanUseCase.StateParams,
    ): AddFlexibleSectionItemState {
        return AddFlexibleSectionItemState(
            id = data.id,
            typeSelectionEnabled = stateParams.typeSelectionEnabled,
            dateSelectionEnabled = stateParams.dateSelectionEnabled,
            saveButtonEnabled = data.sectionName.isNotBlank(),
            deleteButtonEnabled = stateParams.deleteEnabled,
            sectionName = data.sectionName,
            startDateTime = data.startDateTime,
            hasStartTime = data.hasStartTime,
        )
    }

    override fun sectionNameChanged(itemId: String, content: CharSequence) {
        itemStore.update(itemId) {
            it.copy(sectionName = content.toString())
        }
    }

    override fun createEntity(item: AddFlexibleSectionItemState): FlexibleDaySection {
        val name = item.sectionName ?: error("Section name cannot be null")
        return FlexibleDaySection(
            id = item.id,
            name = name,
            date = item.startDateTime,
            categories = emptyList()
        )
    }

    override fun onFlexibleCategoryAdded(itemId: String, category: String) {
        val section = getSection(itemId) ?: return
        coroutineScope.launch {
            repository.saveFlexibleSection(
                tripId, section.copy(
                    categories = section.categories + FlexibleDayCategory(
                        name = category,
                        items = emptyList(),
                    )
                )
            )
        }
    }

    override fun onFlexibleItemSearchTextChanged(
        itemId: String,
        content: CharSequence
    ) {
        // TODO: Update state with search results
    }

    override fun onFlexibleItemSearchResultSelected(
        itemId: String,
        index: Int,
        categoryIndex: Int
    ) {
        val section = getSection(itemId) ?: return
        coroutineScope.launch {
            repository.saveFlexibleSection(
                tripId, section.copy(
                    categories = section.categories.mapIndexed { index, category ->
                        if (index == categoryIndex) {
                            category
                            // TODO: resolve place tapped and save to category
                        } else {
                            category
                        }
                    }
                )
            )
        }
    }

    private fun getSection(itemId: String): FlexibleDaySection? =
        trip.value?.flexibleSections?.firstOrNull { it.id == itemId }
}