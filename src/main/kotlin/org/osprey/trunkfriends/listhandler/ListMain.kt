package org.osprey.trunkfriends.listhandler

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

// List handler functionality is an idea in draft state. This code is unused for now
fun listHandler() {
    val listsRequest: HttpRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://server/api/v1/lists"))
        .header("Authorization", "bearer")
        .method("GET", HttpRequest.BodyPublishers.noBody())
        .build()

    val listsResponse = HttpClient.newHttpClient().send(listsRequest, HttpResponse.BodyHandlers.ofString())

    val body = """
        {
            "account_ids":
            [
                "1",
                "2",
                "3"
            ]
        }
        """.trimIndent()
    val listRequest: HttpRequest = HttpRequest.newBuilder()
        .uri(URI.create("https://server/api/v1/lists/2045/accounts"))
        .header("Authorization", "bearer")
        .header("Content-Type","application/json")
        .POST(
            HttpRequest
                .BodyPublishers.ofString(body)
        )
        .build()

    val listResponse = HttpClient.newHttpClient().send(listRequest, HttpResponse.BodyHandlers.ofString())

    println(listResponse.body())

}