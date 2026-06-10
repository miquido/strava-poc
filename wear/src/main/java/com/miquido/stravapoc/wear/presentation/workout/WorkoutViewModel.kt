package com.miquido.stravapoc.wear.presentation.workout

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.SavedStateHandle
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.wear.data.WorkoutService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
) : MviViewModel<WorkoutViewState>(WorkoutViewState.Idle, MviDefaultConfig()) {

    private val routeId: String = checkNotNull(savedStateHandle.get<String>("routeId"))
    private val activityType: ActivityType = ActivityType.valueOf(
        checkNotNull(savedStateHandle.get<String>("activityTypeName"))
    )

    private var workoutService: WorkoutService? = null
    private var bound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val service = (binder as WorkoutService.LocalBinder).getService()
            workoutService = service
            bound = true
            launch {
                service.state.collect { serviceState ->
                    transform { serviceState }
                    if (serviceState is WorkoutViewState.Finished) {
                        emitSideEffect(
                            WorkoutSideEffect.NavigateToSummary(
                                routeId = serviceState.routeId,
                                distanceKm = serviceState.totalDistanceKm,
                                durationSecs = serviceState.totalSeconds,
                                laps = serviceState.laps
                            )
                        )
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            workoutService = null
            bound = false
        }
    }

    init {
        startAndBindService()
    }

    private fun startAndBindService() {
        val startIntent = Intent(context, WorkoutService::class.java).apply {
            action = WorkoutService.ACTION_START
            putExtra(WorkoutService.EXTRA_ROUTE_ID, routeId)
            putExtra(WorkoutService.EXTRA_ACTIVITY_TYPE, activityType.name)
        }
        context.startForegroundService(startIntent)
        context.bindService(
            Intent(context, WorkoutService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun onPause() = workoutService?.pause()
    fun onResume() = workoutService?.resume()
    fun onLap() = workoutService?.lap()
    fun onFinish() = workoutService?.finish()

    override fun onCleared() {
        if (bound) {
            context.unbindService(serviceConnection)
            bound = false
        }
        super.onCleared()
    }
}
