package org.osprey.trunkfriends.historyhandler

import org.apache.commons.io.FileUtils
import org.osprey.trunkfriends.api.CurrentUser
import org.osprey.trunkfriends.ui.dto.HistoryCard
import org.osprey.trunkfriends.util.mapper
import java.io.File
import java.security.MessageDigest
import java.util.*

class HistoryHandler {

    fun readHistory(configPath: String): List<Pair<CurrentUser, String>> {
        val history = mutableListOf<Pair<CurrentUser, String>>()

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
        return history
    }

    fun createHistoryCards(history: List<Pair<CurrentUser, String>>) =
        history.let {
            val previousUserMap = mutableMapOf<String, CurrentUser>()
            it.map { user ->
                with(user) {
                    previousUserMap[first.acct]?.let {
                        HistoryCard(
                            follower = first.follower,
                            prevFollower = it.follower,
                            following = first.following,
                            prevFollowing = it.following,
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


    fun createNewHistory(
        latestHistory: Map<String, CurrentUser>,
        currentUsers: Map<String, CurrentUser>
    ): List<Pair<CurrentUser, String>> {
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

    fun extractPreviousRunFromHistory(history: List<Pair<CurrentUser, String>>): Map<String, CurrentUser> =
        history.associate {
            it.first.acct to it.first
        }

    fun writeHistory(configPath : String, historyLines: List<Pair<CurrentUser, String>>) {
        val path = FileUtils.getUserDirectoryPath() + "/.trunkfriends/$configPath"
        File("$path/datafile.dmp").renameTo(File("$path/datafile.dmp.${System.currentTimeMillis()}"))
        File("$path/datafile.dmp").printWriter().use { pw ->
            historyLines.forEach {
                pw.println(it.second + mapper.writeValueAsString(it.first))
            }
        }
    }

    fun writeMessyHistory(historyLines: List<Pair<CurrentUser, String>>) {
        File("messdatafile.dmp").renameTo(File("messdatafile.dmp.${System.currentTimeMillis()}"))
        File("messdatafile.dmp").printWriter().use { pw ->
            historyLines.forEach {
                pw.println(it.second + mapper.writeValueAsString(it.first))
            }
        }
    }

}

fun main() {
    val corrupted = HistoryHandler().readHistory("tech.lgbt/lettosprey").map { (curr, stri) ->
        val split = curr.acct.split("@")
        val username = curr.username
        (curr.copy(
            username = String(
                Base64.getEncoder().encode(
                    MessageDigest.getInstance("MD5").digest(username.toByteArray())
                )
            ).replace("=", ""),
            acct = String(
                Base64.getEncoder().encode(
                    MessageDigest.getInstance("MD5").digest(split[0].toByteArray())
                )
            ).replace("=", "") + (if (split.size > 1) "@${split[1]}" else "")
        ) to stri)
    }
    HistoryHandler().writeMessyHistory(corrupted)
}