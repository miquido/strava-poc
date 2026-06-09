package com.miquido.stravapoc.wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.WorkoutResult
import com.miquido.stravapoc.presentation.theme.StravaPocTheme
import com.miquido.stravapoc.wear.data.WorkoutPreferences
import com.miquido.stravapoc.wear.data.WorkoutService
import com.miquido.stravapoc.wear.di.WearAppModule
import com.miquido.stravapoc.wear.presentation.activityselection.ActivitySelectionScreen
import com.miquido.stravapoc.wear.presentation.routelist.WearRouteListScreen
import com.miquido.stravapoc.wear.presentation.summary.WorkoutSummaryScreen
import com.miquido.stravapoc.wear.presentation.workout.WorkoutScreen

class MainActivity : ComponentActivity() {

    private val isAmbient = mutableStateOf(false)

    private val ambientCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
            isAmbient.value = true
        }

        override fun onExitAmbient() {
            isAmbient.value = false
        }

        override fun onUpdateAmbient() {
        }
    }

    private lateinit var ambientObserver: AmbientLifecycleObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        ambientObserver = AmbientLifecycleObserver(this, ambientCallback)
        lifecycle.addObserver(ambientObserver)

        setContent {
            StravaPocTheme {
            val ambient by isAmbient
            val context = LocalContext.current
            val permissionsLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { }

            LaunchedEffect(Unit) {
                val required = buildList {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                val notGranted = required.filter {
                    ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
                }
                if (notGranted.isNotEmpty()) {
                    permissionsLauncher.launch(notGranted.toTypedArray())
                }
            }

            val navController = rememberSwipeDismissableNavController()

            val activeRouteId = remember { WorkoutPreferences.getActiveRouteId(context) }
            val startDestination = remember {
                if (activeRouteId != null) "workout/$activeRouteId/RUNNING" else "activity_selection"
            }

            SwipeDismissableNavHost(
                navController = navController,
                startDestination = startDestination
            ) {

                composable("activity_selection") {
                    ActivitySelectionScreen(
                        onNavigateToRouteList = { activityType ->
                            navController.navigate("route_list/${activityType.name}")
                        }
                    )
                }

                composable("route_list/{activityType}") { backStackEntry ->
                    val typeName = backStackEntry.arguments?.getString("activityType") ?: "RUNNING"
                    val activityType = runCatching { ActivityType.valueOf(typeName) }
                        .getOrDefault(ActivityType.RUNNING)
                    WearRouteListScreen(
                        activityType = activityType,
                        onNavigateToWorkout = { routeId, routeActivityType ->
                            navController.navigate("workout/$routeId/${routeActivityType.name}") {
                                popUpTo("activity_selection") { inclusive = true }
                            }
                        }
                    )
                }

                composable("workout/{routeId}/{activityTypeName}") { backStackEntry ->
                    val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
                    val typeName = backStackEntry.arguments?.getString("activityTypeName") ?: "RUNNING"
                    val workoutActivityType = runCatching { ActivityType.valueOf(typeName) }
                        .getOrDefault(ActivityType.RUNNING)
                    WorkoutScreen(
                        routeId = routeId,
                        activityType = workoutActivityType,
                        isAmbient = ambient,
                        onNavigateToSummary = { resultRouteId, distanceKm, durationSecs, laps ->
                            navController.navigate("summary/$resultRouteId/$distanceKm/$durationSecs/$laps") {
                                popUpTo("workout/{routeId}/{activityTypeName}") { inclusive = true }
                            }
                        }
                    )
                }

                composable("summary/{routeId}/{distanceKm}/{durationSecs}/{laps}") { backStackEntry ->
                    val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
                    val distance = backStackEntry.arguments?.getString("distanceKm")?.toDoubleOrNull() ?: 0.0
                    val duration = backStackEntry.arguments?.getString("durationSecs")?.toLongOrNull() ?: 0L
                    val laps = backStackEntry.arguments?.getString("laps")?.toIntOrNull() ?: 1

                    fun navigateBack() {
                        if (!navController.popBackStack("activity_selection", inclusive = false)) {
                            navController.navigate("activity_selection") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }

                    WorkoutSummaryScreen(
                        totalDistanceKm = distance,
                        totalSeconds = duration,
                        laps = laps,
                        onSave = {
                            val actualRouteId = if (routeId == WorkoutService.CUSTOM_ROUTE_ID) "" else routeId
                            val points = WearAppModule.pendingTrackedPoints
                            WearAppModule.pendingTrackedPoints = emptyList()
                            WearAppModule.workoutResultSender(context).sendResult(
                                WorkoutResult(
                                    routeId = actualRouteId,
                                    totalDistanceKm = distance,
                                    totalDurationSeconds = duration,
                                    laps = laps,
                                    trackedPoints = points
                                )
                            )
                        },
                        onNavigateBack = { navigateBack() },
                        onDiscard = { navigateBack() }
                    )
                }
            }
            } // StravaPocTheme
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
