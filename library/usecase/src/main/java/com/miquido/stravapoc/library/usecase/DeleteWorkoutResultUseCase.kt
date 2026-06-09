package com.miquido.stravapoc.library.usecase

import com.miquido.stravapoc.library.data.repository.WorkoutResultRepository
import javax.inject.Inject

class DeleteWorkoutResultUseCase @Inject constructor(
    private val repository: WorkoutResultRepository
) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
}
