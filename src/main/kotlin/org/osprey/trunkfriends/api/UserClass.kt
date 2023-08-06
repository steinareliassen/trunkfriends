package org.osprey.trunkfriends.api

import com.fasterxml.jackson.annotation.JsonProperty

data class UserClass(
    val id: String,
    val acct: String,
    val username: String,
    @JsonProperty("display_name")
    val displayName: String,
)