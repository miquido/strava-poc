package com.miquido.stravapoc.wear.presentation.summary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WorkoutSummaryScreen(
    totalDistanceKm: Double,
    totalSeconds: Long,
    laps: Int,
    onSave: suspend () -> Boolean,
    onNavigateBack: () -> Unit,
    onDiscard: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showDiscardConfirmation by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var snackbarText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(snackbarText) {
        if (snackbarText != null) {
            delay(3000L)
            onNavigateBack()
        }
    }

    if (showDiscardConfirmation) {
        DiscardConfirmationScreen(
            onConfirm = onDiscard,
            onCancel = { showDiscardConfirmation = false }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, top = 44.dp, end = 20.dp, bottom = 60.dp),
                autoCentering = null,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "Workout complete!",
                        style = MaterialTheme.typography.title3
                    )
                }
                item {
                    Text(
                        text = "Distance: %.2f km".format(totalDistanceKm),
                        style = MaterialTheme.typography.body1
                    )
                }
                item {
                    Text(
                        text = "Time: ${formatDuration(totalSeconds)}",
                        style = MaterialTheme.typography.body1
                    )
                }
                item {
                    Text(
                        text = "Laps: $laps",
                        style = MaterialTheme.typography.body1
                    )
                }
                item {
                    Chip(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (!isSaving) {
                                isSaving = true
                                scope.launch {
                                    val sent = onSave()
                                    snackbarText = if (sent) {
                                        "Synced with phone ✓"
                                    } else {
                                        "Saved locally — will sync when phone connects"
                                    }
                                }
                            }
                        },
                        colors = if (isSaving)
                            ChipDefaults.secondaryChipColors()
                        else
                            ChipDefaults.primaryChipColors(),
                        label = {
                            Text(
                                text = if (isSaving) "Saving…" else "Save",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    )
                }
                item {
                    Chip(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { if (!isSaving) showDiscardConfirmation = true },
                        colors = ChipDefaults.secondaryChipColors(),
                        label = {
                            Text(
                                "Discard",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = snackbarText != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colors.surface,
                            shape = RoundedCornerShape(percent = 50)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = snackbarText ?: "",
                        style = MaterialTheme.typography.caption1,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscardConfirmationScreen(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Discard workout?",
            style = MaterialTheme.typography.title3,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Data will not be saved.",
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Chip(
            modifier = Modifier.fillMaxWidth(0.8f),
            onClick = onConfirm,
            colors = ChipDefaults.primaryChipColors(),
            label = {
                Text(
                    "Yes, discard",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Chip(
            modifier = Modifier.fillMaxWidth(0.8f),
            onClick = onCancel,
            colors = ChipDefaults.secondaryChipColors(),
            label = {
                Text(
                    "Go back",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
