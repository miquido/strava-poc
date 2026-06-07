package com.miquido.stravapoc.library.data.usecase

import com.miquido.stravapoc.library.data.model.Route
import com.miquido.stravapoc.library.data.repository.RouteRepository

class GetRouteByIdUseCase(private val repository: RouteRepository) {
    suspend operator fun invoke(id: String): Result<Route> = repository.getRouteById(id)
}
