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
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.miquido.stravapoc.wear.presentation.activityselection.ActivitySelectionScreen
import com.miquido.stravapoc.wear.presentation.summary.WorkoutSummaryScreen
import com.miquido.stravapoc.wear.presentation.workout.WorkoutScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            val notifPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /* wynik obsługiwany przez system */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            val navController = rememberSwipeDismissableNavController()

            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "activity_selection"
            ) {
                composable("activity_selection") {
                    ActivitySelectionScreen(
                        onNavigateToWorkout = { routeId ->
                            navController.navigate("workout/$routeId")
                        }
                    )
                }

                composable("workout/{routeId}") { backStackEntry ->
                    val routeId = backStackEntry.arguments?.getString("routeId") ?: "warsaw_lazienki"
                    WorkoutScreen(
                        routeId = routeId,
                        onNavigateToSummary = { distanceKm, durationSecs, laps ->
                            navController.navigate("summary/$distanceKm/$durationSecs/$laps") {
                                popUpTo("workout/{routeId}") { inclusive = true }
                            }
                        }
                    )
                }

                composable("summary/{distanceKm}/{durationSecs}/{laps}") { backStackEntry ->
                    val distance = backStackEntry.arguments?.getString("distanceKm")?.toDoubleOrNull() ?: 0.0
                    val duration = backStackEntry.arguments?.getString("durationSecs")?.toLongOrNull() ?: 0L
                    val laps = backStackEntry.arguments?.getString("laps")?.toIntOrNull() ?: 1

                    WorkoutSummaryScreen(
                        totalDistanceKm = distance,
                        totalSeconds = duration,
                        laps = laps,
                        onDone = {
                            navController.popBackStack("activity_selection", inclusive = false)
                        }
                    )
                }
            }
        }
    }
}
