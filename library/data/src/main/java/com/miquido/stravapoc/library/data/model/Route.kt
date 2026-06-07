package com.miquido.stravapoc.library.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val id: String,
    val name: String,
    val distanceKm: Double,
    val points: List<RoutePoint>,
    val activityType: ActivityType
)
