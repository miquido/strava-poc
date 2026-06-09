package com.miquido.stravapoc.wear.presentation.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapPage(
    state: WorkoutViewState.Active,
    onBackToMetrics: () -> Unit
) {
    val routeLatLngs = state.routePoints.map { LatLng(it.lat, it.lng) }

    val trackedLatLngsState = remember { mutableStateOf<List<LatLng>>(emptyList()) }
    SideEffect {
        trackedLatLngsState.value = state.trackedPoints.map { LatLng(it.lat, it.lng) }
    }

    val initialCenter = state.currentLocation?.let { LatLng(it.lat, it.lng) }
        ?: routeLatLngs.firstOrNull()
        ?: LatLng(52.0, 21.0)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCenter, 17f)
    }

    LaunchedEffect(state.currentLocation) {
        state.currentLocation?.let { loc ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLng(LatLng(loc.lat, loc.lng)),
                durationMs = 1500
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false),
            properties = MapProperties(mapType = MapType.TERRAIN)
        ) {
            if (routeLatLngs.size >= 2) {
                Polyline(points = routeLatLngs, color = Color.Blue, width = 6f)
            }

            val trackedLatLngs = trackedLatLngsState.value
            if (trackedLatLngs.size >= 2) {
                Polyline(points = trackedLatLngs, color = Color(0xFFFC4C02), width = 6f)
            }

            state.currentLocation?.let { loc ->
                Marker(
                    state = MarkerState(position = LatLng(loc.lat, loc.lng)),
                    title = "Position"
                )
            }
        }

        // Przycisk powrotu do metryk — lewa strona, środek ekranu
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .size(36.dp)
                .background(color = Color.Black.copy(alpha = 0.45f), shape = CircleShape)
                .clickable { onBackToMetrics() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to metrics",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
