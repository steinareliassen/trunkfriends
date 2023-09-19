package org.osprey.trunkfriends.api

interface GenericHostInterface {
    fun getCurrentUsers(following : List<UserClass>, followers : List<UserClass>) : Map<String, CurrentUser>

    fun getUserId() : String
    suspend fun getFollow(userId: String, direction : String, funk : (String) -> Unit) : List<UserClass>
}