package com.miquido.stravapoc.wear.data.location

import com.miquido.stravapoc.library.data.model.RoutePoint
import kotlinx.coroutines.flow.Flow

/**
 * Abstrakcja źródła pozycji GPS.
 * Implementacje: MockLocationSource (predefiniowana trasa) i RealLocationSource (prawdziwy GPS).
 */
interface LocationSource {
    /** Emituje kolejne punkty GPS w trakcie treningu. */
    val locations: Flow<RoutePoint>
}
