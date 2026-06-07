package com.miquido.stravapoc.wear.data

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.miquido.stravapoc.library.data.model.WorkoutResult
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WorkoutResultSender(private val context: Context) {

    suspend fun sendResult(result: WorkoutResult): Result<Unit> = runCatching {
        val json = Json.encodeToString(result)
        val request = PutDataMapRequest.create("/workout/result").apply {
            dataMap.putString("result_json", json)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }
        Wearable.getDataClient(context)
            .putDataItem(request.asPutDataRequest().setUrgent())
            .await()
    }
}
