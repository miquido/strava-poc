package com.miquido.stravapoc.wear.data

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.miquido.stravapoc.library.data.model.WorkoutResult
import com.miquido.stravapoc.library.data.db.wear.PendingWorkoutResultEntity
import com.miquido.stravapoc.library.data.db.wear.PendingWorkoutResultLocalDataSource
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WorkoutResultSender(
    private val context: Context,
    private val pendingDataSource: PendingWorkoutResultLocalDataSource,
) {

    suspend fun sendResult(result: WorkoutResult): Boolean {
        val json = Json.encodeToString(result)
        return if (trySendJson(json)) {
            true
        } else {
            Log.w(TAG, "Send failed — saving locally for retry")
            pendingDataSource.insert(
                PendingWorkoutResultEntity(
                    workoutResultJson = json,
                    createdAt = System.currentTimeMillis()
                )
            )
            false
        }
    }

    suspend fun retrySendPending() {
        val pending = pendingDataSource.getAll()
        if (pending.isEmpty()) return
        Log.d(TAG, "Retrying ${pending.size} pending workout result(s)")
        for (entity in pending) {
            if (trySendJson(entity.workoutResultJson)) {
                pendingDataSource.delete(entity)
                Log.d(TAG, "Pending result id=${entity.id} sent and removed")
            }
        }
    }

    private suspend fun trySendJson(json: String): Boolean {
        return try {
            val connectedNodes = Wearable.getNodeClient(context).connectedNodes.await()
            if (connectedNodes.isEmpty()) {
                Log.d(TAG, "No connected nodes — phone not reachable")
                return false
            }
            val request = PutDataMapRequest.create("/workout/result").apply {
                dataMap.putString("result_json", json)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }
            Wearable.getDataClient(context)
                .putDataItem(request.asPutDataRequest().setUrgent())
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "putDataItem failed", e)
            false
        }
    }

    companion object {
        private const val TAG = "WorkoutResultSender"
    }
}
