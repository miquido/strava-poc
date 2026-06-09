package com.miquido.stravapoc.library.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.miquido.stravapoc.library.data.db.entity.WorkoutResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WorkoutResultDao {
    @Query("SELECT * FROM workout_results ORDER BY timestamp DESC")
    fun getAllAsFlow(): Flow<List<WorkoutResultEntity>>

    @Query("SELECT * FROM workout_results WHERE id = :id")
    suspend fun getById(id: Long): WorkoutResultEntity?

    @Insert
    suspend fun insert(entity: WorkoutResultEntity): Long

    @Query("DELETE FROM workout_results WHERE id = :id")
    suspend fun deleteById(id: Long)
}