package com.miquido.stravapoc.library.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutResult(
    val routeId: String,
    val totalDistanceKm: Double,
    val totalDurationSeconds: Long,
    val laps: Int
)
