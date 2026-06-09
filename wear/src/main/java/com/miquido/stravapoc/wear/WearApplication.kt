package com.miquido.stravapoc.wear

import android.app.Application
import com.miquido.stravapoc.wear.data.WorkoutResultSender
import com.miquido.stravapoc.wear.di.WearAppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WearApplication : Application() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        WearAppModule.init(this)
        scope.launch {
            WorkoutResultSender(applicationContext).retrySendPending()
        }
    }
}
