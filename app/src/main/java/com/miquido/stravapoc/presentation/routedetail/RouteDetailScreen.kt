package com.miquido.stravapoc.presentation.routedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.miquido.stravapoc.R
import com.miquido.stravapoc.presentation.routedetail.RouteDetailSideEffect.RouteSentError
import com.miquido.stravapoc.presentation.routedetail.RouteDetailSideEffect.RouteSentSuccess
import kotlinx.coroutines.flow.filterIsInstance

@Composable
internal fun RouteDetailScreen(
    onBack: () -> Unit,
    viewModel: RouteDetailViewModel = hiltViewModel()
) {
    val state by viewModel.viewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val msgSuccess = stringResource(R.string.route_sent_success)
    val msgError = stringResource(R.string.route_sent_error)

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.filterIsInstance<RouteDetailSideEffect>().collect { effect ->
            when (effect) {
                RouteSentSuccess ->
                    snackbarHostState.showSnackbar(msgSuccess)

                is RouteSentError ->
                    snackbarHostState.showSnackbar(msgError.format(effect.message))
            }
        }
    }

    RouteDetailContent(
        state = state,
        onBack = onBack,
        onSendToWatch = viewModel::onSendToWatch,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteDetailContent(
    state: RouteDetailViewState,
    onBack: () -> Unit,
    onSendToWatch: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.route?.name ?: stringResource(R.string.route_detail_fallback_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    text = stringResource(R.string.error_message, state.error),
                    modifier = Modifier.align(Alignment.Center)
                )

                state.route != null -> {
                    val route = state.route
                    val points = route.points.map { LatLng(it.lat, it.lng) }
                    val center = points.firstOrNull() ?: LatLng(52.0, 21.0)
                    val cameraState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(center, 14f)
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        GoogleMap(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            cameraPositionState = cameraState,
                            uiSettings = MapUiSettings(zoomControlsEnabled = false),
                            properties = MapProperties(mapType = MapType.TERRAIN)
                        ) {
                            if (points.size >= 2) {
                                Polyline(points = points, width = 8f)
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(
                                    R.string.route_distance_info,
                                    route.distanceKm,
                                    route.points.size
                                ),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onSendToWatch,
                                enabled = !state.isSending,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (state.isSending) {
                                    CircularProgressIndicator()
                                } else {
                                    Text(stringResource(R.string.send_to_watch))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
