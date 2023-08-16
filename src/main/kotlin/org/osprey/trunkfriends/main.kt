package org.osprey.trunkfriends

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

val mapper = jacksonObjectMapper()
    .configure(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        false
    )

val timestamp = System.currentTimeMillis()

fun main() {
    // Set up fetchers
    val currentUserFetcher = UsersFromMastodonApi()
    val historyFetcher = HistoryFetcher()

    // Get current users
    val currentUsers = currentUserFetcher.getCurrentUsers() // Get the current status of each user

    // Find user status from previous run
    val history = historyFetcher.readHistory() // Read old history from file. History can contain multiple entries pr user
    val latestHistory = historyFetcher.extractPreviousRunFromHistory(history) // Find latest status of each user from history

    // Create and write new history
    val newHistory = historyFetcher.createNewHistory(latestHistory, currentUsers) // Compare previous run to current run and create new history lines
    println(newHistory.size)
//  historyFetcher.writeHistory(history. newHistory) // write out old and new history combined
}