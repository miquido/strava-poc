package com.miquido.stravapoc.wear.data.location

import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.RoutePoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Progi śledzenia GPS dla konkretnego typu aktywności.
 *
 * @param jitterKm    minimalne przemieszczenie (km), poniżej traktowane jako szum GPS
 * @param maxDeltaKm  maksymalne przemieszczenie w jednym update (km), powyżej = teleportacja GPS
 * @param mockSpeedKmh prędkość symulacji MockLocationSource dla tej aktywności
 */
data class TrackingConstraints(
    val jitterKm: Double,
    val maxDeltaKm: Double,
    val mockSpeedKmh: Double
)

/** Per-type GPS tracking constraints. */
val ActivityType.trackingConstraints: TrackingConstraints
    get() = when (this) {
        // jitterKm: minimalne przemieszczenie od bazy, poniżej traktowane jako szum GPS.
        //   3m dla chodu/biegu i 5m dla roweru pasuje do typowej dokładności GPS (3-15m).
        //   Dzięki architekturze distanceBaseLocation, akumulacja działa poprawnie mimo progu:
        //   ruch < jitter nie przesuwa bazy, więc kolejne małe kroki narastają do bazy.
        //
        // maxDeltaKm: tylko filtr TELEPORTACJI GPS — odrzuca fikcyjne skoki rzędu setek metrów.
        //   Celowo bardzo liberalne (100-250m), bo:
        //   - prawdziwy GPS ma dokładność 3-15m → dwa kolejne fixy mogą być 20-30m od siebie
        //   - przy lock-in GPS na początku treningu skoki do ~50m są normalne
        //   - za ciasny próg (np. 4-12m) odrzuca KAŻDY prawdziwy odczyt → brak ścieżki i dystansu
        ActivityType.WALKING -> TrackingConstraints(jitterKm = 0.003, maxDeltaKm = 0.10, mockSpeedKmh = 5.0)
        ActivityType.RUNNING -> TrackingConstraints(jitterKm = 0.003, maxDeltaKm = 0.15, mockSpeedKmh = 10.0)
        ActivityType.CYCLING -> TrackingConstraints(jitterKm = 0.005, maxDeltaKm = 0.25, mockSpeedKmh = 25.0)
    }

/**
 * Liczy odległość między dwoma punktami GPS wzorem Haversine'a.
 * Wynik w kilometrach.
 */
fun haversineDistanceKm(p1: RoutePoint, p2: RoutePoint): Double {
    val R = 6371.0
    val dLat = Math.toRadians(p2.lat - p1.lat)
    val dLng = Math.toRadians(p2.lng - p1.lng)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(p1.lat)) * cos(Math.toRadians(p2.lat)) *
            sin(dLng / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
}

/**
 * Oblicza skumulowane odległości segmentów trasy.
 * Wynikowa lista ma rozmiar (points.size - 1):
 *   cumDist[i] = łączna odległość od points[0] do points[i+1].
 */
fun computeCumulativeDistances(points: List<RoutePoint>): List<Double> {
    var cum = 0.0
    return points.zipWithNext().map { (a, b) ->
        cum += haversineDistanceKm(a, b)
        cum
    }
}

/**
 * Interpoluje pozycję GPS dla podanego dystansu [progressKm] wzdłuż trasy.
 * [cumDist] to wynik computeCumulativeDistances(points).
 */
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
