# TabRow + Historia aktywności — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Dodać TabRow (Trasy / Historia) do aplikacji mobilnej i ekran historii aktywności odbieranych z Wear OS, zapisywanych w Room DB.

**Architecture:** Room DB żyje wyłącznie w module `:app` (telefon). `library:data` pozostaje bez zmian — nie dodajemy do niego Room. `WorkoutResultEntity`, `WorkoutResultDao`, `AppDatabase`, `WorkoutResultRepository` i UseCasy historii trafiają do `:app`. MVI (MviViewModel) na każdym nowym ekranie.

**Tech Stack:** Jetpack Compose, Room 2.7.1 + KSP, WearableListenerService, Google Maps Compose, MVI (MviViewModel z :core:architecture), kotlinx.serialization

---

## Mapa plików

| Akcja | Ścieżka |
|-------|---------|
| Modify | `app/build.gradle.kts` — KSP plugin + Room deps |
| Create | `app/src/main/java/com/miquido/stravapoc/data/db/WorkoutResultEntity.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/data/db/WorkoutResultDao.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/data/db/AppDatabase.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/data/repository/WorkoutResultRepository.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/data/repository/WorkoutResultRepositoryImpl.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/data/usecase/GetWorkoutHistoryUseCase.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/data/usecase/GetWorkoutResultByIdUseCase.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/data/usecase/SaveWorkoutResultUseCase.kt` |
| Modify | `app/src/main/java/com/miquido/stravapoc/di/AppModule.kt` — dodanie Room + nowych UseCases |
| Create | `app/src/main/java/com/miquido/stravapoc/sync/WorkoutResultReceiver.kt` |
| Modify | `app/src/main/AndroidManifest.xml` — rejestracja WorkoutResultReceiver |
| Modify | `app/src/main/java/com/miquido/stravapoc/presentation/activitytype/ActivityTypeScreen.kt` — wydzielenie ActivityTypeContent |
| Create | `app/src/main/java/com/miquido/stravapoc/presentation/home/HomeScreen.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/presentation/history/HistoryViewState.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/presentation/history/HistorySideEffect.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/presentation/history/HistoryViewModel.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/presentation/history/HistoryScreen.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/presentation/historydetail/HistoryDetailViewState.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/presentation/historydetail/HistoryDetailSideEffect.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/presentation/historydetail/HistoryDetailViewModel.kt` |
| Create | `app/src/main/java/com/miquido/stravapoc/presentation/historydetail/HistoryDetailScreen.kt` |
| Modify | `app/src/main/java/com/miquido/stravapoc/MainActivity.kt` — nowy nav graf |

---

### Task 1: Dodanie Room + KSP do app/build.gradle.kts

**Files:**
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Zaktualizuj app/build.gradle.kts**

Zastąp blok `plugins` i `dependencies` w `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}
```

W bloku `dependencies` dopisz po `implementation(libs.kotlinx.coroutines.play.services)`:

```kotlin
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
```

- [ ] **Step 2: Sprawdź że projekt się kompiluje**

```
./gradlew :app:assembleDebug
```

Oczekiwany wynik: `BUILD SUCCESSFUL`

---

### Task 2: WorkoutResultEntity, WorkoutResultDao, AppDatabase

**Files:**
- Create: `app/src/main/java/com/miquido/stravapoc/data/db/WorkoutResultEntity.kt`
- Create: `app/src/main/java/com/miquido/stravapoc/data/db/WorkoutResultDao.kt`
- Create: `app/src/main/java/com/miquido/stravapoc/data/db/AppDatabase.kt`

- [ ] **Step 1: Utwórz WorkoutResultEntity.kt**

```kotlin
package com.miquido.stravapoc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_results")
data class WorkoutResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeId: String,
    val routeName: String,
    val activityType: String,          // ActivityType.name
    val totalDistanceKm: Double,
    val totalDurationSeconds: Long,
    val laps: Int,
    val timestamp: Long                // System.currentTimeMillis() przy odbiorze
)
```

- [ ] **Step 2: Utwórz WorkoutResultDao.kt**

