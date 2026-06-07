package com.miquido.stravapoc.sync

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.miquido.stravapoc.library.data.model.Route
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WearSyncManager(private val context: Context) {

    suspend fun sendRoute(route: Route): Result<Unit> = runCatching {
        val json = Json.encodeToString(route)
        val request = PutDataMapRequest.create("/route/selected").apply {
            dataMap.putString("route_json", json)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }
        Wearable.getDataClient(context)
            .putDataItem(request.asPutDataRequest().setUrgent())
            .await()
    }
}
