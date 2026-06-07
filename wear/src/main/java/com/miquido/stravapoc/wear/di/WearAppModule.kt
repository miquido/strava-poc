package com.miquido.stravapoc.wear.di

import android.content.Context
import com.miquido.stravapoc.library.data.datasource.RouteLocalDataSource
import com.miquido.stravapoc.library.data.repository.RouteRepository
import com.miquido.stravapoc.library.data.repository.RouteRepositoryImpl
import com.miquido.stravapoc.library.data.usecase.GetRouteByIdUseCase
import com.miquido.stravapoc.wear.data.WorkoutResultSender
import com.miquido.stravapoc.wear.data.local.WearDatabase

object WearAppModule {
    private val dataSource = RouteLocalDataSource()
    private val repository: RouteRepository = RouteRepositoryImpl(dataSource)

    val getRouteByIdUseCase = GetRouteByIdUseCase(repository)

    fun workoutResultSender(context: Context) = WorkoutResultSender(context)

    fun database(context: Context) = WearDatabase.getInstance(context)
}
