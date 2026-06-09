package com.miquido.stravapoc.wear.presentation.routelist

import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.Route

data class WearRouteListViewState(
    val activityType: ActivityType = ActivityType.RUNNING,
    val routes: List<Route> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
