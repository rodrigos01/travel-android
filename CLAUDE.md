# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Vola — an Android trip-planning app. Kotlin, Jetpack Compose, single `:app` module. Package root: `travel.vola.android`.

## Commands

- Build: `./gradlew assembleLocalDebug` (or `assembleProdDebug`)
- Run all unit tests: `./gradlew testProdReleaseUnitTest` (this is the variant CI runs)
- Run a single test class: `./gradlew testProdReleaseUnitTest --tests "travel.vola.android.ui.trip.viewmodel.SomeClassTest"`
- Format code (run after any change): `./gradlew spotlessApply`
- **Always** run Gradle using the existing daemon — never pass `--no-daemon`.

Build flavors are dimensioned by `host`: `local` (points at `http://10.0.2.2:5000`, no auth) and `prod` (points at the live backend, requires auth). Combine with `Debug`/`Release` build types, e.g. `assembleLocalDebug`, `testProdReleaseUnitTest`.

### Local setup

Secrets are read from `local.properties` (or env vars, used in CI): `MAPS_API_KEY`, `CLIENT_SECRET`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. A valid `google-services.json` must be present in `app/` for Firebase. See [README.md](README.md).

### CI

GitHub Actions (`.github/workflows/`): `unit-tests.yml` runs `testProdReleaseUnitTest` on PRs to `develop`/`main`/`master`; `firebase-distribution.yml` builds and pushes to Firebase App Distribution on pushes to `develop`. The `cloudbuild*.yaml` files are legacy Google Cloud Build configs from before the CI migration.

## Architecture

Clean-architecture-flavored layering with reactive Flows throughout:

```
UI (Compose)  →  ViewModel (exposes one ViewState Flow)  →  UseCase(s)  →  Repository  →  DataSource(s)
```

- **DI**: No framework — manual DI via `ViewModelFactoryDependencies` ([di/ViewModelFactoryDependencies.kt](app/src/main/java/travel/vola/android/di/ViewModelFactoryDependencies.kt)), built once in `MainActivity` and threaded through Compose via `CreationExtras`/`LocalViewModelCreationExtras`. Every ViewModel gets constructed through a companion `Factory` that pulls dependencies off `factoryDependencies` (the `CreationExtras.factoryDependencies` accessor) — follow this pattern for new ViewModels, e.g. [ui/triplist/TripListViewModel.kt](app/src/main/java/travel/vola/android/ui/triplist/TripListViewModel.kt).
- **Navigation**: `NavHost` with typed/route destinations declared in `MainActivity`, each screen exposing a `XDestination` object with `ROUTE`/`Params`.
- **Data layer**: `TripRepository` is backed by `MultiSourceTripRepository` ([model/multisource/MultiSourceTripRepository.kt](app/src/main/java/travel/vola/android/model/multisource/MultiSourceTripRepository.kt)), which picks between multiple `TripDataSource` implementations (`RoomTripDataSource` for local SQLite, `FirebaseTripDataSource` for Firestore) at runtime based on a user preference (`DataSourceType`, stored via DataStore). Don't assume a single source of truth — trace which `TripDataSource` is active when debugging data issues.
- **Networking**: Ktor client ([model/network/KtorClient.kt](app/src/main/java/travel/vola/android/model/network/KtorClient.kt)) talks to the backend for airport/place/lodging search and autocomplete. OAuth2 client-credentials tokens are cached in DataStore via a Ktor `Auth` plugin.
- **Persistence**: Room (`model/room/`) for local trips; DataStore Preferences/Proto for user preferences and cached auth tokens.

### Trip plan-item creation flow

The most involved subsystem — adding flights/lodging/places/restaurants/flexible-sections to a trip — follows a distinct layered pattern documented in full in [.agents/skills/trip-architecture/SKILL.md](.agents/skills/trip-architecture/SKILL.md). Load that skill before touching anything under `ui/trip/creation/`. Summary:

```
UI Composables → TripViewModel ←→ AddPlanUseCase → type-specific UseCase (AddFlightUseCase, ManualAddLodgingUseCase, ...)
                                                          → AddPlanItemStore<PendingData, AddPlanItemState>
                                                          → items: Flow<Map<String, AddPlanItemState>>
```

- `PendingData` (sealed interface, one variant per entity type) is the mutable in-progress domain state; `AddPlanItemState` (also sealed, one variant per type) is the derived, display-ready UI state — always rebuilt from `PendingData` via a transform, never mutated directly.
- `AddPlanUseCase` aggregates all type-specific UseCases, merging their `items` flows and routing `createAddPlanItem()`/`saveItem()` to the right one.
- Chronological event ordering/sorting for the itinerary is handled by `EventComparable` in `TripViewModel`, with tie-break priority rules for same-timestamp events (see `product_spec/technical_specification.md` §4.1 for the exact priority table).

### Other notable pieces

- **GenAI**: `model/genai/` (Firebase AI) powers the trip-creation assistant (`ui/trip/creation/assistant/`) and itinerary suggestions. Treat this as a distinct, separable feature area.
- **Dynamic theming**: `ui/theme/` derives Material 3 color schemes at runtime from a trip/city's cover image via the Android Palette API + `material-kolor` (`PaletteStyle.Expressive`), rather than using a single static theme.
- **Product/technical specs**: [product_spec/product_specification.md](product_spec/product_specification.md) and [product_spec/technical_specification.md](product_spec/technical_specification.md) describe product requirements and the Firestore schema/API surface in more detail than the code alone.

## Testing conventions

Full conventions and boilerplate are documented in [.agents/skills/writing-tests/SKILL.md](.agents/skills/writing-tests/SKILL.md) — load it before writing ViewModel/UseCase tests. Key points:

- Tests live under `app/src/test/java/travel/vola/android/`; shared test helpers live in `app/src/test/java/travel/vola/android/test/` (`UnconfinedDispatcherTestRule`, `Mocks.mockItemStore`, `Captor.getUpdateResult`, `assertions.assertType`).
- Coroutines are driven with `UnconfinedDispatcherTestRule` (installs `UnconfinedTestDispatcher`), so most tests don't need `runTest`/`advanceUntilIdle`.
- Use `mockItemStore<R, T>()` for a functional `AddPlanItemStore` fake rather than a plain mock, and `Captor.getUpdateResult(originalData)` to assert the `PendingData` produced by an `itemStore.update()` call without going through the derived `items` flow.

## Code style

Formatting is enforced by Spotless + ktlint (`build.gradle.kts` root, applied to all subprojects) — run `./gradlew spotlessApply` after edits rather than hand-formatting. Notable ktlint rules disabled project-wide: filename convention, property naming, value-argument comments, function naming, max line length.
