package org.osprey.trunkfriends.api.mastodon

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.osprey.trunkfriends.api.CurrentUser
import org.osprey.trunkfriends.api.GenericHostInterface
import org.osprey.trunkfriends.api.UserClass
import org.osprey.trunkfriends.bearer
import org.osprey.trunkfriends.historyhandler.*
import org.osprey.trunkfriends.server
import org.osprey.trunkfriends.util.mapper
import java.lang.IllegalStateException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.jvm.optionals.getOrNull

class MastodonApi : GenericHostInterface {

    override fun getUserId() =
        mapper.readValue(
            HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                    .uri(URI.create("https://$server/api/v1/accounts/verify_credentials"))
                    .header("Authorization", bearer)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            ).body(),
            UserClass::class.java
        ).id ?: throw IllegalStateException("Cannot extract userid")

    override fun getFollow(userId: String, direction: String): List<UserClass> {
        val follow = mutableListOf<UserClass>()
        var list = findUserPage(0, userId ?: "empty", direction)
        follow.addAll(list.first)
        while (list.second != 0L) {
            list = findUserPage(list.second, userId ?: "empty", direction)
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

    private fun findUserPage(start: Long, id: String, direction: String): Pair<Array<UserClass>, Long> {
        val request = HttpRequest.newBuilder()
            .uri(
                URI.create(
                    "https://$server/api/v1/accounts/${id}/$direction" +
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

        println("* Fetched page")

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

}