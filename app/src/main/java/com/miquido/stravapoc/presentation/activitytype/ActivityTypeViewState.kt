package com.miquido.stravapoc.presentation.activitytype

import com.miquido.stravapoc.library.data.model.ActivityType

internal data class ActivityTypeViewState(
    val activityTypes: List<ActivityType> = ActivityType.entries
)
