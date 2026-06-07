package com.miquido.stravapoc.core.architecture.mvi

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers

interface MviConfig {
    val exceptionHandler: CoroutineExceptionHandler
    val launchDispatcher: CoroutineDispatcher
}

class MviDefaultConfig(
    override val launchDispatcher: CoroutineDispatcher = Dispatchers.Default,
    override val exceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("MVI", "Unhandled exception", throwable)
    }
) : MviConfig
