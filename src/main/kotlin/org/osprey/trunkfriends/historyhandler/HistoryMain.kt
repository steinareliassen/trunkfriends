package org.osprey.trunkfriends.historyhandler

import kotlinx.coroutines.delay
import org.osprey.trunkfriends.api.mastodon.MastodonApi
import org.osprey.trunkfriends.config.Config

suspend fun refresh(selectedConfig : Pair<String, Config>, isCancelled : () -> Boolean,feedbackFunction: (String) -> Unit) {
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

        val following = currentUserFetcher.getFollow(userId, "following", isCancelled ,feedbackFunction)
        val followers = currentUserFetcher.getFollow(userId, "followers", isCancelled, feedbackFunction)

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
    } catch (e : InterruptedException) {
        return
    } catch (e : Exception) {
        feedbackFunction("Error during fetch, list not updated\n\n")
        feedbackFunction("Error: ${e.message}\n\n")
        delay(10000L)
    }

}
