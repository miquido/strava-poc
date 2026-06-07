package com.miquido.stravapoc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_results")
data class WorkoutResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeId: String,
    val routeName: String,
    val activityType: String,          // ActivityType.name
    val totalDistanceKm: Double,
    val totalDurationSeconds: Long,
    val laps: Int,
    val timestamp: Long                // System.currentTimeMillis() przy odbiorze
)
