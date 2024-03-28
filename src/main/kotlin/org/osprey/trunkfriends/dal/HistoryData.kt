package org.osprey.trunkfriends.dal

import org.apache.commons.io.FileUtils
import org.osprey.trunkfriends.api.dto.CurrentUser
import org.osprey.trunkfriends.ui.CompareUser
import org.osprey.trunkfriends.ui.history.HistoryCard
import org.osprey.trunkfriends.util.mapper
import java.io.File

class HistoryData(
    private val configPath: String
) {

    private val history  = mutableListOf<Pair<CurrentUser, String>>().also { history ->
            val path = FileUtils.getUserDirectoryPath() + "/.trunkfriends/$configPath"
            val file = File("$path/datafile.dmp")
            if (file.exists()) {
                file.readLines().forEach {
                    val stra = it.substring(0, it.indexOf("-") + 3)
                    val strb = it.substring(it.indexOf("-") + 3, it.length)
                    val user = mapper.readValue(
                        strb,
                        CurrentUser::class.java
                    )
                    history.add(Pair(user, stra))
                }
            }
        }

    fun isNotEmpty() = history.isNotEmpty()

    fun getTimeslots() = history.map { (_, control) ->
        control.substring(0, control.length - 3).toLong()
    }.distinct()

    fun createListCards(compareUser: CompareUser) =
        history.associate { it.first.acct to it.first }.map { it.value }
            .sortedWith(compareUser).let {
            it.map { user ->
                with(user) {
                    HistoryCard(
                        follower = follower,
                        prevFollower = follower,
                        following = following,
                        prevFollowing = following,
                        acct = acct,
                        username = username,
                        timeStamp = 0L
                    )
                }
            }
        }

    fun createHistoryCards() =
        history.let {
            val previousUserMap = mutableMapOf<String, CurrentUser>()
            it.map { user ->
                with(user) {
                    previousUserMap[first.acct]?.let { currentUser ->
                        HistoryCard(
                            follower = first.follower,
                            prevFollower = currentUser.follower,
                            following = first.following,
                            prevFollowing = currentUser.following,
                            acct = first.acct,
                            username = first.username,
                            timeStamp = second.substring(0, second.length - 3).toLong()
                        ).also {
                            previousUserMap[first.acct] = first
                        }
                    } ?: HistoryCard(
                        follower = first.follower,
                        prevFollower = first.follower,
                        following = first.following,
                        prevFollowing = first.following,
                        acct = first.acct,
                        username = first.username,
                        timeStamp = second.substring(0, second.length - 3).toLong()
                    ).also {
                        previousUserMap[first.acct] = first
                    }
                }
            }
        }

    fun writeHistory(currentUsers: Map<String, CurrentUser>) =
        createNewHistory(currentUsers).let { newHistory ->
            val historyLines = history + newHistory
            val path = FileUtils.getUserDirectoryPath() + "/.trunkfriends/$configPath"
            File("$path/datafile.dmp").renameTo(File("$path/datafile.dmp.${System.currentTimeMillis()}"))
            File("$path/datafile.dmp").printWriter().use { pw ->
                historyLines.forEach {
                    pw.println(it.second + mapper.writeValueAsString(it.first))
                }
            }
            newHistory.size
        }

    private fun createNewHistory(
        currentUsers: Map<String, CurrentUser>
    ): List<Pair<CurrentUser, String>> {
        val latestHistory = extractPreviousRun()
        val timestamp = System.currentTimeMillis()
        val newHistoryLines = mutableListOf<Pair<CurrentUser, String>>()

        fun historyLine(userObject: CurrentUser) {
            val fi = if (userObject.follower) "1" else "*"
            val fo = if (userObject.following) "1" else "*"
            val ctrString = "$timestamp-$fi$fo"
            newHistoryLines.add(Pair(userObject, ctrString))
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
        latestHistory.filter {
            it.value.follower || it.value.following // Filter out users already marked as removed.
        }.forEach {
            if (!currentUsers.containsKey(it.key)) {
                val unfollowed = it.value.copy(
                    following = false,
                    follower = false
                )
                historyLine(unfollowed)
            }
        }
        return newHistoryLines
    }

    private fun extractPreviousRun(): Map<String, CurrentUser> =
        history.associate {
            it.first.acct to it.first
        }

}
