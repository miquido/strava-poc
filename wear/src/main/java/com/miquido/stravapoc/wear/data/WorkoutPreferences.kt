package com.miquido.stravapoc.wear.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class WorkoutPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(routeId: String) {
        prefs.edit { putString(KEY_ROUTE_ID, routeId) }
    }

    fun clear() {
        prefs.edit { remove(KEY_ROUTE_ID) }
    }

    fun getActiveRouteId(): String? =
        prefs.getString(KEY_ROUTE_ID, null)?.takeIf { it.isNotEmpty() }

    companion object {
        private const val PREFS_NAME = "workout_state"
        private const val KEY_ROUTE_ID = "active_route_id"
    }
}
