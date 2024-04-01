package org.osprey.trunkfriends.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenInfo(
    @JsonProperty("access_token")
    val accessToken: String
)