package travel.vola.android.ui.trip.viewmodel

import travel.vola.android.extensions.MapFlow
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.ui.trip.creation.usecase.AddFlexibleSectionItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.creation.usecase.PendingData
import travel.vola.android.ui.trip.state.AddFlexibleSectionItemState
import java.time.ZonedDateTime

class AddFlexibleSectionUseCase(
    private val itemStore: AddPlanItemStore<PendingData.PendingFlexibleSection, AddFlexibleSectionItemState> = AddPlanItemStore(),
) : AddPlanUseCase.AddItemUseCase<FlexibleDaySection, AddFlexibleSectionItemState>,
    AddPlanUseCase.EntityFactory<FlexibleDaySection, AddFlexibleSectionItemState>,
    AddFlexibleSectionItemActionHandler {
    override val items: MapFlow<String, AddFlexibleSectionItemState> = itemStore.items(::createItem)

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
}