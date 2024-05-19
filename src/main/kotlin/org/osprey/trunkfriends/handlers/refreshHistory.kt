package org.osprey.trunkfriends.handlers

import kotlinx.coroutines.delay
import org.osprey.trunkfriends.api.dto.CurrentUser
import org.osprey.trunkfriends.api.Direction
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.dal.HistoryData
import org.osprey.trunkfriends.handlers.dto.BackupOptions
import org.osprey.trunkfriends.util.extractError

suspend fun refreshHistory
            (selectedConfig : Pair<String, Config>,
             backupPlan : Set<BackupOptions> ,
             isCancelled : () -> Boolean,
             feedbackFunction: (String) -> Unit
) {

    try {
        val hostInterface = selectedConfig.second.hostInterface

        val userId = hostInterface.getUserId()

        val following = hostInterface.getFollow(userId, Direction.FOLLOWING, isCancelled ,feedbackFunction)
        val followers = hostInterface.getFollow(userId, Direction.FOLLOWERS, isCancelled, feedbackFunction)

        // Get current users
        val currentUsers = mutableMapOf<String, CurrentUser>()

        followers.forEach {
            currentUsers[it.acct] =
                CurrentUser(following = false, follower = true, it.acct, it.username)
        }

        following.forEach {
            if (currentUsers.containsKey(it.acct)) {
                currentUsers[it.acct] = currentUsers[it.acct]?.copy(
                    following = true
                ) ?: throw IllegalStateException("Should not be possible")
            } else {
                currentUsers[it.acct] =
                    CurrentUser(
                        following = true,
                        follower = false,
                        it.acct,
                        it.username
                    )
            }
        }

        // Find user status from previous run
        val history = HistoryData(selectedConfig.first) // Read old history from file. History can contain multiple entries pr user

         // write out old and new history combined based on
        val newLines = history.writeHistory(currentUsers)
        feedbackFunction("Imported lines : $newLines\n\n")
        delay(5000L)
    } catch (e : InterruptedException) {
        return
    } catch (e : Exception) {
        feedbackFunction("Error during fetch, list not updated\n\n" +
                "Error: ${extractError(e.message)}")
        runCatching {
            while (true) sleepAndCheck(isCancelled)
        }
    }

}
