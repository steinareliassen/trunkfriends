package org.osprey.trunkfriends.api.dto

data class CurrentUser(
    val following: Boolean,
    val follower: Boolean,
    val acct: String,
    val username: String,
    val userId: String?
)