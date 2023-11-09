package org.osprey.trunkfriends.ui.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class HistoryViewState {
    var page by mutableStateOf(0)
    var returnPage by mutableStateOf(0)
    var timeslotPage by mutableStateOf(0)
    var historyDropdownState by mutableStateOf(false)
    var time by mutableStateOf(0L)

    fun reset() {
        page = 0
        timeslotPage = 0
        historyDropdownState = false
        time = 0
    }

    fun nextTimeslot() : Int {
        timeslotPage++
        page = 0
        return timeslotPage
    }

    fun previousTimeslot() : Int {
        timeslotPage--
        page = 0
        return timeslotPage
    }

    fun storeHistoryPage() {
        returnPage = page
        page = 0
    }

    fun resetHistoryPage(zoomedName : String?): Boolean =
        if (zoomedName == null && returnPage != 0) {
            page = returnPage
            returnPage = 0
            true
        } else false


}