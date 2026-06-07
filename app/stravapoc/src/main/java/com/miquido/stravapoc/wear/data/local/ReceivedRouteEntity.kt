package com.miquido.stravapoc.wear.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "received_routes")
data class ReceivedRouteEntity(
    @PrimaryKey val id: Int = 0,
    val routeJson: String,
    val receivedAt: Long = System.currentTimeMillis()
)
