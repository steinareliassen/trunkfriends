package org.osprey.trunkfriends.managementhandler

import kotlinx.coroutines.delay
import org.osprey.trunkfriends.config.Config

suspend fun managementAction(
    accounts: List<String>,
    action: String,
    list: String?,
    selectedConfig : Pair<String, Config>,
    isCancelled : () -> Boolean,
    feedbackFunction: (String) -> Unit
) {
    try {
        with(selectedConfig.second.hostInterface) {
            val listId = if (list!= null) getLists().find { it.title == list }?.id else null
            accounts.forEach { follower ->
                feedbackFunction("Action: $action executed on $follower")
                when (action) {
                    "Follow" -> addFollower(follower)
                    "Unfollow" -> removeFollower(follower)
                    "Add to list" -> addToList(listId ?: throw IllegalStateException("Should not happen"), follower)

                }
                sleepAndCheck(isCancelled)
            }
        }
    } catch (e : InterruptedException) {
        return
    } catch (e : Exception) {
        feedbackFunction(
"""Error, action not applied to all accounts
    
Error: ${e.message}

You might need to obtain a new token. Older Trunkfriends tokens were
read-only tokens, and did not support actions like managing followers.
You can select "Obtain new token" in the menu and retry.
""")
        runCatching {
            while (true) sleepAndCheck(isCancelled)
        }
    }

}

suspend fun sleepAndCheck(isCancelled : () -> Boolean) {
    (1..15).forEach {
        delay(100)
        if (isCancelled()) throw InterruptedException()
    }
}