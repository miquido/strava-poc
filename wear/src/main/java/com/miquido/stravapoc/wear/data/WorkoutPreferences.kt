package com.miquido.stravapoc.wear.data

import android.content.Context

object WorkoutPreferences {
    private const val PREFS_NAME = "workout_state"
    private const val KEY_ROUTE_ID = "active_route_id"

    fun save(context: Context, routeId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_ROUTE_ID, routeId).apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().remove(KEY_ROUTE_ID).apply()
    }

    fun getActiveRouteId(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ROUTE_ID, null)
            ?.takeIf { it.isNotEmpty() }
}
