package com.miquido.stravapoc.wear.presentation.workout

sealed class WorkoutSideEffect {
    data class NavigateToSummary(
        val routeId: String,
        val distanceKm: Double,
        val durationSecs: Long,
        val laps: Int
    ) : WorkoutSideEffect()
}
