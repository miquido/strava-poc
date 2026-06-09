package com.miquido.stravapoc.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.miquido.stravapoc.MainActivity
import com.miquido.stravapoc.library.data.datasource.RouteLocalDataSource
import com.miquido.stravapoc.library.data.model.WorkoutResult
import com.miquido.stravapoc.library.usecase.SaveWorkoutResultUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutResultReceiver : WearableListenerService() {

    @Inject lateinit var saveWorkoutResultUseCase: SaveWorkoutResultUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val routeDataSource = RouteLocalDataSource()

    override fun onDataChanged(events: DataEventBuffer) {
        events
            .filter { it.type == DataEvent.TYPE_CHANGED && it.dataItem.uri.path == "/workout/result" }
            .forEach { event ->
                val uri = event.dataItem.uri
                val json = DataMapItem.fromDataItem(event.dataItem)
                    .dataMap.getString("result_json") ?: return@forEach
                val result = runCatching { Json.decodeFromString<WorkoutResult>(json) }
                    .getOrNull() ?: return@forEach

                val route = routeDataSource.getRouteById(result.routeId)
                val enriched = result.copy(
                    routeName = route?.name ?: result.routeId,
                    activityType = route?.activityType ?: result.activityType,
                    timestamp = System.currentTimeMillis()
                )

                scope.launch {
                    saveWorkoutResultUseCase(enriched)
                    showNotification(enriched)
                    // Usuń data item z Data Layer po zapisaniu — putDataItem() tworzy
                    // trwały stan synchronizowany między urządzeniami. Bez usunięcia
                    // item pozostaje w Data Layer i jest dostarczany ponownie przy
                    // każdej reinstalacji / ponownej rejestracji WearableListenerService.
                    Wearable.getDataClient(this@WorkoutResultReceiver)
                        .deleteDataItems(uri)
                        .await()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun showNotification(result: WorkoutResult) {
        val manager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Workout notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Notifications about workouts synced from the watch" }
        manager.createNotificationChannel(channel)

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (result.routeName.isNotBlank()) result.routeName else "Workout synced!"
        val distanceText = "%.2f km · %s".format(
            result.totalDistanceKm,
            formatDuration(result.totalDurationSeconds)
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(distanceText)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
    }

    companion object {
        private const val CHANNEL_ID = "workout_result_channel"
        private const val NOTIFICATION_ID = 2001
    }
}
