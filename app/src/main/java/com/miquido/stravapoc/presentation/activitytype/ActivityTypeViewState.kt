package com.miquido.stravapoc.presentation.activitytype

import com.miquido.stravapoc.library.data.model.ActivityType

data class ActivityTypeViewState(
    val activityTypes: List<ActivityType> = ActivityType.entries
)
