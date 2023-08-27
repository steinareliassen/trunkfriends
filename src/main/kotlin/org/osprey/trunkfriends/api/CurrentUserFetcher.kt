package org.osprey.trunkfriends.api

import org.osprey.trunkfriends.api.CurrentUser

interface CurrentUserFetcher {
    fun getCurrentUsers() : Map<String, CurrentUser>

    fun getFollow(direction : String) : List<UserClass>
}