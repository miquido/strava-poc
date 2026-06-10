package com.miquido.stravapoc.wear.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.miquido.stravapoc.wear.data.CUSTOM_ROUTE_ID
import com.miquido.stravapoc.wear.presentation.activityselection.ActivitySelectionScreen
import com.miquido.stravapoc.wear.presentation.routelist.WearRouteListScreen
import com.miquido.stravapoc.wear.presentation.summary.WorkoutSummaryScreen
import com.miquido.stravapoc.wear.presentation.workout.WorkoutScreen

@Composable
internal fun WearNavGraph(
    navController: NavHostController,
    startDestination: String,
    isAmbient: Boolean,
    onSaveWorkout: suspend (routeId: String, distanceKm: Double, durationSecs: Long, laps: Int) -> Boolean,
) {
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = startDestination,
    ) {

        composable(WearRoute.ActivitySelection.route) {
            ActivitySelectionScreen(
                onNavigateToRouteList = { activityType ->
                    navController.navigate(WearRoute.RouteList.createRoute(activityType))
                }
            )
        }

        composable(WearRoute.RouteList.route) {
            WearRouteListScreen(
                onNavigateToWorkout = { routeId, activityType ->
                    navController.navigate(WearRoute.Workout.createRoute(routeId, activityType)) {
                        popUpTo(WearRoute.ActivitySelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(WearRoute.Workout.route) {
            WorkoutScreen(
                isAmbient = isAmbient,
                onNavigateToSummary = { routeId, distanceKm, durationSecs, laps ->
                    navController.navigate(
                        WearRoute.Summary.createRoute(routeId, distanceKm, durationSecs, laps)
                    ) {
                        popUpTo(WearRoute.Workout.route) { inclusive = true }
                    }
                }
            )
        }

        composable(WearRoute.Summary.route) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
            val distanceKm = backStackEntry.arguments?.getString("distanceKm")?.toDoubleOrNull() ?: 0.0
            val durationSecs = backStackEntry.arguments?.getString("durationSecs")?.toLongOrNull() ?: 0L
            val laps = backStackEntry.arguments?.getString("laps")?.toIntOrNull() ?: 1

            fun navigateBack() {
                if (!navController.popBackStack(WearRoute.ActivitySelection.route, inclusive = false)) {
                    navController.navigate(WearRoute.ActivitySelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            WorkoutSummaryScreen(
                totalDistanceKm = distanceKm,
                totalSeconds = durationSecs,
                laps = laps,
                onSave = {
                    val actualRouteId = if (routeId == CUSTOM_ROUTE_ID) "" else routeId
                    onSaveWorkout(actualRouteId, distanceKm, durationSecs, laps)
                },
                onNavigateBack = { navigateBack() },
                onDiscard = { navigateBack() },
            )
        }
    }
}
