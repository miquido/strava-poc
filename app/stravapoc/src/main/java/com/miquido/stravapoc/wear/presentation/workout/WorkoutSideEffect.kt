package com.miquido.stravapoc.wear.presentation.workout

sealed class WorkoutSideEffect {
    data class NavigateToSummary(
        val distanceKm: Double,
        val durationSecs: Long,
        val laps: Int
    ) : WorkoutSideEffect()
    data class ShowError(val message: String) : WorkoutSideEffect()
}
