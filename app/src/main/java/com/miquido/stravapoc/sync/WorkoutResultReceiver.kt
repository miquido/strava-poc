package com.miquido.stravapoc.sync

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.miquido.stravapoc.data.db.WorkoutResultEntity
import com.miquido.stravapoc.di.AppModule
import com.miquido.stravapoc.library.data.datasource.RouteLocalDataSource
import com.miquido.stravapoc.library.data.model.WorkoutResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class WorkoutResultReceiver : WearableListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val routeDataSource = RouteLocalDataSource()

    override fun onDataChanged(events: DataEventBuffer) {
        events
            .filter { it.type == DataEvent.TYPE_CHANGED && it.dataItem.uri.path == "/workout/result" }
            .forEach { event ->
                val json = DataMapItem.fromDataItem(event.dataItem)
                    .dataMap.getString("result_json") ?: return@forEach
                val result = runCatching { Json.decodeFromString<WorkoutResult>(json) }
                    .getOrNull() ?: return@forEach

                val route = routeDataSource.getRouteById(result.routeId)
                val entity = WorkoutResultEntity(
                    routeId = result.routeId,
                    routeName = route?.name ?: result.routeId,
                    activityType = route?.activityType?.name ?: "RUNNING",
                    totalDistanceKm = result.totalDistanceKm,
                    totalDurationSeconds = result.totalDurationSeconds,
                    laps = result.laps,
                    timestamp = System.currentTimeMillis()
                )

                scope.launch {
                    AppModule.provideWorkoutUseCases(applicationContext).save(entity)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
