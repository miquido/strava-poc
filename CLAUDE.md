# StravaPoc — Project Guide for Claude

## Project Overview

Android proof-of-concept app with two targets:
- **`app`** — phone app (Jetpack Compose + Google Maps): browse routes, view workout history
- **`wear`** — Wear OS app (Compose for Wear): record workouts with GPS tracking, sync results to phone

The two apps communicate via the **Wearable Data Layer API** (`DataClient`).

---

## Module Structure

```
:core:architecture      — MVI base classes (MviViewModel, MviContainer)
:library:data           — Domain models, repositories, DAOs, Room databases, DI bindings
:library:usecase        — All use cases (depend on :library:data interfaces)
:app                    — Phone UI + sync receivers
:wear                   — Wear OS UI + WorkoutService + data sync senders
```

**Dependency rule:** `:app` and `:wear` depend on `:library:usecase` → `:library:data` → `:core:architecture`. Neither app module depends on the other.

---

## Build Commands

```bash
# Compile all modules (fastest check)
./gradlew :library:data:compileDebugKotlin :library:usecase:compileDebugKotlin :app:compileDebugKotlin :wear:compileDebugKotlin

# Full build
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

Always verify with the compile command after making changes.

---

## Architecture: MVI

ViewModels extend `MviViewModel<STATE>` from `:core:architecture`.

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(...) :
    MviViewModel<MyViewState>(MyViewState(), MviDefaultConfig()) {

    fun doSomething() = launch {           // use launch {} — NOT viewModelScope.launch {}
        transform { copy(isLoading = true) }
        useCase()
            .onSuccess { transform { copy(data = it, isLoading = false) } }
            .onFailure { transform { copy(error = it.message, isLoading = false) } }
    }

    fun navigate() = launch {
        emitSideEffect(MySideEffect.Navigate(...))
    }
}
```

**Rules:**
- Always use MVI `launch {}` inside ViewModels — never `viewModelScope.launch {}`
- State mutations go through `transform {}`
- Navigation and one-time events go through `emitSideEffect()`
- Never import `kotlinx.coroutines.launch` or `androidx.lifecycle.viewModelScope` in ViewModels

---

## Dependency Injection: Hilt

### Repository bindings — all in `library/data/di/DataModule.kt`

All `@Binds` for shared repositories live in one place:

```kotlin
@Module @InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds @Singleton abstract fun bindRouteRepository(impl: RouteRepositoryImpl): RouteRepository
    @Binds @Singleton abstract fun bindWorkoutResultRepository(impl: WorkoutResultRepositoryImpl): WorkoutResultRepository
    companion object {
        @Provides @Singleton fun provideAppDatabase(...): AppDatabase
        @Provides fun provideWorkoutResultDao(db: AppDatabase): WorkoutResultDao
    }
}
```

### Wear-specific bindings — `wear/di/WearModule.kt`

Only wear-specific bindings here (WearDatabase, WearRouteDao, WorkoutResultSender, DataClient, WearRouteRepository).

### Key rule: avoid Hilt DuplicateBindings

Hilt does not allow two modules in the same component to bind the same interface. If `app` and `wear` need different implementations of the same interface, create a **wear-specific subinterface** and use separate use cases:

- `RouteRepository` → `RouteRepositoryImpl` (mock data, used by app)
- `WearRouteRepository : RouteRepository` → `WearRouteRepositoryImpl` (Room, used by wear)
- Wear uses `GetWearRoutesUseCase(WearRouteRepository)`, not `GetRoutesUseCase(RouteRepository)`

---

## Layer Conventions

### Data flow (bottom → top)
```
Room DAO → LocalDataSource → Repository → UseCase → ViewModel → Screen
```

- **DAOs** — pure Room interfaces, in `library/data/db/{app|wear}/`
- **LocalDataSource** — wraps DAO, adds `@Inject`, lives in `library/data/datasource/` or same `db/` package
- **Repository** — business logic aggregation, lives in `library/data/repository/`
- **UseCase** — single-responsibility, lives in `library/usecase/`
- ViewModels never access DAOs or DataSources directly

### Naming
- `XRepository` / `XRepositoryImpl`
- `XLocalDataSource` (internal)
- `XUseCase` / `GetXUseCase` / `SaveXUseCase` / `DeleteXUseCase`
- `XViewState`, `XSideEffect`, `XViewModel`, `XScreen`

---

## Database Structure

```
library/data/db/
├── app/
│   ├── AppDatabase.kt          — Room database (phone)
│   ├── dao/WorkoutResultDao.kt
│   └── entity/WorkoutResultEntity.kt
└── wear/
    ├── WearDatabase.kt         — Room database (wear)
    ├── WearRouteDao.kt
    ├── WearRouteEntity.kt
    ├── WearRouteLocalDataSource.kt
    ├── PendingWorkoutResultEntity.kt  — also contains PendingWorkoutResultDao
    └── PendingWorkoutResultLocalDataSource.kt
```

---

## String Resources

Use `@StringRes` extension functions instead of hardcoded strings for display names:

```kotlin
// app/presentation/ActivityTypeExt.kt
@StringRes
internal fun ActivityType.toDisplayNameRes(): Int = when (this) {
    RUNNING -> R.string.activity_running
    CYCLING -> R.string.activity_cycling
    WALKING -> R.string.activity_walking
}

// In Compose:
Text(stringResource(activityType.toDisplayNameRes()))
```

Never use hardcoded English strings for user-visible text — always use string resources.

---

## Thread Safety

Shared mutable state accessed from multiple coroutines must be marked `@Volatile`:

```kotlin
@Volatile var pendingTrackedPoints: List<RoutePoint> = emptyList()
```

---

## Wear-Specific Constants

Wear-only constants live in `wear/data/WearConstants.kt`:

```kotlin
const val CUSTOM_ROUTE_ID = "_custom_"
```

Do not define wear-specific constants inside companion objects of classes.

---

## Sync: Phone ↔ Wear

- **Phone → Wear:** `WearSyncManager` / `RouteSender` sends routes via `DataClient`
- **Wear → Phone:** `WorkoutResultSender` sends workout results; `WorkoutResultReceiver` (`WearableListenerService`) on phone side receives them
- `WorkoutResultReceiver` uses `@Inject` for all dependencies — never instantiate data sources manually

---

## Key Files Reference

| What | Where |
|------|-------|
| MVI base | `core/architecture/src/.../mvi/MviViewModel.kt` |
| Domain models | `library/data/.../model/` |
| Repository interfaces | `library/data/.../repository/` |
| All use cases | `library/usecase/.../` |
| DI bindings (shared) | `library/data/.../di/DataModule.kt` |
| DI bindings (wear) | `wear/.../di/WearModule.kt` |
| Workout recording service | `wear/.../data/WorkoutService.kt` |
| Phone sync receiver | `app/.../sync/WorkoutResultReceiver.kt` |
