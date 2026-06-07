package com.miquido.stravapoc.presentation.routelist

sealed class RouteListSideEffect {
    data class NavigateToDetail(val routeId: String) : RouteListSideEffect()
}
