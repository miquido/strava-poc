package com.miquido.stravapoc.data.usecase

import com.miquido.stravapoc.data.db.WorkoutResultEntity
import com.miquido.stravapoc.data.repository.WorkoutResultRepository
import kotlinx.coroutines.flow.Flow

class GetWorkoutHistoryUseCase(private val repository: WorkoutResultRepository) {
    operator fun invoke(): Flow<List<WorkoutResultEntity>> = repository.getHistory()
}
