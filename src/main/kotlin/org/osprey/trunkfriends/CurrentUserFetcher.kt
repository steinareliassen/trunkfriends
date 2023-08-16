package org.osprey.trunkfriends

interface CurrentUserFetcher {
    fun getCurrentUsers() : Map<String, CurrentUser>
}