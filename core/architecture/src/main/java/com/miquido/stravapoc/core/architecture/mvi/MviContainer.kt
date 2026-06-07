package com.miquido.stravapoc.core.architecture.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface MviContainer<STATE : Any> {
    val viewState: StateFlow<STATE>
    val sideEffect: SharedFlow<Any>
    fun launch(scope: CoroutineScope, block: suspend () -> Unit): Job
    suspend fun emitSideEffect(value: Any)
    fun transform(transformer: STATE.() -> STATE)
}

internal class MviStateContainer<STATE : Any>(
    initState: STATE,
    private val config: MviConfig
) : MviContainer<STATE> {

    private val _viewState = MutableStateFlow(initState)
    override val viewState: StateFlow<STATE> = _viewState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<Any>()
    override val sideEffect: SharedFlow<Any> = _sideEffect.asSharedFlow()

    override fun launch(scope: CoroutineScope, block: suspend () -> Unit): Job =
        scope.launch(config.launchDispatcher + config.exceptionHandler) { block() }

    override suspend fun emitSideEffect(value: Any) = _sideEffect.emit(value)

    override fun transform(transformer: STATE.() -> STATE) = _viewState.update(transformer)
}
