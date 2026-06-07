package com.miquido.stravapoc.library.data.repository

import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.Route

interface RouteRepository {
    suspend fun getRoutes(type: ActivityType? = null): Result<List<Route>>
    suspend fun getRouteById(id: String): Result<Route>
}
