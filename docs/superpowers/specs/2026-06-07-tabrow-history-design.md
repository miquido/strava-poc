# TabRow + Historia aktywności — Spec

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Dodać TabRow do aplikacji mobilnej (Tab: Trasy / Historia) oraz ekran historii aktywności odbieranych z zegarka Wear OS, przechowywanych w Room DB.

**Architecture:** Clean Architecture (DataSource → Repository → UseCase → ViewModel). Historia zapisywana przez `WearableListenerService` do Room DB; ViewModel pobiera ją przez UseCase. MVI pattern (MviViewModel) na każdym ekranie.

**Tech Stack:** Jetpack Compose, Room, WearableListenerService, Google Maps Compose, MVI (MviViewModel z :core:architecture), kotlinx.serialization

---

## Nawigacja

Obecny nav graf:
```
activity_type → route_list/{type} → route_detail/{id}
```

Nowy nav graf:
```
home → route_list/{type} → route_detail/{id}
       history_detail/{id}
```

`HomeScreen` to nowy ekran startowy z `TabRow` (dwa taby). Tab 0 wyświetla istniejący `ActivityTypeScreen` jako zawartość. Tab 1 wyświetla `HistoryScreen`. Wyjście głębiej (np. tap na typ aktywności) powoduje pełnoekranową nawigację — TabRow znika. `HomeScreen` zastępuje `activity_type` jako `startDestination`.

## Warstwa danych — library:data

### WorkoutResultEntity (Room)

Nowy plik: `library/data/src/main/java/.../model/WorkoutResultEntity.kt`

```kotlin
@Entity(tableName = "workout_results")
data class WorkoutResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeId: String,
    val routeName: String,          // denormalizowana — zapisywana przy odbiorze
    val activityType: String,       // ActivityType.name()
    val totalDistanceKm: Double,
    val totalDurationSeconds: Long,
    val laps: Int,
    val timestamp: Long             // System.currentTimeMillis() przy odbiorze
)
```

Tempo obliczane on-the-fly w ViewModel: `durationSeconds / 60.0 / distanceKm` → formatowane jako `mm:ss`.

### WorkoutResultDao

```kotlin
@Dao
interface WorkoutResultDao {
    @Query("SELECT * FROM workout_results ORDER BY timestamp DESC")
    fun getAllAsFlow(): Flow<List<WorkoutResultEntity>>

    @Query("SELECT * FROM workout_results WHERE id = :id")
    suspend fun getById(id: Long): WorkoutResultEntity?

    @Insert
    suspend fun insert(entity: WorkoutResultEntity): Long
}
```

### AppDatabase

```kotlin
@Database(entities = [WorkoutResultEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutResultDao(): WorkoutResultDao
}
```

Singleton przez `AppModule` (telefon).

### WorkoutResultRepository

```kotlin
interface WorkoutResultRepository {
    fun getHistory(): Flow<List<WorkoutResultEntity>>
    suspend fun getById(id: Long): WorkoutResultEntity?
    suspend fun save(entity: WorkoutResultEntity): Long
}
```

### UseCases

- `GetWorkoutHistoryUseCase`: `invoke(): Flow<List<WorkoutResultEntity>>`
- `GetWorkoutResultByIdUseCase`: `invoke(id: Long): WorkoutResultEntity?`
- `SaveWorkoutResultUseCase`: `invoke(entity: WorkoutResultEntity): Long`

## Odbiór danych z zegarka — app (phone)

### WorkoutResultReceiver (WearableListenerService)

Nowy plik: `app/src/main/java/.../sync/WorkoutResultReceiver.kt`

Nasłuchuje na ścieżce `/workout/result`. Przy odebraniu:
1. Deserializuje JSON do `WorkoutResult` (istniejący model z library:data)
2. Pobiera `Route` po `routeId` przez `RouteLocalDataSource` (w celu uzyskania nazwy i typu)
3. Buduje `WorkoutResultEntity` z `timestamp = System.currentTimeMillis()`
4. Zapisuje przez `SaveWorkoutResultUseCase`

Rejestracja w `AndroidManifest.xml` (phone) z `intent-filter` dla `com.google.android.gms.wearable.DATA_CHANGED`.

## Ekrany — app (phone)

### HomeScreen

`app/src/main/java/.../presentation/home/HomeScreen.kt`

```kotlin
@Composable
fun HomeScreen(
    onNavigateToRouteList: (ActivityType) -> Unit,
    onNavigateToHistoryDetail: (Long) -> Unit
)
```

`HomeScreen` ma własny `Scaffold` z `topBar` zawierającym tylko `TabRow` (bez osobnego `TopAppBar` z tytułem). Stan aktywnego taba: `var selectedTab by remember { mutableIntStateOf(0) }`.

