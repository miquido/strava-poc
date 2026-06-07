package com.miquido.stravapoc.library.data.datasource

import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.Route

class RouteLocalDataSource {

    fun getRoutes(type: ActivityType? = null): List<Route> =
        if (type == null) mockRoutes else mockRoutes.filter { it.activityType == type }

    fun getRouteById(id: String): Route? = mockRoutes.find { it.id == id }
}
