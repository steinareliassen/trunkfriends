package org.osprey.trunkfriends.config

import com.fasterxml.jackson.annotation.JsonIgnore
import org.osprey.trunkfriends.api.GenericHostInterface
import org.osprey.trunkfriends.api.mastodon.MastodonApi

data class Config(
    val bearer : String,
    val server : String
) {
    // For future support of different hosts. Config must be extended with host type.
    @JsonIgnore
    val hostInterface : GenericHostInterface = MastodonApi(this)
}