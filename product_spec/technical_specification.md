# Technical Specification: Vola Travel App (Android)

This document provides technical details for engineers to understand the implementation, data architecture, and integration points of the Vola Android application.

## 1. Technical Architecture

The application follows a clean architecture pattern with a focus on reactive programming using Kotlin Coroutines and Flows.

- **UI Layer**: Built with **Jetpack Compose**. It follows a unidirectional data flow (UDF) pattern where the `ViewModel` exposes a single `ViewState` flow.
- **Domain Layer**: Logic is encapsulated in **UseCases** (e.g., `AddPlanUseCase`, `FlexibleSectionUseCase`) to ensure reusability and testability.
- **Data Layer**: Implements a **Repository pattern**. It supports multi-source data retrieval (Local Room DB + Remote Firestore) through a common `TripDataSource` interface.
- **Networking**: Uses **Ktor Client** for HTTP requests with Kotlinx.serialization for JSON parsing.
- **Dependency Injection**: Manual dependency injection managed through a `factoryDependencies` pattern.

## 2. Firebase Firestore Schema

The application uses Firestore as a remote synchronization layer. The schema is optimized for offline-first access and atomic trip updates.

### 2.1 Collection: `/trips`
Each document in the `/trips` collection represents a single trip.

| Field | Type | Description |
| :--- | :--- | :--- |
| `name` | String | The user-defined name of the trip. |
| `coverImage` | String (URL) | URL for the trip's hero image. |
| `flights` | List<Flight> | Array of flight objects. |
| `lodgings` | List<Lodging> | Array of accommodation objects. |
| `places` | List<TimedPlace> | Array of sightseeing activities. |
| `restaurants` | List<RestaurantReservation> | Array of dining reservations. |
| `flexibleSections` | List<FlexibleDaySection> | Array of non-linear planning sections. |
| `preferences` | Object | User trip preferences (vibe, interests, etc.). |

### 2.2 Data Models (Nested)

#### Flight
- `id`: String (UUID)
- `segments`: List of `FlightSegment` (departure/arrival airports, times).
- `price`: Double (Optional)

#### Lodging
- `id`: String (UUID)
- `name`: String
- `address`: String
- `latitude` / `longitude`: Double
- `checkIn` / `checkout`: String (ISO 8601)

#### TimedPlace (Activities)
- `place`: Object (ID, name, address, coordinates, coverImage).
- `time`: String (ISO 8601)
- `endTime`: String (Optional)
- `hasTime`: Boolean (True if a specific time was set).

#### FlexibleDaySection
- `name`: String
- `date`: String (ISO 8601)
- `categories`: List of `FlexibleSectionCategory` (name, items).
- `items`: List of `FlexibleSectionItem` (place object, custom note).

---

## 3. API Integration & Endpoints

The app communicates with a backend service for location search, airport data, and hotel pricing.

### 3.1 Base Configuration
- **Auth URL**: `https://us-central1-travel-164715.cloudfunctions.net/auth`
- **Server URL**: Defined in `BuildConfig.SERVER_URL`.
- **Authentication**: Oauth2 Client Credentials flow. Tokens are stored in **DataStore** and managed via a Ktor `Auth` interceptor.

### 3.2 Endpoints by Functionality

#### Flight Management
- **Search Airports**: `GET /flights/airport/autocomplete?query={query}`
- **Airport Details**: `GET /flights/airport/{iata_code}`

#### Location & Place Search
- **Place Autocomplete**: `GET /places/autocomplete?query={query}&types={types}&sessionId={uuid}`
  - *Note: `types` can be `(regions)` for cities or `lodging` for hotels.*
- **Place Details**: `GET /places/{place_id}?resolveCity=true`

#### Lodging (Hotel) Search
- **City Autocomplete**: `GET /lodging/autocomplete?query={query}`
- **Hotel Search**: `GET /lodging/search?cityId={id}&checkin={date}&checkout={date}&adults=2&currency=USD`
- **Hotel Details**: `GET /lodging/{hotel_id}?checkin={date}&checkout={date}`

---

## 4. Key Implementation Details

### 4.1 Chronological Sorting Logic
The `EventComparable` class in `TripViewModel` handles the complex sorting required for a travel itinerary.

**Primary Sort**: Timestamp (Ascending).
**Secondary Sort (Conflict Resolution)**:
When two events have the same or overlapping timestamps, priority is assigned based on the `EventType`:

1.  **Inter-city Transitions** (Place change):
    - `CHECKOUT` (Priority 0)
    - `DEPARTURE` / `ARRIVAL` (Priority 1)
    - `CHECKIN` (Priority 2)
    - `PLACE` (Priority 3)
2.  **Intra-city Activities** (Same place):
    - `ARRIVAL` (Priority 1)
    - `CHECKIN` / `CHECKOUT` (Priority 2)
    - `PLACE` (Priority 3)
    - `DEPARTURE` (Priority 4)

### 4.2 Dynamic Theming Engine
The app uses the **Android Palette API** to create an immersive experience.
- **Extraction**: When a city banner enters the view, the dominant color is extracted from its `coverImage`.
- **Generation**: The extracted color is passed as a `seedColor` to the Material 3 `dynamicColorScheme` generator.
- **Transition**: UI components use the `PaletteStyle.Expressive` style for vibrant, destination-matched themes.

### 4.3 Add Plan Intelligent Defaults
The `AddPlanUseCase` provides smart defaults for new entries:
- **Date**: Uses the date of the event currently "focused" in the scroll view.
- **City**: Identifies the current city by looking at the nearest `TimedPlace` or `FlightArrival` relative to the scroll position.

---

## 5. Security & Persistence

- **Local Storage**: Uses **Room SQLite** for persistent storage of trip data when in LOCAL mode.
- **Encrypted Preferences**: Sensitive tokens (if any) should be stored in `EncryptedSharedPreferences`. Current implementation uses standard **DataStore (Preferences)** for auth tokens.
- **ProGuard/R8**: Rules are defined to protect serialized data models and Firebase internal classes.
