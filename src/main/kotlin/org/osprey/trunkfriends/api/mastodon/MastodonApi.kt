package org.osprey.trunkfriends.api.mastodon

import kotlinx.coroutines.delay
import org.osprey.trunkfriends.api.*
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.historyhandler.*
import org.osprey.trunkfriends.util.mapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.jvm.optionals.getOrNull

class MastodonApi(
    config : Config
) : GenericHostInterface(config) {

    override fun getUserId() =
        mapper.readValue(
            HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                    .uri(URI.create("https://${config.server}/api/v1/accounts/verify_credentials"))
                    .header("Authorization", config.bearer)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            ).body(),
            UserClass::class.java
        ).id

    fun lookupId(account : String) =
        mapper.readValue(
            HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                    .uri(URI.create("https://${config.server}/api/v1/accounts/lookup?acct=$account"))
                    .header("Authorization", config.bearer)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            ).body(),
            UserClass::class.java
        ).id

    override suspend fun executeManagementAction(
        accounts: List<String>,
        isCancelled : () -> Boolean,
        funk: (String) -> Unit,
        action: String
    ) {
        accounts.forEach { follower ->
            val id = lookupId(follower)
            funk("Action: $action executed on $follower")
            when (action) {
                "Follow" -> addFollower(id ,follower)
                "Unfollow" -> removeFollower(follower)
                "AddToList" -> addToList("SOME_LIST", follower)
            }
            if (isCancelled()) throw InterruptedException()
            sleepAndCheck(isCancelled)
        }
    }
    override fun addFollower(id : String, follower: String) {
        println("Adding follower : $id $follower")
    }

    override fun removeFollower(follower: String) {
        lookupId(follower)
    }

    override fun addToList(list: String, follower: String) {
        TODO("Not yet implemented")
    }

    fun pingUser(domain: String, user: String) =
            HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                    .uri(URI.create("https://${domain}/api/v2/search?q=%40$user%40$domain&resolve=false&limit=11"))
                    .header("Authorization", config.bearer)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            ).body().toString()

    override suspend fun getFollow(
        userId: String,
        direction: String,
        isCancelled : () -> Boolean,
        funk: (String) -> Unit
    ) : List<UserClass> {
        var followCount = 0
        funk("$direction fetched: $followCount")
        val follow = mutableListOf<UserClass>()
        var list = findUserPage(0, userId, direction)
        follow.addAll(list.first)
        while (list.second != 0L) {
            followCount += 40
            funk("$direction fetched: $followCount")
            sleepAndCheck(isCancelled)
            list = findUserPage(list.second, userId, direction)
            follow.addAll(list.first)
            if (isCancelled()) throw InterruptedException()
        }
        return follow
    }

    suspend fun sleepAndCheck(isCancelled : () -> Boolean) {
        (1..15).forEach {
            delay(100)
            if (isCancelled()) throw InterruptedException()
        }
    }

    override fun getCurrentUsers(following : List<UserClass>, followers : List<UserClass>) : Map<String, CurrentUser> {

        val currentUsers = mutableMapOf<String, CurrentUser>()

        followers.forEach {
            currentUsers[it.acct] =
                CurrentUser(following = false, follower = true, it.acct, it.username)
        }

        following.forEach {
            if (currentUsers.containsKey(it.acct)) {
                currentUsers[it.acct] = currentUsers[it.acct]?.copy(
                    following = true
                ) ?: throw IllegalStateException("nope")
            } else {
                currentUsers[it.acct] =
                    CurrentUser(following = true, follower = false, it.acct, it.username
                )
            }
        }

        return currentUsers
    }

    private fun findUserPage(start: Long, id: String, direction: String): Pair<Array<UserClass>, Long> {
        val startPoint = if (start != 0L) "?max_id=$start" else ""
        val uri = "https://${config.server}/api/v1/accounts/${id}/$direction$startPoint"
        val request = HttpRequest.newBuilder()
            .uri(
                URI.create(uri)
            )
            .header("Authorization", config.bearer)
            .method("GET", HttpRequest.BodyPublishers.noBody())
            .build()

        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        val users = mapper.readValue(response.body(), Array<UserClass>::class.java)

        val header = response.headers().firstValue("Link").getOrNull() ?: "empty"
        val startIndex = header.indexOf("max_id=")
        val stopIndex = header.indexOf(">")

        try {
            if (header == "empty") return Pair(users, 0L)
            val next = header.substring(startIndex + 7, stopIndex).toLong()
            return Pair(users, next)
        } catch (e: NumberFormatException) {
            // this is ok, ignore
        }
        return Pair(users, 0L)
    }

}
