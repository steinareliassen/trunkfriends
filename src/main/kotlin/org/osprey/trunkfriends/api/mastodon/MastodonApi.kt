package org.osprey.trunkfriends.api.mastodon

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.delay
import org.osprey.trunkfriends.api.*
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.historyhandler.*
import org.osprey.trunkfriends.ui.UIState
import org.osprey.trunkfriends.util.mapper
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
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

    fun obtainToken(domain: String,
                    clientId: String,
                    clientSecret: String,
                    code: String) : String {
        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .uri(URI.create("https://${domain}/oauth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .method("POST",
                    HttpRequest
                        .BodyPublishers
                        .ofString(
                            getFormDataAsString(
                                mapOf(
                                    "client_id" to clientId,
                                    "client_secret" to clientSecret,
                                    "redirect_uri" to "urn:ietf:wg:oauth:2.0:oob",
                                    "grant_type" to "authorization_code",
                                    "code" to code,
                                    "scope" to "read"
                                )
                            )
                        )
                )
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        val mapper = jacksonObjectMapper()
            .configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
            )

        return mapper.readValue(response.body(), TokenInfo::class.java).accessToken
    }

    fun registerClient(domain: String) : ClientInfo {
        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .uri(URI.create("https://${domain}/api/v1/apps"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .method("POST",
                    HttpRequest
                        .BodyPublishers
                        .ofString(
                            getFormDataAsString(
                                mapOf(
                                    "client_name" to "Trunkfriends",
                                    "redirect_uris" to "urn:ietf:wg:oauth:2.0:oob",
                                    "scopes" to "read",
                                    "website" to "https://github.com/steinareliassen/trunkfriends"
                                )
                            )
                        )
                )
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        val mapper = jacksonObjectMapper()
            .configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
            )

        return mapper.readValue(response.body(), ClientInfo::class.java)
    }

    private fun getFormDataAsString(formData: Map<String, String>): String {
        val formBodyBuilder = StringBuilder()
        for ((key, value) in formData) {
            if (formBodyBuilder.isNotEmpty()) {
                formBodyBuilder.append("&")
            }
            formBodyBuilder.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
            formBodyBuilder.append("=")
            formBodyBuilder.append(URLEncoder.encode(value, StandardCharsets.UTF_8))
        }
        return formBodyBuilder.toString()
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


    override suspend fun getFollow(userId: String, direction: String, funk : (String) -> Unit, state : UIState): List<UserClass> {
        var followCount = 0
        state.feedback = "Refreshed : $followCount"
        val follow = mutableListOf<UserClass>()
        var list = findUserPage(0, userId, direction, funk)
        follow.addAll(list.first)
        while (list.second != 0L) {
            followCount += 80
            state.feedback = "Refreshed : $followCount"
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

        Thread.sleep(1500) // Do not overload server with requests

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
