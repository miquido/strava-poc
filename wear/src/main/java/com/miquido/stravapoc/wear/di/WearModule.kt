package com.miquido.stravapoc.wear.di

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import com.miquido.stravapoc.library.data.db.wear.PendingWorkoutResultDao
import com.miquido.stravapoc.library.data.db.wear.PendingWorkoutResultLocalDataSource
import com.miquido.stravapoc.library.data.db.wear.WearDatabase
import com.miquido.stravapoc.library.data.db.wear.WearRouteDao
import com.miquido.stravapoc.library.data.repository.WearRouteRepository
import com.miquido.stravapoc.library.data.repository.WearRouteRepositoryImpl
import com.miquido.stravapoc.wear.data.WorkoutResultSender
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WearModule {

    @Binds
    @Singleton
    abstract fun bindWearRouteRepository(impl: WearRouteRepositoryImpl): WearRouteRepository

    companion object {

        @Provides
        @Singleton
        fun provideWearDatabase(@ApplicationContext context: Context): WearDatabase =
            WearDatabase.getInstance(context)

        @Provides
        @Singleton
        fun provideWearRouteDao(database: WearDatabase): WearRouteDao =
            database.wearRouteDao()

        @Provides
        @Singleton
        fun providePendingWorkoutResultDao(database: WearDatabase): PendingWorkoutResultDao =
            database.pendingWorkoutResultDao()

        @Provides
        @Singleton
        fun provideWorkoutResultSender(
            @ApplicationContext context: Context,
            pendingDataSource: PendingWorkoutResultLocalDataSource,
        ): WorkoutResultSender = WorkoutResultSender(context, pendingDataSource)

        @Provides
        @Singleton
        fun provideDataClient(@ApplicationContext context: Context): DataClient =
            Wearable.getDataClient(context)
    }
}
