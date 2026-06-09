package com.miquido.stravapoc.wear.di

import android.content.Context
import com.miquido.stravapoc.library.data.model.RoutePoint
import com.miquido.stravapoc.library.data.repository.RouteRepository
import com.miquido.stravapoc.library.usecase.GetRouteByIdUseCase
import com.miquido.stravapoc.library.usecase.GetRoutesUseCase
import com.miquido.stravapoc.wear.data.WorkoutResultSender
import com.miquido.stravapoc.wear.data.local.WearDatabase
import com.miquido.stravapoc.wear.data.repository.WearRouteRepositoryImpl

object WearAppModule {

    private lateinit var _repository: RouteRepository

    lateinit var getRouteByIdUseCase: GetRouteByIdUseCase
        private set
    lateinit var getRoutesUseCase: GetRoutesUseCase
        private set

    /** Buffer for tracked GPS points between WorkoutService.finish() and MainActivity.onSave() */
    var pendingTrackedPoints: List<RoutePoint> = emptyList()

    fun init(context: Context) {
        val db = WearDatabase.getInstance(context)
        _repository = WearRouteRepositoryImpl(db.wearRouteDao())
        getRouteByIdUseCase = GetRouteByIdUseCase(_repository)
        getRoutesUseCase = GetRoutesUseCase(_repository)
    }

    fun workoutResultSender(context: Context) = WorkoutResultSender(context)
    fun database(context: Context) = WearDatabase.getInstance(context)
}
