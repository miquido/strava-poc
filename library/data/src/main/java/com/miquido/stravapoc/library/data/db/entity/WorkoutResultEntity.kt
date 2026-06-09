package com.miquido.stravapoc.library.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.RoutePoint
import com.miquido.stravapoc.library.data.model.WorkoutResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "workout_results")
internal data class WorkoutResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeId: String,
    val routeName: String,
    val activityType: String,
    val totalDistanceKm: Double,
    val totalDurationSeconds: Long,
    val laps: Int,
    val timestamp: Long,
    val trackedPointsJson: String = ""
)

internal fun WorkoutResultEntity.toDomain() = WorkoutResult(
    id = id,
    routeId = routeId,
    routeName = routeName,
    activityType = runCatching { ActivityType.valueOf(activityType) }.getOrDefault(ActivityType.RUNNING),
    totalDistanceKm = totalDistanceKm,
    totalDurationSeconds = totalDurationSeconds,
    laps = laps,
    timestamp = timestamp,
    trackedPoints = if (trackedPointsJson.isEmpty()) emptyList()
                    else runCatching { Json.decodeFromString<List<RoutePoint>>(trackedPointsJson) }.getOrDefault(emptyList())
)

internal fun WorkoutResult.toEntity() = WorkoutResultEntity(
    id = id,
    routeId = routeId,
    routeName = routeName,
    activityType = activityType.name,
    totalDistanceKm = totalDistanceKm,
    totalDurationSeconds = totalDurationSeconds,
    laps = laps,
    timestamp = timestamp,
    trackedPointsJson = if (trackedPoints.isEmpty()) "" else Json.encodeToString(trackedPoints)
)
