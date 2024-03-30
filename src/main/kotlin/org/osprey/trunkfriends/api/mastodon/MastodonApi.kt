package org.osprey.trunkfriends.api.mastodon

import org.osprey.trunkfriends.api.*
import org.osprey.trunkfriends.api.dto.FollowStatus
import org.osprey.trunkfriends.api.dto.ListClass
import org.osprey.trunkfriends.api.dto.UserClass
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.handlers.sleepAndCheck
import org.osprey.trunkfriends.util.mapper
import java.io.IOException
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

    private fun lookupId(account : String) =
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

    fun addRemoveFollower(follower: String, action: String) : FollowStatus =
        lookupId(follower).let { id ->
            mapper.readValue(
                HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder()
                        .uri(URI.create("https://${config.server}/api/v1/accounts/$id/$action"))
                        .header("Authorization", config.bearer)
                        .method("POST", HttpRequest.BodyPublishers.noBody())
                        .build(),
                    HttpResponse.BodyHandlers.ofString()
                ).also { response ->
                    if (response.statusCode() != 200) {
                        // Did we use an older token without read / write access?
                        if (response.body().contains("This action is outside the authorized scopes"))
                            throw IOException("You have a read only token, a read/write token is required.")
                        throw IOException("Got error code ${response.statusCode()}")
                    }
                }.body(),
                FollowStatus::class.java
            )
        }
    override fun addFollower(follower: String) =
        addRemoveFollower(follower, "follow")

    override fun removeFollower(follower: String): FollowStatus =
        addRemoveFollower(follower, "unfollow")

    override fun addToList(listId: String, follower: String) {
        lookupId(follower).let { id ->
            HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                    .uri(URI.create("https://${config.server}/api/v1/lists/$listId/accounts?account_ids[]=$id"))
                    .header("Authorization", config.bearer)
                    .method("POST", HttpRequest.BodyPublishers.noBody())
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            ).also { response ->
                if (response.statusCode() != 200) {
                    // Did we use an older token without read / write access?
                    if (response.body().contains("This action is outside the authorized scopes"))
                        throw IOException("You have a read only token, a read/write token is required.")
                    throw IOException("Got error code ${response.statusCode()}")
                }
            }.body()
        }
    }

    override fun getLists(): List<ListClass> {
        val uri = "https://${config.server}/api/v1/lists"
        val request = HttpRequest.newBuilder()
            .uri(
                URI.create(uri)
            )
            .header("Authorization", config.bearer)
            .method("GET", HttpRequest.BodyPublishers.noBody())
            .build()

        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        return mapper.readValue(response.body(), Array<ListClass>::class.java).toList()

    }

    override suspend fun getFollow(
        userId: String,
        direction: Direction,
        isCancelled : () -> Boolean,
        feedbackFunction: (String) -> Unit
    ) : List<UserClass> {
        var start = 0L
        val follow = mutableListOf<UserClass>()
        do {
            println("FETCH!")
            val list = findUserPage(start, userId, direction)
            follow.addAll(list.first)
            feedbackFunction("$direction fetched: ${follow.size}")
            sleepAndCheck(isCancelled)
            start = list.second
        } while (start != 0L)
        println("FETCH!2")

        return follow
    }

    private fun findUserPage(start: Long, id: String, direction: Direction): Pair<Array<UserClass>, Long> {
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
