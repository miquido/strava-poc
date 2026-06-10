package com.miquido.stravapoc.library.data.db.wear

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wear_routes")
data class WearRouteEntity(
    @PrimaryKey val id: String,
    val name: String,
    val distanceKm: Double,
    val activityType: String,
    val pointsJson: String,
    val receivedAt: Long = System.currentTimeMillis()
)
