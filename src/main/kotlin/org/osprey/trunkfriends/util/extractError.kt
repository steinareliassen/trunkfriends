package org.osprey.trunkfriends.util

fun extractError(message: String?) =
    (message ?: "Unknown error, no error message found").let {
        when {
            it.contains("The access token is invalid") ->
                "Access token not valid, please obtain new token"
            else -> it
        }
    }
