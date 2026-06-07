package com.miquido.stravapoc.wear.presentation.workout

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutScreen(
    routeId: String,
    onNavigateToSummary: (distanceKm: Double, durationSecs: Long, laps: Int) -> Unit,
    viewModel: WorkoutViewModel = viewModel(
        factory = WorkoutViewModel.factory(
            routeId,
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val state by viewModel.viewState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.collectSideEffect<WorkoutSideEffect>(this) { effect ->
            when (effect) {
                is WorkoutSideEffect.NavigateToSummary ->
                    onNavigateToSummary(effect.distanceKm, effect.durationSecs, effect.laps)
                is WorkoutSideEffect.ShowError -> { /* błąd ładowania — można pokazać dialog */ }
            }
        }
    }

    when (val s = state) {
        is WorkoutViewState.Idle -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is WorkoutViewState.Active -> WorkoutPager(
            state = s,
            isPaused = false,
            onPauseResume = viewModel::onPause,
            onLap = viewModel::onLap,
            onFinish = viewModel::onFinish
        )
        is WorkoutViewState.Paused -> WorkoutPager(
            state = s.snapshot,
            isPaused = true,
            onPauseResume = viewModel::onResume,
            onLap = viewModel::onLap,
            onFinish = viewModel::onFinish
        )
        is WorkoutViewState.Finished -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Kończenie biegu...")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkoutPager(
    state: WorkoutViewState.Active,
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onLap: () -> Unit,
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    HorizontalPager(state = pagerState) { page ->
        when (page) {
            0 -> MetricsPage(
                state = state,
                isPaused = isPaused,
                onPauseResume = onPauseResume,
                onLap = onLap,
                onFinish = onFinish
            )
            1 -> MapPage(state = state)
        }
    }
}
