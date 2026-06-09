package com.miquido.stravapoc.wear.data.location

import com.miquido.stravapoc.library.data.model.RoutePoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Symuluje ruch GPS wzdłuż predefiniowanej trasy.
 *
 * @param routePoints  lista punktów trasy
 * @param speedKmh     prędkość symulacji (domyślnie 10.0 km/h)
 * @param initialProgressKm  startowy dystans na trasie — używane przy wznowieniu po pauzie
 */
class MockLocationSource(
    private val routePoints: List<RoutePoint>,
    private val speedKmh: Double = 10.0,
    private val initialProgressKm: Double = 0.0
) : LocationSource {

    override val locations: Flow<RoutePoint> = flow {
        if (routePoints.size < 2) return@flow

        val cumDist = computeCumulativeDistances(routePoints)
        val totalDistanceKm = cumDist.last()

        // Dystans pokonany na sekundę przy zadanej prędkości
        val distancePerSecondKm = speedKmh / 3600.0

        var progressKm = initialProgressKm.coerceIn(0.0, totalDistanceKm)

        while (progressKm < totalDistanceKm) {
            val point = interpolatePoint(routePoints, cumDist, progressKm)
            emit(point)
            delay(1000L)
            progressKm += distancePerSecondKm
        }

        // Emituj ostatni punkt po zakończeniu trasy
        emit(routePoints.last())
    }
}
