package org.osprey.trunkfriends.ui

enum class View(
    val title: String,
    val header: Boolean = true
) {
    ADD_SERVER("Add server",false),
    NEW_TOKEN("Obtain new token", false),
    HISTORY("History Overview"),
    LIST("Follower Overview"),
    MANAGE("Manage followers"),
    ABOUT("About"),
    REFRESH("Refresh followers"),
    EXECUTE_MANAGEMENT("Execute management actions"),
    PASTE_BAG("Pastebag");
}