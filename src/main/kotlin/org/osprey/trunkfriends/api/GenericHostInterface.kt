package org.osprey.trunkfriends.api

import org.osprey.trunkfriends.config.Config

abstract class GenericHostInterface(protected val config: Config) {
    abstract fun getCurrentUsers(following : List<UserClass>, followers : List<UserClass>) : Map<String, CurrentUser>

    abstract fun getUserId() : String

    abstract fun addFollower(id: String, follower: String)

    abstract fun removeFollower(follower: String)

    abstract fun addToList(list: String, follower: String)

    abstract suspend fun getFollow(
        userId: String,
        direction: String,
        isCancelled : () -> Boolean,
        funk: (String) -> Unit
    ) : List<UserClass>

    abstract suspend fun executeManagementAction(
        accounts: List<String>,
        isCancelled: () -> Boolean,
        funk: (String) -> Unit,
        action: String
    )

}