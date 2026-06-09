package com.miquido.stravapoc.wear.presentation.activityselection

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.ui.graphics.vector.ImageVector
import com.miquido.stravapoc.library.data.model.ActivityType

data class ActivitySelectionViewState(
    val activities: List<ActivityType> = ActivityType.values().toList(),
    val receivedRouteId: String? = null,
    val receivedRouteName: String? = null
)

internal val ActivityType.wearLabel: String
    get() = when (this) {
        ActivityType.RUNNING -> "Running"
        ActivityType.CYCLING -> "Cycling"
        ActivityType.WALKING -> "Walking"
    }

internal val ActivityType.wearIcon: ImageVector
    get() = when (this) {
        ActivityType.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
        ActivityType.CYCLING -> Icons.Filled.PedalBike
        ActivityType.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
    }
