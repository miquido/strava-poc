package com.miquido.stravapoc.wear.data.location

import com.miquido.stravapoc.library.data.model.RoutePoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockLocationSource(
    private val routePoints: List<RoutePoint>,
    private val speedKmh: Double = 10.0,
    private val initialProgressKm: Double = 0.0
) : LocationSource {

    override val locations: Flow<RoutePoint> = flow {
        if (routePoints.size < 2) return@flow

        val cumDist = computeCumulativeDistances(routePoints)
        val totalDistanceKm = cumDist.last()

        val distancePerSecondKm = speedKmh / 3600.0

        var progressKm = initialProgressKm.coerceIn(0.0, totalDistanceKm)

        while (progressKm < totalDistanceKm) {
            val point = interpolatePoint(routePoints, cumDist, progressKm)
            emit(point)
            delay(1000L)
            progressKm += distancePerSecondKm
        }

        emit(routePoints.last())
    }
}
