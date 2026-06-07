package com.miquido.stravapoc.data.repository

import com.miquido.stravapoc.data.db.WorkoutResultEntity
import kotlinx.coroutines.flow.Flow

interface WorkoutResultRepository {
    fun getHistory(): Flow<List<WorkoutResultEntity>>
    suspend fun getById(id: Long): WorkoutResultEntity?
    suspend fun save(entity: WorkoutResultEntity): Long
}
