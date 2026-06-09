package com.miquido.stravapoc.wear.data.repository

import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.Route
import com.miquido.stravapoc.library.data.model.RoutePoint
import com.miquido.stravapoc.library.data.repository.RouteRepository
import com.miquido.stravapoc.wear.data.local.WearRouteDao
import com.miquido.stravapoc.wear.data.local.WearRouteEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WearRouteRepositoryImpl(private val dao: WearRouteDao) : RouteRepository {

    override suspend fun getRoutes(type: ActivityType?): Result<List<Route>> = runCatching {
        val entities = if (type == null) dao.getAll() else dao.getByActivityType(type.name)
        entities.map { it.toRoute() }
    }

    override suspend fun getRouteById(id: String): Result<Route> = runCatching {
        dao.getById(id)?.toRoute() ?: error("Route not found: $id")
    }
}

fun WearRouteEntity.toRoute(): Route = Route(
    id = id,
    name = name,
    distanceKm = distanceKm,
    activityType = ActivityType.valueOf(activityType),
    points = Json.decodeFromString<List<RoutePoint>>(pointsJson)
)

fun Route.toWearEntity(): WearRouteEntity = WearRouteEntity(
    id = id,
    name = name,
    distanceKm = distanceKm,
    activityType = activityType.name,
    pointsJson = Json.encodeToString(points)
)
