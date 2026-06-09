package com.miquido.stravapoc.library.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutResult(
    val id: Long = 0,
    val routeId: String,
    val routeName: String = "",
    val activityType: ActivityType = ActivityType.RUNNING,
    val totalDistanceKm: Double,
    val totalDurationSeconds: Long,
    val laps: Int,
    val timestamp: Long = 0L,
    val trackedPoints: List<RoutePoint> = emptyList()
)
