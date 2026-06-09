package com.miquido.stravapoc.library.data.di

import android.content.Context
import com.miquido.stravapoc.library.data.db.AppDatabase
import com.miquido.stravapoc.library.data.db.dao.WorkoutResultDao
import com.miquido.stravapoc.library.data.repository.RouteRepository
import com.miquido.stravapoc.library.data.repository.RouteRepositoryImpl
import com.miquido.stravapoc.library.data.repository.WorkoutResultRepository
import com.miquido.stravapoc.library.data.repository.WorkoutResultRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindRouteRepository(impl: RouteRepositoryImpl): RouteRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutResultRepository(impl: WorkoutResultRepositoryImpl): WorkoutResultRepository

    companion object {

        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
            AppDatabase.getInstance(context)

        @Provides
        fun provideWorkoutResultDao(db: AppDatabase): WorkoutResultDao =
            db.workoutResultDao()
    }
}
