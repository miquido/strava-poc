package com.miquido.stravapoc.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutResultDao {
    @Query("SELECT * FROM workout_results ORDER BY timestamp DESC")
    fun getAllAsFlow(): Flow<List<WorkoutResultEntity>>

    @Query("SELECT * FROM workout_results WHERE id = :id")
    suspend fun getById(id: Long): WorkoutResultEntity?

    @Insert
    suspend fun insert(entity: WorkoutResultEntity): Long
}
