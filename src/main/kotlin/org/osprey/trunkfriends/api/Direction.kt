package org.osprey.trunkfriends.api

enum class Direction(val direction: String) {
    FOLLOWING("following"),
    FOLLOWERS("followers");

    override fun toString() = direction
}