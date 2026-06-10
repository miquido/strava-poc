package com.miquido.stravapoc.library.data.db.wear

import javax.inject.Inject

class PendingWorkoutResultLocalDataSource @Inject constructor(private val dao: PendingWorkoutResultDao) {

    suspend fun getAll(): List<PendingWorkoutResultEntity> = dao.getAll()

    suspend fun insert(entity: PendingWorkoutResultEntity): Long = dao.insert(entity)

    suspend fun delete(entity: PendingWorkoutResultEntity) = dao.delete(entity)
}
