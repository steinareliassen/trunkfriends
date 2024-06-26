package org.osprey.trunkfriends.handlers

import kotlinx.coroutines.delay

suspend fun sleepAndCheck(isCancelled : () -> Boolean) {
    (1..15).forEach { _ ->
        delay(100)
        if (isCancelled()) throw InterruptedException()
    }
}