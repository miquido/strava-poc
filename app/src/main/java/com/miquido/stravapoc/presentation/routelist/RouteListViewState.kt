package com.miquido.stravapoc.presentation.routelist

import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.Route

internal data class RouteListViewState(
    val activityType: ActivityType = ActivityType.RUNNING,
    val routes: List<Route> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
