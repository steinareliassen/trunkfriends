package org.osprey.trunkfriends.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.WindowState
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.dal.HistoryData
import org.osprey.trunkfriends.ui.history.HistoryViewState

class AppState(
    val configMap: MutableList<Pair<String, Config>>,
    val windowState: WindowState
) {
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

    val historyViewState = HistoryViewState()

    fun getSelectedConfig() =
        selectedConfig?.first ?: "No Server"

    var onServerChanged: () -> Unit = {}

    fun onServerSelect(selectView: View, setConfig: Pair<String, Config>?) {
        selectedConfig = setConfig
        zoomedName = null
        view = selectView
        _history = HistoryData(getSelectedConfig())
        onServerChanged()
    }

    var onZoomOut: () -> Unit = {}

    val changeZoom = { name: String?, oldView: View ->
        zoomedName = name
        if (zoomedName != null) {
            returnView = oldView
            view = View.HISTORY
        } else {
            onZoomOut()
            view = returnView
        }
    }

    fun changeView(newView: View) {
        if (view != newView) {
            view = newView
        }
    }
}