package com.miquido.stravapoc.presentation.historydetail

import com.miquido.stravapoc.library.data.model.Route
import com.miquido.stravapoc.library.data.model.RoutePoint
import com.miquido.stravapoc.library.data.model.WorkoutResult

internal data class HistoryDetailViewState(
    val entity: WorkoutResult? = null,
    val route: Route? = null,
    val trackedPoints: List<RoutePoint> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