```kotlin
package com.miquido.stravapoc.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

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

- [ ] **Step 3: Utwórz AppDatabase.kt**

```kotlin
package com.miquido.stravapoc.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WorkoutResultEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutResultDao(): WorkoutResultDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stravapoc_db"
                ).build().also { INSTANCE = it }
            }
    }
}
```

- [ ] **Step 4: Sprawdź kompilację**

```
./gradlew :app:assembleDebug
```

Oczekiwany wynik: `BUILD SUCCESSFUL`

---

### Task 3: WorkoutResultRepository + 3 UseCases

**Files:**
- Create: `app/src/main/java/com/miquido/stravapoc/data/repository/WorkoutResultRepository.kt`
- Create: `app/src/main/java/com/miquido/stravapoc/data/repository/WorkoutResultRepositoryImpl.kt`
- Create: `app/src/main/java/com/miquido/stravapoc/data/usecase/GetWorkoutHistoryUseCase.kt`
- Create: `app/src/main/java/com/miquido/stravapoc/data/usecase/GetWorkoutResultByIdUseCase.kt`
- Create: `app/src/main/java/com/miquido/stravapoc/data/usecase/SaveWorkoutResultUseCase.kt`

- [ ] **Step 1: Utwórz WorkoutResultRepository.kt (interface)**

```kotlin
package com.miquido.stravapoc.data.repository

import com.miquido.stravapoc.data.db.WorkoutResultEntity
import kotlinx.coroutines.flow.Flow

interface WorkoutResultRepository {
    fun getHistory(): Flow<List<WorkoutResultEntity>>
    suspend fun getById(id: Long): WorkoutResultEntity?
    suspend fun save(entity: WorkoutResultEntity): Long
}
```

- [ ] **Step 2: Utwórz WorkoutResultRepositoryImpl.kt**

```kotlin
package com.miquido.stravapoc.data.repository

import com.miquido.stravapoc.data.db.WorkoutResultDao
import com.miquido.stravapoc.data.db.WorkoutResultEntity
import kotlinx.coroutines.flow.Flow

class WorkoutResultRepositoryImpl(
    private val dao: WorkoutResultDao
) : WorkoutResultRepository {
    override fun getHistory(): Flow<List<WorkoutResultEntity>> = dao.getAllAsFlow()
    override suspend fun getById(id: Long): WorkoutResultEntity? = dao.getById(id)
    override suspend fun save(entity: WorkoutResultEntity): Long = dao.insert(entity)
}
```

- [ ] **Step 3: Utwórz GetWorkoutHistoryUseCase.kt**

```kotlin
package com.miquido.stravapoc.data.usecase

import com.miquido.stravapoc.data.db.WorkoutResultEntity
import com.miquido.stravapoc.data.repository.WorkoutResultRepository
import kotlinx.coroutines.flow.Flow

class GetWorkoutHistoryUseCase(private val repository: WorkoutResultRepository) {
    operator fun invoke(): Flow<List<WorkoutResultEntity>> = repository.getHistory()
}
```

- [ ] **Step 4: Utwórz GetWorkoutResultByIdUseCase.kt**

```kotlin
package com.miquido.stravapoc.data.usecase

import com.miquido.stravapoc.data.db.WorkoutResultEntity
import com.miquido.stravapoc.data.repository.WorkoutResultRepository

class GetWorkoutResultByIdUseCase(private val repository: WorkoutResultRepository) {
    suspend operator fun invoke(id: Long): WorkoutResultEntity? = repository.getById(id)
}
```

- [ ] **Step 5: Utwórz SaveWorkoutResultUseCase.kt**

```kotlin
package com.miquido.stravapoc.data.usecase

import com.miquido.stravapoc.data.db.WorkoutResultEntity
import com.miquido.stravapoc.data.repository.WorkoutResultRepository

