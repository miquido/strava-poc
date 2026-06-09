package com.miquido.stravapoc.sync

import com.miquido.stravapoc.library.data.model.Route

internal interface RouteSender {
    suspend fun send(route: Route): Result<Unit>
}
