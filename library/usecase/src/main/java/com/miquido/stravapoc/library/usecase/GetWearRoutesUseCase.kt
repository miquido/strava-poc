package com.miquido.stravapoc.library.usecase

import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.Route
import com.miquido.stravapoc.library.data.repository.WearRouteRepository
import javax.inject.Inject

class GetWearRoutesUseCase @Inject constructor(private val repository: WearRouteRepository) {
    suspend operator fun invoke(type: ActivityType? = null): Result<List<Route>> =
        repository.getRoutes(type)
}
