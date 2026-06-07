package com.miquido.stravapoc.data.repository

import com.miquido.stravapoc.data.db.WorkoutResultDao
import com.miquido.stravapoc.data.db.WorkoutResultEntity
import kotlinx.coroutines.flow.Flow

class WorkoutResultRepositoryImpl(
    private val dao: WorkoutResultDao
) : WorkoutResultRepository {
    override fun getHistory(): Flow<List<WorkoutResultEntity>> = dao.getAllAsFlow()
    override suspend fun getById(id: Long): WorkoutResultEntity? = dao.getById(id)
    override suspend fun save(entity: WorkoutResultEntity): Long = dao.insert(entity)
}