class SaveWorkoutResultUseCase(private val repository: WorkoutResultRepository) {
    suspend operator fun invoke(entity: WorkoutResultEntity): Long = repository.save(entity)
}
```

- [ ] **Step 6: Sprawdź kompilację**

```
./gradlew :app:assembleDebug
```

Oczekiwany wynik: `BUILD SUCCESSFUL`

---

### Task 4: Aktualizacja AppModule

**Files:**
- Modify: `app/src/main/java/com/miquido/stravapoc/di/AppModule.kt`

- [ ] **Step 1: Zastąp całą zawartość AppModule.kt**

```kotlin
package com.miquido.stravapoc.di

import android.content.Context
import com.miquido.stravapoc.data.db.AppDatabase
import com.miquido.stravapoc.data.repository.WorkoutResultRepositoryImpl
import com.miquido.stravapoc.data.usecase.GetWorkoutHistoryUseCase
import com.miquido.stravapoc.data.usecase.GetWorkoutResultByIdUseCase
import com.miquido.stravapoc.data.usecase.SaveWorkoutResultUseCase
import com.miquido.stravapoc.library.data.datasource.RouteLocalDataSource
import com.miquido.stravapoc.library.data.repository.RouteRepository
import com.miquido.stravapoc.library.data.repository.RouteRepositoryImpl
import com.miquido.stravapoc.library.data.usecase.GetRouteByIdUseCase
import com.miquido.stravapoc.library.data.usecase.GetRoutesUseCase

object AppModule {
    private val routeDataSource = RouteLocalDataSource()
    private val routeRepository: RouteRepository = RouteRepositoryImpl(routeDataSource)

    val getRoutesUseCase = GetRoutesUseCase(routeRepository)
    val getRouteByIdUseCase = GetRouteByIdUseCase(routeRepository)

    fun provideWorkoutUseCases(context: Context): WorkoutUseCases {
        val db = AppDatabase.getInstance(context)
        val repo = WorkoutResultRepositoryImpl(db.workoutResultDao())
        return WorkoutUseCases(
            getHistory = GetWorkoutHistoryUseCase(repo),
            getById = GetWorkoutResultByIdUseCase(repo),
            save = SaveWorkoutResultUseCase(repo)
        )
    }
}

data class WorkoutUseCases(
    val getHistory: GetWorkoutHistoryUseCase,
    val getById: GetWorkoutResultByIdUseCase,
    val save: SaveWorkoutResultUseCase
)
```

- [ ] **Step 2: Sprawdź kompilację**

```
./gradlew :app:assembleDebug
```

Oczekiwany wynik: `BUILD SUCCESSFUL`

---

### Task 5: WorkoutResultReceiver + AndroidManifest

**Files:**
- Create: `app/src/main/java/com/miquido/stravapoc/sync/WorkoutResultReceiver.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Utwórz WorkoutResultReceiver.kt**

```kotlin
package com.miquido.stravapoc.sync

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.miquido.stravapoc.data.db.WorkoutResultEntity
import com.miquido.stravapoc.di.AppModule
import com.miquido.stravapoc.library.data.datasource.RouteLocalDataSource
import com.miquido.stravapoc.library.data.model.WorkoutResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class WorkoutResultReceiver : WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val routeDataSource = RouteLocalDataSource()

    override fun onDataChanged(events: DataEventBuffer) {
        events
            .filter { it.type == DataEvent.TYPE_CHANGED && it.dataItem.uri.path == "/workout/result" }
            .forEach { event ->
                val json = DataMapItem.fromDataItem(event.dataItem)
                    .dataMap.getString("result_json") ?: return@forEach
                val result = runCatching { Json.decodeFromString<WorkoutResult>(json) }
                    .getOrNull() ?: return@forEach

                val route = routeDataSource.getRouteById(result.routeId)
                val entity = WorkoutResultEntity(
                    routeId = result.routeId,
                    routeName = route?.name ?: result.routeId,
                    activityType = route?.activityType?.name ?: "RUNNING",
                    totalDistanceKm = result.totalDistanceKm,
                    totalDurationSeconds = result.totalDurationSeconds,
                    laps = result.laps,
                    timestamp = System.currentTimeMillis()
                )

                scope.launch {
                    AppModule.provideWorkoutUseCases(applicationContext).save(entity)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
```

