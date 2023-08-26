package org.osprey.trunkfriends.historyhandler

data class CurrentUser(
    val following: Boolean,
    val follower: Boolean,
    val acct: String,
    val username: String
)