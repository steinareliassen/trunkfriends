package org.osprey.trunkfriends.managementhandler

import kotlinx.coroutines.delay
import org.osprey.trunkfriends.config.Config

suspend fun managementAction(
    accounts: List<String>,
    action: String,
    selectedConfig : Pair<String, Config>,
    isCancelled : () -> Boolean,
    feedbackFunction: (String) -> Unit
) {

    try {
        val hostInterface = selectedConfig.second.hostInterfaceFactory()
        hostInterface.executeManagementAction(accounts, isCancelled, feedbackFunction, action)
    } catch (e : InterruptedException) {
        return
    } catch (e : Exception) {
        feedbackFunction("Error, action not applied to all accounts\n\n")
        feedbackFunction("Error: ${e.message}\n\n")
        delay(10000L)
    }

}