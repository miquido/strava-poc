package com.miquido.stravapoc.library.data.repository

import com.miquido.stravapoc.library.data.db.wear.WearRouteEntity
import com.miquido.stravapoc.library.data.db.wear.WearRouteLocalDataSource
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.Route
import com.miquido.stravapoc.library.data.model.RoutePoint
import javax.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WearRouteRepositoryImpl @Inject constructor(
    private val dataSource: WearRouteLocalDataSource
) : WearRouteRepository {

    override suspend fun getRoutes(type: ActivityType?): Result<List<Route>> = runCatching {
        val entities = if (type == null) dataSource.getAll() else dataSource.getByActivityType(type.name)
        entities.map { it.toRoute() }
    }

    override suspend fun getRouteById(id: String): Result<Route> = runCatching {
        dataSource.getById(id)?.toRoute() ?: error("Route not found: $id")
    }

    override suspend fun insert(route: Route) = dataSource.insert(route.toWearEntity())

    override suspend fun getMostRecent(): Route? = dataSource.getAll().firstOrNull()?.toRoute()
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
