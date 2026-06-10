package com.miquido.stravapoc.library.usecase

import com.miquido.stravapoc.library.data.model.Route
import com.miquido.stravapoc.library.data.repository.WearRouteRepository
import javax.inject.Inject

class GetMostRecentWearRouteUseCase @Inject constructor(private val repository: WearRouteRepository) {
    suspend operator fun invoke(): Route? = repository.getMostRecent()
}
