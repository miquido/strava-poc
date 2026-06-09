package com.miquido.stravapoc.wear.presentation.activityselection

import com.miquido.stravapoc.library.data.model.ActivityType

sealed class ActivitySelectionSideEffect {
    data class NavigateToRouteList(val activityType: ActivityType) : ActivitySelectionSideEffect()
}