- [ ] **Step 2: Zarejestruj WorkoutResultReceiver w AndroidManifest.xml**

Dodaj wewnątrz `<application>` (przed zamknięciem `</application>`):

```xml
        <service
            android:name=".sync.WorkoutResultReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="com.google.android.gms.wearable.DATA_CHANGED" />
                <data
                    android:host="*"
                    android:pathPrefix="/workout/result"
                    android:scheme="wear" />
            </intent-filter>
        </service>
```

- [ ] **Step 3: Sprawdź kompilację**

```
./gradlew :app:assembleDebug
```

Oczekiwany wynik: `BUILD SUCCESSFUL`

---

### Task 6: Refaktoryzacja ActivityTypeScreen — wydzielenie ActivityTypeContent

**Files:**
- Modify: `app/src/main/java/com/miquido/stravapoc/presentation/activitytype/ActivityTypeScreen.kt`

Obecny `ActivityTypeScreen` ma własny `Scaffold` + `TopAppBar`. `HomeScreen` będzie go osadzać bez Scaffold — trzeba wydzielić `ActivityTypeContent`.

- [ ] **Step 1: Zastąp całą zawartość ActivityTypeScreen.kt**

```kotlin
package com.miquido.stravapoc.presentation.activitytype

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miquido.stravapoc.library.data.model.ActivityType

@Composable
fun ActivityTypeContent(
    onNavigateToRouteList: (ActivityType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivityTypeViewModel = viewModel(factory = ActivityTypeViewModel.Factory)
) {
    val state by viewModel.viewState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.collectSideEffect<ActivityTypeSideEffect>(this) { effect ->
            when (effect) {
                is ActivityTypeSideEffect.NavigateToRouteList ->
                    onNavigateToRouteList(effect.activityType)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        state.activityTypes.forEach { activityType ->
            ActivityTypeCard(
                activityType = activityType,
                onClick = { viewModel.onActivitySelected(activityType) }
            )
        }
    }
}

@Composable
internal fun ActivityTypeCard(
    activityType: ActivityType,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = activityType.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = activityType.displayName,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

internal val ActivityType.displayName: String
    get() = when (this) {
        ActivityType.RUNNING -> "Bieganie"
        ActivityType.CYCLING -> "Rower"
        ActivityType.WALKING -> "Chodzenie"
    }

internal val ActivityType.icon: ImageVector
    get() = when (this) {
        ActivityType.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
        ActivityType.CYCLING -> Icons.Filled.PedalBike
        ActivityType.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
    }
```

- [ ] **Step 2: Sprawdź kompilację**

```
./gradlew :app:assembleDebug
```

Oczekiwany wynik: `BUILD SUCCESSFUL`

---

### Task 7: HomeScreen z TabRow

**Files:**
- Create: `app/src/main/java/com/miquido/stravapoc/presentation/home/HomeScreen.kt`

- [ ] **Step 1: Utwórz HomeScreen.kt**

```kotlin
package com.miquido.stravapoc.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.presentation.activitytype.ActivityTypeContent
import com.miquido.stravapoc.presentation.history.HistoryContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRouteList: (ActivityType) -> Unit,
    onNavigateToHistoryDetail: (Long) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Trasy", "Historia")

    Scaffold(
        topBar = {
            Column {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = selectedTab,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            label = "tab_content"
        ) { tab ->
            when (tab) {
                0 -> ActivityTypeContent(onNavigateToRouteList = onNavigateToRouteList)
                1 -> HistoryContent(onNavigateToDetail = onNavigateToHistoryDetail)
            }
        }
    }
}
```

- [ ] **Step 2: Sprawdź kompilację** (HistoryContent jeszcze nie istnieje — błąd kompilacji jest oczekiwany)

```
./gradlew :app:assembleDebug 2>&1 | grep "error:"
```

Oczekiwany wynik: błąd `Unresolved reference: HistoryContent` — to jest poprawne, tworzymy go w następnym kroku.

---

### Task 8: Historia — MVI + Screen

