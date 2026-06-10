package com.miquido.stravapoc.wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.WorkoutResult
import com.miquido.stravapoc.presentation.theme.StravaPocTheme
import com.miquido.stravapoc.wear.data.WorkoutPreferences
import com.miquido.stravapoc.wear.data.WorkoutResultHolder
import com.miquido.stravapoc.wear.data.WorkoutResultSender
import com.miquido.stravapoc.wear.presentation.navigation.WearNavGraph
import com.miquido.stravapoc.wear.presentation.navigation.WearRoute
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var workoutResultHolder: WorkoutResultHolder
    @Inject lateinit var workoutResultSender: WorkoutResultSender
    @Inject lateinit var workoutPreferences: WorkoutPreferences

    private val isAmbient = mutableStateOf(false)

    private val ambientCallback = object : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
            isAmbient.value = true
        }

        override fun onExitAmbient() {
            isAmbient.value = false
        }

        override fun onUpdateAmbient() {}
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
                val navController = rememberSwipeDismissableNavController()

                val startDestination = remember {
                    val activeRouteId = workoutPreferences.getActiveRouteId()
                    if (activeRouteId != null) {
                        WearRoute.Workout.createRoute(activeRouteId, ActivityType.RUNNING)
                    } else {
                        WearRoute.ActivitySelection.route
                    }
                }

                RequestPermissions()

                WearNavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    isAmbient = ambient,
                    onSaveWorkout = { routeId, distanceKm, durationSecs, laps ->
                        val points = workoutResultHolder.pendingTrackedPoints
                        workoutResultHolder.pendingTrackedPoints = emptyList()
                        workoutResultSender.sendResult(
                            WorkoutResult(
                                routeId = routeId,
                                totalDistanceKm = distanceKm,
                                totalDurationSeconds = durationSecs,
                                laps = laps,
                                trackedPoints = points,
                            )
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun RequestPermissions() {
    val context = LocalContext.current
    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}

    LaunchedEffect(Unit) {
        val required = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        val notGranted = required.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) permissionsLauncher.launch(notGranted.toTypedArray())
    }
}
