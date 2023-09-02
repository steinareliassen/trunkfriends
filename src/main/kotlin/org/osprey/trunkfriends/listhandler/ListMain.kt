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

    val body = """
        {
            "account_ids":
            [
                "109512278008571287",
                "109275921481340905",
                "44213",
                "109296244310049969",
                "109341699389802021",
                "109376276313831601",
                "109543625573618575",
                "109554153739807025",
                "110685435578465927",
                "110697337202575871",
                "109537394327834835",
                "109820688441499368",
                "108838568986197475",
                "136532",
                "110810756836083736",
                "109528413151168955",
                "109525496708847160",
                "109338336196268053",
                "109352596099702993",
                "109369467345538751",
                "109537674865412294",
                "109297024463580269"
            ]
        }
        """.trimIndent()
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