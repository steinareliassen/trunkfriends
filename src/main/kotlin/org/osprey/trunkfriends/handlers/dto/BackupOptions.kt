package org.osprey.trunkfriends.handlers.dto

enum class BackupOptions(
    val title: String
) {
    EVERYTHING("Backup everything"),
    YOU_MUTE("People you mute"),
    YOU_BLOCK("People you block"),
    DOMAIN_BLOCK("Domains you lock"),
    LISTS("Follower Overview"),
    BOOKMARKS("Follower Overview"),
    NOTES("Your notes on people");

    fun getOptions() =
        entries.toMutableSet().also {
            it.remove(EVERYTHING)
        }.toSet()

}