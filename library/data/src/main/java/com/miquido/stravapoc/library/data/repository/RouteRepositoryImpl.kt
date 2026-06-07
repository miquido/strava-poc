package com.miquido.stravapoc.library.data.repository

import com.miquido.stravapoc.library.data.datasource.RouteLocalDataSource
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.Route

class RouteRepositoryImpl(
    private val dataSource: RouteLocalDataSource = RouteLocalDataSource()
) : RouteRepository {

    override suspend fun getRoutes(type: ActivityType?): Result<List<Route>> =
        runCatching { dataSource.getRoutes(type) }

    override suspend fun getRouteById(id: String): Result<Route> =
        runCatching {
            dataSource.getRouteById(id) ?: error("Route not found: $id")
        }
}
