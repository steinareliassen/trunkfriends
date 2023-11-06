package org.osprey.trunkfriends.opt

import org.osprey.trunkfriends.api.CurrentUser
import org.osprey.trunkfriends.historyhandler.HistoryHandler
import org.osprey.trunkfriends.util.mapper
import java.io.File
import java.util.*

fun writeMessyHistory(historyLines: List<Pair<CurrentUser, String>>) {
    File("messdatafile.dmp").renameTo(File("messdatafile.dmp.${System.currentTimeMillis()}"))
    File("messdatafile.dmp").printWriter().use { pw ->
        historyLines.forEach {
            pw.println(it.second + mapper.writeValueAsString(it.first))
        }
    }
}

val animals = listOf(
    "eagle","osprey","dog","cat",
    "elk","swan","deer","duck","dolphin",
    "fox","badger","bat","beaver","owl",
    "snake","lynx","tiger","lion","eel",
    "wolf","ferret","frog","seal","goat",
    "sheep","hawk","husky","okapi","jackal",
    "crow","raven","lemur","mink","kite",
    "otter","puffin","moose","bear","whale",
    "puma","chicken","cow",
)

val usedAnimals = mutableSetOf<Pair<String,String>>()
val userMapping = mutableMapOf<String, Pair<String,String>>()
fun toAnimal(server : String, username : String) : Pair<String,String> {
    return if(userMapping.containsKey("$server/$username"))
        userMapping["$server/$username"]!!
    else {
        var firstName: String
        var lastName: String
        do {
            firstName = animals[(0..animals.size - 1).random()]
            lastName = animals[(0..animals.size - 1).random()]
        } while (usedAnimals.contains(server to "${firstName}${lastName}"))
        val pair = server to "${firstName}${lastName}"
        usedAnimals.add(pair)
        userMapping["$server/$username"] = "${firstName}${lastName}" to "${firstName.capitalize(Locale.getDefault())} ${lastName.capitalize(Locale.getDefault())}"
        "${firstName}${lastName}" to "${firstName.capitalize(Locale.getDefault())} ${lastName.capitalize(Locale.getDefault())}"
    }
}


fun main() {

    val corrupted = HistoryHandler().readHistory("tech.lgbt/lettosprey").map { (curr, stri) ->
        curr.run {
            val split = acct.split("@")
            val servername = if (split.size > 1) split[1] else "tech.lgbt"
            val username = username
            println("Animaling $username/$servername")
            val animal = toAnimal(servername, username)
            println(animal)
            (copy(
                username = animal.second,
                acct = animal.first +"@$servername"
            ) to stri)
        }
    }

    writeMessyHistory(corrupted)
}