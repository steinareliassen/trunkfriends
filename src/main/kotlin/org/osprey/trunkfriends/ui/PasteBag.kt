package org.osprey.trunkfriends.ui

import androidx.compose.runtime.mutableStateListOf

class PasteBag {
    val pasteBag = mutableStateListOf<String>()

    fun getSize() = pasteBag.size

    fun isNotEmpty() = pasteBag.isNotEmpty()

    fun clearSelect() {
        pasteBag.clear()
    }

    fun add(string : String) = pasteBag.add(string)
    fun remove(string : String) = pasteBag.remove(string)
    fun contains(string : String) = pasteBag.contains(string)

    fun getSelected(limit : Int = 0) =
        if (pasteBag.isEmpty())
            ""
        else if (limit == 0)
            pasteBag.reduce { acc, s -> "$acc\n$s" }
        else
            pasteBag.take(limit).reduce { acc, s -> "$acc\n$s" }
}