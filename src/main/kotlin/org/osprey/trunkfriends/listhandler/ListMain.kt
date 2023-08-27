package org.osprey.trunkfriends.listhandler

import org.osprey.trunkfriends.bearer
import org.osprey.trunkfriends.server
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


fun main() {
    val listsRequest: HttpRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://$server/api/v1/lists"))
        .header("Authorization", bearer)
        .method("GET", HttpRequest.BodyPublishers.noBody())
        .build()

    val listsResponse = HttpClient.newHttpClient().send(listsRequest, HttpResponse.BodyHandlers.ofString())

    println(listsResponse.body())

    val body = """{"account_ids":["109155529132859901","110906885214152482"]}"""
    val listRequest: HttpRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://$server/api/v1/lists/2045/accounts"))
        .header("Authorization", bearer)
        .header("Content-Type","application/json")
        .POST(
            HttpRequest
                .BodyPublishers.ofString(body)
        )
        .build()

    val listResponse = HttpClient.newHttpClient().send(listRequest, HttpResponse.BodyHandlers.ofString())

    println(listResponse.body())

}