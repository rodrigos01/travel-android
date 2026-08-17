---
name: Writing Unit Tests
description: How to write and structure unit tests for ViewModels and UseCases in this project
---

# Writing Unit Tests

## Test Setup Boilerplate

Every ViewModel/UseCase test follows this structure:

```kotlin
class MyUseCaseTest {
    @get:Rule
    val rule = UnconfinedDispatcherTestRule()

    private val testScope = TestScope(rule.dispatcher)

    // Mocks
    private val repository: MyRepository = mock()
    private val itemStore = mockItemStore<PendingMyType, MyItemState>()

    // Subject under test
    private val subject = MyUseCase(testScope, itemStore, repository)

    // Observe items as StateFlow for assertions
    private val items = subject.items.stateIn(
        testScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyMap(),
    )
}
```

**Key imports:**
- `travel.vola.android.test.UnconfinedDispatcherTestRule`
- `travel.vola.android.test.Mocks.mockItemStore`
- `travel.vola.android.test.Captor.getUpdateResult`
- `travel.vola.android.extensions.get` (for `map["key"]` shorthand on StateFlow)
- `travel.vola.android.extensions.zonedDateTime`

## Test Helpers

### `UnconfinedDispatcherTestRule`
JUnit rule that installs `UnconfinedTestDispatcher` as the main dispatcher. Wrap `TestScope` with it:
```kotlin
private val testScope = TestScope(rule.dispatcher)
```
Coroutines run synchronously — no need for `runTest` or `advanceUntilIdle` in most tests.

### `mockItemStore<R, T>()`
Returns a **functional** mock `AddPlanItemStore`. It actually stores data and runs the transform, so items flow emits real values. Use it exactly like a real store in test assertions.

```kotlin
private val itemStore = mockItemStore<PendingFlight, AddFlightItemState>()
```

### `Captor.getUpdateResult(originalData)`
Used to verify what data an update call produces **without** going through the items flow. Captures the lambda passed to `itemStore.update()` and runs it against the given data:

```kotlin
val result = itemStore.getUpdateResult(originalData)
assertThat(result.name).isEqualTo("Hotel Name")
```

Use this when you want to assert the final `PendingData` state after an action, rather than the derived UI state.

### `zonedDateTime(source: String)`
Parses ISO 8601 strings into `ZonedDateTime`. Preferred over `mock<ZonedDateTime>()` for time values:
```kotlin
val time = zonedDateTime("2025-10-16T15:23:00+01:00")
```

### `items["key"]` shorthand
The `travel.vola.android.extensions.get` extension allows:
```kotlin
assertThat(items["lodging_id"]?.id).isEqualTo("lodging_id")
// instead of items.value["lodging_id"]
```

## Common Patterns

### Assert items flow after action
```kotlin
subject.addItem("id", zonedDateTime("2025-10-16T15:00:00+01:00"), mock())
val item = items.value["id"] ?: fail()
assertThat(item.startState.locationText).isNull()
```

### Assert update result (PendingData after mutation)
```kotlin
val originalData = PendingFlight(id = "id", departure = checkInTime)
itemStore.stub {
    on { getData("id") } doReturn originalData
}
subject.onSomeAction("id", newValue)
val result = itemStore.getUpdateResult(originalData)
assertThat(result.someField).isEqualTo(newValue)
```

### Stub suspended repository calls
```kotlin
repository.stub {
    onBlocking { autocomplete("hotel", autocompleteKey = "id") } doReturn results
}
```

### Assert verify use case delegation
```kotlin
verify(addFlightUseCase).addItem(eq("item_id"), eq(initialTime), any())
verify(itemStore).remove(item)
```

## Test File Locations

- Tests live in `app/src/test/java/travel/vola/android/ui/trip/viewmodel/`
- Test helpers: `app/src/test/java/travel/vola/android/test/`
  - `Captor.kt` — `getUpdateResult`
  - `Mocks.kt` — `mockItemStore`, `mockTime`
  - `UnconfinedDispatcherTestRule.kt`
  - `assertions.kt` — `assertType<T>()`
