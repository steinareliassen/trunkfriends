package org.osprey.trunkfriends.config

import org.osprey.trunkfriends.api.GenericHostInterface
import org.osprey.trunkfriends.api.mastodon.MastodonApi

data class Config(
    val bearer : String,
    val server : String
) {
    val hostInterface : GenericHostInterface = MastodonApi(this)
}