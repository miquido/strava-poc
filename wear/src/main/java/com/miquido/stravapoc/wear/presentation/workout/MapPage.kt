package com.miquido.stravapoc.wear.presentation.workout

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapPage(state: WorkoutViewState.Active) {
    val points = state.routePoints.map { LatLng(it.lat, it.lng) }
    val currentPos = points.getOrNull(state.routePointIndex)
    val center = currentPos ?: points.firstOrNull() ?: LatLng(52.0, 21.0)

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 15f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraState
    ) {
        if (points.size >= 2) {
            Polyline(
                points = points,
                color = Color.Blue,
                width = 6f
            )
        }
        currentPos?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Aktualna pozycja"
            )
        }
    }
}
