package org.osprey.trunkfriends.historyhandler

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.osprey.trunkfriends.bearer
import org.osprey.trunkfriends.server
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.jvm.optionals.getOrNull

class UsersFromMastodonApi : CurrentUserFetcher {

    override fun getCurrentUsers() : Map<String, CurrentUser> {
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://$server/api/v1/accounts/verify_credentials"))
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

    fun findUserPage(start: Long, id: String, direction: String = "followers"): Pair<Array<UserClass>, Long> {
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

}