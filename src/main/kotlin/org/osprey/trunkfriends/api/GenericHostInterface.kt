package org.osprey.trunkfriends.api

import org.osprey.trunkfriends.config.Config

abstract class GenericHostInterface(protected val config: Config) {

    abstract fun getUserId() : String

    abstract fun addFollower(follower: String) : FollowStatus

    abstract fun removeFollower(follower: String) : FollowStatus

    abstract fun addToList(listId: String, follower: String)

    abstract fun getLists() : List<ListClass>

    abstract suspend fun getFollow(
        userId: String,
        direction: Direction,
        isCancelled : () -> Boolean,
        feedbackFunction: (String) -> Unit
    ) : List<UserClass>

}