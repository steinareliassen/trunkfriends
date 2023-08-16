package org.osprey.trunkfriends

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.jvm.optionals.getOrNull

val mapper = jacksonObjectMapper()
    .configure(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        false
    )

data class UserClass(
    val id: String? = null,
    val acct: String? = null,
    val username: String? = null
)

data class CurrentUser(
    val following: Boolean,
    val follower: Boolean,
    val acct: String,
    val username: String
)

val timestamp = System.currentTimeMillis()

fun findUserPage(start: Long, id: String, direction: String = "followers"): Pair<Array<UserClass>, Long> {
    val request = HttpRequest.newBuilder()
        .uri(
            URI.create(
                "https://mastodon.green/api/v1/accounts/${id}/$direction" +
                        if (start != 0L) "?max_id=$start" else ""
            )
        )
        .header("Authorization", bearer)
        .method("GET", HttpRequest.BodyPublishers.noBody())
        .build()

    val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

    val mapper = jacksonObjectMapper()
        .configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
        )

    val users = mapper.readValue(response.body(), Array<UserClass>::class.java)

    users.forEachIndexed { index, userClass ->
        println("+$timestamp,${mapper.writeValueAsString(userClass)}")
    }
    val header = response.headers().firstValue("Link").getOrNull() ?: "empty"
    val startIndex = header.indexOf("max_id=")
    val stopIndex = header.indexOf(">")

    Thread.sleep(500) // Do not overload server with requests

    try {
        val next = header.substring(startIndex + 7, stopIndex).toLong()
        return Pair(users, next)
    } catch (e: java.lang.NumberFormatException) {
        // ignore
    }
    return Pair(users, 0L)

}

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

fun main() {
    val history = readHistory() // Read old history from file. History can contain multiple entries pr user
    val latestHistory = extractPreviousRunFromHistory(history) // Find latest status of each user from history
    val currentUsers = getCurrentUsers() // Get the current status of each user
    val newHistory = createNewHistory(latestHistory, currentUsers) // Compare previous run to current run and create new history lines
//    writeHistory(history. newHistory) // write out old and new history combined
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

fun getCurrentUsers() : Map<String, CurrentUser> {
    val request: HttpRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://mastodon.green/api/v1/accounts/verify_credentials"))
        .header("Authorization", bearer)
        .method("GET", HttpRequest.BodyPublishers.noBody())
        .build()

    val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

    val user = mapper.readValue(
        response.body(),
        UserClass::class.java
    )

    val following = mutableListOf<UserClass>()
    var list = findUserPage(0, user.id ?: "empty", "following")
    following.addAll(list.first)
    while (list.second != 0L) {
        list = findUserPage(list.second, user.id ?: "empty", "following")
        following.addAll(list.first)
    }

    val followers = mutableListOf<UserClass>()
    list = findUserPage(0, user.id ?: "empty")
    followers.addAll(list.first)
    while (list.second != 0L) {
        list = findUserPage(list.second, user.id ?: "empty")
        followers.addAll(list.first)
    }

    val currentUsers = mutableMapOf<String, CurrentUser>()

    followers.forEach {
        currentUsers[it.acct ?: "empty"] = CurrentUser(
            following = false,
            follower = true,
            it.acct ?: "empty",
            it.username ?: "empty"
        )
    }

    following.forEach {
        val acct = it.acct ?: "empty"
        if (currentUsers.containsKey(acct)) {
            currentUsers[acct] = currentUsers[acct]?.copy(
                following = true
            ) ?: throw java.lang.IllegalStateException("nope")
        } else {
            currentUsers[acct] = CurrentUser(
                following = true,
                follower = false,
                it.acct ?: "empty",
                it.username ?: "empty"
            )
        }
    }

    return currentUsers
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