**Files:**
- Create: `app/src/main/java/com/miquido/stravapoc/presentation/history/HistoryViewState.kt`
- Create: `app/src/main/java/com/miquido/stravapoc/presentation/history/HistorySideEffect.kt`
- Create: `app/src/main/java/com/miquido/stravapoc/presentation/history/HistoryViewModel.kt`
- Create: `app/src/main/java/com/miquido/stravapoc/presentation/history/HistoryScreen.kt`

- [ ] **Step 1: Utwórz HistoryViewState.kt**

```kotlin
package com.miquido.stravapoc.presentation.history

import com.miquido.stravapoc.data.db.WorkoutResultEntity

data class HistoryViewState(
    val items: List<WorkoutResultEntity> = emptyList(),
    val isLoading: Boolean = true
)
```

- [ ] **Step 2: Utwórz HistorySideEffect.kt**

```kotlin
package com.miquido.stravapoc.presentation.history

sealed class HistorySideEffect {
    data class NavigateToDetail(val id: Long) : HistorySideEffect()
}
```

- [ ] **Step 3: Utwórz HistoryViewModel.kt**

```kotlin
package com.miquido.stravapoc.presentation.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.di.AppModule
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HistoryViewModel(
    application: Application
) : MviViewModel<HistoryViewState>(HistoryViewState(), MviDefaultConfig()) {

    private val useCases = AppModule.provideWorkoutUseCases(application)

    init {
        useCases.getHistory()
            .onEach { items ->
                transform { copy(items = items, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onItemSelected(id: Long) = launch {
        emitSideEffect(HistorySideEffect.NavigateToDetail(id))
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer { HistoryViewModel(application) }
        }
    }
}
```

- [ ] **Step 4: Utwórz HistoryScreen.kt**

```kotlin
package com.miquido.stravapoc.presentation.history

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miquido.stravapoc.data.db.WorkoutResultEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryContent(
    onNavigateToDetail: (Long) -> Unit,
    viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.factory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val state by viewModel.viewState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.collectSideEffect<HistorySideEffect>(this) { effect ->
            when (effect) {
                is HistorySideEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            state.items.isEmpty() -> Text(
                text = "Brak aktywności",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    HistoryItem(item = item, onClick = { viewModel.onItemSelected(item.id) })
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(item: WorkoutResultEntity, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // czerwona lewa krawędź
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .padding(vertical = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .then(
                            Modifier.padding(0.dp)
                        )
                )
            }
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
            ) {
                drawRect(color = androidx.compose.ui.graphics.Color(0xFFE8161B))
            }

            Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = item.routeName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${formatDate(item.timestamp)} • ${item.activityType.toDisplayName()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "%.1f km".format(item.totalDistanceKm),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    MetricLabel(value = formatDuration(item.totalDurationSeconds), label = "czas")
                    MetricLabel(value = formatPace(item.totalDistanceKm, item.totalDurationSeconds), label = "/km")
                    MetricLabel(value = item.laps.toString(), label = "okrążenia")
                }
            }
        }
    }
}

@Composable
private fun MetricLabel(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale("pl")).format(Date(timestamp))

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

private fun formatPace(distanceKm: Double, durationSeconds: Long): String {
    if (distanceKm == 0.0) return "--:--"
    val paceSeconds = (durationSeconds / distanceKm).toLong()
    val m = paceSeconds / 60
    val s = paceSeconds % 60
    return "%d:%02d".format(m, s)
}

private fun String.toDisplayName(): String = when (this) {
    "RUNNING" -> "Bieganie"
    "CYCLING" -> "Rower"
    "WALKING" -> "Chodzenie"
    else -> this
}
```

- [ ] **Step 5: Sprawdź kompilację**

```
./gradlew :app:assembleDebug
```

Oczekiwany wynik: `BUILD SUCCESSFUL`

---

### Task 9: HistoryDetail — MVI + Screen

