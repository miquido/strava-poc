# Activity Type Selection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an activity type selection screen (Bieganie / Rower / Chodzenie) as the entry point of the phone app, with the route list filtered by the selected type.

**Architecture:** New `ActivityType` enum lives in `:library:data` and is added to `MockRoute`. `GetRoutesUseCase` gains an optional type filter. Phone app gains a new `ActivityTypeScreen` as `startDestination`; `RouteListScreen` receives the type as a nav argument and shows only matching routes.

**Tech Stack:** Kotlin, Jetpack Compose, Material3, Navigation Compose, MVI (MviViewModel), kotlinx.serialization, JUnit4 + runBlocking for data-layer tests.

---

## File Map

| Action | File |
|--------|------|
| CREATE | `library/data/src/main/java/com/miquido/stravapoc/library/data/model/ActivityType.kt` |
| MODIFY | `library/data/src/main/java/com/miquido/stravapoc/library/data/model/MockRoute.kt` |
| MODIFY | `library/data/src/main/java/com/miquido/stravapoc/library/data/datasource/RouteLocalDataSource.kt` |
| MODIFY | `library/data/src/main/java/com/miquido/stravapoc/library/data/repository/RouteRepository.kt` |
| MODIFY | `library/data/src/main/java/com/miquido/stravapoc/library/data/repository/RouteRepositoryImpl.kt` |
| MODIFY | `library/data/src/main/java/com/miquido/stravapoc/library/data/usecase/GetRoutesUseCase.kt` |
| CREATE | `library/data/src/test/java/com/miquido/stravapoc/library/data/usecase/GetRoutesUseCaseTest.kt` |
| MODIFY | `gradle/libs.versions.toml` — add `material-icons-extended` |
| MODIFY | `app/build.gradle.kts` — add `material-icons-extended` dependency |
| CREATE | `app/src/main/java/com/miquido/stravapoc/presentation/activitytype/ActivityTypeViewState.kt` |
| CREATE | `app/src/main/java/com/miquido/stravapoc/presentation/activitytype/ActivityTypeSideEffect.kt` |
| CREATE | `app/src/main/java/com/miquido/stravapoc/presentation/activitytype/ActivityTypeViewModel.kt` |
| CREATE | `app/src/main/java/com/miquido/stravapoc/presentation/activitytype/ActivityTypeScreen.kt` |
| MODIFY | `app/src/main/java/com/miquido/stravapoc/presentation/routelist/RouteListViewState.kt` |
| MODIFY | `app/src/main/java/com/miquido/stravapoc/presentation/routelist/RouteListViewModel.kt` |
| MODIFY | `app/src/main/java/com/miquido/stravapoc/presentation/routelist/RouteListScreen.kt` |
| MODIFY | `app/src/main/java/com/miquido/stravapoc/MainActivity.kt` |

---

### Task 1: `ActivityType` enum + update `MockRoute`

**Files:**
- Create: `library/data/src/main/java/com/miquido/stravapoc/library/data/model/ActivityType.kt`
- Modify: `library/data/src/main/java/com/miquido/stravapoc/library/data/model/MockRoute.kt`

- [ ] **Step 1: Create `ActivityType.kt`**

```kotlin
// library/data/src/main/java/com/miquido/stravapoc/library/data/model/ActivityType.kt
package com.miquido.stravapoc.library.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ActivityType { RUNNING, CYCLING, WALKING }
```

- [ ] **Step 2: Add `activityType` field to `MockRoute`**

Replace the entire file:
```kotlin
// library/data/src/main/java/com/miquido/stravapoc/library/data/model/MockRoute.kt
package com.miquido.stravapoc.library.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MockRoute(
    val id: String,
    val name: String,
    val distanceKm: Double,
    val points: List<RoutePoint>,
    val activityType: ActivityType
)
```

- [ ] **Step 3: Verify the build fails with expected errors only**

Run: `./gradlew :library:data:compileDebugKotlin 2>&1 | grep -E "error:|ERROR"`

Expected: errors in `RouteLocalDataSource.kt` because existing `MockRoute(...)` calls are missing `activityType`. No errors in `MockRoute.kt` or `ActivityType.kt`.

