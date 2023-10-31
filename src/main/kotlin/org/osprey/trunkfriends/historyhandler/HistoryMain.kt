package org.osprey.trunkfriends.historyhandler

import kotlinx.coroutines.delay
import org.osprey.trunkfriends.api.mastodon.MastodonApi
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.ui.UIState

val timestamp = System.currentTimeMillis()

suspend fun refresh(state: UIState, selectedConfig : Pair<String, Config>, feedbackFunction: (String) -> Unit) {
    // Set up fetchers
    try {
        val currentUserFetcher = MastodonApi(
            selectedConfig.second
        )
        val historyHandler = HistoryHandler()

        // Find user status from previous run
        val history =
            historyHandler.readHistory(selectedConfig.first) // Read old history from file. History can contain multiple entries pr user
        val latestHistory =
            historyHandler.extractPreviousRunFromHistory(history) // Find latest status of each user from history

        val userId = currentUserFetcher.getUserId()

        val following = currentUserFetcher.getFollow(userId, "following", feedbackFunction, state)
        val followers = currentUserFetcher.getFollow(userId, "followers", feedbackFunction, state)

        // Get current users
        val currentUsers =
            currentUserFetcher.getCurrentUsers(following, followers) // Get the current status of each user

        // Create and write new history
        val newHistory = historyHandler.createNewHistory(
            latestHistory,
            currentUsers
        ) // Compare previous run to current run and create new history lines
        feedbackFunction("Imported lines : "+newHistory.size+"\n\n")
        delay(1000L)
        historyHandler.writeHistory(selectedConfig.first,history + newHistory) // write out old and new history combined*/
    } catch (e : Exception) {
        feedbackFunction("Error during fetch, list not updated\n\n")
        feedbackFunction("Error: ${e.message}\n\n")
        delay(1000L)
    }
}
