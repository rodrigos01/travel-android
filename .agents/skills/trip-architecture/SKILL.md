---
name: Trip Feature Architecture
description: The stateless-reducer ViewModel/UseCase/PendingDataStore pattern used for trip plan item management
---

# Trip Feature Architecture

## Overview

Exactly one plan item can be in progress at a time (surfaced by the floating
`TripDetailsToolbar`). The type-specific UseCases hold no state of their own — they are
traditional, stateless MVVM reducers. `AddPlanUseCase` is the sole owner of the currently
displayed item:

```
UI Composables
     ↓ onUpdated(state: AddPlanItemState)
TripViewModel  ←→  AddPlanUseCase                 (owns _current: MutableStateFlow<AddPlanItemState?>)
                        ↓ dispatches by runtime type
              Type-specific UseCases              (stateless: createItem / onUpdated / createEntity)
              (AddFlightUseCase, ManualAddLodgingUseCase, etc.)
                        ↓ reads/writes
              PendingDataStore                    (single shared instance; non-displayable domain entities)
```

The composable already has enough display data to compute the next `AddPlanItemState` itself
(via `.copy(...)`) and calls one `onUpdated(state)` per type. `AddPlanUseCase` dispatches that
call to the matching type-specific UseCase's **suspend** `onUpdated(state)`, awaits the result,
and writes it into its own `_current` flow. There's no "previous state" passed in — a UseCase
that needs to know whether a search-result selection is already resolved compares
`state.startState.selectedResultId` against the id already sitting in `PendingDataStore` (e.g.
`entry.airportFrom?.iata`) rather than diffing against a prior call; and an autocomplete search
just always fires when there's no selection and the text is long enough, since the user can't
trigger a second `onUpdated` before either picking a result or typing more (which is itself a
legitimate reason to search again).

## PendingDataStore

**Location:** `app/src/main/java/travel/vola/android/ui/trip/creation/usecase/PendingDataStore.kt`

Single shared instance (owned by `AddPlanUseCase`, passed to every stateless sub-UseCase). Holds
only the fields that exist in a `TripEntity` but have **no** counterpart in the matching
`AddPlanItemState` — real domain entities (a resolved `Airport`, a resolved lodging `Place`) and
save-time plumbing (the real persisted entity id when editing), via a sealed `Entry` hierarchy
(`Entry.Flight`, `Entry.Lodging`, `Entry.TimedPlace`, `Entry.Restaurant`, `Entry.FlexibleSection`,
`Entry.LodgingSearch`). Plain mutable holder — nothing observes it reactively, only
`onUpdated`/`createEntity` read or write it synchronously. `currentAs<T>()` / `requireCurrent<T>()`
extension functions cast the current entry to the expected variant.

## Type-Specific UseCases

Each entity type has a dedicated, **stateless** UseCase (e.g. `ManualAddLodgingUseCase`). They:
- Implement `AddItemUseCase<Entity, ItemState>` and `EntityFactory<Entity, ItemState>`
- Hold no `StateFlow`/mutable state of their own — every method is a pure function (or a
  suspend function whose only side effect is reading/writing the shared `PendingDataStore`)

**Interface hierarchy:**
```kotlin
interface AddItemUseCase<E : TripEntity, T : AddPlanItemState> {
    fun createItem(id: String, time: ZonedDateTime, params: StateParams): T
    fun createItem(id: String, entity: E, params: StateParams): T
    suspend fun onUpdated(state: T): T
}
interface EntityFactory<E : TripEntity, T : AddPlanItemState> {
    fun createEntity(item: T): E
}
```

`AddLodgingUseCase` is a pure dispatcher over `ManualAddLodgingUseCase`/`LodgingSearchParamsUseCase`
— which of the two is active is just "which sealed subtype `state` is," no lookup needed. Mode-switch
actions (`onSwitchToManualButtonTapped`, `onFindLodgingButtonTapped`) live on `AddPlanUseCase`
itself (not the sub-UseCase) since only `AddPlanUseCase` has read/write access to "the current
state."

## AddPlanUseCase

**Location:** `app/src/main/java/travel/vola/android/ui/trip/viewmodel/AddPlanUseCase.kt`

The **only** stateful class in this subsystem — `_current: MutableStateFlow<AddPlanItemState?>`,
exposed as `items: StateFlow<AddPlanItemState?>`. Responsibilities:
- `createAddPlanItem(...)` clears `_current` + `PendingDataStore` before creating a new item —
  this is what guarantees at most one item is ever pending.
- `onUpdated(state)` dispatches to the matching type-specific UseCase's suspend `onUpdated`
  inside `coroutineScope.launch { ... }`, then writes the result to `_current`.
- `saveItem()` / `removeItem()` operate on `_current.value` directly (no id lookup).

## State Classes

**Location:** `app/src/main/java/travel/vola/android/ui/trip/state/TripItemState.kt`

