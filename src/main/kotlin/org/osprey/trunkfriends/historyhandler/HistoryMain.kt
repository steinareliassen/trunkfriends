package org.osprey.trunkfriends.historyhandler

import org.osprey.trunkfriends.api.mastodon.MastodonApi
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.util.mapper
import java.io.File

val timestamp = System.currentTimeMillis()

suspend fun refresh(feedbackFunction: (String) -> Unit) {
    // Set up fetchers
    try {
        val file = File("config.json")
        val config = if (file.exists()) {
            file.readLines().first().let {
                mapper.readValue(
                    it, Config::class.java
                )
            }
        } else throw Exception("Config file not found")

        val currentUserFetcher = MastodonApi(config)
        val historyHandler = HistoryHandler()

        val userId = currentUserFetcher.getUserId()
        val following = currentUserFetcher.getFollow(userId, "following", feedbackFunction)
        val followers = currentUserFetcher.getFollow(userId, "followers", feedbackFunction)

        File("following_you.dmp").printWriter().use { pw ->
            following.forEach {
                pw.println(mapper.writeValueAsString(it))
            }

        }
        File("followers_of_you.dmp").printWriter().use { pw ->
            followers.forEach {
                pw.println(mapper.writeValueAsString(it))
            }
        }

        // Get current users
        val currentUsers =
            currentUserFetcher.getCurrentUsers(following, followers) // Get the current status of each user

        // Find user status from previous run
        val history =
            historyHandler.readHistory() // Read old history from file. History can contain multiple entries pr user
        val latestHistory =
            historyHandler.extractPreviousRunFromHistory(history) // Find latest status of each user from history

        // Create and write new history
        val newHistory = historyHandler.createNewHistory(
            latestHistory,
            currentUsers
        ) // Compare previous run to current run and create new history lines
        feedbackFunction("Imported lines : "+newHistory.size+"\n\n")
        historyHandler.writeHistory(history + newHistory) // write out old and new history combined
    } catch (e : Exception) {
        feedbackFunction("Error during fetch, list not updated\n\n")
        feedbackFunction("Error: ${e.message}\n\n")
    }
}
