package org.osprey.trunkfriends.api.mastodon

import org.osprey.trunkfriends.api.*
import org.osprey.trunkfriends.util.mapper
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class MastodonAuthApi {

    fun getUserInformation(bearer : String, domain: String)  =
        mapper.readValue(
            HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                    .uri(URI.create("https://$domain/api/v1/accounts/verify_credentials"))
                    .header("Authorization", "Bearer $bearer")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            ).body(),
            UserClass::class.java
        )
    fun obtainToken(domain: String,
                    clientId: String,
                    clientSecret: String,
                    code: String) : String {
        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .uri(URI.create("https://$domain/oauth/token"))
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
                                    "scope" to "read write follow"
                                )
                            )
                        )
                )
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        return mapper.readValue(response.body(), TokenInfo::class.java).accessToken
    }

    fun registerClient(domain: String) : ClientInfo {
        val response = HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .uri(URI.create("https://$domain/api/v1/apps"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .method("POST",
                    HttpRequest
                        .BodyPublishers
                        .ofString(
                            getFormDataAsString(
                                mapOf(
                                    "client_name" to "Trunkfriends",
                                    "redirect_uris" to "urn:ietf:wg:oauth:2.0:oob",
                                    "scopes" to "read write follow",
                                    "website" to "https://github.com/steinareliassen/trunkfriends"
                                )
                            )
                        )
                )
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        return mapper.readValue(response.body(), ClientInfo::class.java)
    }

    private fun getFormDataAsString(formData: Map<String, String>) =
        StringBuilder().apply{
            for ((key, value) in formData) {
                if (isNotEmpty()) {
                    append("&")
                }
                append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                append("=")
                append(URLEncoder.encode(value, StandardCharsets.UTF_8))
            }
        }.toString()

}
