package com.miquido.stravapoc.wear.data

import android.content.Context

/**
 * Persists the active workout's routeId across app restarts.
 * When a workout is in progress, re-opening the app jumps directly to WorkoutScreen.
 */
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

    /** Returns null when no workout is active, otherwise the routeId (may be CUSTOM_ROUTE_ID).
     *  Empty string is treated as absent to guard against stale state from older app versions. */
    fun getActiveRouteId(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ROUTE_ID, null)
            ?.takeIf { it.isNotEmpty() }
}
