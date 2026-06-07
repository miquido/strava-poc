package com.miquido.stravapoc.wear.presentation.activityselection

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.miquido.stravapoc.core.architecture.mvi.MviDefaultConfig
import com.miquido.stravapoc.core.architecture.mvi.MviViewModel
import com.miquido.stravapoc.library.data.model.Route
import com.miquido.stravapoc.wear.data.local.ReceivedRouteEntity
import com.miquido.stravapoc.wear.data.local.WearDatabase
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private const val DEFAULT_ROUTE_ID = "warsaw_lazienki"
private const val TAG = "ActivitySelectionVM"

class ActivitySelectionViewModel(
    private val dataClient: DataClient,
    private val appContext: Context
) : MviViewModel<ActivitySelectionViewState>(ActivitySelectionViewState(), MviDefaultConfig()) {

    private val dao = WearDatabase.getInstance(appContext).receivedRouteDao()

    private val dataListener = DataClient.OnDataChangedListener { events ->
        events
            .filter { it.dataItem.uri.path == "/route/selected" }
            .forEach { event ->
                val json = DataMapItem.fromDataItem(event.dataItem)
                    .dataMap
                    .getString("route_json") ?: return@forEach
                Log.d(TAG, "KUBAS route received via foreground DataClient listener")
                saveToRoomAndApply(json)
            }
    }

    init {
        dataClient.addListener(dataListener)
        observeRouteFromDb()
        checkExistingDataLayerItems()
    }

    private fun observeRouteFromDb() {
        viewModelScope.launch {
            dao.observeRoute().collect { entity ->
                if (entity != null) {
                    Log.d(TAG, "KUBAS route loaded from Room: ${entity.routeJson.take(50)}")
                    applyRouteJson(entity.routeJson)
                }
            }
        }
    }

    private fun checkExistingDataLayerItems() {
        dataClient.dataItems.addOnSuccessListener { items ->
            items.filter { it.uri.path == "/route/selected" }
                .forEach { item ->
                    val json = DataMapItem.fromDataItem(item)
                        .dataMap
                        .getString("route_json") ?: return@forEach
                    Log.d(TAG, "KUBAS route found in existing DataLayer items")
                    saveToRoomAndApply(json)
                }
            items.release()
        }
    }

    private fun saveToRoomAndApply(json: String) {
        viewModelScope.launch {
            dao.saveRoute(ReceivedRouteEntity(routeJson = json))
        }
        applyRouteJson(json)
    }

    private fun applyRouteJson(json: String) {
        val route = runCatching { Json.decodeFromString<Route>(json) }
            .getOrNull() ?: return
        transform { copy(receivedRouteId = route.id, receivedRouteName = route.name) }
    }

    fun onActivitySelected(activityId: String) = launch {
        val routeId = viewState.value.receivedRouteId ?: DEFAULT_ROUTE_ID
        emitSideEffect(ActivitySelectionSideEffect.NavigateToWorkout(activityId, routeId))
    }

    override fun onCleared() {
        dataClient.removeListener(dataListener)
        super.onCleared()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ActivitySelectionViewModel(
                    dataClient = Wearable.getDataClient(context),
                    appContext = context.applicationContext
                )
            }
        }
    }
}
