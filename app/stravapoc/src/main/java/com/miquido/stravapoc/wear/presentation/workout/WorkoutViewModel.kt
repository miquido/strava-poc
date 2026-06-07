package com.miquido.stravapoc.wear.presentation.workout

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.wear.data.WorkoutService
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val routeId: String,
    private val context: Context,
) : MviViewModel<WorkoutViewState>(WorkoutViewState.Idle, MviDefaultConfig()) {

    private var workoutService: WorkoutService? = null
    private var bound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val service = (binder as WorkoutService.LocalBinder).getService()
            workoutService = service
            bound = true
            viewModelScope.launch {
                service.state.collect { serviceState ->
                    transform { serviceState }
                    if (serviceState is WorkoutViewState.Finished) {
                        emitSideEffect(
                            WorkoutSideEffect.NavigateToSummary(
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

    companion object {
        fun factory(routeId: String, context: Context): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    WorkoutViewModel(routeId = routeId, context = context.applicationContext)
                }
            }
    }
}
