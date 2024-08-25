package org.osprey.trunkfriends.api.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UserRelation(
    val id: String,
    val blocking: Boolean,
    @JsonProperty("blocked_by")
    val blockedBy: String,
    val note: String,
)