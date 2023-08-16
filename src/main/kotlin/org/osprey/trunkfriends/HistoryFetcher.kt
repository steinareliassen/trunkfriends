package org.osprey.trunkfriends

import java.io.File

class HistoryFetcher {

    fun readHistory() : List<Pair<CurrentUser,String>> {
        val history = mutableListOf<Pair<CurrentUser,String>>()
        File("datafile.dmp").readLines().forEach {
            val stra = it.substring(0,it.indexOf("-")+3)
            val strb = it.substring(it.indexOf("-")+3, it.length)
            val user = mapper.readValue(
                strb,
                CurrentUser::class.java
            )
            history.add(Pair(user, stra))
        }
        return history
    }

    fun createNewHistory(
        latestHistory: Map<String, CurrentUser>,
        currentUsers: Map<String, CurrentUser>
    ) : List<Pair<CurrentUser,String>> {
        val newHistoryLines = mutableListOf<Pair<CurrentUser,String>>()

        fun historyLine(userObject : CurrentUser) {
            val fi = if (userObject.follower) "1" else "*"
            val fo = if (userObject.following) "1" else "*"
            val ctrString = "$timestamp-$fi$fo"+mapper.writeValueAsString(userObject)
            newHistoryLines.add(Pair(userObject,ctrString))
        }

        currentUsers.forEach {
            val currentUser = it.value
            if (!latestHistory.containsKey(it.key)) {
                // user is not contained, add a new line
                historyLine(currentUser)
            } else {
                val preUser = latestHistory[it.key] ?: throw java.lang.IllegalStateException("Should Not Happen")
                if (preUser.following == currentUser.following &&
                    preUser.follower == currentUser.follower
                ) {
                    // What about name change?
                } else {
                    historyLine(currentUser)
                }
            }
        }
        // Check if any users have been removed
        latestHistory.forEach {
            if(!currentUsers.containsKey(it.key)) {
                val unfollowed = it.value.copy(
                    following = false,
                    follower = false
                )
                historyLine(unfollowed)
            }
        }
        return newHistoryLines
    }

    fun extractPreviousRunFromHistory(history: List<Pair<CurrentUser, String>>) : Map<String, CurrentUser> =
        history.associate {
            it.first.acct to it.first
        }

    fun writeHistory(currentUsers : Map<String, CurrentUser>)  {
        File("datafile.dmp").renameTo(File("datafile.dmp.${System.currentTimeMillis()}"))
        File("datafile.dmp").printWriter().use { pw ->
            currentUsers.forEach {
                val fi = if (it.value.follower) "1" else "*"
                val fo = if (it.value.following) "1" else "*"
                pw.println("$timestamp-$fi$fo"+mapper.writeValueAsString(it.value))
            }
        }
    }

}