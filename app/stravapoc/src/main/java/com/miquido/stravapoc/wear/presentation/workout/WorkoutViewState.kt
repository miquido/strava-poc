package com.miquido.stravapoc.wear.presentation.workout

import com.miquido.stravapoc.library.data.model.RoutePoint

sealed class WorkoutViewState {
    object Idle : WorkoutViewState()

    data class Active(
        val elapsedSeconds: Long = 0,
        val distanceKm: Double = 0.0,
        val pacePerKm: String = "--:--",
        val lapNumber: Int = 1,
        val lapDistanceKm: Double = 0.0,
        val routePointIndex: Int = 0,
        val routePoints: List<RoutePoint> = emptyList()
    ) : WorkoutViewState()

    data class Paused(val snapshot: Active) : WorkoutViewState()

    data class Finished(
        val totalDistanceKm: Double,
        val totalSeconds: Long,
        val laps: Int,
        val routeId: String
    ) : WorkoutViewState()
}
