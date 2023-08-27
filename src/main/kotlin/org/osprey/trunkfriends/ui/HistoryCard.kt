package org.osprey.trunkfriends.ui

data class HistoryCard(
    val timeStamp: Long,
    val prevFollowing: Boolean,
    val prevFollower: Boolean,
    val following: Boolean,
    val follower: Boolean,
    val acct: String,
    val username: String
)