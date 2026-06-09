package com.miquido.stravapoc.presentation.activitytype

import com.miquido.stravapoc.library.data.model.ActivityType

internal sealed class ActivityTypeSideEffect {
    data class NavigateToRouteList(val activityType: ActivityType) : ActivityTypeSideEffect()
}
