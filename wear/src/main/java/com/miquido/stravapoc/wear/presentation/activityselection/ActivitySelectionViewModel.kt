package com.miquido.stravapoc.wear.presentation.activityselection

import android.net.Uri
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataMapItem
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.Route
import com.miquido.stravapoc.library.usecase.GetMostRecentWearRouteUseCase
import com.miquido.stravapoc.library.usecase.SaveWearRouteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.json.Json
import javax.inject.Inject

private const val TAG = "ActivitySelectionVM"

@HiltViewModel
class ActivitySelectionViewModel @Inject constructor(
    private val dataClient: DataClient,
    private val getMostRecentWearRouteUseCase: GetMostRecentWearRouteUseCase,
    private val saveWearRouteUseCase: SaveWearRouteUseCase,
) : MviViewModel<ActivitySelectionViewState>(ActivitySelectionViewState(), MviDefaultConfig()) {

    private val dataListener = DataClient.OnDataChangedListener { events ->
        events
            .filter { it.dataItem.uri.path == "/route/selected" }
            .forEach { event ->
                val uri = event.dataItem.uri
                val json = DataMapItem.fromDataItem(event.dataItem)
                    .dataMap
                    .getString("route_json") ?: return@forEach
                Log.d(TAG, "route received via foreground DataClient listener")
                saveToDbAndApply(json, uri)
            }
    }

    init {
        dataClient.addListener(dataListener)
        loadMostRecentRouteFromDb()
        checkExistingDataLayerItems()
    }

    private fun loadMostRecentRouteFromDb() = launch {
        val route = getMostRecentWearRouteUseCase()
        if (route != null) {
            Log.d(TAG, "route loaded from Room: ${route.name}")
            transform { copy(receivedRouteId = route.id, receivedRouteName = route.name) }
        }
    }

    private fun checkExistingDataLayerItems() {
        dataClient.dataItems.addOnSuccessListener { items ->
            items.filter { it.uri.path == "/route/selected" }
                .forEach { item ->
                    val uri = item.uri
                    val json = DataMapItem.fromDataItem(item)
                        .dataMap
                        .getString("route_json") ?: return@forEach
                    Log.d(TAG, "route found in existing DataLayer items")
                    saveToDbAndApply(json, uri)
                }
            items.release()
        }
    }

    private fun saveToDbAndApply(json: String, uri: Uri) {
        val route = runCatching { Json.decodeFromString<Route>(json) }
            .getOrNull() ?: return
        launch {
            saveWearRouteUseCase(route)
            dataClient.deleteDataItems(uri)
        }
        transform { copy(receivedRouteId = route.id, receivedRouteName = route.name) }
    }

    fun onActivitySelected(activity: ActivityType) = launch {
        emitSideEffect(ActivitySelectionSideEffect.NavigateToRouteList(activity))
    }

    override fun onCleared() {
        dataClient.removeListener(dataListener)
        super.onCleared()
    }
}
