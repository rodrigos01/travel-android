---
name: Trip Feature Architecture
description: The ViewModel/UseCase/PendingData/AddPlanItemStore pattern used for trip plan item management
---

# Trip Feature Architecture

## Overview

The trip plan item creation flow uses a layered pattern:

```
UI Composables
     ↓ (events)
TripViewModel  ←→  AddPlanUseCase
                        ↓
              Type-specific UseCases
              (AddFlightUseCase, ManualAddLodgingUseCase, etc.)
                        ↓
              AddPlanItemStore<PendingData, AddPlanItemState>
                        ↓ (transform)
              items: Flow<Map<String, AddPlanItemState>>
```

## AddPlanItemStore

**Location:** `app/src/main/java/travel/vola/android/ui/trip/creation/usecase/AddPlanItemStore.kt`

Generic container for in-progress (unsaved) plan items. Type parameters:
- `R : PendingData` — raw mutable domain state
- `T : AddPlanItemState` — derived UI state

Key API:
```kotlin
// Register transform once; items Flow derives UI state from PendingData on every update
fun items(transform: (R, StateParams) -> T): MapFlow<String, T>

fun addItem(data: R, stateParams: StateParams)
fun getData(itemId: String): R?
fun update(itemId: String, updater: (R) -> R)   // atomic update
fun remove(item: T)
fun hasItem(itemId: String): Boolean
```

The transform is called every time data changes, producing new immutable `AddPlanItemState` values.

## PendingData

**Location:** `app/src/main/java/travel/vola/android/ui/trip/creation/usecase/PendingData.kt`

Sealed interface. One implementation per entity type:
- `PendingFlight` — departure/arrival airports, times
- `PendingLodging` — hotel search results, check-in/out, city, coordinates
- `PendingTimedPlace` — place search results, time
- `PendingRestaurant` — restaurant search results, time
- `PendingFlexibleSection` — name

`PendingData` accumulates all user input and is the single source of truth during item creation. The UI state (`AddPlanItemState`) is always derived from it.

## Type-Specific UseCases

Each entity type has a dedicated UseCase (e.g., `ManualAddLodgingUseCase`). They:
- Hold an `AddPlanItemStore<PendingX, XItemState>`
- Implement `AddItemUseCase<Entity, ItemState>` and `EntityFactory<Entity, ItemState>`
- Expose `items: MapFlow<String, XItemState>` via the store's transform
- Handle all user interactions for that type (text changes, time selection, search result taps)

**Interface hierarchy:**
```
AddItemUseCase<E, T>           — addItem(id, time, params), addItem(id, entity, params)
EntityFactory<E, T>            — createEntity(item: T): E
[Type]ItemActionHandler        — event handlers specific to the type
```

## AddPlanUseCase

**Location:** `app/src/main/java/travel/vola/android/ui/trip/viewmodel/AddPlanUseCase.kt`

Aggregator. Holds all type-specific UseCases and:
- Merges their `items` flows into one `Map<String, AddPlanItemState>` via `mergeMaps()`
- Routes `createAddPlanItem()` to the right UseCase based on entity type
- Routes `saveItem()` to `createEntity()` on the right UseCase
- Delegates action handler calls to the owning UseCase

## State Classes

**Location:** `app/src/main/java/travel/vola/android/ui/trip/state/TripItemState.kt`

`AddPlanItemState` sealed interface — one implementation per entity type:
- `AddFlightItemState`
- `ManualAddLodgingItemState`
- `AddTimedPlaceItemState`
- `AddRestaurantItemState`
- `AddFlexibleSectionItemState`

Each contains display-ready data and state flags:
```kotlin
val saveButtonEnabled: Boolean
val deleteButtonEnabled: Boolean
val dateSelectionEnabled: Boolean
val typeSelectionEnabled: Boolean
```

## StateParams

Passed at item creation time. Controls which UI controls are enabled:
```kotlin
data class StateParams(
    val dateSelectionEnabled: Boolean,
    val deleteEnabled: Boolean,
    val typeSelectionEnabled: Boolean,
)
```

## Data Flow Example

```
User types "hotel" →
  ManualAddLodgingUseCase.lodgingTextChanged("id", "hotel") →
    repository.autocomplete("hotel", "id") // suspend call →
    itemStore.update("id") { it.copy(searchResults = results) } →
      transform(PendingLodging, stateParams) runs →
        items emits new ManualAddLodgingItemState with populated searchResults
```

## Adding a New Entity Type

1. Add a new `PendingX` data class to `PendingData.kt`
2. Add a new `XItemState` to the `AddPlanItemState` sealed interface
3. Create `XUseCase` implementing `AddItemUseCase` and `EntityFactory`
   - Instantiate `AddPlanItemStore<PendingX, XItemState>()`
   - Define the transform lambda in `items { data, params -> ... }`
4. Add `XItemActionHandler` interface for UI events
5. Register the use case in `AddPlanUseCase` and merge its items
6. Handle the new type in `saveItem()` and `createAddPlanItem()` routing

## Key File Paths

| Component | Path |
|-----------|------|
| AddPlanItemStore | `ui/trip/creation/usecase/AddPlanItemStore.kt` |
| PendingData | `ui/trip/creation/usecase/PendingData.kt` |
| ActionHandlers | `ui/trip/creation/usecase/ActionHandlers.kt` |
| AddPlanUseCase | `ui/trip/viewmodel/AddPlanUseCase.kt` |
| AddFlightUseCase | `ui/trip/viewmodel/AddFlightUseCase.kt` |
| ManualAddLodgingUseCase | `ui/trip/viewmodel/ManualAddLodgingUseCase.kt` |
| TripViewModel | `ui/trip/viewmodel/TripViewModel.kt` |
| TripItemState | `ui/trip/state/TripItemState.kt` |

All paths are under `app/src/main/java/travel/vola/android/`.

## Flow Type Aliases

```kotlin
typealias MapFlow<K, V> = Flow<Map<K, V>>
typealias MapStateFlow<K, V> = StateFlow<Map<K, V>>
typealias MutableMapStateFlow<K, V> = MutableStateFlow<Map<K, V>>
```

`MutableMapStateFlow` has extension operators (`set`, `get`, `update`, `remove`) that always produce new immutable map copies. Located in `extensions/MutableMapStateFlow.kt`.