**Files:**
- Create: `app/src/main/java/com/miquido/stravapoc/presentation/historydetail/HistoryDetailViewState.kt`
- Create: `app/src/main/java/com/miquido/stravapoc/presentation/historydetail/HistoryDetailSideEffect.kt`
- Create: `app/src/main/java/com/miquido/stravapoc/presentation/historydetail/HistoryDetailViewModel.kt`
- Create: `app/src/main/java/com/miquido/stravapoc/presentation/historydetail/HistoryDetailScreen.kt`

- [ ] **Step 1: Utwórz HistoryDetailViewState.kt**

```kotlin
package com.miquido.stravapoc.presentation.historydetail

import com.miquido.stravapoc.data.db.WorkoutResultEntity
import com.miquido.stravapoc.library.data.model.Route

data class HistoryDetailViewState(
    val entity: WorkoutResultEntity? = null,
    val route: Route? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
```

- [ ] **Step 2: Utwórz HistoryDetailSideEffect.kt**

```kotlin
package com.miquido.stravapoc.presentation.historydetail

sealed class HistoryDetailSideEffect
```

- [ ] **Step 3: Utwórz HistoryDetailViewModel.kt**

```kotlin
package com.miquido.stravapoc.presentation.historydetail

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.di.AppModule

class HistoryDetailViewModel(
    private val workoutId: Long,
    application: Application
) : MviViewModel<HistoryDetailViewState>(HistoryDetailViewState(), MviDefaultConfig()) {

    private val workoutUseCases = AppModule.provideWorkoutUseCases(application)

    init {
        loadData()
    }

    private fun loadData() = launch {
        transform { copy(isLoading = true, error = null) }
        val entity = workoutUseCases.getById(workoutId)
        if (entity == null) {
            transform { copy(isLoading = false, error = "Nie znaleziono aktywności") }
            return@launch
        }
        val route = runCatching {
            AppModule.getRouteByIdUseCase(entity.routeId).getOrNull()
        }.getOrNull()
        transform { copy(entity = entity, route = route, isLoading = false) }
    }

    companion object {
        fun factory(workoutId: Long, application: Application): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { HistoryDetailViewModel(workoutId, application) }
            }
    }
}
```

- [ ] **Step 4: Utwórz HistoryDetailScreen.kt**

```kotlin
package com.miquido.stravapoc.presentation.historydetail

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    workoutId: Long,
    onBack: () -> Unit,
    viewModel: HistoryDetailViewModel = viewModel(
        factory = HistoryDetailViewModel.factory(
            workoutId,
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val state by viewModel.viewState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.entity?.routeName ?: "Szczegóły aktywności") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    text = state.error!!,
                    modifier = Modifier.align(Alignment.Center)
                )
                state.entity != null -> {
                    val entity = state.entity!!
                    val points = state.route?.points?.map { LatLng(it.lat, it.lng) } ?: emptyList()
                    val center = points.firstOrNull() ?: LatLng(52.0, 21.0)
                    val cameraState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(center, 14f)
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Data + badge
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDateTime(entity.timestamp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = entity.activityType.toDisplayName(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Mapa
                        GoogleMap(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            cameraPositionState = cameraState
                        ) {
                            if (points.size >= 2) {
                                Polyline(points = points, width = 8f)
                            }
                        }

                        // Metryki 2x2
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricTile(
                                label = "Dystans",
                                value = "%.1f".format(entity.totalDistanceKm),
                                unit = "km",
                                modifier = Modifier.weight(1f)
                            )
                            MetricTile(
                                label = "Czas",
                                value = formatDuration(entity.totalDurationSeconds),
                                unit = "mm:ss",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricTile(
                                label = "Tempo",
                                value = formatPace(entity.totalDistanceKm, entity.totalDurationSeconds),
                                unit = "min/km",
                                modifier = Modifier.weight(1f)
                            )
                            MetricTile(
                                label = "Okrążenia",
                                value = entity.laps.toString(),
                                unit = "lap",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale("pl")).format(Date(timestamp))

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

private fun formatPace(distanceKm: Double, durationSeconds: Long): String {
    if (distanceKm == 0.0) return "--:--"
    val paceSeconds = (durationSeconds / distanceKm).toLong()
    val m = paceSeconds / 60
    val s = paceSeconds % 60
    return "%d:%02d".format(m, s)
}

private fun String.toDisplayName(): String = when (this) {
    "RUNNING" -> "Bieganie"
    "CYCLING" -> "Rower"
    "WALKING" -> "Chodzenie"
    else -> this
}
```

