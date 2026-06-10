package com.miquido.stravapoc.wear.data.location

import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.ActivityType.CYCLING
import com.miquido.stravapoc.library.data.model.ActivityType.RUNNING
import com.miquido.stravapoc.library.data.model.ActivityType.WALKING
import com.miquido.stravapoc.library.data.model.RoutePoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class TrackingConstraints(
    val jitterKm: Double,
    val maxDeltaKm: Double,
    val mockSpeedKmh: Double
)

val ActivityType.trackingConstraints: TrackingConstraints
    get() = when (this) {
        WALKING -> TrackingConstraints(jitterKm = 0.003, maxDeltaKm = 0.10, mockSpeedKmh = 5.0)
        RUNNING -> TrackingConstraints(jitterKm = 0.003, maxDeltaKm = 0.15, mockSpeedKmh = 10.0)
        CYCLING -> TrackingConstraints(jitterKm = 0.005, maxDeltaKm = 0.25, mockSpeedKmh = 25.0)
    }

fun haversineDistanceKm(p1: RoutePoint, p2: RoutePoint): Double {
    val R = 6371.0
    val dLat = Math.toRadians(p2.lat - p1.lat)
    val dLng = Math.toRadians(p2.lng - p1.lng)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(p1.lat)) * cos(Math.toRadians(p2.lat)) *
            sin(dLng / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
}

fun computeCumulativeDistances(points: List<RoutePoint>): List<Double> {
    var cum = 0.0
    return points.zipWithNext().map { (a, b) ->
        cum += haversineDistanceKm(a, b)
        cum
    }
}

fun interpolatePoint(
    points: List<RoutePoint>,
    cumDist: List<Double>,
    progressKm: Double
): RoutePoint {
    if (points.isEmpty()) return RoutePoint(0.0, 0.0)
    if (points.size == 1) return points[0]
    if (progressKm <= 0.0) return points[0]
    if (progressKm >= cumDist.last()) return points.last()

    val idx = cumDist.indexOfFirst { it >= progressKm }
    if (idx < 0) return points.last()

    val segStart = if (idx == 0) 0.0 else cumDist[idx - 1]
    val segEnd = cumDist[idx]
    val segLen = segEnd - segStart
    val t = if (segLen > 0.0) (progressKm - segStart) / segLen else 0.0

    val p1 = points[idx]
    val p2 = points[idx + 1]
    return RoutePoint(
        lat = p1.lat + (p2.lat - p1.lat) * t,
        lng = p1.lng + (p2.lng - p1.lng) * t
    )
}
