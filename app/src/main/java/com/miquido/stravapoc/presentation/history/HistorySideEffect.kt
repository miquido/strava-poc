package com.miquido.stravapoc.presentation.history

internal sealed class HistorySideEffect {
    data class NavigateToDetail(val id: Long) : HistorySideEffect()
}
