package com.miquido.stravapoc.wear.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.tasks.await
import com.miquido.stravapoc.library.data.model.Route
import com.miquido.stravapoc.wear.data.local.WearDatabase
import com.miquido.stravapoc.wear.data.repository.toWearEntity
import com.miquido.stravapoc.wear.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class RouteReceiver : WearableListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(events: DataEventBuffer) {
        events
            .filter { it.dataItem.uri.path == "/route/selected" }
            .forEach { event ->
                val uri = event.dataItem.uri
                val json = DataMapItem.fromDataItem(event.dataItem)
                    .dataMap
                    .getString("route_json") ?: return@forEach

                val route = runCatching { Json.decodeFromString<Route>(json) }
                    .getOrElse { Log.e(TAG, "JSON parse error", it); return@forEach }

                Log.d(TAG, "Route received: ${route.name}")
                saveRoute(route, uri)
                showNotification(route)
            }
    }

    private fun saveRoute(route: Route, uri: android.net.Uri) {
        serviceScope.launch {
            WearDatabase.getInstance(applicationContext)
                .wearRouteDao()
                .insert(route.toWearEntity())
            // Usuń data item po zapisaniu — putDataItem() tworzy trwały stan w Data Layer,
            // który jest dostarczany ponownie przy każdej reinstalacji aplikacji.
            Wearable.getDataClient(applicationContext).deleteDataItems(uri).await()
        }
    }

    private fun showNotification(route: Route) {
        val manager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Route notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Notifications about new routes from the phone" }
        manager.createNotificationChannel(channel)

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("Route ready!")
            .setContentText(route.name)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    @Suppress("DEPRECATION")
    override fun onPeerConnected(peer: Node) {
        super.onPeerConnected(peer)
        Log.d(TAG, "Peer connected: ${peer.displayName} — retrying pending workout results")
        serviceScope.launch {
            WorkoutResultSender(applicationContext).retrySendPending()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "RouteReceiver"
        private const val CHANNEL_ID = "route_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
