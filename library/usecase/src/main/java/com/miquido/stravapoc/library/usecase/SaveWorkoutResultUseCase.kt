package com.miquido.stravapoc.library.usecase

import com.miquido.stravapoc.library.data.model.WorkoutResult
import com.miquido.stravapoc.library.data.repository.WorkoutResultRepository
import javax.inject.Inject

class SaveWorkoutResultUseCase @Inject constructor(private val repository: WorkoutResultRepository) {
    suspend operator fun invoke(result: WorkoutResult): Long = repository.save(result)
}
