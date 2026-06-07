package com.miquido.stravapoc.presentation.history

sealed class HistorySideEffect {
    data class NavigateToDetail(val id: Long) : HistorySideEffect()
}
