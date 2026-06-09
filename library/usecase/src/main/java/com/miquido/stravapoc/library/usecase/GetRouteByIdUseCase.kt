package com.miquido.stravapoc.library.usecase

import com.miquido.stravapoc.library.data.model.Route
import com.miquido.stravapoc.library.data.repository.RouteRepository
import javax.inject.Inject

class GetRouteByIdUseCase @Inject constructor(private val repository: RouteRepository) {
    suspend operator fun invoke(id: String): Result<Route> = repository.getRouteById(id)
}
