package org.osprey.trunkfriends.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.historyhandler.refresh
import org.osprey.trunkfriends.managementhandler.managementAction
import org.osprey.trunkfriends.ui.history.HistoryViewState

class UIState(var configMap: MutableList<Pair<String, Config>>) {
    var selectedConfig by mutableStateOf<Pair<String, Config>?>(null)
    var selectServerDropDownState by mutableStateOf(false)
    var menuDrownDownState by mutableStateOf(false)

    var feedback by mutableStateOf("Refreshing")
    var zoomedName by mutableStateOf<String?>(null)
    var view by mutableStateOf("About")
    var activeButtons by mutableStateOf(true)
    private var coroutineScope = CoroutineScope(Dispatchers.Main)

    var refreshActive = false

    var historyViewState = HistoryViewState()

    fun startListRefresh() {
        if (refreshActive) return
        refreshActive = true
        coroutineScope.launch {
            activeButtons = false
            refresh(
                selectedConfig ?: throw IllegalStateException("Should not be null"),
                { !refreshActive }
            ) { param ->
                feedback = param
            }
            refreshActive = false
            activeButtons = true
            view = "History"
            historyViewState.reset()
        }
    }

    fun startExecuteManagementAction(action: String, accounts: List<String>) {
        if (refreshActive) return
        refreshActive = true
        coroutineScope.launch {
            activeButtons = false
            managementAction(
                accounts,
                action,
                selectedConfig ?: throw IllegalStateException("Should not be null"),
                { !refreshActive }
            ) { param ->
                feedback = param
            }
            refreshActive = false
            activeButtons = true
            view = "Management"
            historyViewState.reset()
        }
    }
    fun onServerSelect(selectView : String, setConfig : Pair<String, Config>?) {
        selectServerDropDownState = false
        selectedConfig = setConfig
        historyViewState.reset()
        zoomedName = null
        view = selectView
    }

    fun clearSelect() {
        historyViewState.pasteBag.clear()
    }

    fun getSelected(limit : Int = 0) =
        if (historyViewState.pasteBag.isEmpty())
            ""
        else if (limit == 0)
            historyViewState.pasteBag.reduce { acc, s -> "$acc\n$s" }
        else
            historyViewState.pasteBag.take(limit).reduce { acc, s -> "$acc\n$s" }
}