---

### Task 2: Update `RouteLocalDataSource` — add types + 6 new routes + filter

**Files:**
- Modify: `library/data/src/main/java/com/miquido/stravapoc/library/data/datasource/RouteLocalDataSource.kt`

- [ ] **Step 1: Replace `RouteLocalDataSource.kt` with full updated version**

```kotlin
package com.miquido.stravapoc.library.data.datasource

import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.MockRoute
import com.miquido.stravapoc.library.data.model.RoutePoint

class RouteLocalDataSource {

    fun getRoutes(type: ActivityType? = null): List<MockRoute> {
        val all = allRoutes()
        return if (type == null) all else all.filter { it.activityType == type }
    }

    fun getRouteById(id: String): MockRoute? = allRoutes().find { it.id == id }

    private fun allRoutes(): List<MockRoute> = listOf(
        // RUNNING
        MockRoute(
            id = "warsaw_lazienki",
            name = "Warszawa — Łazienki",
            distanceKm = 5.2,
            activityType = ActivityType.RUNNING,
            points = warsawLazienkiPoints()
        ),
        MockRoute(
            id = "krakow_planty",
            name = "Kraków — Planty",
            distanceKm = 4.0,
            activityType = ActivityType.RUNNING,
            points = krakowPlantyPoints()
        ),
        MockRoute(
            id = "trojmiejski_park",
            name = "Trójmiejski Park Krajobrazowy",
            distanceKm = 7.1,
            activityType = ActivityType.RUNNING,
            points = trojmiejskiPoints()
        ),
        // CYCLING
        MockRoute(
            id = "wroclaw_odra",
            name = "Wrocław — Odra",
            distanceKm = 6.5,
            activityType = ActivityType.CYCLING,
            points = wroclawOdraPoints()
        ),
        MockRoute(
            id = "wisla_rowerowa",
            name = "Wisła — Trasa Rowerowa",
            distanceKm = 12.0,
            activityType = ActivityType.CYCLING,
            points = wislaRowerowaPoints()
        ),
        MockRoute(
            id = "mazury_petla",
            name = "Mazury — Wielka Pętla",
            distanceKm = 18.0,
            activityType = ActivityType.CYCLING,
            points = mazuryPoints()
        ),
        // WALKING
        MockRoute(
            id = "tatry_giewont",
            name = "Tatry — Giewont",
            distanceKm = 8.3,
            activityType = ActivityType.WALKING,
            points = tatryGiewontPoints()
        ),
        MockRoute(
            id = "bieszczady_wetlinska",
            name = "Bieszczady — Połonina Wetlińska",
            distanceKm = 9.0,
            activityType = ActivityType.WALKING,
            points = bieszczadyPoints()
        ),
        MockRoute(
            id = "beskid_barania",
            name = "Beskid Śląski — Barania Góra",
            distanceKm = 5.5,
            activityType = ActivityType.WALKING,
            points = beskidPoints()
        )
    )

    private fun warsawLazienkiPoints() = listOf(
        RoutePoint(52.2155, 21.0355), RoutePoint(52.2163, 21.0367),
        RoutePoint(52.2172, 21.0378), RoutePoint(52.2181, 21.0389),
        RoutePoint(52.2190, 21.0398), RoutePoint(52.2199, 21.0405),
        RoutePoint(52.2208, 21.0410), RoutePoint(52.2215, 21.0412),
        RoutePoint(52.2220, 21.0408), RoutePoint(52.2223, 21.0400),
        RoutePoint(52.2220, 21.0390), RoutePoint(52.2214, 21.0382),
        RoutePoint(52.2205, 21.0375), RoutePoint(52.2195, 21.0368),
        RoutePoint(52.2185, 21.0360), RoutePoint(52.2175, 21.0355),
        RoutePoint(52.2165, 21.0352), RoutePoint(52.2155, 21.0355)
    )

    private fun krakowPlantyPoints() = listOf(
        RoutePoint(50.0614, 19.9383), RoutePoint(50.0625, 19.9370),
        RoutePoint(50.0638, 19.9358), RoutePoint(50.0650, 19.9348),
        RoutePoint(50.0660, 19.9340), RoutePoint(50.0668, 19.9335),
        RoutePoint(50.0672, 19.9332), RoutePoint(50.0668, 19.9325),
        RoutePoint(50.0660, 19.9318), RoutePoint(50.0648, 19.9313),
        RoutePoint(50.0635, 19.9312), RoutePoint(50.0622, 19.9318),
        RoutePoint(50.0612, 19.9328), RoutePoint(50.0607, 19.9340),
        RoutePoint(50.0608, 19.9355), RoutePoint(50.0614, 19.9370),
        RoutePoint(50.0614, 19.9383)
    )

    private fun trojmiejskiPoints() = listOf(
        RoutePoint(54.4520, 18.5350), RoutePoint(54.4535, 18.5365),
        RoutePoint(54.4550, 18.5380), RoutePoint(54.4568, 18.5392),
        RoutePoint(54.4585, 18.5400), RoutePoint(54.4600, 18.5405),
        RoutePoint(54.4612, 18.5398), RoutePoint(54.4620, 18.5385),
        RoutePoint(54.4618, 18.5368), RoutePoint(54.4608, 18.5352),
        RoutePoint(54.4592, 18.5340), RoutePoint(54.4575, 18.5335),
        RoutePoint(54.4558, 18.5338), RoutePoint(54.4540, 18.5345),
        RoutePoint(54.4525, 18.5352), RoutePoint(54.4520, 18.5350)
    )

    private fun wroclawOdraPoints() = listOf(
        RoutePoint(51.1100, 17.0320), RoutePoint(51.1112, 17.0335),
        RoutePoint(51.1125, 17.0350), RoutePoint(51.1138, 17.0368),
        RoutePoint(51.1150, 17.0385), RoutePoint(51.1162, 17.0400),
        RoutePoint(51.1172, 17.0415), RoutePoint(51.1180, 17.0428),
        RoutePoint(51.1185, 17.0442), RoutePoint(51.1188, 17.0456),
        RoutePoint(51.1185, 17.0470), RoutePoint(51.1178, 17.0482),
        RoutePoint(51.1168, 17.0490), RoutePoint(51.1156, 17.0495),
        RoutePoint(51.1143, 17.0492), RoutePoint(51.1130, 17.0485),
        RoutePoint(51.1118, 17.0475), RoutePoint(51.1108, 17.0462),
        RoutePoint(51.1100, 17.0448), RoutePoint(51.1095, 17.0432),
        RoutePoint(51.1095, 17.0416), RoutePoint(51.1100, 17.0402),
        RoutePoint(51.1100, 17.0320)
    )

    private fun wislaRowerowaPoints() = listOf(
        RoutePoint(50.0560, 20.0200), RoutePoint(50.0548, 20.0230),
        RoutePoint(50.0535, 20.0262), RoutePoint(50.0520, 20.0295),
        RoutePoint(50.0505, 20.0330), RoutePoint(50.0490, 20.0368),
        RoutePoint(50.0475, 20.0405), RoutePoint(50.0460, 20.0442),
        RoutePoint(50.0445, 20.0480), RoutePoint(50.0430, 20.0518),
        RoutePoint(50.0415, 20.0555), RoutePoint(50.0400, 20.0590),
        RoutePoint(50.0385, 20.0625)
    )

    private fun mazuryPoints() = listOf(
        RoutePoint(53.8500, 21.5700), RoutePoint(53.8530, 21.5760),
        RoutePoint(53.8565, 21.5820), RoutePoint(53.8600, 21.5880),
        RoutePoint(53.8640, 21.5930), RoutePoint(53.8680, 21.5970),
        RoutePoint(53.8720, 21.5995), RoutePoint(53.8760, 21.6005),
        RoutePoint(53.8795, 21.5995), RoutePoint(53.8825, 21.5970),
        RoutePoint(53.8845, 21.5935), RoutePoint(53.8852, 21.5892),
        RoutePoint(53.8845, 21.5850), RoutePoint(53.8825, 21.5812),
        RoutePoint(53.8800, 21.5780), RoutePoint(53.8770, 21.5755),
        RoutePoint(53.8735, 21.5738), RoutePoint(53.8695, 21.5730),
        RoutePoint(53.8655, 21.5730), RoutePoint(53.8615, 21.5738),
        RoutePoint(53.8575, 21.5752), RoutePoint(53.8540, 21.5770),
        RoutePoint(53.8510, 21.5790), RoutePoint(53.8500, 21.5700)
    )

    private fun tatryGiewontPoints() = listOf(
        RoutePoint(49.2720, 19.9800), RoutePoint(49.2700, 19.9788),
        RoutePoint(49.2682, 19.9772), RoutePoint(49.2665, 19.9755),
        RoutePoint(49.2648, 19.9738), RoutePoint(49.2632, 19.9720),
        RoutePoint(49.2618, 19.9700), RoutePoint(49.2605, 19.9680),
        RoutePoint(49.2592, 19.9658), RoutePoint(49.2580, 19.9635),
        RoutePoint(49.2568, 19.9612), RoutePoint(49.2558, 19.9588),
        RoutePoint(49.2550, 19.9562)
    )

    private fun bieszczadyPoints() = listOf(
        RoutePoint(49.1400, 22.5500), RoutePoint(49.1382, 22.5532),
        RoutePoint(49.1365, 22.5565), RoutePoint(49.1348, 22.5598),
        RoutePoint(49.1332, 22.5632), RoutePoint(49.1318, 22.5668),
        RoutePoint(49.1305, 22.5705), RoutePoint(49.1295, 22.5742),
        RoutePoint(49.1285, 22.5780), RoutePoint(49.1278, 22.5818),
        RoutePoint(49.1272, 22.5856), RoutePoint(49.1268, 22.5895)
    )

    private fun beskidPoints() = listOf(
        RoutePoint(49.6500, 18.8800), RoutePoint(49.6518, 18.8832),
        RoutePoint(49.6535, 18.8865), RoutePoint(49.6550, 18.8900),
        RoutePoint(49.6562, 18.8935), RoutePoint(49.6572, 18.8970),
        RoutePoint(49.6578, 18.9005), RoutePoint(49.6582, 18.9040),
        RoutePoint(49.6582, 18.9075), RoutePoint(49.6578, 18.9108)
    )
}
```

