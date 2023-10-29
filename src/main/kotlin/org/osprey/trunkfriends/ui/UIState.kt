package org.osprey.trunkfriends.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.historyhandler.refresh

class UIState(var configMap: List<Pair<String, Config>>) {
    var page by mutableStateOf(0)
    var timeslotPage by mutableStateOf(0)
    var selectedConfig by mutableStateOf<Pair<String, Config>?>(null)
    var dropDownState by mutableStateOf(false)
    var feedback by mutableStateOf("Refreshing")
    var historyDropdownState by mutableStateOf(false)
    var name by mutableStateOf("")
    var time by mutableStateOf(0L)
    var view by mutableStateOf("About")
    var refreshText by mutableStateOf("\n\nRefreshing followers / following list, please wait\n\nStarting fetch\n")
    var activeButtons by mutableStateOf(true)

    private var coroutineScope = CoroutineScope(Dispatchers.Main)
    private var refreshActive = false

    fun start() {
        if (refreshActive) return
        val uiState = this
        refreshActive = true
        coroutineScope.launch {
            activeButtons = false
            refreshText = "Starting fetch\n"
            refresh(uiState, selectedConfig ?: throw IllegalStateException("Should not be null")) {
                refreshText += it+"\n"
            }
            refreshActive = false
            activeButtons = true
        }
    }

}