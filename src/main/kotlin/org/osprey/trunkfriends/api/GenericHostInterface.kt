package org.osprey.trunkfriends.api

interface GenericHostInterface {
    fun getCurrentUsers(following : List<UserClass>, followers : List<UserClass>) : Map<String, CurrentUser>

    fun getUserId() : String
    fun getFollow(userId: String, direction : String) : List<UserClass>
}