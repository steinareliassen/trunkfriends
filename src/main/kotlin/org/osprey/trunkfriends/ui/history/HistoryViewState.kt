package org.osprey.trunkfriends.ui.history

import androidx.compose.runtime.*

class HistoryViewState {
    var returnPage by mutableStateOf(0)
    var returnTimeslot by mutableStateOf(0)

    fun storeHistoryPage(page : Int, timeslotPage : Int = 0) {
        returnPage = page
        returnTimeslot = timeslotPage
    }

    fun resetHistoryPage(zoomedName : String?): Boolean =
        if (zoomedName == null && returnPage != 0) {
            returnPage = 0
            returnTimeslot = 0
            true
        } else false

}