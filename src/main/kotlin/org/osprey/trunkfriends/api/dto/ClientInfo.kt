package org.osprey.trunkfriends.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ClientInfo(
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("client_secret")
    val clientSecret: String
)