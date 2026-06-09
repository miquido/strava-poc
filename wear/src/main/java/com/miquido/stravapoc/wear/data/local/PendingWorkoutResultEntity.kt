package com.miquido.stravapoc.wear.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "pending_workout_results")
data class PendingWorkoutResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutResultJson: String,
    val createdAt: Long
)

@Dao
interface PendingWorkoutResultDao {
    @Query("SELECT * FROM pending_workout_results ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingWorkoutResultEntity>

    @Insert
    suspend fun insert(entity: PendingWorkoutResultEntity): Long

    @Delete
    suspend fun delete(entity: PendingWorkoutResultEntity)
}
