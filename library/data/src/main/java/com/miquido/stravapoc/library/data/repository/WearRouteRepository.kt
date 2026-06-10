package com.miquido.stravapoc.library.data.repository

import com.miquido.stravapoc.library.data.model.Route

interface WearRouteRepository : RouteRepository {
    suspend fun insert(route: Route)
    suspend fun getMostRecent(): Route?
}