- [ ] **Step 2: Build `:library:data`**

Run: `./gradlew :library:data:assembleDebug 2>&1 | tail -5`

Expected: `BUILD SUCCESSFUL`

---

### Task 3: Update Repository interface + impl + UseCase

**Files:**
- Modify: `library/data/src/main/java/com/miquido/stravapoc/library/data/repository/RouteRepository.kt`
- Modify: `library/data/src/main/java/com/miquido/stravapoc/library/data/repository/RouteRepositoryImpl.kt`
- Modify: `library/data/src/main/java/com/miquido/stravapoc/library/data/usecase/GetRoutesUseCase.kt`

- [ ] **Step 1: Update `RouteRepository` interface**

```kotlin
package com.miquido.stravapoc.library.data.repository

import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.MockRoute

interface RouteRepository {
    suspend fun getRoutes(type: ActivityType? = null): Result<List<MockRoute>>
    suspend fun getRouteById(id: String): Result<MockRoute>
}
```

- [ ] **Step 2: Update `RouteRepositoryImpl`**

```kotlin
package com.miquido.stravapoc.library.data.repository

import com.miquido.stravapoc.library.data.datasource.RouteLocalDataSource
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.MockRoute

class RouteRepositoryImpl(
    private val dataSource: RouteLocalDataSource = RouteLocalDataSource()
) : RouteRepository {

    override suspend fun getRoutes(type: ActivityType?): Result<List<MockRoute>> =
        runCatching { dataSource.getRoutes(type) }

    override suspend fun getRouteById(id: String): Result<MockRoute> =
        runCatching { dataSource.getRouteById(id) ?: error("Route not found: $id") }
}
```

