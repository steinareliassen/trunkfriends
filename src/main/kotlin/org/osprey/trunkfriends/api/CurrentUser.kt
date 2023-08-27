package org.osprey.trunkfriends.api

data class CurrentUser(
    val following: Boolean,
    val follower: Boolean,
    val acct: String,
    val username: String
)