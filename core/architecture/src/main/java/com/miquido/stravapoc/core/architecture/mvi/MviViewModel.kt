package com.miquido.stravapoc.core.architecture.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

abstract class MviViewModel<STATE : Any>(
    initState: STATE,
    config: MviConfig = MviDefaultConfig(),
    private val container: MviContainer<STATE> = MviStateContainer(initState, config)
) : ViewModel() {

    val viewState: StateFlow<STATE> = container.viewState
    val sideEffect: SharedFlow<Any> = container.sideEffect

    protected fun launch(block: suspend () -> Unit): Job =
        container.launch(viewModelScope, block)

    protected fun transform(transformer: STATE.() -> STATE) =
        container.transform(transformer)

    protected suspend fun emitSideEffect(value: Any) =
        container.emitSideEffect(value)

    inline fun <reified T : Any> collectSideEffect(
        scope: CoroutineScope,
        crossinline collector: suspend (T) -> Unit
    ) = scope.launch {
        sideEffect.filterIsInstance<T>().collect { collector(it) }
    }
}