- [ ] **Step 3: Update `GetRoutesUseCase`**

```kotlin
package com.miquido.stravapoc.library.data.usecase

import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.MockRoute
import com.miquido.stravapoc.library.data.repository.RouteRepository

class GetRoutesUseCase(private val repository: RouteRepository) {
    suspend operator fun invoke(type: ActivityType? = null): Result<List<MockRoute>> =
        repository.getRoutes(type)
}
```

- [ ] **Step 4: Build `:library:data`**

Run: `./gradlew :library:data:assembleDebug 2>&1 | tail -5`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit data layer changes**

```bash
git add library/data/
git commit -m "feat: add ActivityType to library:data, filter GetRoutesUseCase by type"
```

---

### Task 4: Unit tests for `GetRoutesUseCase`

**Files:**
- Create: `library/data/src/test/java/com/miquido/stravapoc/library/data/usecase/GetRoutesUseCaseTest.kt`

- [ ] **Step 1: Create test directory if needed**

```bash
mkdir -p library/data/src/test/java/com/miquido/stravapoc/library/data/usecase
```

- [ ] **Step 2: Write the test**

```kotlin
package com.miquido.stravapoc.library.data.usecase

import com.miquido.stravapoc.library.data.datasource.RouteLocalDataSource
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.repository.RouteRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetRoutesUseCaseTest {

    private val useCase = GetRoutesUseCase(RouteRepositoryImpl(RouteLocalDataSource()))

    @Test
    fun `returns only RUNNING routes when type is RUNNING`() = runBlocking {
        val routes = useCase(ActivityType.RUNNING).getOrThrow()
        assertTrue(routes.isNotEmpty())
        assertTrue(routes.all { it.activityType == ActivityType.RUNNING })
    }

    @Test
    fun `returns only CYCLING routes when type is CYCLING`() = runBlocking {
        val routes = useCase(ActivityType.CYCLING).getOrThrow()
        assertTrue(routes.isNotEmpty())
        assertTrue(routes.all { it.activityType == ActivityType.CYCLING })
    }

    @Test
    fun `returns only WALKING routes when type is WALKING`() = runBlocking {
        val routes = useCase(ActivityType.WALKING).getOrThrow()
        assertTrue(routes.isNotEmpty())
        assertTrue(routes.all { it.activityType == ActivityType.WALKING })
    }

    @Test
    fun `null type returns all routes`() = runBlocking {
        val all = useCase(null).getOrThrow()
        val running = useCase(ActivityType.RUNNING).getOrThrow()
        val cycling = useCase(ActivityType.CYCLING).getOrThrow()
        val walking = useCase(ActivityType.WALKING).getOrThrow()
        assertEquals(running.size + cycling.size + walking.size, all.size)
    }

    @Test
    fun `each activity type has exactly 3 routes`() = runBlocking {
        ActivityType.entries.forEach { type ->
            val count = useCase(type).getOrThrow().size
            assertEquals("Expected 3 routes for $type, got $count", 3, count)
        }
    }
}
```