- [ ] **Step 5: Sprawdź kompilację**

```
./gradlew :app:assembleDebug
```

Oczekiwany wynik: `BUILD SUCCESSFUL`

---

### Task 10: Aktualizacja MainActivity — nowy nav graf

**Files:**
- Modify: `app/src/main/java/com/miquido/stravapoc/MainActivity.kt`

- [ ] **Step 1: Zastąp całą zawartość MainActivity.kt**

```kotlin
package com.miquido.stravapoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.presentation.historydetail.HistoryDetailScreen
import com.miquido.stravapoc.presentation.home.HomeScreen
import com.miquido.stravapoc.presentation.routedetail.RouteDetailScreen
import com.miquido.stravapoc.presentation.routelist.RouteListScreen
import com.miquido.stravapoc.ui.theme.StravaPocTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StravaPocTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onNavigateToRouteList = { activityType ->
                                navController.navigate("route_list/${activityType.name}")
                            },
                            onNavigateToHistoryDetail = { workoutId ->
                                navController.navigate("history_detail/$workoutId")
                            }
                        )
                    }
                    composable("route_list/{activityType}") { backStackEntry ->
                        val typeName = backStackEntry.arguments?.getString("activityType")
                            ?: ActivityType.RUNNING.name
                        val activityType = runCatching { ActivityType.valueOf(typeName) }
                            .getOrDefault(ActivityType.RUNNING)
                        RouteListScreen(
                            activityType = activityType,
                            onBack = { navController.popBackStack() },
                            onNavigateToDetail = { routeId ->
                                navController.navigate("route_detail/$routeId")
                            }
                        )
                    }
                    composable("route_detail/{routeId}") { backStackEntry ->
                        val routeId = backStackEntry.arguments?.getString("routeId")
                            ?: return@composable
                        RouteDetailScreen(
                            routeId = routeId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("history_detail/{workoutId}") { backStackEntry ->
                        val workoutId = backStackEntry.arguments?.getString("workoutId")
                            ?.toLongOrNull() ?: return@composable
                        HistoryDetailScreen(
                            workoutId = workoutId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Sprawdź pełny build**

```
./gradlew :app:assembleDebug
```

Oczekiwany wynik: `BUILD SUCCESSFUL`

- [ ] **Step 3: Sprawdź testy**

```
./gradlew :library:data:test
```

Oczekiwany wynik: `BUILD SUCCESSFUL` — wszystkie 5 testów przechodzi.

---

## Self-Review

**Spec coverage:**
- ✅ TabRow jako ekran główny (HomeScreen z TabRow)
- ✅ Tab 0 = Trasy (ActivityTypeContent bez Scaffold)
- ✅ Tab 1 = Historia (HistoryContent)
- ✅ Room DB w `:app` (WorkoutResultEntity, Dao, AppDatabase)
- ✅ WorkoutResultRepository + 3 UseCases
- ✅ WearableListenerService (WorkoutResultReceiver) z rejestracją w manifeście
- ✅ HistoryScreen — karty z krawędzią, metrykami, datą
- ✅ HistoryDetailScreen — mapa + 2×2 kafelki metryk
- ✅ Nawigacja `history_detail/{workoutId}` (Long)
- ✅ Tempo i czas formatowane on-the-fly w UI

**Placeholder scan:** brak TBD/TODO — wszystkie kroki mają kompletny kod.

**Type consistency:**
- `WorkoutResultEntity` używana spójnie w HistoryViewState, HistoryDetailViewState, ViewModel, Screen
- `HistoryViewModel.factory(application)` ← spójne z `viewModel(factory = HistoryViewModel.factory(...))`
- `WorkoutUseCases` data class z `getHistory`, `getById`, `save` — spójne ze wszystkimi miejscami użycia
