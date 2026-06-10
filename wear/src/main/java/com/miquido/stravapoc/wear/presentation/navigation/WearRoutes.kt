package com.miquido.stravapoc.wear.presentation.navigation

import com.miquido.stravapoc.library.data.model.ActivityType

internal sealed class WearRoute(val route: String) {

    data object ActivitySelection : WearRoute("activity_selection")

    data object RouteList : WearRoute("route_list/{activityType}") {
        fun createRoute(activityType: ActivityType) = "route_list/${activityType.name}"
    }

    data object Workout : WearRoute("workout/{routeId}/{activityTypeName}") {
        fun createRoute(routeId: String, activityType: ActivityType) =
            "workout/$routeId/${activityType.name}"
    }

    data object Summary : WearRoute("summary/{routeId}/{distanceKm}/{durationSecs}/{laps}") {
        fun createRoute(routeId: String, distanceKm: Double, durationSecs: Long, laps: Int) =
            "summary/$routeId/$distanceKm/$durationSecs/$laps"
    }
}
