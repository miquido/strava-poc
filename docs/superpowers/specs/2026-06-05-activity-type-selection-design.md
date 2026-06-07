# Activity Type Selection — Design Spec
**Date:** 2026-06-05  
**Scope:** Phone app only. Wear OS unchanged.

## Summary

Add an activity type selection screen as the entry point of the phone app. User picks one of three activity types; the route list is then filtered to show only routes for that type.

---

## Data Layer Changes (`:library:data`)

### New: `ActivityType` enum
```kotlin
enum class ActivityType { RUNNING, CYCLING, WALKING }
```

### Modified: `MockRoute`
Add required field:
```kotlin
data class MockRoute(
    val id: String,
    val name: String,
    val distanceKm: Double,
    val points: List<RoutePoint>,
    val activityType: ActivityType        // NEW
)
```

### Modified: `GetRoutesUseCase`
```kotlin
suspend operator fun invoke(type: ActivityType? = null): Result<List<MockRoute>>
```
`type = null` returns all routes (no breaking change). Provided type filters the list.

### Updated mock data — 9 routes, 3 per type:
| Type | Routes |
|------|--------|
| RUNNING | Warszawa Łazienki (5.2 km), Kraków Planty (4.0 km), Trójmiejski Park Krajobrazowy (7.1 km) |
| CYCLING | Wrocław Odra (6.5 km), Wisła Trasa Rowerowa (12.0 km), Mazury Wielka Pętla (18.0 km) |
| WALKING | Tatry — Giewont (8.3 km), Bieszczady — Połonina Wetlińska (9.0 km), Beskid Śląski — Barania Góra (5.5 km) |

---

## Phone App Navigation

### New flow
```
ActivityTypeScreen  →  RouteListScreen(activityType)  →  RouteDetailScreen(routeId)
```

### New screen: `ActivityTypeScreen`
- Location: `presentation/activitytype/`
- Shows 3 cards: Bieganie, Rower, Chodzenie — each with an icon and name
- ViewModel: `ActivityTypeViewModel : MviViewModel<ActivityTypeViewState>`
- SideEffect: `ActivityTypeSideEffect.NavigateToRouteList(activityType: ActivityType)`
- State: `ActivityTypeViewState` (stateless — just holds the list of available types)

### Modified: `RouteListViewModel`
- Constructor receives `activityType: ActivityType`
- Passes it to `GetRoutesUseCase(type = activityType)`
- `RouteListViewState` gains `activityType: ActivityType` for display

### Modified: `RouteListScreen`
- `TopAppBar` title reflects the activity: "Trasy biegowe" / "Trasy rowerowe" / "Trasy spacerowe"

### Modified: `MainActivity` navigation
```kotlin
NavHost(startDestination = "activity_type") {
    composable("activity_type") { ActivityTypeScreen(...) }
    composable("route_list/{activityType}") { RouteListScreen(activityType, ...) }
    composable("route_detail/{routeId}") { RouteDetailScreen(routeId, ...) }
}
```

---

## Out of scope
- Wear OS changes
- Persisting last selected activity type
- Icons sourced from a design system (use Material Icons)
- Dark mode
