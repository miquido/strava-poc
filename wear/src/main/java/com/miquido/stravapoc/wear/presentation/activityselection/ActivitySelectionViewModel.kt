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
import com.miquido.stravapoc.library.data.model.ActivityType
import com.miquido.stravapoc.library.data.model.Route
import com.miquido.stravapoc.wear.data.local.WearDatabase
import com.miquido.stravapoc.wear.data.repository.toWearEntity
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private const val TAG = "ActivitySelectionVM"

class ActivitySelectionViewModel(
    private val dataClient: DataClient,
    private val appContext: Context
) : MviViewModel<ActivitySelectionViewState>(ActivitySelectionViewState(), MviDefaultConfig()) {

    private val dao = WearDatabase.getInstance(appContext).wearRouteDao()

    private val dataListener = DataClient.OnDataChangedListener { events ->
        events
            .filter { it.dataItem.uri.path == "/route/selected" }
            .forEach { event ->
                val uri = event.dataItem.uri
                val json = DataMapItem.fromDataItem(event.dataItem)
                    .dataMap
                    .getString("route_json") ?: return@forEach
                Log.d(TAG, "route received via foreground DataClient listener")
                saveToRoomAndApply(json, uri)
            }
    }

    init {
        dataClient.addListener(dataListener)
        loadMostRecentRouteFromDb()
        checkExistingDataLayerItems()
    }

    private fun loadMostRecentRouteFromDb() {
        viewModelScope.launch {
            val entity = dao.getAll().firstOrNull()
            if (entity != null) {
                Log.d(TAG, "route loaded from Room: ${entity.name}")
                transform { copy(receivedRouteId = entity.id, receivedRouteName = entity.name) }
            }
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
                    saveToRoomAndApply(json, uri)
                }
            items.release()
        }
    }

    private fun saveToRoomAndApply(json: String, uri: android.net.Uri) {
        val route = runCatching { Json.decodeFromString<Route>(json) }
            .getOrNull() ?: return
        viewModelScope.launch {
            dao.insert(route.toWearEntity())
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
