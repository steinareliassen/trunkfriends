package org.osprey.trunkfriends.ui.history

import androidx.compose.runtime.*

class HistoryViewState {
    var returnPage by mutableStateOf(0)
    var returnTimeslot by mutableStateOf(0)

    // Used to indicate when overview page need to do a reset after zoom
    var overviewReset = false

    fun storeHistoryPage(page : Int, timeslotPage : Int = 0) {
        returnPage = page
        returnTimeslot = timeslotPage
    }

}