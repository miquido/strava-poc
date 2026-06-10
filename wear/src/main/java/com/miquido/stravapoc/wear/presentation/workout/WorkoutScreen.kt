package com.miquido.stravapoc.wear.presentation.workout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.Text
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutScreen(
    isAmbient: Boolean = false,
    onNavigateToSummary: (routeId: String, distanceKm: Double, durationSecs: Long, laps: Int) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val state by viewModel.viewState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.collectSideEffect<WorkoutSideEffect>(this) { effect ->
            when (effect) {
                is WorkoutSideEffect.NavigateToSummary ->
                    onNavigateToSummary(effect.routeId, effect.distanceKm, effect.durationSecs, effect.laps)
            }
        }
    }

    when (val s = state) {
        is WorkoutViewState.Idle -> {
            Box(modifier = Modifier.fillMaxSize())
        }
        is WorkoutViewState.Active -> {
            if (isAmbient) {
                AmbientWorkoutScreen(
                    elapsedSeconds = s.elapsedSeconds,
                    distanceKm = s.distanceKm
                )
            } else {
                WorkoutPager(
                    state = s,
                    isPaused = false,
                    onPauseResume = viewModel::onPause,
                    onLap = viewModel::onLap,
                    onFinish = viewModel::onFinish
                )
            }
        }
        is WorkoutViewState.Paused -> {
            if (isAmbient) {
                AmbientWorkoutScreen(
                    elapsedSeconds = s.snapshot.elapsedSeconds,
                    distanceKm = s.snapshot.distanceKm
                )
            } else {
                WorkoutPager(
                    state = s.snapshot,
                    isPaused = true,
                    onPauseResume = viewModel::onResume,
                    onLap = viewModel::onLap,
                    onFinish = viewModel::onFinish
                )
            }
        }
        is WorkoutViewState.Finished -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Finishing workout...")
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
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val pageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float get() = pagerState.currentPageOffsetFraction
            override val selectedPage: Int get() = pagerState.currentPage
            override val pageCount: Int get() = 2
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .onRotaryScrollEvent { event ->
                    val delta = event.verticalScrollPixels
                    if (delta > 0f) {
                        scope.launch {
                            pagerState.animateScrollToPage(minOf(pagerState.currentPage + 1, 1))
                        }
                    } else if (delta < 0f) {
                        scope.launch {
                            pagerState.animateScrollToPage(maxOf(pagerState.currentPage - 1, 0))
                        }
                    }
                    true
                },
            userScrollEnabled = pagerState.settledPage == 0
        ) { page ->
            when (page) {
                0 -> MetricsPage(
                    state = state,
                    isPaused = isPaused,
                    onPauseResume = onPauseResume,
                    onLap = onLap,
                    onFinish = onFinish
                )
                1 -> MapPage(
                    state = state,
                    onBackToMetrics = {
                        scope.launch { pagerState.animateScrollToPage(0) }
                    }
                )
            }
        }

        HorizontalPageIndicator(
            pageIndicatorState = pageIndicatorState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
        )
    }
}
