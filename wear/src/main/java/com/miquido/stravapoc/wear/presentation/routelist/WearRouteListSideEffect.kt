package com.miquido.stravapoc.wear.presentation.routelist

import com.miquido.stravapoc.library.data.model.ActivityType

sealed class WearRouteListSideEffect {
    data class NavigateToWorkout(
        val routeId: String,
        val activityType: ActivityType
    ) : WearRouteListSideEffect()
}
