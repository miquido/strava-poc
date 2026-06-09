package com.miquido.stravapoc.di

import android.content.Context
import com.miquido.stravapoc.sync.RouteSender
import com.miquido.stravapoc.sync.WearSyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object SyncModule {

    @Provides
    @Singleton
    fun provideRouteSender(@ApplicationContext context: Context): RouteSender =
        WearSyncManager(context)
}
