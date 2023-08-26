package org.osprey.trunkfriends.historyhandler

interface CurrentUserFetcher {
    fun getCurrentUsers() : Map<String, CurrentUser>
}