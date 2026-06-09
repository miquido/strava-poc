package com.miquido.stravapoc.library.usecase

import com.miquido.stravapoc.library.data.datasource.RouteLocalDataSource
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.repository.RouteRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetRoutesUseCaseTest {

    private val useCase = GetRoutesUseCase(RouteRepositoryImpl(RouteLocalDataSource()))

    @Test
    fun `returns only RUNNING routes when type is RUNNING`() = runBlocking {
        val routes = useCase(ActivityType.RUNNING).getOrThrow()
        assertTrue(routes.isNotEmpty())
        assertTrue(routes.all { it.activityType == ActivityType.RUNNING })
    }

    @Test
    fun `returns only CYCLING routes when type is CYCLING`() = runBlocking {
        val routes = useCase(ActivityType.CYCLING).getOrThrow()
        assertTrue(routes.isNotEmpty())
        assertTrue(routes.all { it.activityType == ActivityType.CYCLING })
    }

    @Test
    fun `returns only WALKING routes when type is WALKING`() = runBlocking {
        val routes = useCase(ActivityType.WALKING).getOrThrow()
        assertTrue(routes.isNotEmpty())
        assertTrue(routes.all { it.activityType == ActivityType.WALKING })
    }

    @Test
    fun `null type returns all routes`() = runBlocking {
        val all = useCase(null).getOrThrow()
        val running = useCase(ActivityType.RUNNING).getOrThrow()
        val cycling = useCase(ActivityType.CYCLING).getOrThrow()
        val walking = useCase(ActivityType.WALKING).getOrThrow()
        assertEquals(running.size + cycling.size + walking.size, all.size)
    }

    @Test
    fun `each activity type has exactly 3 routes`() = runBlocking {
        ActivityType.entries.forEach { type ->
            val count = useCase(type).getOrThrow().size
            assertEquals("Expected 3 routes for $type, got $count", 3, count)
        }
    }
}
