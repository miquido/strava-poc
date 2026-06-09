package com.miquido.stravapoc.library.data.repository

import com.miquido.stravapoc.library.data.datasource.RouteLocalDataSource

/**
 * Public factory for modules that cannot access internal [RouteRepositoryImpl] directly.
 */
fun createRouteRepository(): RouteRepository = RouteRepositoryImpl(RouteLocalDataSource())
