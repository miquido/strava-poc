package com.miquido.stravapoc.wear.data

import com.miquido.stravapoc.library.data.model.RoutePoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutResultHolder @Inject constructor() {
    @Volatile var pendingTrackedPoints: List<RoutePoint> = emptyList()
}