- [ ] **Step 3: Run the tests**

Run: `./gradlew :library:data:test 2>&1 | tail -15`

Expected: `5 tests completed, 0 failures`

- [ ] **Step 4: Commit**

```bash
git add library/data/src/test/
git commit -m "test: add GetRoutesUseCase filter tests"
```

---

### Task 5: Add Material Icons Extended dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add to `libs.versions.toml`**

In the `[libraries]` section, add after `androidx-compose-material3`:
```toml
material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
```

- [ ] **Step 2: Add to `app/build.gradle.kts`**

In the `dependencies` block, add after `implementation(libs.androidx.compose.material3)`:
```kotlin
implementation(libs.material.icons.extended)
```

- [ ] **Step 3: Verify sync**

Run: `./gradlew :app:assembleDebug 2>&1 | tail -5`

Expected: `BUILD SUCCESSFUL` (icons not used yet, but dependency resolves)

---

### Task 6: `ActivityType` MVI components

**Files:**
- Create: `app/src/main/java/com/miquido/stravapoc/presentation/activitytype/ActivityTypeViewState.kt`
- Create: `app/src/main/java/com/miquido/stravapoc/presentation/activitytype/ActivityTypeSideEffect.kt`
- Create: `app/src/main/java/com/miquido/stravapoc/presentation/activitytype/ActivityTypeViewModel.kt`

