package com.miquido.stravapoc.di

import android.content.Context
import com.miquido.stravapoc.data.db.AppDatabase
import com.miquido.stravapoc.data.repository.WorkoutResultRepositoryImpl
import com.miquido.stravapoc.data.usecase.GetWorkoutHistoryUseCase
import com.miquido.stravapoc.data.usecase.GetWorkoutResultByIdUseCase
import com.miquido.stravapoc.data.usecase.SaveWorkoutResultUseCase
import com.miquido.stravapoc.library.data.datasource.RouteLocalDataSource
import com.miquido.stravapoc.library.data.repository.RouteRepository
import com.miquido.stravapoc.library.data.repository.RouteRepositoryImpl
import com.miquido.stravapoc.library.data.usecase.GetRouteByIdUseCase
import com.miquido.stravapoc.library.data.usecase.GetRoutesUseCase

object AppModule {
    private val routeDataSource = RouteLocalDataSource()
    private val routeRepository: RouteRepository = RouteRepositoryImpl(routeDataSource)

    val getRoutesUseCase = GetRoutesUseCase(routeRepository)
    val getRouteByIdUseCase = GetRouteByIdUseCase(routeRepository)

    fun provideWorkoutUseCases(context: Context): WorkoutUseCases {
        val db = AppDatabase.getInstance(context)
        val repo = WorkoutResultRepositoryImpl(db.workoutResultDao())
        return WorkoutUseCases(
            getHistory = GetWorkoutHistoryUseCase(repo),
            getById = GetWorkoutResultByIdUseCase(repo),
            save = SaveWorkoutResultUseCase(repo)
        )
    }
}

data class WorkoutUseCases(
    val getHistory: GetWorkoutHistoryUseCase,
    val getById: GetWorkoutResultByIdUseCase,
    val save: SaveWorkoutResultUseCase
)
