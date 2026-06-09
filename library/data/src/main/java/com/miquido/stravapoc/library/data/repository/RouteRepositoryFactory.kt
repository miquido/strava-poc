package com.miquido.stravapoc.library.data.repository

import com.miquido.stravapoc.library.data.datasource.RouteLocalDataSource

fun createRouteRepository(): RouteRepository = RouteRepositoryImpl(RouteLocalDataSource())