- [ ] **Step 1: Create `ActivityTypeViewState.kt`**

```kotlin
package com.miquido.stravapoc.presentation.activitytype

import com.miquido.stravapoc.library.data.model.ActivityType

data class ActivityTypeViewState(
    val activityTypes: List<ActivityType> = ActivityType.entries
)
```

- [ ] **Step 2: Create `ActivityTypeSideEffect.kt`**

```kotlin
package com.miquido.stravapoc.presentation.activitytype

import com.miquido.stravapoc.library.data.model.ActivityType

sealed class ActivityTypeSideEffect {
    data class NavigateToRouteList(val activityType: ActivityType) : ActivityTypeSideEffect()
}
```

- [ ] **Step 3: Create `ActivityTypeViewModel.kt`**

```kotlin
package com.miquido.stravapoc.presentation.activitytype

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.library.data.model.ActivityType

class ActivityTypeViewModel : MviViewModel<ActivityTypeViewState>(
    ActivityTypeViewState(),
    MviDefaultConfig()
) {
    fun onActivitySelected(activityType: ActivityType) = launch {
        emitSideEffect(ActivityTypeSideEffect.NavigateToRouteList(activityType))
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { ActivityTypeViewModel() }
        }
    }
}
```

- [ ] **Step 4: Build**

Run: `./gradlew :app:compileDebugKotlin 2>&1 | grep -E "error:|BUILD"`

Expected: `BUILD SUCCESSFUL` or only errors in MainActivity (not yet updated).

---

### Task 7: `ActivityTypeScreen`

**Files:**
- Create: `app/src/main/java/com/miquido/stravapoc/presentation/activitytype/ActivityTypeScreen.kt`