Zawartość tabów:
- **Tab 0 — Trasy**: wyświetla `ActivityTypeContent` — nowa bezscaffoldowa wersja zawartości `ActivityTypeScreen`. Istniejący `ActivityTypeScreen` zostaje przepisany tak, by wydzielić `ActivityTypeContent(@Composable, bez Scaffold)` wywoływane przez `HomeScreen`, a pełny `ActivityTypeScreen` (ze Scaffoldem) usunięty — `HomeScreen` przejmuje jego rolę jako ekranu startowego.
- **Tab 1 — Historia**: wyświetla `HistoryContent` — analogicznie bezscaffoldowa zawartość listy historii.

Stan tabów zarządzany przez `HorizontalPager` lub prostą `when(selectedTab)` z animacją `AnimatedContent`.

### HistoryScreen

`app/src/main/java/.../presentation/history/HistoryScreen.kt`

Lista aktywności jako `LazyColumn`. Każdy element to karta (Opcja A z mockupu):
- Lewa krawędź czerwona (`border-left`)
- Nazwa trasy + data/typ aktywności
- Trzy metryki w rzędzie: czas / tempo / okrążenia
- Dystans w prawym górnym rogu (czerwony)

Stany: `isLoading` → `CircularProgressIndicator`; pusta lista → `Text("Brak aktywności")`.

MVI: `HistoryViewState(items: List<WorkoutResultEntity>, isLoading: Boolean)`, `HistoryViewModel`.

### HistoryDetailScreen

`app/src/main/java/.../presentation/historydetail/HistoryDetailScreen.kt`

Nawigacja: `history_detail/{workoutId}` (Long).

Zawartość (zgodnie z mockupem):
- `TopAppBar` z nazwą trasy + strzałka wstecz
- Data/godzina + badge typu aktywności
- `GoogleMap` z `Polyline` trasy (punkty z `Route` pobrane po `routeId`)
- Siatka 2×2 kafelków: Dystans / Czas / Tempo / Okrążenia

MVI: `HistoryDetailViewState(entity: WorkoutResultEntity?, route: Route?, isLoading: Boolean, error: String?)`, `HistoryDetailViewModel`.

## Zmiany w istniejących plikach

| Plik | Zmiana |
|------|--------|
| `MainActivity.kt` | `startDestination = "home"`, nowe composable `home`, `history_detail/{workoutId}` |
| `AppModule.kt` | Dodanie `AppDatabase`, `WorkoutResultDao`, `WorkoutResultRepository`, `WorkoutResultLocalDataSource`, 3 nowe UseCases |
| `AndroidManifest.xml` (phone) | Rejestracja `WorkoutResultReceiver` |
| `app/build.gradle.kts` | Dodanie Room dependencies |
| `gradle/libs.versions.toml` | `room = "2.6.1"`, `room-runtime`, `room-ktx`, `room-compiler` |

## Nowe pliki

```
library/data/
  model/WorkoutResultEntity.kt
  datasource/WorkoutResultLocalDataSource.kt
  repository/WorkoutResultRepository.kt  (interface)
  repository/WorkoutResultRepositoryImpl.kt
  usecase/GetWorkoutHistoryUseCase.kt
  usecase/GetWorkoutResultByIdUseCase.kt
  usecase/SaveWorkoutResultUseCase.kt

app/
  sync/WorkoutResultReceiver.kt
  presentation/home/HomeScreen.kt
  presentation/history/HistoryViewState.kt
  presentation/history/HistorySideEffect.kt
  presentation/history/HistoryViewModel.kt
  presentation/history/HistoryScreen.kt
  presentation/historydetail/HistoryDetailViewState.kt
  presentation/historydetail/HistoryDetailSideEffect.kt
  presentation/historydetail/HistoryDetailViewModel.kt
  presentation/historydetail/HistoryDetailScreen.kt
```

## Dao (Room)

```kotlin
@Dao
interface WorkoutResultDao {
    @Query("SELECT * FROM workout_results ORDER BY timestamp DESC")
    fun getAllAsFlow(): Flow<List<WorkoutResultEntity>>

    @Query("SELECT * FROM workout_results WHERE id = :id")
    suspend fun getById(id: Long): WorkoutResultEntity?

    @Insert
    suspend fun insert(entity: WorkoutResultEntity): Long
}
```

## Weryfikacja

1. `./gradlew :library:data:assembleDebug` — kompiluje się
2. `./gradlew :app:assembleDebug` — kompiluje się
3. Phone emulator: ekran główny ma dwa taby → Tab Historia pusta → Tab Trasy działa jak dotychczas
4. Po wysłaniu wyniku z Wear OS emulatora przez Data Layer → wpis pojawia się w historii → tap → szczegóły z mapą i metrykami
