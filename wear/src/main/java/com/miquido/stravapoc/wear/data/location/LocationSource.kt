package com.miquido.stravapoc.wear.data.location

import com.miquido.stravapoc.library.data.model.RoutePoint
import kotlinx.coroutines.flow.Flow

interface LocationSource {
    val locations: Flow<RoutePoint>
}