- [ ] **Step 1: Create `ActivityTypeScreen.kt`**

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTypeScreen(
    onNavigateToRouteList: (ActivityType) -> Unit,
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

    Scaffold(
        topBar = { TopAppBar(title = { Text("Wybierz aktywność") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
}

@Composable
private fun ActivityTypeCard(
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

private val ActivityType.displayName: String
    get() = when (this) {
        ActivityType.RUNNING -> "Bieganie"
        ActivityType.CYCLING -> "Rower"
        ActivityType.WALKING -> "Chodzenie"
    }

private val ActivityType.icon: ImageVector
    get() = when (this) {
        ActivityType.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
        ActivityType.CYCLING -> Icons.Filled.PedalBike
        ActivityType.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
    }
```

- [ ] **Step 2: Build**

Run: `./gradlew :app:compileDebugKotlin 2>&1 | grep -E "error:|BUILD"`

Expected: `BUILD SUCCESSFUL` or only remaining errors in files not yet updated.

---

### Task 8: Update `RouteListViewState` + `RouteListViewModel`

**Files:**
- Modify: `app/src/main/java/com/miquido/stravapoc/presentation/routelist/RouteListViewState.kt`
- Modify: `app/src/main/java/com/miquido/stravapoc/presentation/routelist/RouteListViewModel.kt`

- [ ] **Step 1: Update `RouteListViewState.kt`**

```kotlin
package com.miquido.stravapoc.presentation.routelist

import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.MockRoute

data class RouteListViewState(
    val activityType: ActivityType = ActivityType.RUNNING,
    val routes: List<MockRoute> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

- [ ] **Step 2: Update `RouteListViewModel.kt`**

```kotlin
package com.miquido.stravapoc.presentation.routelist

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.di.AppModule
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.usecase.GetRoutesUseCase

class RouteListViewModel(
    private val activityType: ActivityType,
    private val getRoutesUseCase: GetRoutesUseCase
) : MviViewModel<RouteListViewState>(RouteListViewState(activityType = activityType), MviDefaultConfig()) {

    init {
        loadRoutes()
    }

    private fun loadRoutes() = launch {
        transform { copy(isLoading = true, error = null) }
        getRoutesUseCase(type = activityType)
            .onSuccess { routes -> transform { copy(routes = routes, isLoading = false) } }
            .onFailure { e -> transform { copy(isLoading = false, error = e.message) } }
    }

    fun onRouteSelected(routeId: String) = launch {
        emitSideEffect(RouteListSideEffect.NavigateToDetail(routeId))
    }

    companion object {
        fun factory(activityType: ActivityType): ViewModelProvider.Factory = viewModelFactory {
            initializer { RouteListViewModel(activityType, AppModule.getRoutesUseCase) }
        }
    }
}
```

---

### Task 9: Update `RouteListScreen` + `MainActivity`

**Files:**
- Modify: `app/src/main/java/com/miquido/stravapoc/presentation/routelist/RouteListScreen.kt`
- Modify: `app/src/main/java/com/miquido/stravapoc/MainActivity.kt`

- [ ] **Step 1: Replace `RouteListScreen.kt` with full updated version**

```kotlin
package com.miquido.stravapoc.presentation.routelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.MockRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteListScreen(
    activityType: ActivityType,
    onNavigateToDetail: (String) -> Unit,
    viewModel: RouteListViewModel = viewModel(factory = RouteListViewModel.factory(activityType))
) {
    val state by viewModel.viewState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.collectSideEffect<RouteListSideEffect>(this) { effect ->
            when (effect) {
                is RouteListSideEffect.NavigateToDetail -> onNavigateToDetail(effect.routeId)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(activityType.routeListTitle) }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    text = "Błąd: ${state.error}",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> RouteList(
                    routes = state.routes,
                    onRouteClick = viewModel::onRouteSelected
                )
            }
        }
    }
}

@Composable
private fun RouteList(
    routes: List<MockRoute>,
    onRouteClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(routes, key = { it.id }) { route ->
            RouteItem(route = route, onClick = { onRouteClick(route.id) })
        }
    }
}

@Composable
private fun RouteItem(route: MockRoute, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = route.name, style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "%.1f km".format(route.distanceKm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${route.points.size} punktów",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private val ActivityType.routeListTitle: String
    get() = when (this) {
        ActivityType.RUNNING -> "Trasy biegowe"
        ActivityType.CYCLING -> "Trasy rowerowe"
        ActivityType.WALKING -> "Trasy spacerowe"
    }
```

- [ ] **Step 2: Update `MainActivity.kt`**

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
import com.miquido.stravapoc.presentation.activitytype.ActivityTypeScreen
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
                NavHost(navController = navController, startDestination = "activity_type") {
                    composable("activity_type") {
                        ActivityTypeScreen(
                            onNavigateToRouteList = { activityType ->
                                navController.navigate("route_list/${activityType.name}")
                            }
                        )
                    }
                    composable("route_list/{activityType}") { backStackEntry ->
                        val typeName = backStackEntry.arguments?.getString("activityType")
                            ?: ActivityType.RUNNING.name
                        val activityType = ActivityType.valueOf(typeName)
                        RouteListScreen(
                            activityType = activityType,
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
                }
            }
        }
    }
}
```

- [ ] **Step 3: Full build**

Run: `./gradlew :app:assembleDebug 2>&1 | tail -5`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add app/src/main/ gradle/
git commit -m "feat: add activity type selection screen to phone app"
```

---

### Task 10: Smoke test

- [ ] **Step 1: Run all tests**

Run: `./gradlew :library:data:test :app:test 2>&1 | tail -10`

Expected: all tests pass, 0 failures.

- [ ] **Step 2: Install on phone/emulator and verify**

Manual checks:
1. App opens on "Wybierz aktywność" screen with 3 cards
2. Tapping "Bieganie" → route list shows only running routes (Łazienki, Planty, Trójmiejski)
3. Tapping "Rower" → only cycling routes (Odra, Wisła, Mazury)
4. Tapping "Chodzenie" → only walking routes (Giewont, Wetlińska, Barania Góra)
5. Tapping a route → detail screen with map still works
6. "Wyślij na zegarek" still sends the route
