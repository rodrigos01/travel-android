package travel.vola.android.ui.trip.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import travel.vola.android.common.coroutines.MutexScope
import travel.vola.android.common.coroutines.launch
import travel.vola.android.extensions.MapFlow
import travel.vola.android.extensions.MutableMapStateFlow
import travel.vola.android.extensions.dayOfMonthString
import travel.vola.android.extensions.dayOfWeekString
import travel.vola.android.extensions.get
import travel.vola.android.extensions.remove
import travel.vola.android.extensions.set
import travel.vola.android.model.data.FlexibleDayCategory
import travel.vola.android.model.data.FlexibleDayItem
import travel.vola.android.model.data.FlexibleDaySection
import travel.vola.android.model.data.Place
import travel.vola.android.model.data.SimplePlace
import travel.vola.android.model.repository.PlaceAutoCompleteRepository
import travel.vola.android.model.repository.TripRepository
import travel.vola.android.ui.trip.creation.usecase.AddFlexibleSectionItemActionHandler
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemStore
import travel.vola.android.ui.trip.creation.usecase.PendingData
import travel.vola.android.ui.trip.state.AddFlexibleSectionItemState
import travel.vola.android.ui.trip.state.SearchResultItemState
import travel.vola.android.ui.trip.state.TripItemState
import java.time.ZonedDateTime
import java.util.TimeZone

class FlexibleSectionUseCase(
    private val tripId: String,
    private val repository: TripRepository,
    private val coroutineScope: CoroutineScope,
    private val itemStore: AddPlanItemStore<PendingData.PendingFlexibleSection, AddFlexibleSectionItemState> = AddPlanItemStore(),
    private val autoCompleteRepository: PlaceAutoCompleteRepository = PlaceAutoCompleteRepository(
        types = listOf()
    ),
) : AddPlanUseCase.AddItemUseCase<FlexibleDaySection, AddFlexibleSectionItemState>,
    AddPlanUseCase.EntityFactory<FlexibleDaySection, AddFlexibleSectionItemState>,
    AddFlexibleSectionItemActionHandler {
    override val items: MapFlow<String, AddFlexibleSectionItemState> = itemStore.items(::createItem)

    val trip = repository.findTripById(tripId)
        .stateIn(coroutineScope, SharingStarted.Eagerly, initialValue = null)

    private val searchSessions = MutableMapStateFlow<String, List<SimplePlace>>()

    val flexibleSectionItems = trip.filterNotNull().combine(searchSessions) { trip, sessions ->
        trip.flexibleSections.sortedBy { it.date }.map { section ->
            createState(section, sessions)
        }
    }

    fun createState(
        section: FlexibleDaySection,
        sessions: Map<String, List<SimplePlace>> = emptyMap(),
        showDate: Boolean = false,
        backgroundStyle: TripItemState.EventItemState.BackgroundStyle = TripItemState.EventItemState.BackgroundStyle.SINGLE,
        isGenerated: Boolean = false,
    ): TripItemState.FlexibleDaySectionState = TripItemState.FlexibleDaySectionState(
        id = section.id,
        timestamp = section.date,
        showDate = showDate,
        dayOfMonth = section.date.dayOfMonthString,
        dayOfWeek = section.date.dayOfWeekString,
        backgroundStyle = backgroundStyle,
        name = section.name,
        subtitle = section.categories.flatMap { categories -> categories.items.map { it.place.name } }.take(3)
            .joinToString(", "),
        categories = section.categories.map { category ->
            TripItemState.DaySectionCategory(
                name = category.name,
                items = category.items.map {
                    TripItemState.SectionOption(
                        id = it.id,
                        title = it.place.name,
                        subtitle = it.place.address,
                        imageUrl = it.place.coverImage ?: "",
                    )
                },
            )
        },
        searchResults = sessions[section.id]?.map {
            SearchResultItemState(
                it.name,
                it.address
            )
        } ?: emptyList(),
        isGenerated = isGenerated,
    )

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
                city = params.place ?: Place(
                    id = "",
                    name = "",
                    address = "",
                    latitude = 0.0,
                    longitude = 0.0,
                    coverImage = null,
                    timeZone = TimeZone.getTimeZone(time.zone.id),
                    externalId = "",
                    source = "",
                ),
                categories = emptyList(),
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
                city = entity.city,
                categories = entity.categories,
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

    override fun onFlexibleItemDateTimeUpdated(
        itemId: String,
        dateTime: ZonedDateTime,
        timeSelected: Boolean
    ) {
        itemStore.update(itemId) {
            it.copy(startDateTime = dateTime, hasStartTime = timeSelected)
        }
    }

    override fun createEntity(item: AddFlexibleSectionItemState): FlexibleDaySection {
        val data: PendingData.PendingFlexibleSection = itemStore.getData(item.id)
            ?: error("Pending Flexible section with id ${item.id} not found")
        val name = item.sectionName ?: error("Section name cannot be null")
        return FlexibleDaySection(
            id = item.id,
            name = name,
            date = item.startDateTime,
            categories = data.categories,
            data.city,
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

    private val mutexScope = MutexScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onFlexibleItemSearchTextChanged(
        itemId: String,
        content: CharSequence
    ) {
        if (content.length < 3) {
            return
        }
        val section = getSection(itemId) ?: return
        val locationBias =
            (section.city.latitude to section.city.longitude)
                .takeIf { (latitude, longitude) -> latitude != 0.0 && longitude != 0.0 }
        mutexScope.launch {
            val results =
                autoCompleteRepository.autocomplete(content.toString(), itemId, locationBias)
            searchSessions[itemId] = results
        }
    }

    override fun onFlexibleItemSearchResultSelected(
        itemId: String,
        index: Int,
        categoryIndex: Int
    ) {
        val section = getSection(itemId) ?: return
        coroutineScope.launch {
            val selectedResult = searchSessions[itemId]?.getOrNull(index) ?: return@launch
            val place = autoCompleteRepository.details(selectedResult.id, itemId) ?: return@launch
            searchSessions.remove(itemId)
            val newSection = section.copy(
                categories = section.categories.mapIndexed { index, category ->
                    if (index == categoryIndex) {
                        category.copy(
                            items = category.items + FlexibleDayItem(
                                id = place.place.id,
                                place = place.place,
                                note = "",
                            )
                        )
                    } else {
                        category
                    }
                }
            )
            repository.saveFlexibleSection(tripId, newSection)
        }
    }

    private fun getSection(itemId: String): FlexibleDaySection? =
        trip.value?.flexibleSections?.firstOrNull { it.id == itemId }
}