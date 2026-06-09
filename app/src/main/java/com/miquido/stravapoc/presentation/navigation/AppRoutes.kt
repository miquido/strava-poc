package com.miquido.stravapoc.presentation.navigation

import com.miquido.stravapoc.library.data.model.ActivityType
import kotlinx.serialization.Serializable

internal sealed interface AppRoute {

    @Serializable
    data object Home : AppRoute

    @Serializable
    data class RouteList(val activityType: ActivityType) : AppRoute

    @Serializable
    data class RouteDetail(val routeId: String) : AppRoute

    @Serializable
    data class HistoryDetail(val workoutId: Long) : AppRoute
}
