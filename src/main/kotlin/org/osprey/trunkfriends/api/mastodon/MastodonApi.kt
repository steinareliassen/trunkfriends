package org.osprey.trunkfriends.api.mastodon

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.delay
import org.osprey.trunkfriends.api.CurrentUser
import org.osprey.trunkfriends.api.GenericHostInterface
import org.osprey.trunkfriends.api.UserClass
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.historyhandler.*
import org.osprey.trunkfriends.util.mapper
import java.lang.IllegalStateException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.jvm.optionals.getOrNull

class MastodonApi(
    val config : Config
) : GenericHostInterface {

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

    override suspend fun getFollow(userId: String, direction: String, funk : (String) -> Unit): List<UserClass> {
        val follow = mutableListOf<UserClass>()
        var list = findUserPage(0, userId, direction, funk)
        follow.addAll(list.first)
        while (list.second != 0L) {
            list = findUserPage(list.second, userId, direction, funk)
            follow.addAll(list.first)
        }
        return follow
    }

    override fun getCurrentUsers(following : List<UserClass>, followers : List<UserClass>) : Map<String, CurrentUser> {

        val currentUsers = mutableMapOf<String, CurrentUser>()

        followers.forEach {
            currentUsers[it.acct] = CurrentUser(
                following = false,
                follower = true,
                it.acct,
                it.username
            )
        }

        following.forEach {
            if (currentUsers.containsKey(it.acct)) {
                currentUsers[it.acct] = currentUsers[it.acct]?.copy(
                    following = true
                ) ?: throw IllegalStateException("nope")
            } else {
                currentUsers[it.acct] = CurrentUser(
                    following = true,
                    follower = false,
                    it.acct,
                    it.username
                )
            }
        }

        return currentUsers
    }

    private suspend fun findUserPage(start: Long, id: String, direction: String, funk : (String) -> Unit): Pair<Array<UserClass>, Long> {
        val request = HttpRequest.newBuilder()
            .uri(
                URI.create(
                    "https://${config.server}/api/v1/accounts/${id}/$direction" +
                            if (start != 0L) "?max_id=$start" else ""
                )
            )
            .header("Authorization", config.bearer)
            .method("GET", HttpRequest.BodyPublishers.noBody())
            .build()

        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

        val mapper = jacksonObjectMapper()
            .configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
            )

        val users = mapper.readValue(response.body(), Array<UserClass>::class.java)

        funk("$direction page $start")
        delay(100L)

        val header = response.headers().firstValue("Link").getOrNull() ?: "empty"
        val startIndex = header.indexOf("max_id=")
        val stopIndex = header.indexOf(">")

        Thread.sleep(100) // Do not overload server with requests

        try {
            if (header == "empty") return Pair(users, 0L)
            val next = header.substring(startIndex + 7, stopIndex).toLong()
            return Pair(users, next)
        } catch (e: java.lang.NumberFormatException) {
            // this is ok, ignore
        }
        return Pair(users, 0L)

    }

}