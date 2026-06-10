package com.miquido.stravapoc.presentation

import androidx.annotation.StringRes
import com.miquido.stravapoc.R
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.ActivityType.CYCLING
import com.miquido.stravapoc.library.data.model.ActivityType.RUNNING
import com.miquido.stravapoc.library.data.model.ActivityType.WALKING

@StringRes
internal fun ActivityType.toDisplayNameRes(): Int = when (this) {
    RUNNING -> R.string.activity_running
    CYCLING -> R.string.activity_cycling
    WALKING -> R.string.activity_walking
}
