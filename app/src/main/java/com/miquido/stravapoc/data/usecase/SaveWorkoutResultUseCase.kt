package com.miquido.stravapoc.data.usecase

import com.miquido.stravapoc.data.db.WorkoutResultEntity
import com.miquido.stravapoc.data.repository.WorkoutResultRepository

class SaveWorkoutResultUseCase(private val repository: WorkoutResultRepository) {
    suspend operator fun invoke(entity: WorkoutResultEntity): Long = repository.save(entity)
}
