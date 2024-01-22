package org.osprey.trunkfriends.ui.history

import androidx.compose.runtime.*
import org.osprey.trunkfriends.ui.View

class HistoryViewState {
    var page by mutableStateOf(0)
    var height by mutableStateOf(800)
    var searchText by mutableStateOf("")
    var timeslotPage by mutableStateOf(0)
    var historyDropdownState by mutableStateOf(false)
    var time by mutableStateOf(0L)
    val pasteBag = mutableStateListOf<String>()
    var returnView = View.HISTORY

    private var returnPage by mutableStateOf(0)
    private var returnTime by mutableStateOf(0L)
    private var returnTimeslot by mutableStateOf(0)

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
        returnTime = time
        returnTimeslot = timeslotPage
        page = 0
    }

    fun resetHistoryPage(zoomedName : String?): Boolean =
        if (zoomedName == null && (returnPage != 0 || returnTime != 0L)) {
            page = returnPage
            time = returnTime
            timeslotPage = returnTimeslot
            returnPage = 0
            returnTime = 0
            returnTimeslot = 0
            true
        } else false

}