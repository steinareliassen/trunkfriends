package org.osprey.trunkfriends

data class CurrentUser(
    val following: Boolean,
    val follower: Boolean,
    val acct: String,
    val username: String
)