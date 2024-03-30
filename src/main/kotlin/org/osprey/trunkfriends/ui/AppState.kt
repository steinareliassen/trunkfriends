package org.osprey.trunkfriends.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.dal.HistoryData
import org.osprey.trunkfriends.ui.history.HistoryViewState

class AppState(var configMap: MutableList<Pair<String, Config>>) {
    var selectedConfig by mutableStateOf<Pair<String, Config>?>(null)

    var zoomedName by mutableStateOf<String?>(null)
    var view by mutableStateOf(View.ABOUT)
    var returnView = View.HISTORY
    var managementAction by mutableStateOf<ManagementAction?>(null)
    val pasteBag = PasteBag()
    var actionList = listOf<String>()
    private var _history: HistoryData? = null
    val history: HistoryData
        get() {
            return _history ?: throw IllegalStateException("No server selected ")
        }

    var networkTaskActive by mutableStateOf(false)

    var historyViewState = HistoryViewState()

    fun getSelectedConfig() =
        selectedConfig?.first ?: "No Server"

    var onServerChanged: (() -> Unit) = {}

    fun onServerSelect(selectView: View, setConfig: Pair<String, Config>?) {
        selectedConfig = setConfig
        zoomedName = null
        view = selectView
        _history = HistoryData(getSelectedConfig())
        onServerChanged()
    }

    val changeZoom = { name: String?, oldView: View ->
        zoomedName = name
        if (zoomedName != null) {
            returnView = oldView
            view = View.HISTORY
        } else {
            view = returnView
        }
    }

    fun changeView(newView: View) {
        if (view != newView) {
            view = newView
        }
    }
}