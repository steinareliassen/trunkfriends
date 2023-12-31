package org.osprey.trunkfriends.config

import com.fasterxml.jackson.annotation.JsonIgnore
import org.osprey.trunkfriends.api.GenericHostInterface
import org.osprey.trunkfriends.api.mastodon.MastodonApi

data class Config(
    val bearer : String,
    val server : String
) {
    @JsonIgnore
    val hostInterface : GenericHostInterface = MastodonApi(this)
}