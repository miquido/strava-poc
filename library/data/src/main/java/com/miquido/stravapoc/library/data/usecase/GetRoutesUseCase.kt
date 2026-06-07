package com.miquido.stravapoc.library.data.usecase

import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.Route
import com.miquido.stravapoc.library.data.repository.RouteRepository

class GetRoutesUseCase(private val repository: RouteRepository) {
    suspend operator fun invoke(type: ActivityType? = null): Result<List<Route>> =
        repository.getRoutes(type)
}