`AddPlanItemState` sealed interface — one implementation per entity type:
`AddFlightItemState`, `ManualAddLodgingItemState`, `LodgingSearchItemState`, `AddPlaceItemState`,
`AddRestaurantItemState`, `AddFlexibleSectionItemState`. The four `ManualStartEndAddPlanState`
implementers (`AddFlightItemState`, `ManualAddLodgingItemState`, `AddPlaceItemState`,
`AddRestaurantItemState`) all store real `startState`/`endState: ManualAddPlanState` fields
(not derived properties) specifically so composables can `.copy(startState = ...)` directly.

`ManualAddPlanState.selectedResultId: String?` and the `id` field on `AutoCompleteResultState` /
`SearchResultItemState` are what let a composable put a tapped search result's reference straight
into the state it passes to `onUpdated` — no index-based side channel back to the UseCase.

## StateParams

Passed at item creation time. Controls which UI controls are enabled:
```kotlin
data class StateParams(
    val dateSelectionEnabled: Boolean,
    val typeSelectionEnabled: Boolean,
    val deleteEnabled: Boolean,
    val place: Place? = null,
)
```

## Data Flow Example

```
User taps a lodging search result →
  AddLodgingListItem builds uiState.copy(startState = uiState.startState.copy(selectedResultId = "hotel_123")) →
  calls the single onUpdated(newState) →
  TripViewModel → AddPlanUseCase.onUpdated(newState) → launches a coroutine →
    ManualAddLodgingUseCase.onUpdated(state) (suspend):
      sees state.startState.selectedResultId ("hotel_123") differs from
        pendingDataStore.current.selectedPlace?.id (null so far) →
      repository.details("hotel_123") (awaited) →
      pendingDataStore.update { ... full Place details ... } →
      returns corrected ManualAddLodgingItemState (locationText resolved, saveButtonEnabled recomputed) →
  AddPlanUseCase writes the result into _current → TripViewModel.viewState recomposes the toolbar
```

## FlexibleSectionUseCase: the one deliberate exception

`FlexibleSectionUseCase` mixes two unrelated concerns:
1. The pending "add new flexible section" flow (`createItem`, `onUpdated`, `generateSuggestions`)
   — stateless, follows the pattern above.
2. Direct inline editing of already-**saved** sections shown in the main itinerary list
   (`onFlexibleCategoryAdded`, `onFlexibleItemSearchTextChanged`, `onFlexibleItemSearchResultSelected`,
   `onFlexibleItemNoteAdded`), wired from `TripDetailItem.kt` against a saved section's own id.
   This half keeps real state (`trip`, `searchSessions`) and its own `itemId`-keyed methods on
   `AddFlexibleSectionPersistedActionHandler` — it has nothing to do with the single-pending-item
   invariant and is not part of the reducer collapse.

`delete(type, itemId)` on `ActionHandlers` is similarly id-addressed rather than "the current
item," since its only live caller passes a persisted section's id, not necessarily the pending
item's. It's currently only reachable for `FlexibleDaySection` — a known, pre-existing gap for
the other entity types (no delete button wired in the toolbar UI).

## Adding a New Entity Type

1. Add a new `Entry` variant to `PendingDataStore.kt` for whatever non-displayable data the type
   needs (or none, if the entity is fully representable in `AddPlanItemState`).
2. Add a new `XItemState` to the `AddPlanItemState` sealed interface in `TripItemState.kt`.
3. Create `XUseCase` implementing `AddItemUseCase<Entity, XItemState>` and
   `EntityFactory<Entity, XItemState>` — stateless, taking `PendingDataStore` as a constructor
   dependency.
4. Register the use case in `AddPlanUseCase`'s constructor and add its branches to the `when`
   blocks in `createAddPlanItem`/`onUpdated`/`saveItem`/`Type.useCase()`.
5. Add an `Add*ListItem` composable following the pattern in `AddFlightListItem.kt` (or
   `AddPlaceListItem.kt` for a `ManualStartEndAddPlanState`), and wire it into `AddPlanContent`
   in `AddPlanListItem.kt`.

## Key File Paths

| Component | Path |
|-----------|------|
| PendingDataStore | `ui/trip/creation/usecase/PendingDataStore.kt` |
| ActionHandlers | `ui/trip/creation/usecase/ActionHandlers.kt` |
| AddPlanUseCase | `ui/trip/viewmodel/AddPlanUseCase.kt` |
| AddFlightUseCase | `ui/trip/viewmodel/AddFlightUseCase.kt` |
| ManualAddLodgingUseCase | `ui/trip/viewmodel/ManualAddLodgingUseCase.kt` |
| FlexibleSectionUseCase | `ui/trip/viewmodel/FlexibleSectionUseCase.kt` |
| TripViewModel | `ui/trip/viewmodel/TripViewModel.kt` |
| TripItemState | `ui/trip/state/TripItemState.kt` |
| AddPlanContent / AddPlanListItem.kt | `ui/trip/eventlist/composable/AddPlanListItem.kt` |

All paths are under `app/src/main/java/travel/vola/android/`.
