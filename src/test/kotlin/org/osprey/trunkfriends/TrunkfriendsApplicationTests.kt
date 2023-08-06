package org.osprey.trunkfriends

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono





data class UserClass(
	var id : String? = null,
	var username : String? = null
)

@SpringBootTest
class TrunkfriendsApplicationTests {

	@Test
	fun contextLoads() {
		val client = WebClient.create()


		val uriSpec = client
			.get()
			.uri("https://mastodon.green/api/v1/accounts/lookup?acct=panduck")
			.header("Authorization", bearer)
			.retrieve().bodyToMono(UserClass::class.java).block()
		println(uriSpec ?: "NOTHING")

		var uriSpecString = client
			.get()
			.uri("https://mastodon.green/api/v1/accounts/verify_credentials")
			.header("Authorization", bearer)
			.retrieve().bodyToMono(String::class.java).block()
		println(uriSpecString?: "NOTHING")

		val userArray = client
			.get()
			.uri("https://mastodon.green/api/v1/accounts/${uriSpec?.id}/followers")
			.header("Authorization", bearer)
			.retrieve().bodyToMono(Array<UserClass>::class.java).block()
		println(userArray?.size ?: "NOTHING")

	}

}
